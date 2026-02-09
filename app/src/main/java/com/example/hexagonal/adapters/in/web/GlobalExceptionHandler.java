package com.example.hexagonal.adapters.in.web;

import com.example.hexagonal.adapters.in.web.dto.ErrorResponse;
import com.example.hexagonal.domain.coupon.CouponNotFoundException;
import com.example.hexagonal.domain.coupon.CouponValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CouponNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCouponNotFound(CouponNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("COUPON_NOT_FOUND", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(CouponValidationException.class)
    public ResponseEntity<ErrorResponse> handleCouponInvalid(CouponValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("COUPON_INVALID", ex.getMessage(), Instant.now()));
    }
}
