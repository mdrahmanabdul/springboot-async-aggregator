package com.asyncagg.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.asyncagg.dto.PostDto;
import com.asyncagg.dto.TodoDto;
import com.asyncagg.dto.UserDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JsonPlaceHolderClient {

	@Autowired
    private RestClient restClient;
    
    public UserDto fetchUser() {

        return restClient.get()
                .uri("https://jsonplaceholder.typicode.com/users/1")
                .retrieve()
                .body(UserDto.class);
    }
    
    public TodoDto fetchTodo() {

        return restClient.get()
                .uri("https://jsonplaceholder.typicode.com/todos/1")
                .retrieve()
                .body(TodoDto.class);
    }
    
    public PostDto fetchPost() {

        return restClient.get()
                .uri("https://jsonplaceholder.typicode.com/posts/1")
                .retrieve()
                .body(PostDto.class);
    }
}