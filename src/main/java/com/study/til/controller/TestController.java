package com.study.til.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/success")
    public String successTest() {
        return "test";
    }

    @GetMapping("/warn")
    public String warnTest() {
        throw new NumberFormatException("waning");
    }
}
