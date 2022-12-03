package com.example.account.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class CreateAccount {
	
	@Getter
	@Setter
	@AllArgsConstructor 
	@NoArgsConstructor
	@Builder
	public static class Request { //innerClass - 명시적이다
		@NotNull //@Valid 검증 조건 -> 어떻게 Build할 것인지 -> java.validation 어노테이션 중 NotNull, Min(1) 추가
		@Min(1)
		private Long userId;
		
		@NotNull
		@Min(100)
		private Long initialBalance;
	}
	
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Response {
		private Long userId;
		private String accountNumber;
		private LocalDateTime registeredAt;
		
		//AccountDTO 가공
		public static Response from(AccountDTO accountDTO) {
			return Response.builder()
					.userId(accountDTO.getUserId())
					.accountNumber(accountDTO.getAccountNumber())
					.registeredAt(accountDTO.getRegisteredAt())
					.build();
		}
		
	}
}
