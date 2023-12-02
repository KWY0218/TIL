package com.study.til.common.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@RequiredArgsConstructor
public class ErrorResponse {
    private final int value;
    private final String msg;

    public static ErrorResponse error(int value, String msg) {
        return new ErrorResponse(value, msg);
    }
}
