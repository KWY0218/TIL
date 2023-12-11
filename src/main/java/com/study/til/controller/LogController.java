package com.study.til.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class LogController {

    @GetMapping("/test/1")
    public String test1() {
        log.info("test1");
        return "return test 1";
    }

    @GetMapping("/test/2")
    public String test2() {
        log.info("test2");
        return "return test 2";
    }

    @GetMapping("/test/err")
    public void test3() {
        throw new ArrayStoreException("error");
    }
}
