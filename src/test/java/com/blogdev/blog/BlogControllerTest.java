package com.blogdev.blog;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

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
    void createPostWithNullTitle() throws Exception{
        PostDto postDto = new PostDto();
        postDto.setTitle("");
        postDto.setContext("这是内容");

        mockMvc.perform(post("/api/post")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("文章标题不能为空"));
    }

    //内容为空
    @Test
    void createPostWithNullContext() throws Exception{
        PostDto postDto = new PostDto();
        postDto.setTitle("这是标题");
        postDto.setContext("");

        mockMvc.perform(post("/api/post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.context").value("文章内容不能为空"));
    }

    //标题过长
    @Test
    void createPostWithTitleTooLong() throws Exception {
        PostDto postDto = new PostDto();
        postDto.setTitle("近日，CD PROJECT RED官方商城Gear Store正式宣布，" +
                "与玩具品牌Zing合作推出《赛博朋克2077》标志性武器“热能武士刀”的等比例精致复制品，" +
                "并已开放预售，售价为120美元。这款武器复制品预计将于2025年9月开始发货。\n" +
                "“热能武士刀”是《赛博朋克2077》中极具代表性的近战武器，" +
                "以其炽热的刀锋和极高的杀伤力闻名于夜之城，" +
                "如今，这把武器首次在现实世界中实体化，由 Zing 与 " +
                "CDPR 联手打造，完美还原游戏中的未来科技感。");
        postDto.setContext("这是内容");

        mockMvc.perform(post("/api/post")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("文章标题的长度不能超过150个字符"));
    }

    //查找全部，但是没有文章
    @Test
    void findAllButNonePostExists() throws Exception {
        given(postRepository.findAll()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/post")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    //查找全部，有文章（1或多篇）
    @Test
    void findAllAndPostExists() throws Exception {
        Post post1 = new Post();
        post1.setPostID(1L);
        post1.setTitle("标题1");
        post1.setContext("内容1");

        Post post2 = new Post();
        post2.setPostID(2L);
        post2.setTitle("标题2");
        post2.setContext("内容2");


        List<Post> posts = List.of(post1,post2);

        given(postRepository.findAll()).willReturn(posts);

        mockMvc.perform(get("/api/post"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())//验证返回的是JSON数组
                .andExpect(jsonPath("$",hasSize(2)))//验证大小是两个
                .andExpect(jsonPath("$[0].id",is(1)))
                .andExpect(jsonPath("$[0].title").value("标题1"))
                .andExpect(jsonPath("$[0].context").value("内容1"))
                .andExpect(jsonPath("$[1].id",is(2)))
                .andExpect(jsonPath("$[1].title").value("标题2"))
                .andExpect(jsonPath("$[1].context").value("内容2"));

    }

    //按id查找，且id存在
    @Test
    void findByExistedId() throws Exception{
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
    void findByNoneExistedId() throws Exception {
        given(postRepository.findById(99L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/post/99"))
                .andExpect(status().isNotFound());
    }

    //正常更新
    @Test
    void updateSuccessfully() throws Exception{
        long id = 1L;
        PostDto updatedPost = new PostDto();
        updatedPost.setTitle("【更新】标题");
        updatedPost.setContext("【更新】内容");

        Post originalPost = new Post();
        originalPost.setPostID(id);
        originalPost.setTitle("标题");
        originalPost.setContext("内容");

        Post savedPost = new Post();
        savedPost.setPostID(id);
        savedPost.setTitle("【更新】标题");
        savedPost.setContext("【更新】内容");

        given(postRepository.findById(id)).willReturn(Optional.of(originalPost));
        given(postRepository.save(any(Post.class))).willReturn(savedPost);

        mockMvc.perform(put("/api/post/{id}",id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedPost)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("【更新】标题"))
                .andExpect(jsonPath("$.context").value("【更新】内容"));
    }

    //更新，id不存在
    @Test
    void updatePostButNoId() throws Exception {
        //准备一个不存在的id
        long id = 99L;
        PostDto postDto = new PostDto();
        postDto.setTitle("标题");
        postDto.setContext("内容");

        given(postRepository.findById(id)).willReturn(Optional.empty());

        mockMvc.perform(put("/api/post/{id}",id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isNotFound());
    }

    //更新，标题太长
    @Test
    void updatePostButTitleTooLong() throws Exception{
        long id = 1L;
        PostDto postDto = new PostDto();
        //不合法的标题
        postDto.setTitle("a".repeat(151));
        postDto.setContext("内容");

        mockMvc.perform(put("/api/post/{id}",id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("文章标题的长度不能超过150个字符"));
    }

    //正常删除
    @Test
    void deleteSuccessfully() throws Exception {
        long id = 1L;

        //需要先假设这个id存在
        given(postRepository.existsById(id)).willReturn(true);

        mockMvc.perform(delete("/api/post/{id}",id))
                .andExpect(status().isNoContent());

        //验证deleteById确实被调用了
        verify(postRepository).deleteById(id);
    }

    //删除，id不存在
    @Test
    void deleteButNoMatchId() throws Exception{
        long id = 99L;

        //模拟id不存在
        given(postRepository.existsById(id)).willReturn(false);

        mockMvc.perform(delete("/api/post/{id}",id))
                .andExpect(status().isNotFound());

        //验证deleteById从未被调用
        verify(postRepository,never()).deleteById(id);
    }

}
