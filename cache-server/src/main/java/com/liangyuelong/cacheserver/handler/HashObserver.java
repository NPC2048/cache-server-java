package com.liangyuelong.cacheserver.handler;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;

/**
 * calc 方法判断 memcached 中是否已有正在处理中的观察者
 *
 * @author yuelong.liang
 */
@Slf4j
public class HashObserver implements Observer<String> {

    private String result = "init";

    public HashObserver(Observable<String> observable) {
        observable.subscribe(this);
    }

    @Override
    public void onSubscribe(Disposable d) {
    }

    @Override
    public void onNext(String s) {
        this.result = s;
    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onComplete() {

    }

    public Object result() {
        return result;
    }

}
