package com.liangyuelong.cacheserver.util;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class HashUtils {

    /**
     * 从 HttpServletRequest 中获取 header 信息
     * 组装成 map
     *
     * @param request request
     * @return static
     */
    public static Map<String, String> getHeaders(HttpServletRequest request) {
        Enumeration<String> enumeration = request.getHeaderNames();
        Map<String, String> headers = new HashMap<>();
        while (enumeration.hasMoreElements()) {
            String headerName = enumeration.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return headers;
    }

    public static Map<String, String> getParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> enumeration = request.getParameterNames();
        while (enumeration.hasMoreElements()) {
            String paramName = enumeration.nextElement();
            params.put(paramName, request.getParameter(paramName));
        }
        return params;
    }

}
