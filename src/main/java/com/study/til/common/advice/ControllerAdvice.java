package com.study.til.common.advice;

import com.study.til.common.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ControllerAdvice {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NumberFormatException.class)
    protected ErrorResponse errorTest(final NumberFormatException error) {
        return ErrorResponse.error(HttpStatus.BAD_REQUEST.value(), error.getMessage());
    }
}
