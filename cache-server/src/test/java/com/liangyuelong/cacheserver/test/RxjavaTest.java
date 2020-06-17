package com.liangyuelong.cacheserver.test;

import com.liangyuelong.cacheserver.handler.HashObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public class RxjavaTest {

    /**
     * 被观察者
     */
    private Observable<String> observable;

    /**
     * 观察者
     */
    private HashObserver reader;

    @Before
    public void init() {
        observable = Observable.create((ObservableOnSubscribe<String>) emitter -> {
            String md5 = DigestUtils.md5DigestAsHex(RandomStringUtils.random(100).getBytes(StandardCharsets.UTF_8));
            String base64 = Base64.getEncoder().encodeToString(md5.getBytes(StandardCharsets.UTF_8));
            emitter.onNext("hash:" + base64);
            emitter.onComplete();
        }).subscribeOn(Schedulers.computation());

        reader = new HashObserver(observable);
//        observable.subscribe(reader);
    }

    @Test
    public void test() {
        System.out.println("当前线程: " + Thread.currentThread());
        System.out.println(reader.result());
        observable.blockingSubscribe();
        observable.blockingSubscribe();

        System.out.println(reader);


    }
}
