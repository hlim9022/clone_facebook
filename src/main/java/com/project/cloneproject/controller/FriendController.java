package com.project.cloneproject.controller;

import com.project.cloneproject.controller.dto.request.FriendReqDto;
import com.project.cloneproject.domain.UserDetailsImpl;
import com.project.cloneproject.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/api/friends")
    public ResponseEntity<?> addFriend(@RequestBody FriendReqDto friendReqDto,
                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return friendService.addFriend(friendReqDto, userDetails);
    }

    @GetMapping("/api/friends")
    public ResponseEntity<?> getFriendList(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return friendService.getFriendList(userDetails);
    }
}
