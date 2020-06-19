package com.liangyuelong.cacheserver.test;

import com.github.kevinsawicki.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
public class HttpTest {

    int size = 20;
    int[] seed;
    int count = 200;

    // 总共 10 波
    int total = 20;

    private static Pattern pattern = Pattern.compile("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$");

    @Before
    public void init() {
        seed = new int[size];
        for (int i = 0; i < size; i++) {
            seed[i] = i;
        }
    }

    private volatile long maxTime;

    @Test
    public void test() throws InterruptedException {
        String host = "http://18.234.28.187:8080/calc";
//        host = "http://cache-elb-897235646.ap-southeast-1.elb.amazonaws.com/calc";
        host = "http://47.107.78.83:6660/calc";
//        host = "http://cache-elb-897235646.ap-southeast-1.elb.amazonaws.com/calc";
        host = "http://localhost:8081/speed";
//        host = "http://139.9.142.248:8080/calc";
        int index = 1;
        CountDownLatch countDownLatch = new CountDownLatch(count * total);
        long beginTime = System.currentTimeMillis();
        while (total-- > 0) {
            String finalHost = host;
            System.out.println("==================== 第" + index + "波流量 ==================");
            System.out.println("==================== 第" + index + "波流量 ==================");
            System.out.println("==================== 第" + index + "波流量 ==================");
            System.out.println("==================== 第" + index + "波流量 ==================");
            ConcurrentRun.run(count, () -> {
                long time = System.currentTimeMillis();
                Object input = seed[RandomUtils.nextInt(0, size)];
//                input = RandomStringUtils.random(100);
                HttpRequest request = HttpRequest.get(finalHost,
                        true, "input", input);
                String body = request.body();
                time = System.currentTimeMillis() - time;
                if (time > maxTime) {
                    maxTime = time;
                }
                countDownLatch.countDown();
                log.info("第" + (countDownLatch.getCount()) + "个请求");
                log.info("input:" + input);
                log.info("body:" + body);
                log.info("time: " + time + " ms");
                log.info("success:" + pattern.matcher(body).matches());
            });
            System.out.println("==================== 第" + index + "波流量结束 ==================");
            System.out.println("==================== 第" + index + "波流量结束 ==================");
            System.out.println("==================== 第" + index + "波流量结束 ==================");
            System.out.println("==================== 第" + index + "波流量结束 ==================");
            index++;
            TimeUnit.MICROSECONDS.sleep(100);
        }
        beginTime = System.currentTimeMillis() - beginTime;
        System.out.println("最长用时:" + maxTime);
        System.out.println("完成用时:" + beginTime);
    }

}
