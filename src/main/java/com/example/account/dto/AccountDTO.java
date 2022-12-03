package com.example.account.dto;

import java.time.LocalDateTime;

import com.example.account.domain.Account;

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
public class AccountDTO {
	private Long userId;
	private String accountNumber;
	private Long balance;
	
	private LocalDateTime registeredAt;
	private LocalDateTime unRegisteredAt;
	
	//Account -> AccountDTO 로 변환
	public static AccountDTO fromEntity(Account account) {
		return AccountDTO.builder()
					.userId(account.getAccountUser().getId())
					.accountNumber(account.getAccountNumber())
					.balance(account.getBalance())
					.registeredAt(account.getRegisteredAt())
					.unRegisteredAt(account.getUnregiteredAt())
					.build();
	}
}
