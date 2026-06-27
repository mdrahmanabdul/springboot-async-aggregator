package com.asyncagg.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DemoAsyncSchedulerService {

	Logger log = LoggerFactory.getLogger(DemoAsyncSchedulerService.class);
	
	
	@Async("apiExecutor")
	public void syncCustomers() {
		
		try {
			log.info("Customer sync started on thread {}",Thread.currentThread().getName());
			Thread.sleep(10000);
			log.info("Customer sync completed on thread {}",Thread.currentThread().getName());
		} catch (Exception e) {
			log.error("Customer sync failed !",e.getMessage());
		}
	}
}
