package com.project.cloneproject.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberInfoDto {
  private Long id;
  private String nickname;
  private String username;
  private String profileImg;
  private List<FriendResDto> toMembers = new ArrayList<>();
}
