package org.seefly.mymq.rocketmq.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author liujianxin
 * @date 2019-04-25 17:37
 */
@RestController
public class LogServerController {

    @GetMapping("/log")
    public String test(){
        int i = 1 /0;
        return "ok";
    }
}
