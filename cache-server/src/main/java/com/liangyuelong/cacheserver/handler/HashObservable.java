package com.liangyuelong.cacheserver.handler;

import com.liangyuelong.cacheserver.hash.HashServerUtils;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import org.springframework.http.server.reactive.ServerHttpRequest;

/**
 * 观察 input 值
 * 完成对 server 的请求后
 * 通知其他同一个 input 的地方返回值
 *
 * @author yuelong.liang
 */
public class HashObservable implements ObservableOnSubscribe<String> {

    private ServerHttpRequest request;

    private String path;

    private String body;

    public HashObservable(ServerHttpRequest request, String path, String body) {
        this.request = request;
        this.path = path;
        this.body = body;
    }

    @Override
    public void subscribe(ObservableEmitter<String> emitter) {
        String hash;
        // 从 hash server 获取值
        do {
            hash = HashServerUtils.request(request, path, body);
            // 判断是否正确
        } while (!HashServerUtils.isSuccess(hash));
        // 请求成功, 通知其他 input 解锁
        emitter.onNext(hash);
        emitter.onComplete();
    }
}
