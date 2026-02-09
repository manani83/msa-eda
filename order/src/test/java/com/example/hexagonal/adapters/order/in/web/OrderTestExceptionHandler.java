package com.example.hexagonal.adapters.order.in.web;

import com.example.hexagonal.domain.coupon.CouponNotFoundException;
import com.example.hexagonal.domain.coupon.CouponValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class OrderTestExceptionHandler {

    @ExceptionHandler(CouponNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCouponNotFound(CouponNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorBody("COUPON_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(CouponValidationException.class)
    public ResponseEntity<Map<String, Object>> handleCouponInvalid(CouponValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorBody("COUPON_INVALID", ex.getMessage()));
    }

    private Map<String, Object> errorBody(String code, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message);
        body.put("timestamp", Instant.now().toString());
        return body;
    }
}
