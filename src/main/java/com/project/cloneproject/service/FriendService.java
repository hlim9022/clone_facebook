package com.project.cloneproject.service;

import com.project.cloneproject.controller.dto.request.FriendReqDto;
import com.project.cloneproject.controller.dto.response.FriendResDto;
import com.project.cloneproject.controller.dto.response.ResponseDto;
import com.project.cloneproject.domain.Friend;
import com.project.cloneproject.domain.Member;
import com.project.cloneproject.domain.UserDetailsImpl;
import com.project.cloneproject.repository.FriendRepository;
import com.project.cloneproject.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ResponseEntity<?> addFriend(FriendReqDto friendReqDto, UserDetailsImpl userDetails) {

        Member friend = checkIfMember(friendReqDto.getFriendId());
        Member member = userDetails.getMember();

        if(friend.getUsername().equals(member.getUsername())) {
            return new ResponseEntity<>(ResponseDto.fail("BAD_REQUEST",
                    "자신은 친구로 추가할 수 없습니다."), HttpStatus.BAD_REQUEST);
        }

        if(friendRepository.findByFromMemberAndToMember(member, friend)!=null) {
            friendRepository.deleteByFromMemberAndToMember(member,friend);
            return new ResponseEntity<>(ResponseDto.success("팔로잉이 취소되었습니다."),HttpStatus.OK);
        }

        Friend savedFriend = friendRepository.save(new Friend(member, friend));
        member.getToMembers().add(savedFriend);
        return new ResponseEntity<>(ResponseDto.success( "팔로잉이 완료되었습니다.")
                ,HttpStatus.OK);
    }

    public Member checkIfMember(Long friendId) {
        Optional<Member> friend = memberRepository.findById(friendId);
        if(friend.isEmpty()){
            throw new RuntimeException("찾으시는 회원이 없습니다.");
        }
        return friend.get();
    }


    public ResponseEntity<?> getFriendList(UserDetailsImpl userDetails) {
        List<Friend> toMembers = userDetails.getMember().getToMembers();
        List<FriendResDto> friendResDtoList = new ArrayList<>();

        for(Friend friend:toMembers) {
            Member findFriend = memberRepository.findById(friend.getId()).orElse(null);
            if(findFriend != null) friendResDtoList.add(new FriendResDto(findFriend));
        }


        return new ResponseEntity<>(ResponseDto.success(friendResDtoList), HttpStatus.OK);
    }
}
