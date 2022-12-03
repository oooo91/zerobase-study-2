package com.example.account.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.account.dto.ErrorResponse;
import com.example.account.type.ErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice //전역적으로 예외를 처리할 수 있는 어노테이션
public class GlobalExceptionHandler {
	
	@ExceptionHandler(AccountException.class) //accountException이 발생했을 때 처리하는 예외핸들러
	public ErrorResponse handleAccountException(AccountException e) {
		log.error("{} is occurred", e.getErrorCode());
		return new ErrorResponse(e.getErrorCode(), e.getErrorMessage());
	}
	
	@ExceptionHandler(DataIntegrityViolationException.class) //DB의 유니크 키 중복 등의 예외
	public ErrorResponse handlerDataIntegrityViolationException(DataIntegrityViolationException e) {
		log.error("DataIntegrityVioleationException is occurred", e);
		return new ErrorResponse(ErrorCode.INVALID_REQUEST, ErrorCode.INVALID_REQUEST.getDescription());
	}
	
	@ExceptionHandler(Exception.class) //accountException 말고 다른 모든 exception (우리가 모르는 내부 서버의 문제)
	public ErrorResponse handleException(Exception e) {
		log.error("Exception is occurred", e);
		return new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR.getDescription());
	}
}
