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
		 ?????? given ???????????? ????????? ?????????, account.useBalance(amount) ?????? ??????
		 ???????????? ?????? ???????????? ??????????????? ????????? ??? ?????? ????????? ????????????.
		 ????????? ??????????????? DB??? ??????????????? ??? ??? ??????. ?????? ArgumentCaptor??? ????????????.
		 
		 ?????? ?????? ArgumentCaptor? 
		 ?????? ???????????? ???????????? argument??? captor(??????)????????? ?????? ??? ?????? ??????(getValue)??? ??? ??????
		 argument ????????? ??? ????????????.
		
		 1. useBalance(1, "1000000000", 200)
		 2. given?????? ????????? findById
		 3. given?????? ????????? accountNumber
		 4. TransactionService??? account.useBalance(200)
		 5. given?????? ????????? Build ????????? save??? ?????? ?????? ?????????, 1-2-3-4 ???????????? ???????????? Transaction ????????? save ??????
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
	 		verify(): ???????????? ????????? ????????? ?????????????????? ????????????.
			????????? ?????? ????????? ????????? ??? ??????. (ex. any())
			@Captor??? ???????????? ?????? ????????? ?????????????????? ????????? ??? ??????
		*/
		
		verify(transactionRepository, times(1)).save(captor.capture()); //????????? ?????????
		
		assertEquals(200L, captor.getValue().getAmount());
		assertEquals(account, captor.getValue().getAccount());
		assertEquals(TransactionType.USE, captor.getValue().getTransactionType());
		assertEquals(9800L, captor.getValue().getBalanceSnapshot()); //10000 - 200
		
	}
	
	
	@Test
	@DisplayName("?????? ?????? ?????? - ?????? ?????? ??????")
	void createAccount_UserNotFound() {
		//given
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.empty()); 
		
		//when
		AccountException exception = assertThrows(AccountException.class, ()-> transactionService.useBalance(1L, "1000000000", 1000L));
		
		//then
		assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
	}
	
	
	@Test
	@DisplayName("?????? ?????? ?????? - ?????? ?????? ??????")
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
	@DisplayName("?????? ????????? ??????")
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
	@DisplayName("?????? ????????? ????????? ??? ??????.")
	void deleteAccountFailed_alreadyUnregistered() {
		//given 
		AccountUser pobi = AccountUser.builder().id(12L).name("Pobi").build();
		
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.of(pobi)); 
		
		//??? ????????? UNREGISTERED?????? 
		given(accountRepository.findByAccountNumber(anyString())).willReturn(Optional.of(Account.builder().accountUser(pobi).accountStatus(AccountStatus.UNREGISTERED).balance(0L).accountNumber("1000000012").build()));
		
		//when
		AccountException exception = assertThrows(AccountException.class, ()-> transactionService.useBalance(1L, "1000000000", 1000L));
		
		//then
		assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
	}

	
	
	@Test
	@DisplayName("?????? ????????? ???????????? ??? ??????")
	void exceedAmount_UseBalance() {
		//given
		AccountUser user = AccountUser.builder().id(12L).name("Pobi").build();
		
		//account??? 100????????? ?????????
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
		verify(transactionRepository, times(0)).save(any()); //???????????? ?????????
		
	}
	
	
	@Test
	@DisplayName("?????? ???????????? ?????? ??????")
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
		assertEquals(10000L, captor.getValue().getBalanceSnapshot()); // 10000 - 200 ??????
		
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
	@DisplayName("?????? ?????? ?????? - ?????? ?????? ?????? ??????")
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
	@DisplayName("??? ?????? ?????? ?????? - ?????? ?????? ?????? ??????")
	void cancelTransaction_TransactionNotFound() {
		
		//given 
		given(transactionRepository.findByTransactionId(anyString())).willReturn(Optional.empty());
		
		//when
		AccountException exception = assertThrows(AccountException.class, ()-> transactionService.cancelBalance("transactionId", "1000000000", 1000L));
		
		//then
		assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, exception.getErrorCode());
	}
	
	
	@Test
	@DisplayName("????????? ????????? ???????????? - ?????? ?????? ?????? ??????")
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
				.accountNumber("1000000013") //?????? ??????
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
	@DisplayName("??????????????? ??????????????? ?????? - ?????? ?????? ?????? ??????")
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
	@DisplayName("????????? 1???????????? ?????? - ?????? ?????? ?????? ??????")
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
		
		given(accountRepository.findByAccountNumber(anyString())).willReturn(Optional.of(account)); //transaction??? ?????? 
		
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
	@DisplayName("??? ?????? ?????? - ?????? ?????? ??????")
	void queryTransaction_TransactionNotFound() {
		
		//given 
		given(transactionRepository.findByTransactionId(anyString())).willReturn(Optional.empty());
		
		//when
		AccountException exception = assertThrows(AccountException.class, ()-> transactionService.queryTransaction("transactionId"));
		
		//then
		assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, exception.getErrorCode());
	}
	
}
