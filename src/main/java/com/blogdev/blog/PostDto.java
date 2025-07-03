package com.blogdev.blog;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class PostDto {
    private Long id;

    @NotEmpty(message = "文章标题不能为空")
    @Size(max = 150,message = "文章标题的长度不能超过150个字符")
    private String title;

    @NotEmpty(message = "文章内容不能为空")
    private String context;
}
