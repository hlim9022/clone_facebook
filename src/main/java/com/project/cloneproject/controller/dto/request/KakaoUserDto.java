package com.project.cloneproject.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KakaoUserDto {
    private Long id;
    private String nickname;
    private String username;
    private String profileImg;
}
