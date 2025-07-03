package com.blogdev.blog;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BlogController {
    //注入postRepository
    private final PostRepository pr;

    public BlogController(PostRepository pr) {
        this.pr = pr;
    }

    //将Dto转换成Post实体存储到数据库
    private Post PostDtoToPost(PostDto pd) {
        Post p = new Post();
        p.setTitle(pd.getTitle());
        p.setContext(pd.getContext());
        return p;
    }



    //将Post实体转换成PostDto返回给前端
    private PostDto PostToPostDto(Post p) {
        PostDto pd = new PostDto();
        pd.setId(p.getPostID());
        pd.setTitle(p.getTitle());
        pd.setContext(p.getContext());
        return pd;
    }

    //创建文章
    @PostMapping("/post")
    public PostDto createPost(@RequestBody PostDto pd) {
        //将Dto转换成实体
        Post postEntity = PostDtoToPost(pd);

        //保存实体到数据库
        Post savedPost = pr.save(postEntity);

        //将保存后带有Id的实体再转换成Dto再返回
        return PostToPostDto(savedPost);
    }

    //获取所有文章
    @GetMapping("/post")
    public List<PostDto> getAllPosts() {
        List<Post> allPosts = pr.findAll();

        return allPosts.stream().map(this::PostToPostDto).collect(Collectors.toList());
    }

    //获取单篇post
    @GetMapping("/post/{id}")
    public ResponseEntity<PostDto> getPostById(@PathVariable long id) {
        return pr.findById(id).map(this::PostToPostDto).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //更新文章
    @PutMapping("/post/{id}")
    public ResponseEntity<PostDto> updatePost(@PathVariable Long id, @RequestBody PostDto updatedDetails) {
        return pr.findById(id).map(existingPost -> {
            existingPost.setTitle(updatedDetails.getTitle());
            existingPost.setContext(updatedDetails.getContext());

            //保存更新后的实体
            Post savedPost = pr.save(existingPost);
            return ResponseEntity.ok(PostToPostDto(savedPost));
        }).orElse(ResponseEntity.notFound().build());
    }

    //删除文章
    @DeleteMapping("/post/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable long id) {
        if(pr.existsById(id)) {
            pr.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
