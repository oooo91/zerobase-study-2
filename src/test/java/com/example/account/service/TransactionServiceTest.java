package com.example.account.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

	public static final long USE_AMOUNT = 200L;
	public static final long CANCEL_AMOUNT = 200L;
	
	@Mock
	private AccountRepository accountRepository;
	
	@Mock
	private AccountUserRepository accountUserRepository;
	
	@Mock
	private TransactionRepository transactionRepository;
	
	@InjectMocks
	private TransactionService transactionService;
	
	@Test
	void successUseBalance() {
		//given
		AccountUser user = AccountUser.builder().id(12L).name("Pobi").build();
		
		Account account = Account.builder()
				.accountUser(user)
				.balance(10000L)
				.accountStatus(AccountStatus.IN_USE)
				.accountNumber("1000000012")
				.build();
		
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.of(user));
		given(accountRepository.findByAccountNumber(anyString())).willReturn(Optional.of(account));
		given(transactionRepository.save(any())).willReturn(Transaction.builder()
				.account(account)
				.transactionType(TransactionType.USE)
				.transactionResultType(TransactionResultType.S)
				.transactionId("transactionId")
				.transactedAt(LocalDateTime.now())
				.amount(1000L)
				.balanceSnapshot(9000L) // 10000L - 1000L
				.build());
		
		/*
		 위는 given 메소드만 실행한 것으로, account.useBalance(amount) 실행 안함
		 예상되는 잔액 변경값을 수동적으로 넣어서 그 값을 확인한 것뿐이다.
		 따라서 실질적으로 DB에 저장되는지 알 수 없다. 이때 ArgumentCaptor을 사용한다.
		 
		 모의 객체 ArgumentCaptor? 
		 특정 메소드에 사용되는 argument를 captor(저장)했다가 검증 시 다시 사용(getValue)할 수 있다
		 argument 테스트 시 진행한다.
		
		 1. useBalance(1, "1000000000", 200)
		 2. given에서 정의된 findById
		 3. given에서 정의된 accountNumber
		 4. TransactionService의 account.useBalance(200)
		 5. given에서 정의된 Build 객체로 save를 하는 것이 아니라, 1-2-3-4 과정으로 만들어진 Transaction 토대로 save 진행
		*/
		
		ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
		
		//when
		TransactionDTO transactionDTO = transactionService.useBalance(1L, "1000000000", 200L);
		
		//then
		assertEquals(9000L, transactionDTO.getBalanceSnapshot()); //10000 - 1000
		assertEquals(TransactionResultType.S, transactionDTO.getTransactionResultType());
		assertEquals(TransactionType.USE, transactionDTO.getTransactionType());
		assertEquals(1000L, transactionDTO.getAmount());
		
		/*
	 		verify(): 메서드가 주어진 인자로 호출되었는지 검증한다.
			유연한 인자 매칭을 사용할 수 있다. (ex. any())
			@Captor를 이용하여 어떤 인자로 호출되었는지 확인할 수 있다
		*/
		
		verify(transactionRepository, times(1)).save(captor.capture()); //저장이 되는가
		
		assertEquals(200L, captor.getValue().getAmount());
		assertEquals(account, captor.getValue().getAccount());
		assertEquals(TransactionType.USE, captor.getValue().getTransactionType());
		assertEquals(9800L, captor.getValue().getBalanceSnapshot()); //10000 - 200
		
	}
	
	
	@Test
	@DisplayName("해당 유저 없음 - 잔액 사용 실패")
	void createAccount_UserNotFound() {
		//given
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.empty()); 
		
		//when
		AccountException exception = assertThrows(AccountException.class, ()-> transactionService.useBalance(1L, "1000000000", 1000L));
		
		//then
		assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
	}
	
	
	@Test
	@DisplayName("해당 계좌 없음 - 잔액 사용 실패")
	void deleteAccount_AccountNotFound() {
		//given 
		AccountUser user = AccountUser.builder().id(12L).name("Pobi").build();
		
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.of(user));
		
		given(accountRepository.findByAccountNumber(anyString())).willReturn(Optional.empty());
		
		//when
		AccountException exception = assertThrows(AccountException.class, ()-> transactionService.useBalance(1L, "1000000000", 1000L));
		
		//then
		assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
	}
	
	
	@Test
	@DisplayName("계좌 소유주 다름")
	void deleteAccountFailed_userUnMatch() {
		//given 
		AccountUser pobi = AccountUser.builder().id(12L).name("Pobi").build();
		AccountUser harry = AccountUser.builder().id(13L).name("Harry").build();
		
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.of(pobi)); 
		given(accountRepository.findByAccountNumber(anyString())).willReturn(Optional.of(Account.builder().accountUser(harry).balance(0L).accountNumber("1000000012").build()));
		
		//when
		AccountException exception = assertThrows(AccountException.class, ()-> transactionService.useBalance(1L, "1000000000", 1000L));
		
		//then
		assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
	}
	
	
	@Test
	@DisplayName("해지 계좌는 해지할 수 없다.")
	void deleteAccountFailed_alreadyUnregistered() {
		//given 
		AccountUser pobi = AccountUser.builder().id(12L).name("Pobi").build();
		
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.of(pobi)); 
		
		//이 계좌는 UNREGISTERED이다 
		given(accountRepository.findByAccountNumber(anyString())).willReturn(Optional.of(Account.builder().accountUser(pobi).accountStatus(AccountStatus.UNREGISTERED).balance(0L).accountNumber("1000000012").build()));
		
		//when
		AccountException exception = assertThrows(AccountException.class, ()-> transactionService.useBalance(1L, "1000000000", 1000L));
		
		//then
		assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
	}

	
	
	@Test
	@DisplayName("거래 금액이 잔액보다 큰 경우")
	void exceedAmount_UseBalance() {
		//given
		AccountUser user = AccountUser.builder().id(12L).name("Pobi").build();
		
		//account에 100원밖에 없는데
		Account account = Account.builder()
				.accountUser(user)
				.balance(100L)
				.accountStatus(AccountStatus.IN_USE)
				.accountNumber("1000000012")
				.build();
		
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.of(user));
		given(accountRepository.findByAccountNumber(anyString())).willReturn(Optional.of(account));
		
		
		//when
		AccountException exception = assertThrows(AccountException.class, ()-> transactionService.useBalance(1L, "1000000000", 1000L));
		
		//then
		assertEquals(ErrorCode.AMOUNT_EXCEED_BALANCE, exception.getErrorCode());
		verify(transactionRepository, times(0)).save(any()); //저장되면 안된다
		
	}
	
	
	@Test
	@DisplayName("실패 트랜잭션 저장 성공")
	void saveFailedUseTransation() {
		//given
		AccountUser user = AccountUser.builder().id(12L).name("Pobi").build();
		
		Account account = Account.builder()
				.accountUser(user)
				.balance(10000L)
				.accountStatus(AccountStatus.IN_USE)
				.accountNumber("1000000012")
				.build();
		
		given(accountRepository.findByAccountNumber(anyString())).willReturn(Optional.of(account));
		given(transactionRepository.save(any())).willReturn(Transaction.builder()
				.account(account)
				.transactionType(TransactionType.USE)
				.transactionResultType(TransactionResultType.S)
				.transactionId("transactionId")
				.transactedAt(LocalDateTime.now())
				.amount(1000L)
				.balanceSnapshot(9000L) 
				.build());
		
		ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class); 
		
		//when
		transactionService.saveFailedUseTransaction("1000000000", 200L); 
	
		//then

		verify(transactionRepository, times(1)).save(captor.capture()); 
		
		assertEquals(200L, captor.getValue().getAmount());
		assertEquals(TransactionResultType.F, captor.getValue().getTransactionResultType());
		assertEquals(10000L, captor.getValue().getBalanceSnapshot()); // 10000 - 200 안됨
		
	}
	
	
	@Test
	void successCancelBalance() {
		//given
		AccountUser user = AccountUser.builder().id(12L).name("Pobi").build();
		
		Account account = Account.builder()
				.accountUser(user)
				.balance(10000L)
				.accountStatus(AccountStatus.IN_USE)
				.accountNumber("1000000012")
				.build();
		
		Transaction transaction = Transaction.builder()
				.account(account)
				.transactionType(TransactionType.USE)
				.transactionResultType(TransactionResultType.S)
				.transactionId("transactionId")
				.transactedAt(LocalDateTime.now())
				.amount(CANCEL_AMOUNT)
				.balanceSnapshot(9000L) 
				.build();
		
		given(transactionRepository.findByTransactionId(anyString())).willReturn(Optional.of(transaction));
		
		given(accountRepository.findByAccountNumber(anyString())).willReturn(Optional.of(account));
		
		given(transactionRepository.save(any())).willReturn(Transaction.builder()
				.account(account)
				.transactionType(TransactionType.CANCEL)
				.transactionResultType(TransactionResultType.S)
				.transactionId("transactionIdForCancel")
				.transactedAt(LocalDateTime.now())
				.amount(CANCEL_AMOUNT)
				.balanceSnapshot(10000L)
				.build()); 
		
		ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
		
		//when
		TransactionDTO transactionDTO = transactionService.cancelBalance("transactionId", "1000000000", CANCEL_AMOUNT); 
		
		//then
		assertEquals(10000L, transactionDTO.getBalanceSnapshot());
		assertEquals(TransactionResultType.S, transactionDTO.getTransactionResultType());
		assertEquals(TransactionType.CANCEL, transactionDTO.getTransactionType());
		assertEquals(CANCEL_AMOUNT, transactionDTO.getAmount());
		
		verify(transactionRepository, times(1)).save(captor.capture());
		
		assertEquals(CANCEL_AMOUNT, captor.getValue().getAmount());
		assertEquals(10000L + CANCEL_AMOUNT, captor.getValue().getBalanceSnapshot()); // 10000L + CANCEL_AMOUNT
		
	}
	
	
	@Test
	@DisplayName("해당 계좌 없음 - 잔액 사용 취소 실패")
	void cancelTransaction_AccountNotFound() {
		//given 
		given(transactionRepository.findByTransactionId(anyString())).willReturn(Optional.of(Transaction.builder()
																								.build()));
		
		given(accountRepository.findByAccountNumber(anyString())).willReturn(Optional.empty());
		
		//when
		AccountException exception = assertThrows(AccountException.class, ()-> transactionService.cancelBalance("transactionId", "1000000000", 1000L));
		
		//then
		assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
	}
	
	@Test
	@DisplayName("원 사용 거래 없음 - 잔액 사용 취소 실패")
	void cancelTransaction_TransactionNotFound() {
		
		//given 
		given(transactionRepository.findByTransactionId(anyString())).willReturn(Optional.empty());
		
		//when
		AccountException exception = assertThrows(AccountException.class, ()-> transactionService.cancelBalance("transactionId", "1000000000", 1000L));
		
		//then
		assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, exception.getErrorCode());
	}
	
	
	@Test
	@DisplayName("거래와 거래가 매칭실패 - 잔액 사용 취소 실패")
	void cancelTransaction_TransactionAcccountUnMatch() {
		
		AccountUser user = AccountUser.builder().id(12L).name("Pobi").build();
		
		Account account = Account.builder()
				.id(1L)
				.accountUser(user)
				.balance(10000L)
				.accountStatus(AccountStatus.IN_USE)
				.accountNumber("1000000012")
				.build();
		
		Account accountNotUse = Account.builder()
				.id(2L)
				.accountUser(user)
				.balance(10000L)
				.accountStatus(AccountStatus.IN_USE)
				.accountNumber("1000000013") //계좌 다름
				.build();
		
		Transaction transaction = Transaction.builder()
				.account(account)
				.transactionType(TransactionType.USE)
				.transactionResultType(TransactionResultType.S)
				.transactionId("transactionId")
				.transactedAt(LocalDateTime.now())
				.amount(CANCEL_AMOUNT)
				.balanceSnapshot(9000L) 
				.build();
		
		//given 
		given(transactionRepository.findByTransactionId(anyString())).willReturn(Optional.of(transaction));
		
		given(accountRepository.findByAccountNumber(anyString())).willReturn(Optional.of(accountNotUse)); 
		
		//when
		AccountException exception = assertThrows(AccountException.class, ()-> transactionService.cancelBalance("transactionId", "1000000000", CANCEL_AMOUNT));
		
		//then
		assertEquals(ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH, exception.getErrorCode());
	}
	
	
	@Test
	@DisplayName("거래금액과 취소금액이 다름 - 잔액 사용 취소 실패")
	void cancelTransaction_CancelMustFully() {
		
		AccountUser user = AccountUser.builder().id(12L).name("Pobi").build();
		
		Account account = Account.builder()
				.id(1L)
				.accountUser(user)
				.balance(10000L)
				.accountStatus(AccountStatus.IN_USE)
				.accountNumber("1000000012")
				.build();
		
		Transaction transaction = Transaction.builder()
				.account(account)
				.transactionType(TransactionType.USE)
				.transactionResultType(TransactionResultType.S)
				.transactionId("transactionId")
				.transactedAt(LocalDateTime.now())
				.amount(CANCEL_AMOUNT + 1000L)
				.balanceSnapshot(9000L) 
				.build();
		
		//given 
		given(transactionRepository.findByTransactionId(anyString())).willReturn(Optional.of(transaction));
		
		given(accountRepository.findByAccountNumber(anyString())).willReturn(Optional.of(account)); 
		
		//when
		AccountException exception = assertThrows(AccountException.class, ()-> transactionService.cancelBalance("transactionId", "1000000000", CANCEL_AMOUNT));
		
		//then
		assertEquals(ErrorCode.CANCEL_MUST_FULLY, exception.getErrorCode());
	}
	
	
	@Test
	@DisplayName("취소는 1년까지만 가능 - 잔액 사용 취소 실패")
	void cancelTransaction_TooOldOrder() {
		
		AccountUser user = AccountUser.builder().id(12L).name("Pobi").build();
		
		Account account = Account.builder()
				.id(1L)
				.accountUser(user)
				.balance(10000L)
				.accountStatus(AccountStatus.IN_USE)
				.accountNumber("1000000012")
				.build();
		
		Transaction transaction = Transaction.builder()
				.account(account)
				.transactionType(TransactionType.USE)
				.transactionResultType(TransactionResultType.S)
				.transactionId("transactionId")
				.transactedAt(LocalDateTime.now().minusYears(1))
				.amount(CANCEL_AMOUNT)
				.balanceSnapshot(9000L) 
				.build();
		
		//given 
		given(transactionRepository.findByTransactionId(anyString())).willReturn(Optional.of(transaction));
		
		given(accountRepository.findByAccountNumber(anyString())).willReturn(Optional.of(account)); //transaction과 다른 
		
		//when
		AccountException exception = assertThrows(AccountException.class, ()-> transactionService.cancelBalance("transactionId", "1000000000", CANCEL_AMOUNT));
		
		//then
		assertEquals(ErrorCode.TOO_OLD_ORDER_TO_CANCEL, exception.getErrorCode());
	}
	
	
	@Test
	void successQueryTransaction() {
		
		AccountUser user = AccountUser.builder().id(12L).name("Pobi").build();
		
		Account account = Account.builder()
				.id(1L)
				.accountUser(user)
				.balance(10000L)
				.accountStatus(AccountStatus.IN_USE)
				.accountNumber("1000000012")
				.build();
		
		Transaction transaction = Transaction.builder()
				.account(account)
				.transactionType(TransactionType.USE)
				.transactionResultType(TransactionResultType.S)
				.transactionId("transactionId")
				.transactedAt(LocalDateTime.now().minusYears(1))
				.amount(CANCEL_AMOUNT)
				.balanceSnapshot(9000L) 
				.build();
		
		//given
		given(transactionRepository.findByTransactionId(anyString()))
				.willReturn(Optional.of(transaction));
		
		//when
		TransactionDTO transactionDTO = transactionService.queryTransaction("trxId");
		
		//then
		assertEquals(TransactionType.USE, transactionDTO.getTransactionType());
		assertEquals(TransactionResultType.S, transactionDTO.getTransactionResultType());
		assertEquals(CANCEL_AMOUNT, transactionDTO.getAmount());
		assertEquals("transactionId", transactionDTO.getTransactionId());
		
		
	}
	
	
	@Test
	@DisplayName("원 거래 없음 - 계좌 조회 실패")
	void queryTransaction_TransactionNotFound() {
		
		//given 
		given(transactionRepository.findByTransactionId(anyString())).willReturn(Optional.empty());
		
		//when
		AccountException exception = assertThrows(AccountException.class, ()-> transactionService.queryTransaction("transactionId"));
		
		//then
		assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, exception.getErrorCode());
	}
	
}
