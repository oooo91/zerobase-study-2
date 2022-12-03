package com.example.account.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDTO;
import com.example.account.exception.AccountException;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;

/*

@SpringBootTest ?

SpringBootTest : testImplementation 'org.springframework.boot:spring-boot-starter-test'의 기능 중 하나다.
SpringBootTest가 테스트에 필요한 모든 빈을 가져옴 주입만 해주면 됨


Mockito?

테스트하는데 Service 객체를 생성해서 사용할 필요가 없으므로 적당히 리턴값을 던져주는 Fake 객체를 사용

ExtendWith : Mockito의 Mock 객체를 사용하기 위한 Annotation
JUnit4에서는 RunWith(MockitoJUnitRunner.class)를, JUnit5에서는 ExtendWith를 쓰도록 되어있다.

BDD(시나리오) 방법 => given : Mock객체의 동작 정의, whan : 실제 Test할 Method 수행, then : 검증

*/


@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

	//@Autowired : SpringBootTest 때 사용하는 방식
	
	@Mock //mockito 패키지에 있는 mock(모조품)객체를 생성한다.
	private AccountRepository accountRepository;
	
	@Mock
	private AccountUserRepository accountUserRepository;
	
	@InjectMocks //@Mock이 붙은 목객체를 @InjectMocks이 붙은 객체에 주입시킬 수 있다.
	private AccountService accountService;

	
	@Test
	@DisplayName("계좌 생성")
	void createAccountSuccess() {
		//given
		AccountUser user = AccountUser.builder().id(15L).name("Pobi").build(); //테스트하기 위한 값 준비
		
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.of(user)); //Optional.of : null이 아닌 명시된 값을 가지는 Optional 객체를 반환
		
		given(accountRepository.findFirstByOrderByIdDesc()).willReturn(Optional.of(Account.builder().accountNumber("1000000012").build()));
		
		given(accountRepository.save(any())).willReturn(Account.builder().accountUser(user).accountNumber("1000000012").build());
		
		ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class); //+1이 되어서 1000000013이 되는지 검증할 것임
		
		/*
		Argument?
		
		verify(검증)할 시 argument 값을 유연하게 넘길 수 있다

		ArgumentCaptor<Integer> integerArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
		 
		// verify(mockedList).get(1); < 일일이 값 넣지 않음
		// verify(mockedList).get(2);
		// verify(mockedList).get(3);
		 
		verify(mockedList, times(3)).get(integerArgumentCaptor.capture()); //검증, times(n) : n만큼 호출
		 */
		
		
		//when
		AccountDTO accountDTO = accountService.createAccount(1L, 1000L);
		
		//then
		verify(accountRepository, times(1)).save(captor.capture());
		
		assertEquals(15L, accountDTO.getUserId());
		assertEquals("1000000013", captor.getValue().getAccountNumber()); //+1
	}
	
	
	@Test
	@DisplayName("계좌 생성 - 계좌가 없는 경우")
	void createFirstSuccess() {
		//given
		AccountUser user = AccountUser.builder().id(15L).name("Pobi").build();
		
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.of(user));
		
		given(accountRepository.findFirstByOrderByIdDesc()).willReturn(Optional.empty()); //계좌번호 비어있음, 응답해줄 기본값은 100000000이 된다.
		
		given(accountRepository.save(any())).willReturn(Account.builder().accountUser(user).accountNumber("1000000015").build());
		
		ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
		
		//when
		AccountDTO accountDTO = accountService.createAccount(1L, 1000L);
		
		//then
		verify(accountRepository, times(1)).save(captor.capture());
		
		assertEquals(15L, accountDTO.getUserId());
		assertEquals("100000000000", captor.getValue().getAccountNumber());
	}
	
	
	@Test
	@DisplayName("해당 유저 없음 - 계좌 생성 실패")
	void createAccount_UserNotFound() {
		//given
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.empty()); //유저 없음 -> exception -> ErrorCode.User_NOT_FOUND
		
		//when
		AccountException exception = assertThrows(AccountException.class, ()-> accountService.createAccount(1L, 1000L));
		
		//then
		assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
	}

	
	@Test
	@DisplayName("계좌 생성 - 계좌 10개 이상이면 예외")
	void createAccount_maxAccountIs10() {
		//given
		AccountUser user = AccountUser.builder().id(15L).name("Pobi").build();
		
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.of(user));
		given(accountRepository.countByAccountUser(any())).willReturn(10);
		
		AccountException exception = assertThrows(AccountException.class, () ->accountService.createAccount(1L, 1000L));
		
		//then
		assertEquals(ErrorCode.MAX_ACCOUNT_PER_USER_10, exception.getErrorCode());
		
	}
	
	
	@Test
	@DisplayName("계좌 해지")
	void deleteAccountSuccess() {
		//given 
		AccountUser user = AccountUser.builder().id(12L).name("Pobi").build(); 
		
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.of(user)); 
		given(accountRepository.findByAccountNumber(anyString())).willReturn(Optional.of(Account.builder().accountUser(user).balance(0L).accountNumber("1000000012").build()));
		
		ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class); 
		
		//when
		AccountDTO accountDTO = accountService.deleteAccount(12L, "1000000012");
		
		//then
		verify(accountRepository, times(1)).save(captor.capture());
		
		assertEquals(12L, accountDTO.getUserId());
		assertEquals("1000000012", captor.getValue().getAccountNumber());
		assertEquals(AccountStatus.UNREGISTERED, captor.getValue().getAccountStatus());
	}
	
	@Test
	@DisplayName("해당 유저 없음 - 계좌 해지 실패")
	void deleteAccount_UserNotFound() {
		//given
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.empty()); //유저 없음 -> exception 처리해야지 -> ErrorCode.User_NOT_FOUND
		
		//when
		AccountException exception = assertThrows(AccountException.class, ()-> accountService.deleteAccount(1L, "1234567890"));
		
		//then
		assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
	}
	
	
	@Test
	@DisplayName("해당 계좌 없음 - 계좌 해지 실패")
	void deleteAccount_AccountNotFound() {
		//given 
		AccountUser user = AccountUser.builder().id(12L).name("Pobi").build();
		
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.of(user));
		
		given(accountRepository.findByAccountNumber(anyString())).willReturn(Optional.empty());
		
		//when
		AccountException exception = assertThrows(AccountException.class, ()-> accountService.deleteAccount(1L, "1234567890"));
		
		//then
		assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
	}
	
	
	@Test
	@DisplayName("계좌 소유주 다름")
	void deleteAccountFailed_userUnMatch() {
		//given 
		AccountUser pobi = AccountUser.builder().id(12L).name("Pobi").build();
		AccountUser harry = AccountUser.builder().id(13L).name("Harry").build();
		
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.of(pobi)); //user 조회할 땐 pobi가 나오고, 
		given(accountRepository.findByAccountNumber(anyString())).willReturn //account 조회할 땐 harry가 나온다.
		(Optional.of(Account.builder().accountUser(harry).balance(0L).accountNumber("1000000012").build()));
		
		//when
		AccountException exception = assertThrows(AccountException.class, ()-> accountService.deleteAccount(1L, "1234567890"));
		
		//then
		assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
	}
	

	@Test
	@DisplayName("해지 계좌는 잔액이 없어야 한다.")
	void deleteAccountFailed_balanceNotEmpty() {
		//given 
		AccountUser pobi = AccountUser.builder().id(12L).name("Pobi").build();
		
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.of(pobi)); 
		
		given(accountRepository.findByAccountNumber(anyString())).willReturn
		(Optional.of(Account.builder().accountUser(pobi).balance(100L).accountNumber("1000000012").build())); 
		
		//when
		AccountException exception = assertThrows(AccountException.class, ()-> accountService.deleteAccount(1L, "1234567890"));
		
		//then
		assertEquals(ErrorCode.BALANCE_NOT_EMPTY, exception.getErrorCode());
	}
	

	@Test
	@DisplayName("해지 계좌는 해지할 수 없다.")
	void deleteAccountFailed_alreadyUnregistered() {
		//given 
		AccountUser pobi = AccountUser.builder().id(12L).name("Pobi").build();
		
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.of(pobi)); 
		given(accountRepository.findByAccountNumber(anyString())).willReturn
		(Optional.of(Account.builder().accountUser(pobi).accountStatus(AccountStatus.UNREGISTERED).balance(0L).accountNumber("1000000012").build()));
		
		//when
		AccountException exception = assertThrows(AccountException.class, ()-> accountService.deleteAccount(1L, "1234567890"));
		
		//then
		assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
	}
	

	@Test
	@DisplayName("계좌 조회 성공")
	void testXXX() {
		//given
		given(accountRepository.findById(anyLong()))
							.willReturn(Optional.of(Account.builder()
							.accountStatus(AccountStatus.UNREGISTERED)
							.accountNumber("65789").build()));
		
		ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
		
		//when
		Account account = accountService.getAccount(4555L);
		
		verify(accountRepository, times(1)).findById(captor.capture());
		verify(accountRepository, times(0)).save(any());
		
		assertEquals(4555L, captor.getValue());
		assertEquals("65789", account.getAccountNumber());
		assertEquals(AccountStatus.UNREGISTERED, account.getAccountStatus());
	}
	
	
	@Test
	void successGetAccountsByUserId() {
		//when이 실행하면 필요한 객체 준비, given() <- 해당 함수 실행하면 willReturn을 리턴하게 된다.
		
		//1.AccountUser 객체 준비
		AccountUser pobi = AccountUser.builder().id(12L).name("Pobi").build();
		
		//2.Account 가상 계좌 리스트 준비
		List<Account> accounts = Arrays.asList(
				
				Account.builder().accountUser(pobi).accountNumber("1111111111").balance(1000L).build(),
				Account.builder().accountUser(pobi).accountNumber("2222222222").balance(2000L).build(),
				Account.builder().accountUser(pobi).accountNumber("3333333333").balance(3000L).build()
			);
		
		//findById 함수 실행시 pobi 반환
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.of(pobi));
		
		//findByAccountUser 했을 때 Account 리스트 반환
		given(accountRepository.findByAccountUser(any())).willReturn(accounts);
		
		//when AccountService대로 리턴될 것임
		List<AccountDTO> accountDTOs = accountService.getAccountsByUserId(2L); //아무거나 넣음 accountUser은 12L고 List에는 accounts, lL든 2L든 상관없다.
		
		//then 리턴값이 예상값과 일치하는지
		assertEquals(3, accountDTOs.size());
		assertEquals("1111111111", accountDTOs.get(0).getAccountNumber());
		assertEquals(1000, accountDTOs.get(0).getBalance());
		assertEquals("2222222222", accountDTOs.get(1).getAccountNumber());
		assertEquals(2000, accountDTOs.get(1).getBalance());
		assertEquals("3333333333", accountDTOs.get(2).getAccountNumber());
		assertEquals(3000, accountDTOs.get(2).getBalance());
		
	}
	
	
	@Test
	void failedToGetAccounts() {
		given(accountUserRepository.findById(anyLong())).willReturn(Optional.empty());
		
		//when
		AccountException exception = assertThrows(AccountException.class, ()-> accountService.getAccountsByUserId(1L));
				
		//then
		assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
	}

}
