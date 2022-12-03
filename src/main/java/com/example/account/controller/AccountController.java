package com.example.account.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.domain.Account;
import com.example.account.dto.AccountInfo;
import com.example.account.dto.CreateAccount;
import com.example.account.dto.DeleteAccount;
import com.example.account.service.AccountService;

import lombok.RequiredArgsConstructor;

//layer 아키텍쳐
//client -> controller -> service -> repository(DAO)
//controller에서는 service만 생성하도록 한다

//test 코드 작성 및 HTTP 통해서 수동적인 test도 필요

@RestController //Spring MVC Controller에 @ResponseBody가 추가된 것이다. @ResponseBody 어노테이션을 붙이지 않아도 문자열과 JSON 등을 전송할 수 있다.
@RequiredArgsConstructor //초기화 되지않은 final 필드나, @NonNull이 붙은 필드에 대해 생성자를 생성한다.
public class AccountController {
	private final AccountService accountService; 
	
	@PostMapping("/account")
	public CreateAccount.Response createAccount(@RequestBody @Valid CreateAccount.Request request) { //Valid : 제약 조건을 검증
		return CreateAccount.Response.from(accountService.createAccount(request.getUserId(), request.getInitialBalance()));
	}
	
	@GetMapping("/account/{id}")
	public Account getAccount(@PathVariable Long id) { //PathVariable : 주소에서 값 꺼내기
		return accountService.getAccount(id);
	}
	
	@DeleteMapping("/account")
	public DeleteAccount.Response deleteAccount(@RequestBody @Valid DeleteAccount.Request request) {
		return DeleteAccount.Response.from(accountService.deleteAccount(request.getUserId(), request.getAccountNumber()));
	}
	
	//계좌 확인
	@GetMapping("/account")
	public List<AccountInfo> getAccountsByUserId(@RequestParam("user_id") Long userId){
		//accountDTO를 accountInfo로 바꿈
		return accountService.getAccountsByUserId(userId).stream().map(accountDTO -> AccountInfo.builder().accountNumber(accountDTO.getAccountNumber())
																									.balance(accountDTO.getBalance()).build()).collect(Collectors.toList());		
	}
	

}

