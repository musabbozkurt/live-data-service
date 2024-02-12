package com.mb.livedataservice.client.jsonplaceholder;

import com.mb.livedataservice.client.jsonplaceholder.request.PostRequest;
import com.mb.livedataservice.client.jsonplaceholder.response.PostResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

public interface DeclarativeJSONPlaceholderRestClient {

    @GetExchange("/posts")
    List<PostResponse> getAllPosts();

    @GetExchange("/posts/{id}")
    PostResponse getPost(@PathVariable Integer id);

    @DeleteExchange("/posts/{id}")
    void deletePost(@PathVariable Integer id);

    @PostExchange("/posts")
    PostResponse createPost(@RequestBody PostRequest post);
}
