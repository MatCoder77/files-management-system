package com.awscourse.filesmanagementsystem.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration
public class ExecutorConfiguration {

    private static final int CORE_POOL_SIZE = 100;
    private static final int MAX_POOL_SIZE = 200;
    private static final int QUEUE_CAPACITY = 2000;

    @Bean
    public AsyncTaskExecutor customizedThreadPoolExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.initialize();
        return executor;
    }

}
