package com.blogdev.blog;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest 注解用于测试Controller层，它只会加载与Web层相关的Bean
@WebMvcTest(BlogController.class)
public class BlogControllerTest {
    // MockMvc 允许我们模拟HTTP请求并测试Controller的行为
    @Autowired
    private MockMvc mockMvc;

    // @MockitoBean 会创建一个PostRepository的模拟对象，我们用它来定义当调用数据库方法时应返回什么
    @MockitoBean
    private PostRepository postRepository;

    @Autowired
    private ObjectMapper objectMapper;

    //成功创建一个Post
    @Test
    void example1() throws Exception {
        //准备数据
        PostDto pd = new PostDto();
        pd.setTitle("测试1：成功创建一个Post");
        pd.setContext("这是内容");

        Post savedPost = new Post();
        savedPost.setPostID(1L);
        savedPost.setTitle("测试1：成功创建一个Post");
        savedPost.setContext("这是内容");

        //模拟行为：当postRepository.save被调用时，返回savedPost
        given(postRepository.save(any(Post.class))).willReturn(savedPost);

        //执行并验证
        mockMvc.perform(post("/api/post")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("测试1：成功创建一个Post"));
    }

    //标题为空
    @Test
    void example2() throws Exception{
        PostDto postDto = new PostDto();
        postDto.setTitle("");
        postDto.setContext("这是内容");

        mockMvc.perform(post("/api/post")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("文章标题不能为空"));
    }

    //按id查找，且id存在
    @Test
    void example3 () throws Exception{
        Post post = new Post();
        post.setPostID(1L);
        post.setTitle("标题");
        post.setContext("内容");

        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        mockMvc.perform(get("/api/post/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("标题"));

    }

    //按id查找，id不存在
    @Test
    void example4 () throws Exception {
        given(postRepository.findById(99L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/post/99"))
                .andExpect(status().isNotFound());
    }


}
