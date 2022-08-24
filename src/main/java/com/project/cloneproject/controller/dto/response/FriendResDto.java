package com.project.cloneproject.controller.dto.response;

import com.project.cloneproject.domain.Friend;
import com.project.cloneproject.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendResDto {
    private Long id;
    private String username;
    private String nickname;
    private String profileImg;

    public FriendResDto(Member member) {
        this.id = member.getId();
        this.username = member.getUsername();
        this.nickname = member.getNickname();
        this.profileImg = member.getProfileImg();
    }
}
