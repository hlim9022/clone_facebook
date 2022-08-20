package com.project.cloneproject.service;


import com.project.cloneproject.controller.request.PostRequestDto;
import com.project.cloneproject.controller.response.PostResponseDto;
import com.project.cloneproject.controller.response.ResponseDto;
import com.project.cloneproject.domain.Post;
import com.project.cloneproject.domain.UserDetailsImpl;
import com.project.cloneproject.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final AwsS3Service awsS3Service;


    public ResponseDto<PostResponseDto> createPost(PostRequestDto postRequestDto, UserDetailsImpl userDetails) {
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
            awsS3Service.deleteImage(deleteUrl);
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
}
