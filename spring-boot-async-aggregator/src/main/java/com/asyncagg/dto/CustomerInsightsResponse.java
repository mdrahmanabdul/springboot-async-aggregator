package com.asyncagg.dto;


public class CustomerInsightsResponse {

    private UserDto user;

    private TodoDto todo;

    private PostDto post;

    private long executionTimeMs;

	public UserDto getUser() {
		return user;
	}

	public void setUser(UserDto user) {
		this.user = user;
	}

	public TodoDto getTodo() {
		return todo;
	}

	public void setTodo(TodoDto todo) {
		this.todo = todo;
	}

	public PostDto getPost() {
		return post;
	}

	public void setPost(PostDto post) {
		this.post = post;
	}

	public long getExecutionTimeMs() {
		return executionTimeMs;
	}

	public void setExecutionTimeMs(long executionTimeMs) {
		this.executionTimeMs = executionTimeMs;
	}

	public CustomerInsightsResponse(UserDto user, TodoDto todo, PostDto post, long executionTimeMs) {
		super();
		this.user = user;
		this.todo = todo;
		this.post = post;
		this.executionTimeMs = executionTimeMs;
	}

	public CustomerInsightsResponse() {
		super();
	}
    
    
}