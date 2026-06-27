# Spring Boot Async Aggregator Service

## Overview

The Async Aggregator Service is a Spring Boot application that demonstrates production-grade asynchronous processing using:

* Spring Boot Async (`@Async`)
* Custom Thread Pool Configuration
* `CompletableFuture`
* Parallel API Calls
* External Service Integration
* Response Aggregation
* Concurrent Execution

The application receives a request, invokes multiple third-party APIs concurrently, aggregates their responses, and returns a consolidated result.

---

# Problem Statement

In traditional synchronous applications, external service calls are executed sequentially.

Example:

```text
Fetch User      -> 3 seconds
Fetch Todo      -> 2 seconds
Fetch Post      -> 4 seconds

Total Time = 9 seconds
```

This leads to increased response time and poor scalability.

Using asynchronous processing:

```text
Fetch User      -> 3 seconds
Fetch Todo      -> 2 seconds
Fetch Post      -> 4 seconds

Executed in Parallel

Total Time = 4 seconds
```

The overall execution time becomes the duration of the slowest task instead of the sum of all tasks.

---

# Architecture

```text
Client Request
      |
      v
Controller
      |
      v
CustomerInsightsService
      |
      +-----------------------------+
      |             |               |
      v             v               v
UserService    TodoService    PostService
      |             |               |
      v             v               v
External API  External API  External API
      |
      +-------------+--------------+
                    |
                    v
           CompletableFuture.allOf()
                    |
                    v
            Aggregated Response
```

---

# Project Structure

```text
com.asyncagg
│
├── config
│   └── AsyncConfig
│
├── controller
│   └── CustomerInsightsController
│
├── service
│   ├── CustomerInsightsService
│   ├── UserService
│   ├── TodoService
│   └── PostService
│
├── client
│   └── JsonPlaceholderClient
│
├── dto
│   ├── UserDto
│   ├── TodoDto
│   ├── PostDto
│   └── CustomerInsightsResponse
│
└── AsyncAggregatorApplication
```

---

# Key Async Concepts

---

## What is Asynchronous Programming?

Asynchronous programming is a programming model in which a task is delegated for execution and the caller continues processing without waiting for the task to complete.

### Synchronous Flow

```text
Task A
  |
Task B
  |
Task C
```

Execution Time:

```text
A + B + C
```

---

### Asynchronous Flow

```text
Task A ---->
Task B ---->
Task C ---->
```

Execution Time:

```text
Longest Running Task
```

---

# @EnableAsync

## Definition

`@EnableAsync` enables Spring's asynchronous method execution capability.

It instructs Spring to scan for methods annotated with `@Async` and create the necessary infrastructure required for asynchronous execution.

Example:

```java
@Configuration
@EnableAsync
public class AsyncConfig {
}
```

Without this annotation:

```java
@Async
public void process() {
}
```

will execute synchronously.

---

# @Async

## Definition

`@Async` is a Spring annotation that delegates method execution to a separate thread managed by a `TaskExecutor`.

Instead of executing the method on the caller thread, Spring submits the task to a thread pool and immediately returns control to the caller.

Example:

```java
@Async("apiExecutor")
public CompletableFuture<UserDto> getUser() {

    UserDto user = client.fetchUser();

    return CompletableFuture.completedFuture(user);
}
```

---

# Internal Working of @Async

Spring Async is implemented using Proxy-Based AOP.

When an async method is invoked:

```text
Caller Thread
      |
      v
Spring Proxy
      |
      v
TaskExecutor
      |
      v
Thread Pool
      |
      v
Worker Thread
      |
      v
Actual Method
```

---

## Execution Flow

```text
Client Request
      |
      v
Tomcat Thread
(http-nio-8080-exec-1)
      |
      v
Controller
      |
      v
Spring Proxy
      |
      v
TaskExecutor.submit()
      |
      +-------------------+
      |                   |
      v                   |
Request Thread Free       |
                          |
                          v
                  Worker Thread
                  (api-worker-1)
                          |
                          v
                    Async Method
```

---

# TaskExecutor

## Definition

A TaskExecutor is a Spring abstraction responsible for executing asynchronous tasks.

The TaskExecutor manages the thread pool and determines how tasks are executed.

Example:

```java
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
```

---

# Thread Pool

## Definition

A thread pool is a collection of reusable worker threads maintained by the executor.

Instead of creating a new thread for every request, existing threads are reused.

Benefits:

* Better performance
* Reduced thread creation overhead
* Improved scalability
* Controlled resource utilization

---

# CompletableFuture

## Definition

`CompletableFuture` is an asynchronous programming construct introduced in Java 8 that represents the future result of an asynchronous computation.

It allows:

* Non-blocking execution
* Chaining
* Combining multiple tasks
* Error handling
* Timeouts

---

# CompletableFuture.allOf()

## Definition

`CompletableFuture.allOf()` combines multiple futures into a single future.

The combined future completes only when all supplied futures complete.

Example:

```java
CompletableFuture.allOf(
        userFuture,
        todoFuture,
        postFuture
).join();
```

---

## What Happens Internally?

```text
userFuture   -> Running

todoFuture   -> Running

postFuture   -> Running

allOfFuture  -> Waiting
```

When all tasks complete:

```text
userFuture   -> Completed

todoFuture   -> Completed

postFuture   -> Completed

allOfFuture  -> Completed
```

---

# join()

## Definition

`join()` waits for a CompletableFuture to complete and returns its result.

Example:

```java
UserDto user = userFuture.join();
```

Equivalent to:

```text
Wait until userFuture completes
Return result
```

---

# Why Use allOf().join()?

Example:

```java
CompletableFuture.allOf(
        userFuture,
        todoFuture,
        postFuture
).join();
```

Meaning:

```text
Wait until ALL asynchronous tasks complete.
```

After completion:

```java
userFuture.join();
todoFuture.join();
postFuture.join();
```

return immediately because results are already available.

---

# Request Lifecycle

```text
Request Arrives
      |
      v
Controller
      |
      v
Launch Async Tasks
      |
      +-------------------------------+
      |               |               |
      v               v               v
User API        Todo API       Post API
      |               |               |
      +---------------+---------------+
                      |
                      v
      CompletableFuture.allOf()
                      |
                      v
          Aggregate Results
                      |
                      v
              Return Response
```

---

# Sample Response

```json
{
  "user": {
    "id": 1,
    "name": "Leanne Graham"
  },
  "todo": {
    "id": 1,
    "title": "delectus aut autem"
  },
  "post": {
    "id": 1,
    "title": "sunt aut facere"
  },
  "executionTimeMs": 842
}
```

---

# Interview Questions

## What is @Async?

`@Async` delegates method execution to a separate thread managed by a Spring TaskExecutor, allowing the caller thread to continue without blocking.

---

## Why is @EnableAsync required?

It activates Spring's asynchronous method execution infrastructure and enables detection of methods annotated with `@Async`.

---

## How does @Async work internally?

Spring creates a proxy around the target bean. The proxy intercepts calls to methods annotated with `@Async` and submits them to a TaskExecutor for execution on a worker thread.

---

## What is the role of TaskExecutor?

TaskExecutor manages asynchronous task execution and thread pools.

---

## What is CompletableFuture?

A Java abstraction representing the result of an asynchronous computation.

---

## What does allOf() do?

Combines multiple futures and completes only when all supplied futures complete.

---

## What does join() do?

Blocks the current thread until the future completes and returns the result.

---

## Why use a custom ThreadPoolTaskExecutor?

To control:

* Thread count
* Queue size
* Resource utilization
* Application scalability

instead of relying on default executor behavior.

---

# Future Enhancements

* Exception Handling
* Fallback Responses
* Timeout Management
* Retry Mechanisms
* Async Exception Handler
* Request Correlation IDs (MDC)
* Micrometer Metrics
* Actuator Monitoring
* Resilience4j Integration
* Distributed Tracing

---

# Key Takeaways

* Async improves responsiveness and scalability.
* @Async is implemented using Spring AOP proxies.
* TaskExecutor manages thread execution.
* Thread pools enable efficient resource utilization.
* CompletableFuture simplifies asynchronous workflows.
* allOf() is used to wait for multiple concurrent tasks.
* join() retrieves results from completed futures.
* Async is ideal for external API calls, notifications, reporting, and background processing.
* Self-invocation bypasses Spring proxies and prevents @Async from working.
* Custom executors should always be preferred in production systems.

```
```
