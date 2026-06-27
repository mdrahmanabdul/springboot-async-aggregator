package com.asyncagg.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.asyncagg.dto.CustomerInsightsResponse;
import com.asyncagg.dto.PostDto;
import com.asyncagg.dto.TodoDto;
import com.asyncagg.dto.UserDto;


@Service
public class CustomerInsightsService {

	@Autowired
    private UserService userService;

	@Autowired
    private TodoService todoService;

	@Autowired
    private PostService postService;

    public CustomerInsightsResponse getInsights() {

        long start =
                System.currentTimeMillis();

        CompletableFuture<UserDto> userFuture =
                userService.getUser();

        CompletableFuture<TodoDto> todoFuture =
                todoService.getTodo();

        CompletableFuture<PostDto> postFuture =
                postService.getPost();

        //Wait until everyone is finished
        //it means block the current thread until all three futures complete.
        CompletableFuture.allOf(
                userFuture,
                todoFuture,
                postFuture
        ).join();

        long end =
                System.currentTimeMillis();

        return new CustomerInsightsResponse(
        		//the below methods returns immediately because results are already available.
                userFuture.join(),
                todoFuture.join(),
                postFuture.join(),
                end - start
        );
    }
}