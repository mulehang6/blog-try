package com.blogdev.blog;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

//注解使这个类成为一个全局的异常处理组件，Spring Boot会自动扫描并应用它。
@ControllerAdvice
public class GlobalExceptionHandler {

    //注解指定这个方法专门处理 MethodArgumentNotValidException 异常。
    //当 @Valid 校验失败时，就会抛出这个异常。
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> HandleValidationException(MethodArgumentNotValidException e) {
        //字段名 -> 错误信息
        Map<String,String> errors = new HashMap<>();

        //遍历所有校验失败的错误
        e.getBindingResult().getAllErrors().forEach(error -> {
            //导致错误的具体字段名
            String name = ((FieldError) error).getField();
            //获取错误信息
            String errorMessage = error.getDefaultMessage();

            errors.put(name,errorMessage);
        });

        return new ResponseEntity<>(errors,HttpStatus.BAD_REQUEST);
    }

}
