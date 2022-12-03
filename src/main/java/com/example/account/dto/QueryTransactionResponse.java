package com.example.account.dto;

import java.time.LocalDateTime;

import com.example.account.dto.CancelBalance.Response;
import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryTransactionResponse {
	private String accountNumber;
	private TransactionType transactionType;
	private TransactionResultType transactionResultType;
	private String transactionId;
	private Long amount;
	private LocalDateTime transactedAt;
	
	public static QueryTransactionResponse from(TransactionDTO transactionDTO) {
		return QueryTransactionResponse.builder()
				.accountNumber(transactionDTO.getAccountNumber())
				.transactionType(transactionDTO.getTransactionType())
				.transactionResultType(transactionDTO.getTransactionResultType())
				.transactionId(transactionDTO.getTransactionId())
				.amount(transactionDTO.getAmount())
				.transactedAt(transactionDTO.getTransactedAt())
				.build();
	}
}
