package com.example.house.common.exception;

import com.example.house.common.api.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBusiness(BusinessException ex) {
        return ApiResponse.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            String message = methodArgumentNotValidException.getBindingResult()
                .getAllErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("request validation failed");
            return ApiResponse.fail(4001, message);
        }
        if (ex instanceof BindException bindException) {
            String message = bindException.getBindingResult()
                .getAllErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("request validation failed");
            return ApiResponse.fail(4001, message);
        }
        if (ex instanceof ConstraintViolationException constraintViolationException) {
            String message = constraintViolationException.getConstraintViolations()
                .stream()
                .findFirst()
                .map(violation -> violation.getMessage())
                .orElse("request validation failed");
            return ApiResponse.fail(4001, message);
        }
        return ApiResponse.fail(4001, "request validation failed");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleOther(Exception ex) {
        return ApiResponse.fail(5000, ex.getMessage());
    }
}
