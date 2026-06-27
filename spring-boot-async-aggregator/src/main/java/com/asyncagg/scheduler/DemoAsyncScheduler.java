package com.asyncagg.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.asyncagg.service.DemoAsyncSchedulerService;

@Component
public class DemoAsyncScheduler {

	@Autowired
	private DemoAsyncSchedulerService demoService;
	
	Logger log = LoggerFactory.getLogger(DemoAsyncScheduler.class);
	
	@Scheduled(fixedRate = 5000) //better to externalize via application.properties file
	public void triggerCustomerSync() {
		log.info("Scheduler triggered on thread {}",Thread.currentThread().getName());
		demoService.syncCustomers();
	}
	
}
