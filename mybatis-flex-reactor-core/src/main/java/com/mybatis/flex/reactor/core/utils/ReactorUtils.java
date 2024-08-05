package com.mybatis.flex.reactor.core.utils;

import com.mybatisflex.core.row.Db;
import org.apache.ibatis.cursor.Cursor;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
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
        return runAsync(Mono.fromRunnable(runnable));
    }

    /**
     * 异步执行 Mono（弹性调度 | 忽略值）
     *
     * @param mono 目标 mono
     * @return 任务生命周期对象
     */
    public static Disposable runAsync(Mono<Object> mono) {
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
     * @param cursor mybatis 游标对象
     * @param <T>    游标泛型
     * @return Flux
     */
    public static <T> Flux<T> cursorToFlux(Cursor<T> cursor) {
        return Flux.create(emitter -> Db.tx(() -> {
            try (cursor) {
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
