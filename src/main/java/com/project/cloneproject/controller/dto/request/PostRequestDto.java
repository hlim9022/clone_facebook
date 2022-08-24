package com.project.cloneproject.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostRequestDto {

    private String imageUrl;
    private String content;
}
