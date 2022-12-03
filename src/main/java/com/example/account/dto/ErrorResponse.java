package com.example.account.dto;

import com.example.account.type.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class ErrorResponse { //각각 트라이-캐치로 예외처리하는 것보다 글로벌한 exceptionHandler을 만드는 것이 좋다 -> exception.GlobalExceptionHandler
	private ErrorCode errorCode;
	private String errorMessage;
}
