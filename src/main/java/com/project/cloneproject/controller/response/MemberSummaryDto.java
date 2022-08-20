package com.project.cloneproject.controller.response;

import com.project.cloneproject.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberSummaryDto {
    private Long id;
    private String username;
    private String nickname;
    private String profileImg;

    public MemberSummaryDto(Member member) {
        this.id = member.getId();
        this.username = member.getUsername();
        this.nickname = member.getNickname();
        this.profileImg = member.getProfileImg();
    }
}
