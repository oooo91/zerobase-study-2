package com.example.account.service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.domain.Transaction;
import com.example.account.dto.TransactionDTO;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
	private final TransactionRepository transactionRepository; 
	private final AccountUserRepository accountUserRepository; //userId 
	private final AccountRepository accountRepository; //accountNumber 
	
	@Transactional
	public TransactionDTO useBalance(Long userId, String accountNumber, Long amount) {
		
		AccountUser user = accountUserRepository.findById(userId).orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));
		
		Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND)); 
		
		validateUseBalance(user, account, amount); //유효성 확인
		System.out.println("amount 값: " + amount);
		
		account.useBalance(amount); //잔액 변경

		return TransactionDTO.fromEntity(saveAndGetTransaction(TransactionType.USE, TransactionResultType.S, account, amount));
	}
	
	
	private void validateUseBalance(AccountUser user, Account account, Long amount) {
		if(!Objects.equals(user.getId(), account.getAccountUser().getId())) {
			throw new AccountException(ErrorCode.USER_ACCOUNT_UN_MATCH);
		}
		
		if(account.getAccountStatus() != AccountStatus.IN_USE) {
			throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
		}
		
		if(account.getBalance() < amount)  { //잔액보다 높은 금액 사용하려는 경우
			throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
		}
	}


	@Transactional
	public void saveFailedUseTransaction(String accountNumber, Long amount) {
		Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));
		
		//해당 계좌가 잔액 사용 오류 기록
		saveAndGetTransaction(TransactionType.USE, TransactionResultType.F, account, amount);
		
	}
	
	//저장
	private Transaction saveAndGetTransaction(TransactionType transactionType, TransactionResultType transactionResultType, Account account, Long amount) {
		
		return transactionRepository.save(
				Transaction.builder()
				.transactionType(transactionType)
				.transactionResultType(transactionResultType) //Fail
				.account(account)
				.amount(amount)
				.balanceSnapshot(account.getBalance()) //변경된 값 저장
				.transactionId(UUID.randomUUID().toString().replace("-", "")) //UUID란? UNIQUE IDENTIFIED , 대쉬는 없애기
				.transactedAt(LocalDateTime.now())
				.build()
				);
	}


	@Transactional
	public TransactionDTO cancelBalance(String transactionId, String accountNumber, Long amount) {
		
		Transaction transaction = transactionRepository.findByTransactionId(transactionId).orElseThrow(() -> new AccountException(ErrorCode.TRANSACTION_NOT_FOUND));
		Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));
		
		validateCancelBalance(transaction, account, amount);
		
		account.cancelBalance(amount);
		
		return TransactionDTO.fromEntity(saveAndGetTransaction(TransactionType.CANCEL, TransactionResultType.S, account, amount)); 
	}


	private void validateCancelBalance(Transaction transaction, Account account, Long amount) {
		if(!Objects.equals(transaction.getAccount().getId(), account.getId())) { //transaction.getAccount().getId() != account.getId()
			throw new AccountException(ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH);
		}
		
		if(!Objects.equals(transaction.getAmount(), amount)) {
			throw new AccountException(ErrorCode.CANCEL_MUST_FULLY);
		}
		
		if(transaction.getTransactedAt().isBefore(LocalDateTime.now().minusYears(1))) { //isBefore : 거래시간이 1년보다 더 이전이면 에러, LocalDateTime에서 제공하는 메서드임
			throw new AccountException(ErrorCode.TOO_OLD_ORDER_TO_CANCEL);
		}
		
	}


	@Transactional
	public void saveFailedCancelTransaction(String accountNumber, @NotNull Long amount) {
		
		Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));
		
		//해당 계좌가 잔액 사용 오류 기록
		saveAndGetTransaction(TransactionType.CANCEL, TransactionResultType.F, account, amount);

	}


	public TransactionDTO queryTransaction(String transactionId) {
		return TransactionDTO.fromEntity(transactionRepository.findByTransactionId(transactionId).
				orElseThrow(() -> new AccountException(ErrorCode.TRANSACTION_NOT_FOUND)));
	
	}
}
