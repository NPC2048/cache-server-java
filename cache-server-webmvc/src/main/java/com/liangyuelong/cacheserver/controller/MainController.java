package com.liangyuelong.cacheserver.controller;

import com.liangyuelong.cacheserver.util.HashServerUtils;
import com.liangyuelong.cacheserver.util.HashUtils;
import com.whalin.MemCached.MemCachedClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

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

    @Resource(name = "hashGetThreadPoolTaskExecutor")
    private ThreadPoolTaskExecutor getPool;

    private Map<String, BlockingQueue<CurrentThread>> map = new ConcurrentHashMap<>(256);

    @RequestMapping("/{path}")
    public Object resolve(@PathVariable String path, HttpServletRequest request, String input, @RequestBody(required = false) String body) throws ExecutionException, InterruptedException {
        if (StringUtils.isEmpty(input)) {
            return HashServerUtils.request(path, request.getMethod(), HashUtils.getHeaders(request), HashUtils.getParams(request), StringUtils.getBytes(body, StandardCharsets.UTF_8));
        }
        String key = DigestUtils.md5DigestAsHex(input.getBytes(StandardCharsets.UTF_8));
        String hash = (String) client.get(key);
        if (hash != null) {
            return hash;
        }
        BlockingQueue<CurrentThread> queue = map.get(key);
        if (queue != null) {
            CurrentThread thread = new CurrentThread(Thread.currentThread());
            queue.offer(thread);
            LockSupport.park();
            return thread.value;
        }
        queue = new LinkedBlockingQueue<>();
        map.put(key, queue);
        // 从
        hash = getPool.submit(() -> {
            while (true) {
                try {
                    String getHash = HashServerUtils.request(path, request.getMethod(), HashUtils.getHeaders(request), HashUtils.getParams(request), StringUtils.getBytes(body, StandardCharsets.UTF_8));
                    boolean isSuccess = HashServerUtils.isSuccess(getHash);
                    log.info("body: " + getHash);
                    log.info("success: " + isSuccess);
                    if (isSuccess) {
                        return getHash;
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }).get();
        // 通知 queue
        map.remove(key);
        // 存入 mem
        client.set(key, hash);
        for (CurrentThread currentThread : queue) {
            currentThread.value = hash;
            LockSupport.unpark(currentThread.thread);
        }
        return hash;
    }

    public static class CurrentThread {

        private Thread thread;

        private String value;

        public CurrentThread(Thread thread) {
            this.thread = thread;
        }
    }

}
