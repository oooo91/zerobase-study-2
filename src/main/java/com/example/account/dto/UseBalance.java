package com.example.account.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.example.account.aop.AccountLockIdInterface;
import com.example.account.type.TransactionResultType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class UseBalance {
	
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor 
	@Builder 
	public static class Request implements AccountLockIdInterface {  //lockAopAspect에서 accountNumber을 가져올 때 Request request 의 공통 부문인 accoutNumber을 가져오기 위해 인터페이스를 상속
		@NotNull
		@Min(1)
		private Long userId;
		
		@NotBlank
		@Size(min = 10, max = 10)
		private String accountNumber;
		
		@NotNull
		@Min(10)
		@Max(1000_000_000) //최대거래금액 10억
		private Long amount;
	}
	
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder 
	public static class Response {
		private String accountNumber;
		private TransactionResultType transactionResultType;
		private String transactionId;
		private Long amount;
		private LocalDateTime transactedAt;
		
		public static Response from(TransactionDTO transactionDTO) {
			return Response.builder()
					.accountNumber(transactionDTO.getAccountNumber())
					.transactionResultType(transactionDTO.getTransactionResultType())
					.transactionId(transactionDTO.getTransactionId())
					.amount(transactionDTO.getAmount())
					.transactedAt(transactionDTO.getTransactedAt())
					.build();
		}
	
	}

		
}
