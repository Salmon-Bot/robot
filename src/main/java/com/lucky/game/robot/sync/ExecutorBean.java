package com.lucky.game.robot.sync;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class ExecutorBean {

    @Value("${core.pool.size:120}")
    private int corePoolSize;
    @Value("${max.pool.size:200}")
    private int maxPoolSize;
    private int queueCapacity = 1;


    @Bean
    public Executor marketMonitor() {
        ThreadPoolTaskExecutor executor = init();
        executor.setThreadNamePrefix("marketMonitor-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor zbMarketMonitor() {
        ThreadPoolTaskExecutor executor = init();
        executor.setThreadNamePrefix("zbMarketMonitor-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor oneMarketMonitor() {
        ThreadPoolTaskExecutor executor = init();
        executor.setThreadNamePrefix("oneMarketMonitor-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor initScaleToRedisAndMonitor() {
        ThreadPoolTaskExecutor executor = init();
        executor.setThreadNamePrefix("initScaleToRedisAndMonitor-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor shuffleMonitor() {
        ThreadPoolTaskExecutor executor = init();
        executor.setThreadNamePrefix("shuffleMonitor-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor syncKlineInsert() {
        ThreadPoolTaskExecutor executor = init();
        executor.setThreadNamePrefix("syncKlineInsert-");
        executor.initialize();
        return executor;
    }

    private ThreadPoolTaskExecutor init() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        return executor;

    }

}
