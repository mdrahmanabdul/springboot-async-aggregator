package com.asyncagg.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClient;

@Configuration
@EnableAsync
public class AsyncConfig {
	
	@Bean
	public RestClient restClient(
	        RestClient.Builder builder) {

	    return builder.build();
	}

	 @Bean("apiExecutor")
	    public Executor apiExecutor() {

	        ThreadPoolTaskExecutor executor =
	                new ThreadPoolTaskExecutor();

	        executor.setCorePoolSize(5);

	        executor.setMaxPoolSize(10);

	        executor.setQueueCapacity(50);

	        executor.setThreadNamePrefix("api-worker-");

	        executor.initialize();

	        return executor;
	    }
}
