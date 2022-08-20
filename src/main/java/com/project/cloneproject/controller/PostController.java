package com.project.cloneproject.controller;

import com.project.cloneproject.controller.request.PostRequestDto;
import com.project.cloneproject.domain.UserDetailsImpl;
import com.project.cloneproject.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/api/posts")
    public ResponseEntity<?> createPost(@RequestBody PostRequestDto postRequestDto,
                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return new ResponseEntity<>(postService.createPost(postRequestDto, userDetails), HttpStatus.OK);
    }

    @PutMapping("/api/posts/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable Long postId,
                                        @RequestBody PostRequestDto postRequestDto,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

        return postService.updatePost(postId,postRequestDto,userDetails);
    }


    @DeleteMapping("/api/posts/{postId}")
    public ResponseEntity<?> removePost(@PathVariable Long postId,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return postService.removePost(postId, userDetails);
    }
}
