package com.project.cloneproject.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.cloneproject.controller.dto.request.KakaoUserDto;
import com.project.cloneproject.controller.dto.request.TokenDto;
import com.project.cloneproject.controller.dto.response.ResponseDto;
import com.project.cloneproject.domain.Member;
import com.project.cloneproject.jwt.TokenProvider;
import com.project.cloneproject.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KaKaoMemberService {

    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Value("${spring.security.oauth2.kakao.client_id}")
    private String KAKAO_CLIENT_ID;

    @Value("spring.security.oauth2.kakao.redirect_uri")
    private String KAKAO_REDIRECT_URI;

    public ResponseDto<KakaoUserDto> kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {
        String[] kakaoTokens = getKakaoTokens(code);

        KakaoUserDto kakaoUserInfo = getKakaoUserInfo(kakaoTokens);

        Member kakaoMember = registerKakaoUserIfNeed(kakaoUserInfo);

        TokenDto tokenDto = tokenProvider.generateTokenDto(kakaoMember);
        memberService.tokenToHeaders(tokenDto, response);

        return ResponseDto.success(
                KakaoUserDto.builder()
                .id(kakaoMember.getId())
                .nickname(kakaoMember.getNickname())
                .username(kakaoMember.getUsername())
                .profileImg(kakaoMember.getProfileImg())
                .build());
    }


    public String[] getKakaoTokens(String code) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", KAKAO_CLIENT_ID);
        body.add("redirect_uri", KAKAO_REDIRECT_URI);
        body.add("code",code);


        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(body,headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class);

        String responseBody = response.getBody(); // json 형태
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode jsonNode = objectMapper.readTree(responseBody);
        String accessToken = jsonNode.get("access_token").asText();
        String refreshToken = jsonNode.get("refresh_token").asText();
        return new String[]{accessToken, refreshToken};
    }

    public KakaoUserDto getKakaoUserInfo(String[] tokens) throws JsonProcessingException {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + tokens[0]);
        headers.add("Refresh_Token", tokens[1]);
        headers.add("Content-type", "Content-Type: application/json;charset=UTF-8");


        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest =
                new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange("https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET, kakaoUserInfoRequest, String.class);

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        long userId = jsonNode.get("id").asLong();
        String nickname = jsonNode.get("properties").get("nickname").asText();
        String username = jsonNode.get("kakao_account").get("email").asText();
        String profileImg = jsonNode.get("kakao_account").get("profile").get("profile_image_url").asText();

        return new KakaoUserDto(userId, nickname, username, profileImg);
    }


    private Member registerKakaoUserIfNeed(KakaoUserDto kakaoUserDto) {
        String username = kakaoUserDto.getUsername();
        String nickname = kakaoUserDto.getNickname();
        String profileImg = kakaoUserDto.getProfileImg();
        Member kakaoMember = memberRepository.findByUsername(username).orElse(null);

        if(kakaoMember == null) {
            String password = UUID.randomUUID().toString();

            kakaoMember = Member.builder()
                    .username(username)
                    .nickname(nickname)
                    .profileImg(profileImg)
                    .password(passwordEncoder.encode(password))
                    .build();

            memberRepository.save(kakaoMember);
        }
        return kakaoMember;
    }
    
}
