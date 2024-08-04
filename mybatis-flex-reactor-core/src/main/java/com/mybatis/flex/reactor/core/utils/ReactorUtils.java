package com.mybatis.flex.reactor.core.utils;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ExecutionException;

public class ReactorUtils {
    /**
     * 运行一个异步任务（弹性调度 | 忽略返回值）
     *
     * @param runnable 运行的任务
     * @return 任务生命周期对象
     */
    public static Disposable runAsync(Runnable runnable) {
        return Mono.fromRunnable(runnable)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    /**
     * 直接获取 Mono 的值
     *
     * @param mono 目标 Mono
     * @param <T>  值泛型
     * @return Mono 中的值
     */
    public static <T> T runBlock(Mono<T> mono) {
        try {
            return mono.toFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
