package com.asyncagg.dto;

public class TodoDto {

    private Long id;

    private String title;

    private Boolean completed;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Boolean getCompleted() {
		return completed;
	}

	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}

	public TodoDto(Long id, String title, Boolean completed) {
		super();
		this.id = id;
		this.title = title;
		this.completed = completed;
	}

	public TodoDto() {
		super();
	}
    
    
}