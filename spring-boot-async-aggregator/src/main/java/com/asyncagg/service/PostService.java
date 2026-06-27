package com.asyncagg.service;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.asyncagg.client.JsonPlaceHolderClient;
import com.asyncagg.dto.PostDto;


@Service
public class PostService {

	@Autowired
    private JsonPlaceHolderClient client;
    Logger log = LoggerFactory.getLogger(PostService.class);

    @Async("apiExecutor")
    public CompletableFuture<PostDto> getPost() {

        log.info(
            "Post API Thread : {}",
            Thread.currentThread().getName()
        );

        PostDto response =
                client.fetchPost();

        return CompletableFuture
                .completedFuture(response);
    }
}