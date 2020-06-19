package com.liangyuelong.cacheserver.util;

import com.github.kevinsawicki.http.HttpRequest;
import com.liangyuelong.cacheserver.config.CommConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * hash server 工具类
 *
 * @author yuelong.liang
 */
@Configuration
public class HashServerUtils {

    /**
     * 编译正则
     */
    private static Pattern pattern = Pattern.compile("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$");

    /**
     * server 服务器地址
     */
    private static String hashServerHost;

    @Autowired
    public void init(CommConfig commConfig) {
        hashServerHost = commConfig.getHashServerHost();
    }

    /**
     * 转发请求值 hash server
     *
     * @param path        转发 url 路径
     * @param method      请求方法类型
     * @param headers     请求头 map
     * @param params      请求参数 map
     * @param requestBody request body 字节数组
     * @return response body
     */
    public static String request(String path, String method, Map<String, String> headers, Map<String, String> params, byte[] requestBody) {
        // 添加请求参数并 url 编码
        String url = HttpRequest.encode(HttpRequest.append(hashServerHost + path, params));
        // 组装 http 报文
        HttpRequest http = new HttpRequest(url, method).headers(headers);
        // 设置 request body
        if (requestBody != null) {
            http = http.send(requestBody);
        }
        return http.body();
    }

    /**
     * 判断 server 返回是否正确
     * 1.符合 base64 格式, 成功
     * 2.Too busy. Service unavailable. 不成功, server 忙
     * 3.彩蛋
     *
     * @param body hash
     */
    public static boolean isSuccess(String body) {
        if (StringUtils.isEmpty(body)) {
            return false;
        }
        if (pattern.matcher(body).matches()) {
            return true;
        }
        return !body.equals("Too busy. Service unavailable.");
    }

}
