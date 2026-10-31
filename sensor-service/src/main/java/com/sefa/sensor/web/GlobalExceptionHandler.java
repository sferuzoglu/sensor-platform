package com.sefa.sensor.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sefa.sensor.exceptions.DuplicateSensorNameException;
import com.sefa.sensor.exceptions.SensorNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler
    public ProblemDetail duplicateSensorNameExceptionHandler(DuplicateSensorNameException e) {

        log.info("Mükerrer sensör adı: {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler
    public ProblemDetail dataIntegrityViolationExceptionHandler(DataIntegrityViolationException e) {

        log.warn("Veri bütünlüğü ihlali", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Bu isimde bir sensör zaten var.");
    }

    @ExceptionHandler
    public ProblemDetail sensorNotFoundExceptionHandler(SensorNotFoundException e) {

        log.info("{}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                "Sensor Bulunamadi");
    }

    @ExceptionHandler
    public ProblemDetail methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {

        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();

        fieldErrors.forEach(error -> log.warn("Validation Error | Field: {} | Value: {} | Message: {}",
                error.getField(),
                error.getRejectedValue(),
                error.getDefaultMessage()));

        Map<String, String> errors = new LinkedHashMap<>();

        fieldErrors.forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Hatalı parametre");

        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

}
