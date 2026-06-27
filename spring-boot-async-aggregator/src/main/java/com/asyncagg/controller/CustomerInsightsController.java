package com.asyncagg.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asyncagg.dto.CustomerInsightsResponse;
import com.asyncagg.service.CustomerInsightsService;

@RestController
@RequestMapping("/api/v1/insights")
public class CustomerInsightsController {

	@Autowired
    private CustomerInsightsService service;

    @GetMapping
    public CustomerInsightsResponse getInsights() {

        return service.getInsights();
    }
}