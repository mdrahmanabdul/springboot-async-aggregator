package com.asyncagg.service;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.asyncagg.client.JsonPlaceHolderClient;
import com.asyncagg.dto.TodoDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TodoService {

	@Autowired
    private JsonPlaceHolderClient client;
	
    Logger log = LoggerFactory.getLogger(TodoService.class);

    @Async("apiExecutor")
    public CompletableFuture<TodoDto> getTodo() {

        log.info(
            "Todo API Thread : {}",
            Thread.currentThread().getName()
        );

        TodoDto response =
                client.fetchTodo();

        return CompletableFuture
                .completedFuture(response);
    }
}