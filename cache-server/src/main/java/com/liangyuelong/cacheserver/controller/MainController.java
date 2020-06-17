package com.liangyuelong.cacheserver.controller;

import com.github.kevinsawicki.http.HttpRequest;
import com.liangyuelong.cacheserver.hash.HashServerUtils;
import com.whalin.MemCached.MemCachedClient;
import io.reactivex.Observable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;

/**
 * 主要 controller
 *
 * @author yuelong.liang
 */
@RestController
@Slf4j
public class MainController {

    @Resource
    private MemCachedClient client;

    @Value("${cache-server.hash-server-host}")
    private String hashServerHost;

    //    @RequestMapping("/{path}")
    public Mono<?> calc(@PathVariable String path, ServerHttpRequest request, String input, @RequestBody(required = false) String body) {
        log.info("thread: " + Thread.currentThread());
        log.info("target path: " + path);
        log.info("body: " + body);
        log.info("input: " + input);
        log.info(request.toString());
        // 对 input 进行 md5 取值
        String key = DigestUtils.md5DigestAsHex(input.getBytes(StandardCharsets.UTF_8));
        // 从 redis 获取
        String hash = (String) client.get(key);
//        String hash = redisTemplate.opsForValue().get(key);
        // hash 不为空，直接返回
        if (hash != null) {
//            log.info("缓存命中: " + key);
            return Mono.just(hash);
        }
        // 加入订阅者队列

        // 从 hash server 获取值
        do {
            hash = HashServerUtils.request(request, path, body);
            // 判断是否正确
        } while (!HashServerUtils.isSuccess(hash));
        // 缓存至 redis
        client.set(key, hash);
        return Mono.just(hash);
    }

    private ArrayBlockingQueue<FluxSink<String>> queue = new ArrayBlockingQueue<>(100);

    private ArrayBlockingQueue<String> mark = new ArrayBlockingQueue<>(1);

    /**
     * 模拟，第一个请求等待 10 后返回, 后续的请求加入队列，等待第一个请求返回的值
     * 暂不考虑线程安全的问题
     *
     * @param request request
     * @param input   input
     * @return Flux
     */
    @RequestMapping("/ac")
    public Flux<String> ac(ServerHttpRequest request, String input) {
        // 判断是否为第一个
        Consumer<FluxSink<String>> sink;
        if (mark.isEmpty()) {
            mark.add(input);
            log.info("第一个:" + request.getId() + ", input: " + input);
            sink = fluxSink -> {
                // 请求
                String hash = HttpRequest.get(hashServerHost + "/calc", true, "input", input).body();
                // 返回
                fluxSink.next(hash);
                fluxSink.complete();
                // 通知其他 flux
                for (FluxSink<String> stringFluxSink : queue) {
                    stringFluxSink.next(hash);
                    stringFluxSink.complete();
                }
                mark.clear();
            };
        } else {
            log.info("后续: " + request.getId() + ", input:" + input);
            sink = fluxSink -> {
                queue.add(fluxSink);
            };
        }
        return Flux.create(sink);
    }


    @RequestMapping("/ab")
    public Flux<String> ab(ServerHttpRequest request, String input) {
        Observable<String> observable = Observable.create(emitter -> {
            emitter.onNext("hello world");
            emitter.onComplete();
        });
//        HashObserver hashObserver = new HashObserver(hashObservable);
//        response.bufferFactory().wrap("")
//        return Mono.just("String");
//        DataBuffer dataBuffer = response.bufferFactory().wrap("hello world".getBytes());
//        return Flux.empty();
        System.out.println(request.getQueryParams().toSingleValueMap());
        // 是否正在处理中
        Flux<String> flux = Flux.create(stringFluxSink -> {
            String hkey = (String) client.get("h-" + input);
            // 否, 创建，标志为处理中
            if (StringUtils.isEmpty(hkey)) {
                String value = RandomStringUtils.random(10, "abcdefghijklmnopqrstuvwxyz1234567890");
                log.info(request.getId() + "处理key: " + hkey + ", value: " + value);
                boolean setBool = client.set(hkey, value);
                log.info(request.getId() + "更新 key: " + hkey + ", result: " + setBool);
            } else {

            }
            // 判断是否已存在获取 key 的 Flux
            stringFluxSink.complete();
        });

        flux.subscribe(System.out::println);
        return flux;
    }
}
