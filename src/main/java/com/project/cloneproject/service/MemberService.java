package com.project.cloneproject.service;


import com.project.cloneproject.controller.dto.request.LoginRequestDto;
import com.project.cloneproject.controller.dto.request.MemberRequestDto;
import com.project.cloneproject.controller.dto.request.TokenDto;
import com.project.cloneproject.controller.dto.response.FriendResDto;
import com.project.cloneproject.controller.dto.response.MemberInfoDto;
import com.project.cloneproject.controller.dto.response.MemberResponseDto;
import com.project.cloneproject.controller.dto.response.ResponseDto;
import com.project.cloneproject.domain.Friend;
import com.project.cloneproject.domain.Member;
import com.project.cloneproject.domain.UserDetailsImpl;
import com.project.cloneproject.jwt.TokenProvider;
import com.project.cloneproject.repository.FriendRepository;
import com.project.cloneproject.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Transactional
    public ResponseDto<?> createMember(MemberRequestDto requestDto) {
        if (null != isPresentMember(requestDto.getUsername())) {
            return ResponseDto.fail("DUPLICATED_NICKNAME",
                    "중복된 이메일 입니다.");
        }

        if (!requestDto.getPassword().equals(requestDto.getPasswordConfirm())) {
            return ResponseDto.fail("PASSWORDS_NOT_MATCHED",
                    "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        Member member = Member.builder()
                .username(requestDto.getUsername())
                .nickname(requestDto.getNickname())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .build();

        memberRepository.save(member);


        return ResponseDto.success(
                MemberResponseDto.builder()
                        .id(member.getId())
                        .username(member.getUsername())
                        .nickname(member.getNickname())
                        .createdAt(member.getCreatedAt())
                        .modifiedAt(member.getModifiedAt())
                        .build()
        );
    }

    @Transactional
    public ResponseDto<?> login(LoginRequestDto requestDto, HttpServletResponse response) {
        Member member = isPresentMember(requestDto.getUsername());
        if (null == member) {
            return ResponseDto.fail("MEMBER_NOT_FOUND",
                    "사용자를 찾을 수 없습니다.");
        }

        if (!member.validatePassword(passwordEncoder, requestDto.getPassword())) {
            return ResponseDto.fail("INVALID_MEMBER", "사용자를 찾을 수 없습니다.");
        }

        TokenDto tokenDto = tokenProvider.generateTokenDto(member);
        tokenToHeaders(tokenDto, response);

        return ResponseDto.success(
                MemberResponseDto.builder()
                        .id(member.getId())
                        .username(member.getUsername())
                        .nickname(member.getNickname())
                        .createdAt(member.getCreatedAt())
                        .modifiedAt(member.getModifiedAt())
                        .build()
        );
    }

    public ResponseDto<?> logout(HttpServletRequest request) {
        if (!tokenProvider.validateToken(request.getHeader("Refresh-Token"))) {
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
        }
        Member member = tokenProvider.getMemberFromAuthentication();
        if (null == member) {
            return ResponseDto.fail("MEMBER_NOT_FOUND",
                    "사용자를 찾을 수 없습니다.");
        }

        return tokenProvider.deleteRefreshToken(member);
    }

    @Transactional(readOnly = true)
    public Member isPresentMember(String username) {
        Optional<Member> optionalMember = memberRepository.findByUsername(username);
        return optionalMember.orElse(null);
    }

    public void tokenToHeaders(TokenDto tokenDto, HttpServletResponse response) {
        response.addHeader("Authorization", "Bearer " + tokenDto.getAccessToken());
        response.addHeader("Refresh-Token", tokenDto.getRefreshToken());
        response.addHeader("Access-Token-Expire-Time", tokenDto.getAccessTokenExpiresIn().toString());
    }

    @Transactional
    public ResponseDto<?> getMyInfo(UserDetailsImpl userDetails) {

        Optional<Member> findMember = memberRepository.findByUsername(userDetails.getUsername());
        if(findMember.isPresent()) {
            Member user = findMember.get();
            List<FriendResDto> friendResDtoList = new ArrayList<>();
            List<Friend> toMembers = findMember.get().getToMembers();

            for(Friend friend : toMembers) {
                Member findFriend = memberRepository.findById(friend.getId()).orElse(null);

                if(findMember != null) friendResDtoList.add(new FriendResDto(findFriend));
            }

            return ResponseDto.success(MemberInfoDto.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .profileImg(user.getProfileImg())
                    .toMembers(friendResDtoList)
                    .build());
        }
        return null;
    }
}
