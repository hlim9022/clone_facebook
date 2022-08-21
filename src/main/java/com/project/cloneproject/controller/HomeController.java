package com.project.cloneproject.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.cloneproject.controller.response.ResponseDto;
import com.project.cloneproject.service.KaKaoMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final KaKaoMemberService kaKaoMemberService;

    @GetMapping("/")
    public String login() {
        return "login";
    }

    @GetMapping("/user/kakao/callback")
    @ResponseBody
    public ResponseDto<?> kakaoLogin(@RequestParam String code, HttpServletResponse response) throws JsonProcessingException {
        return kaKaoMemberService.kakaoLogin(code, response);
    }
}
