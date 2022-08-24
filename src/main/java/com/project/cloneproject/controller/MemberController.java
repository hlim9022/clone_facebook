package com.project.cloneproject.controller;


import com.project.cloneproject.controller.dto.request.LoginRequestDto;
import com.project.cloneproject.controller.dto.request.MemberRequestDto;
import com.project.cloneproject.controller.dto.response.ResponseDto;
import com.project.cloneproject.domain.UserDetailsImpl;
import com.project.cloneproject.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
public class MemberController {

  private final MemberService memberService;

  @RequestMapping(value = "/api/member/signup", method = RequestMethod.POST)
  public ResponseDto<?> signup(@RequestBody @Valid MemberRequestDto requestDto) {
    return memberService.createMember(requestDto);
  }

  @RequestMapping(value = "/api/member/login", method = RequestMethod.POST)
  public ResponseDto<?> login(@RequestBody @Valid LoginRequestDto requestDto,
      HttpServletResponse response
  ) {
    return memberService.login(requestDto, response);
  }

  @RequestMapping(value = "/api/auth/member/logout", method = RequestMethod.POST)
  public ResponseDto<?> logout(HttpServletRequest request) {
    return memberService.logout(request);
  }


  @GetMapping("/api/member/mypage")
  public ResponseDto<?> myPage(@AuthenticationPrincipal UserDetailsImpl userDetails) {
    return memberService.getMyInfo(userDetails);
  }

}
