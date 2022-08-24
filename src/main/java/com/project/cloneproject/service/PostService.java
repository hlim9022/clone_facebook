package com.project.cloneproject.service;


import com.project.cloneproject.controller.dto.request.PostRequestDto;
import com.project.cloneproject.controller.dto.response.PostResponseDto;
import com.project.cloneproject.controller.dto.response.ResponseDto;
import com.project.cloneproject.domain.Friend;
import com.project.cloneproject.domain.Member;
import com.project.cloneproject.domain.Post;
import com.project.cloneproject.domain.UserDetailsImpl;
import com.project.cloneproject.repository.MemberRepository;
import com.project.cloneproject.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final AwsS3Service awsS3Service;


    public ResponseDto<PostResponseDto> createPost(PostRequestDto postRequestDto,
                                                   UserDetailsImpl userDetails)  {
        String fileUrl = awsS3Service.getSavedS3ImageUrl(postRequestDto);
        postRequestDto.setImageUrl(fileUrl);

        Post post = new Post(postRequestDto, userDetails.getMember());
        postRepository.save(post);

        PostResponseDto postResponseDto = new PostResponseDto(post);
        return ResponseDto.success(postResponseDto);
    }

    @Transactional
    public ResponseEntity<?> updatePost(Long postId, PostRequestDto postRequestDto, UserDetailsImpl userDetails) {
        if(postRepository.findById(postId).isEmpty()){
            log.error("요청하신 게시글은 존재하지 않습니다.");
            return new ResponseEntity<>(ResponseDto.fail("NOT_FOUND", "찾으시는 게시글이 없습니다."), HttpStatus.NOT_FOUND);
        }

        Post findPost = postRepository.findById(postId).get();

        if(findPost.getMember().getUsername().equals(userDetails.getUsername())) {
            String deleteUrl = findPost.getImageUrl();

             //사진이 첨부되었던 게시글이라면 다른 사진으로 업데이트하면서 이전 사진파일을 S3에서 지워줌
            if(deleteUrl != null) awsS3Service.deleteImage(deleteUrl);
            String updateImageUrl = awsS3Service.getSavedS3ImageUrl(postRequestDto);
            postRequestDto.setImageUrl(updateImageUrl);
            findPost.update(postRequestDto);
            return new ResponseEntity<>(ResponseDto.success(new PostResponseDto(findPost)),HttpStatus.OK);
        }
        return new ResponseEntity<>(ResponseDto.fail("BAD_REQUEST", "작성자가 아니므로 수정권한이 없습니다."),
                    HttpStatus.BAD_REQUEST);
    }

    @Transactional
    public ResponseEntity<?> removePost(Long postId, UserDetailsImpl userDetails) {
        if(postRepository.findById(postId).isEmpty()){
            log.error("요청하신 게시글은 존재하지 않습니다.");
            return new ResponseEntity<>(ResponseDto.fail("NOT_FOUND", "찾으시는 게시글이 없습니다."), HttpStatus.NOT_FOUND);
        }

        Post findPost = postRepository.findById(postId).get();

        if(findPost.getMember().getUsername().equals(userDetails.getUsername())) {
            awsS3Service.deleteImage(findPost.getImageUrl());
            postRepository.delete(findPost);

            return new ResponseEntity<>(ResponseDto.success("게시글 삭제가 완료되었습니다."), HttpStatus.OK);
        }

        return new ResponseEntity<>(ResponseDto.fail("BAD_REQUEST", "작성자가 아니므로 수정권한이 없습니다."),
                HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<?> getPostList(UserDetailsImpl userDetails) {
        List<Friend> friendList = userDetails.getMember().getToMembers();
        List<Member> tempMemberList = new ArrayList<>();
        tempMemberList.add(userDetails.getMember());

        // 현재 사용자 친구리스트의 id값으로 멤버 객체 찾아오기
        for(Friend friend:friendList) {
            memberRepository.findById(friend.getToMember().getId()).ifPresent(tempMemberList::add);
        }

        List<PostResponseDto> allPosts = new ArrayList<>();
        // 내 정보 + 친구들이 쓴 Post 리스트 생성
        for(Member member : tempMemberList) {
            List<Post> postsByMember = postRepository.findAllByMember(member);

            for(Post post : postsByMember) {
                allPosts.add(new PostResponseDto(post));
            }
        }

        // 리스트 시간순으로 sort
        allPosts.sort(new Comparator<>() {
            @Override
            public int compare(PostResponseDto o1, PostResponseDto o2) {
                return o2.getCreatedAt().compareTo(o1.getCreatedAt());
            }
        });

        return new ResponseEntity<>(ResponseDto.success(allPosts), HttpStatus.OK);


    }
}
