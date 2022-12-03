package com.example.account.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Service;

import com.example.account.domain.Account;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDTO;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;

import lombok.RequiredArgsConstructor;

@Service //빈으로 등록
@RequiredArgsConstructor //@Autowired, 필드 삽입은 권장하지 않는 추세, 생성자 삽입을 하는 것을 권장하므로 @RequiredArgsConstructor 사용
public class AccountService {
	
	//AccountService가 생성됨과 동시에 accountRepository가 만들어짐
	private final AccountRepository accountRepository;
	private final AccountUserRepository accountUserRepository; 
	
	//DB와 관련된, 트랜잭션이 필요한 서비스 클래스 혹은 메서드에 @Transactional 어노테이션을 달아줌
	//Account는 entity이기 때문에 직접 건들이는 것보다는 entity를 수정하고 필요한 필드만 AccountDTO에 담는다
	
	//계좌 생성
	@Transactional //작업의 단위를 묶어줌, 트랜잭션
	public AccountDTO createAccount(Long userId, Long initialBalance) {
		
		/*
			사용자가 있는지 조회
			계좌의 번호를 생성하고
			계좌를 저장하고 그 정보를 넘긴다
		*/
		
		//userId가 있으면 반환 없으면 예외처리 (예외처리 상황에 맞게 커스텀)
		AccountUser accountUser = 
				accountUserRepository.findById(userId).orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));
		
		//계좌 개수 반환
		validateCreateAccount(accountUser);
		
		//계좌번호 받아서 +1한 새로운 계좌
		String newAccountNumber = accountRepository.findFirstByOrderByIdDesc()
							.map(account -> (Integer.parseInt(account.getAccountNumber())) + 1 + "") //정보가 있으면 ACCOUNT 가지고 ACCOUNT의 NUMBER를 +1 (UPDATE)
							.orElse("100000000000"); //없으면 반환
		
		//ENTITY -> DTO
		return AccountDTO.fromEntity(accountRepository.save(Account.builder()
				.accountUser(accountUser)
				.accountStatus(AccountStatus.IN_USE)
				.accountNumber(newAccountNumber)
				.balance(initialBalance)
				.registeredAt(LocalDateTime.now())
				.build()
			));
	}
	
	private void validateCreateAccount(AccountUser accountUser) {
		if(accountRepository.countByAccountUser(accountUser) >= 10) {
			throw new AccountException(ErrorCode.MAX_ACCOUNT_PER_USER_10);
		}
	}
	
	@Transactional
	public Account getAccount(Long id) {
		if(id < 0) {
			throw new RuntimeException("Minus");
		}
		
		return accountRepository.findById(id).get(); //레코드에서 ID값에 해당하는 row 정보를 select하여 account로 받아서 return
	}

	@Transactional
	public AccountDTO deleteAccount(@NotNull Long userId, String accountNumber) {
		//USER 찾음
		AccountUser accountUser = accountUserRepository.findById(userId).orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));
		
		//계좌
		Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));
		
		//유효성 확인
		validateDeleteAccount(accountUser, account);
		
		//유효성 확인되면 업데이트
		account.setAccountStatus(AccountStatus.UNREGISTERED);
		account.setUnregiteredAt(LocalDateTime.now());
		
		accountRepository.save(account);
		
		//ENTITY -> DTO
		return AccountDTO.fromEntity(account);
		
	}

	private void validateDeleteAccount(AccountUser accountUser, Account account) {
		//객체간의 비교 -> ID값 비교하기
		if(!Objects.equals(accountUser.getId(), account.getAccountUser().getId())) {
			throw new AccountException(ErrorCode.USER_ACCOUNT_UN_MATCH);
		}
		
		//계좌가 동일하나, 이미 계좌가 해지된 상황
		if(account.getAccountStatus() == AccountStatus.UNREGISTERED) {
			throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
		}
		
		//잔액이 남아있다면
		if(account.getBalance() > 0) {
			throw new AccountException(ErrorCode.BALANCE_NOT_EMPTY);
		}
	}

	@Transactional
	public List<AccountDTO> getAccountsByUserId(Long userId) {
		AccountUser accountUser = accountUserRepository.findById(userId).orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));
		
		List<Account> accounts = accountRepository.findByAccountUser(accountUser);
		return accounts.stream().map(AccountDTO::fromEntity).collect(Collectors.toList()); //ENTITY -> LIST<DTO>
	}
	
	
}
