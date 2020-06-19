package com.liangyuelong.cacheserver.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 主要 controller
 *
 * @author yuelong.liang
 */
@RestController
public class MainController {

    @RequestMapping("/{path}")
    public Object resolve(@PathVariable String path, HttpServletRequest request, String input) {
        if (StringUtils.isEmpty(input)) {

        }
        return null;
    }

}
