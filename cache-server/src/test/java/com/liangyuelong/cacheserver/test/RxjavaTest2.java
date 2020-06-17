package com.liangyuelong.cacheserver.test;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import org.junit.Before;
import org.junit.Test;

public class RxjavaTest2 {

    private BehaviorSubject<String> subject;

    private Observable<String> observable;


    @Before
    public void init() {
        subject = BehaviorSubject.create();
        observable = Observable.fromArray("a", "b", "c", "d");
        observable.subscribe(subject);
    }

    @Test
    public void test() {
        System.out.println(subject.getValue());
    }

}
