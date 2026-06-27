package com.asyncagg.service;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.asyncagg.client.JsonPlaceHolderClient;
import com.asyncagg.dto.UserDto;


@Service
public class UserService {

	  @Autowired
	  private JsonPlaceHolderClient client;
	  
	  Logger log = LoggerFactory.getLogger(UserService.class);

	    @Async("apiExecutor")
	    public CompletableFuture<UserDto> getUser() {

	        log.info(
	            "User API Thread : {}",
	            Thread.currentThread().getName()
	        );

	        UserDto response =
	                client.fetchUser();

	        return CompletableFuture
	                .completedFuture(response);
	    }
}
