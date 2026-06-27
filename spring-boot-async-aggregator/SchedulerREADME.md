# Spring Boot Scheduling Deep Dive - Interview Revision Guide

## Overview

This module demonstrates how Scheduling works in Spring Boot and how it integrates with Asynchronous Processing.

The objective is to understand:

* `@EnableScheduling`
* `@Scheduled`
* Fixed Rate Scheduling
* Fixed Delay Scheduling
* Cron Scheduling
* Scheduler Thread Pools
* `@Async` with Scheduling
* Long Running Jobs
* ThreadPoolTaskScheduler
* ThreadPoolTaskExecutor
* Common Spring Scheduling Pitfalls
* Production-Grade Scheduling Practices

---

# Learning Objectives

After completing this module, I should be able to explain:

1. How Spring executes scheduled jobs.
2. Difference between Fixed Rate and Fixed Delay.
3. Why default scheduling is single-threaded.
4. How to configure a Scheduler Thread Pool.
5. How `@Async` works with scheduled tasks.
6. Why overlapping executions can occur.
7. Common production issues with scheduling.
8. How Spring resolves TaskExecutor beans.

---

# Scheduling Architecture

```text
+--------------------+
| Spring Scheduler   |
+--------------------+
          |
          V
+--------------------+
| @Scheduled Method  |
+--------------------+
          |
          V
+--------------------+
| @Async Method      |
+--------------------+
          |
          V
+--------------------+
| Async Thread Pool  |
+--------------------+
```

The scheduler is responsible for triggering jobs.

The async executor is responsible for executing business logic.

---

# Enabling Scheduling

Scheduling is disabled by default.

```java
@SpringBootApplication
@EnableScheduling
public class SpringBootAsyncAggregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(
            SpringBootAsyncAggregatorApplication.class,
            args
        );
    }
}
```

---

# Basic Scheduled Job

```java
@Component
public class DemoScheduler {

    @Scheduled(fixedRate = 5000)
    public void run() {

        System.out.println("Running...");
    }
}
```

Output:

```text
Running...
Running...
Running...
```

Every 5 seconds.

---

# Fixed Rate Scheduling

```java
@Scheduled(fixedRate = 5000)
```

Definition:

Run every 5 seconds measured from the START time of the previous execution.

Timeline:

```text
0s   -> Start Job
5s   -> Start Job
10s  -> Start Job
15s  -> Start Job
```

Use Cases:

* Polling APIs
* Monitoring systems
* Health checks
* Cache refreshes

---

# Fixed Delay Scheduling

```java
@Scheduled(fixedDelay = 5000)
```

Definition:

Run 5 seconds after the PREVIOUS execution completes.

Timeline:

```text
Start
Finish
Wait 5 sec
Start
Finish
Wait 5 sec
```

Use Cases:

* Batch processing
* File processing
* Sequential jobs

---

# Fixed Rate vs Fixed Delay

| Feature              | Fixed Rate | Fixed Delay      |
| -------------------- | ---------- | ---------------- |
| Measured From        | Start Time | Completion Time  |
| Overlapping Possible | Yes        | No               |
| Suitable For         | Monitoring | Batch Processing |
| Maintains Frequency  | Yes        | No               |

Interview Question:

Q: Difference between Fixed Rate and Fixed Delay?

Answer:

Fixed Rate measures from the task start time, while Fixed Delay measures from the task completion time.

---

# Long Running Jobs

Example:

```java
@Scheduled(fixedRate = 5000)
public void process() throws Exception {

    Thread.sleep(10000);
}
```

Problem:

```text
Task Duration = 10 sec
Schedule Interval = 5 sec
```

Potential Issues:

* Backlog
* Delayed executions
* Thread starvation
* Resource exhaustion

---

# Why Use @Async With Scheduling?

Instead of making the scheduler wait:

```java
@Scheduled(fixedRate = 5000)
public void process() {

    Thread.sleep(10000);
}
```

Delegate the work:

```java
@Async("apiExecutor")
@Scheduled(fixedRate = 5000)
public void process() {

}
```

Benefits:

* Scheduler remains free
* Long-running work executes independently
* Multiple executions can run concurrently

---

# Async Configuration

```java
@Configuration
@EnableAsync
public class AsyncConfig {

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
```

Purpose:

```text
@Async
      ↓
api-worker-1
api-worker-2
api-worker-3
```

---

# Scheduler Configuration

```java
@Configuration
public class SchedulerConfig {

    @Bean
    public TaskScheduler taskScheduler() {

        ThreadPoolTaskScheduler scheduler =
                new ThreadPoolTaskScheduler();

        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("scheduler-");

        scheduler.initialize();

        return scheduler;
    }
}
```

Purpose:

```text
@Scheduled
      ↓
scheduler-1
scheduler-2
scheduler-3
```

---

# Scheduler Service

```java
@Service
@Slf4j
public class DemoAsyncSchedulerService {

    @Async("apiExecutor")
    public void syncCustomers() {

        try {

            log.info(
                "Customer sync started on thread {}",
                Thread.currentThread().getName()
            );

            Thread.sleep(10000);

            log.info(
                "Customer sync completed on thread {}",
                Thread.currentThread().getName()
            );

        } catch (Exception ex) {

            log.error(
                "Error occurred",
                ex
            );
        }
    }
}
```

---

# Scheduler Trigger

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class DemoAsyncScheduler {

    private final DemoAsyncSchedulerService service;

    @Scheduled(fixedRate = 5000)
    public void triggerCustomerSync() {

        log.info(
            "Scheduler triggered on thread {}",
            Thread.currentThread().getName()
        );

        service.syncCustomers();
    }
}
```

---

# Final Execution Flow

```text
scheduler-1
      ↓
Trigger Job
      ↓
api-worker-1
      ↓
Execute Business Logic
```

Next execution:

```text
scheduler-2
      ↓
Trigger Job
      ↓
api-worker-2
```

Scheduler threads never execute business logic directly.

---

# Logs Observed

```text
Scheduler triggered on thread scheduler-1

Customer sync started on thread api-worker-1

Scheduler triggered on thread scheduler-2

Customer sync started on thread api-worker-2

Scheduler triggered on thread scheduler-3

Customer sync started on thread api-worker-3
```

Observation:

Multiple customer sync executions were running simultaneously.

This proves concurrency.

---

# Issues Faced During Implementation

## Issue 1

### Error

```text
Type mismatch:
cannot convert from ThreadPoolTaskExecutor to TaskScheduler
```

### Cause

Used:

```java
ThreadPoolTaskExecutor
```

instead of:

```java
ThreadPoolTaskScheduler
```

inside SchedulerConfig.

### Fix

Use:

```java
ThreadPoolTaskScheduler
```

for scheduling.

Remember:

```text
ThreadPoolTaskExecutor
       ↑
      @Async

ThreadPoolTaskScheduler
       ↑
    @Scheduled
```

---

# Issue 2

### Error

```text
More than one TaskExecutor bean found
```

### Cause

Spring found:

```text
apiExecutor
taskScheduler
```

Both implement TaskExecutor.

Spring could not decide which executor to use.

### Fix

Explicitly specify:

```java
@Async("apiExecutor")
```

This removes ambiguity.

---

# Issue 3

### Mistake

```java
@Bean(name = "taskExecutor")
public TaskScheduler taskScheduler()
```

### Result

Spring started using scheduler threads for async execution.

Logs looked like:

```text
scheduler-1
scheduler-2
scheduler-3
```

for both scheduling and async processing.

### Fix

Remove:

```java
name = "taskExecutor"
```

and use:

```java
@Bean
public TaskScheduler taskScheduler()
```

---

# Issue 4

### Unexpected Thread Names

Observed:

```text
SimpleAsyncTaskExecutor-1
SimpleAsyncTaskExecutor-2
```

instead of:

```text
api-worker-1
api-worker-2
```

### Cause

Spring could not determine which executor to use.

It fell back to:

```text
SimpleAsyncTaskExecutor
```

### Fix

Use:

```java
@Async("apiExecutor")
```

---

# Production Best Practices

## Keep Scheduler Thin

Bad:

```java
@Scheduled(...)
public void run() {

    // 200 lines of logic
}
```

Good:

```java
@Scheduled(...)
public void run() {

    service.process();
}
```

---

## Handle Exceptions

Bad:

```java
@Scheduled(...)
public void run() {

    throw new RuntimeException();
}
```

Good:

```java
try {

} catch (Exception ex) {

    log.error("Error", ex);
}
```

---

## Externalize Schedule

Bad:

```java
@Scheduled(fixedRate = 5000)
```

Good:

```java
@Scheduled(
    fixedRateString =
        "${jobs.customer-sync.rate}"
)
```

application.yml

```yaml
jobs:
  customer-sync:
    rate: 5000
```

---

## Use Time Zones

```java
@Scheduled(
    cron = "0 0 2 * * *",
    zone = "Asia/Kolkata"
)
```

---

## Configure Thread Pools

Never rely on defaults in production.

Configure:

* Scheduler Pool
* Async Pool

Explicitly.

---

## Prevent Duplicate Execution

In multi-instance deployments:

```text
Instance-1
Instance-2
Instance-3
```

All schedulers run independently.

Potential Issue:

```text
Invoice generated 3 times
```

Solutions:

* ShedLock
* Quartz
* Database Locks
* Distributed Locks

---

# Frequently Asked Interview Questions

### Why do we need @EnableScheduling?

It enables Spring's scheduling infrastructure and allows methods annotated with `@Scheduled` to run automatically.

---

### Why do we need @EnableAsync?

It enables Spring's asynchronous execution capability and allows methods annotated with `@Async` to run in separate threads.

---

### Can @Scheduled and @Async be used together?

Yes.

Scheduler triggers the task.

Async executor performs the work.

---

### Why did we use ThreadPoolTaskScheduler?

To allow multiple scheduled jobs to run concurrently.

---

### Why did we use ThreadPoolTaskExecutor?

To execute async business logic using a dedicated thread pool.

---

### Why specify @Async("apiExecutor")?

To explicitly tell Spring which executor should execute async methods.

---

### What happens if a scheduled task takes longer than the scheduling interval?

Multiple executions can overlap when using Fixed Rate scheduling and asynchronous execution.

---

# Final Interview Summary

```text
@EnableScheduling
        ↓
Activates scheduling

@Scheduled
        ↓
Triggers jobs automatically

fixedRate
        ↓
Measures from task start

fixedDelay
        ↓
Measures from task completion

ThreadPoolTaskScheduler
        ↓
Runs scheduled jobs

@EnableAsync
        ↓
Enables async processing

@Async("apiExecutor")
        ↓
Runs business logic in worker threads

ThreadPoolTaskExecutor
        ↓
Executes async work

Scheduler Threads
        ↓
scheduler-1
scheduler-2

Async Threads
        ↓
api-worker-1
api-worker-2

Production
        ↓
Logging
Exception Handling
Externalized Config
Thread Pools
ShedLock
Quartz
```
