package com.project.cloneproject.controller.response;

import com.project.cloneproject.domain.Post;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PostResponseDto {
    private Long postId;
    private MemberSummaryDto member;
    private String imageUrl;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Long likeNum = 0L;
    private Long commentsNum = 0L;

    public PostResponseDto(Post post) {
        this.postId = post.getId();
        this.member = new MemberSummaryDto(post.getMember());
        this.content = post.getContent();
        this.imageUrl = post.getImageUrl();
        this.createdAt = post.getCreatedAt();
        this.modifiedAt = post.getModifiedAt();
    }
}
