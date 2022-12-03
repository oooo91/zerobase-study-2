package com.example.account.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class DeleteAccount {
	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class Request { 
		@NotNull
		@Min(1)
		private Long userId;
		
		@NotBlank //notNull 보다 강력하다
		@Size(min = 10, max = 10) //문자열 길이
		private String accountNumber; //계좌번호
	}
	
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Response {
		private Long userId;
		private String accountNumber;
		private LocalDateTime unregisteredAt; //등록안된 날짜
		
		//AccountDTO 가공
		public static Response from(AccountDTO accountDTO) {
			return Response.builder()
					.userId(accountDTO.getUserId())
					.accountNumber(accountDTO.getAccountNumber())
					.unregisteredAt(accountDTO.getUnRegisteredAt())
					.build();
		}
		
	}
}
