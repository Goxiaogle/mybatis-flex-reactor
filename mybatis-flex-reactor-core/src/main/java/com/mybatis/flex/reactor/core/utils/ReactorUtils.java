package com.mybatis.flex.reactor.core.utils;

import com.mybatisflex.core.row.Db;
import org.apache.ibatis.cursor.Cursor;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class ReactorUtils {
    /**
     * 运行一个异步任务（弹性调度 | 忽略返回值）
     *
     * @param runnable 运行的任务
     * @return 任务生命周期对象
     */
    public static Disposable runAsync(Runnable runnable) {
        return runAsync(Mono.fromRunnable(runnable));
    }

    /**
     * 异步执行 Mono（弹性调度 | 忽略值）
     *
     * @param mono 目标 mono
     * @return 任务生命周期对象
     */
    public static <T> Disposable runAsync(Mono<T> mono) {
        return mono
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

    /**
     * 将 Cursor 转为 Flux
     *
     * @param supplier mybatis 游标对象（此处请传入一个函数，如果是将游标变量传入会导致抛出游标已关闭异常）
     * @param <T>    游标泛型
     * @return Flux
     */
    public static <T> Flux<T> cursorToFlux(Supplier<Cursor<T>> supplier) {
        return Flux.create(emitter -> Db.tx(() -> {
            try(Cursor<T> cursor = supplier.get()) {
                for (T it : cursor) {
                    emitter.next(it);
                }
            } catch (Exception e) {
                emitter.error(e);
                return false;
            }
            emitter.complete();
            return true;
        }));
    }
}
