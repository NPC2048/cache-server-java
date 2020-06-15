package com.liangyuelong.cacheserver.test;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RegExUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.DigestUtils;
import org.springframework.util.PatternMatchUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class RxjavaTest {

    /**
     * 被观察者
     */
    private Observable<String> observable;

    /**
     * 观察者
     */
    private Observer<String> reader;

    @Before
    public void init() {
        reader = new Observer<String>() {
            @Override
            public void onSubscribe(Disposable disposable) {
                log.info(disposable.toString());
            }

            @Override
            public void onNext(String s) {
                log.info("t: " + Thread.currentThread());
                log.info("server on next: " + s);
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {
                log.info("server on complete");
            }
        };
        observable.subscribe(reader);
    }

    @Test
    public void test() {
        System.out.println("当前线程: " + Thread.currentThread());
        observable.blockingSubscribe();
        observable.blockingSubscribe();
        System.out.println(reader);
        String ba = "Yj2l9tCHfZMh2fcZz2ssuog19az/jnU0PjDXJNRGqQ==";

    }
}
