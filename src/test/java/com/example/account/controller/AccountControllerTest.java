package com.example.account.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.account.domain.Account;
import com.example.account.dto.AccountDTO;
import com.example.account.dto.CreateAccount;
import com.example.account.dto.DeleteAccount;
import com.example.account.exception.AccountException;
import com.example.account.service.AccountService;
import com.example.account.service.LockService;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
/*

  anyString() : 어떠한 문자열
  anyLong() : 어떠한 Long
  anyBoolean() : 어떠한 Boolean 값
  any() : 모든 객체

  MockMvc?
  
  실제 객체와 비슷하지만 테스트에 필요한 기능만 가지는 가짜 객체를 만들어 애플리케이션 서버에 배포하지 않고도 스프링 MVC 동작을 재현할 수 있는 클래스를 의미
  
  mockMvc의 메소드?
  
  perform() : 요청을 전송하는 역할
  get() :  HTTP 메소드 결정
  andExpect() : 응답 검증
  - isOk : 200
  andDo : 요청/응답 전체 메시지 확인
  jsonPath : JSON 객체의 요소를 쿼리하는 표준화된 방법

 */

@WebMvcTest(AccountController.class) //MVC를 위한 테스트, 특정 컨트롤러 테스트할 클래스를 괄호 안에 입력해야한다.
class AccountControllerTest {
	
	@MockBean //이미 있는 BEAN이면 MockBean을 사용, 아니면 Mock
	private AccountService accountService;
	
	@MockBean
	private LockService redisService;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper; //ObjectMapper : (1) Object -> Serialize(직렬화) -> "JSON", (2) "JSON" -> Deserialize(역직렬화) -> Java Object
	
	//성공 케이스
	@Test
	void successCreateAccount() throws Exception {
		//given
		given(accountService.createAccount(anyLong(), anyLong()))
								.willReturn(AccountDTO.builder()
								.userId(1L)
								.accountNumber("1234567890") //임의의 값
								.registeredAt(LocalDateTime.now())
								.unRegisteredAt(LocalDateTime.now())
								.build());
		//then
		mockMvc.perform(post("/account")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString( //map 타입이 JSON 형식의 String 타입으로 변환된다.
					new CreateAccount.Request(1L, 100L)
				)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value(1))
				.andExpect(jsonPath("$.accountNumber").value("1234567890"))
				.andDo(print());
		
	}
	
	
	@Test
	void successGetAccountsByUserId() throws Exception {
		
		//given
		List<AccountDTO> accountDTOs = Arrays.asList(AccountDTO.builder().accountNumber("1234567890").balance(1000L).build(),
													 AccountDTO.builder().accountNumber("1111111111").balance(2000L).build(),
													 AccountDTO.builder().accountNumber("2222222222").balance(3000L).build());

		given(accountService.getAccountsByUserId(anyLong())).willReturn(accountDTOs);
		
		//then 컨트롤러가 아래와 같이 응답하면 OK
		mockMvc.perform(get("/account?user_id=1"))
				.andDo(print())
				.andExpect(jsonPath("$[0].accountNumber").value("1234567890")) //리스트가 JSON형식으로 넘어와, JSON의 0번째의 ACCOUNTNUMBER 값은 12364567890이어야한다.
				.andExpect(jsonPath("$[0].balance").value(1000))
				.andExpect(jsonPath("$[1].accountNumber").value("1111111111"))
				.andExpect(jsonPath("$[1].balance").value(2000))
				.andExpect(jsonPath("$[2].accountNumber").value("2222222222"))
				.andExpect(jsonPath("$[2].balance").value(3000));
		
	}
	
	
	@Test
	void successGetAccount() throws Exception {
		
		//given
		given(accountService.getAccount(anyLong()))
			.willReturn(Account.builder().accountNumber("3456").accountStatus(AccountStatus.IN_USE).build());
		
		//then
		mockMvc.perform(get("/account/876"))
				.andDo(print())
				.andExpect(jsonPath("accountNumber").value("3456"))
				.andExpect(jsonPath("accountStatus").value("IN_USE"))
				.andExpect(status().isOk());
		
	}
	
	//해지 케이스
	@Test
	void successDeleteAccount() throws Exception {
		//given
		given(accountService.deleteAccount(anyLong(), anyString()))
								.willReturn(AccountDTO.builder()
								.userId(1L)
								.accountNumber("1234567890") 
								.registeredAt(LocalDateTime.now())
								.unRegisteredAt(LocalDateTime.now())
								.build());
		
		//then
		mockMvc.perform(delete("/account")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(
					new DeleteAccount.Request(3333L, "0987654321")
				)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value(1L))
				.andExpect(jsonPath("$.accountNumber").value("1234567890"))
				.andDo(print());
		
	}
	
	
	@Test
	void failGetAccount() throws Exception {
		
		//given
		given(accountService.getAccount(anyLong()))
			.willThrow(new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));
		
		//then
		mockMvc.perform(get("/account/876"))
				.andDo(print())
				.andExpect(jsonPath("errorCode").value("ACCOUNT_NOT_FOUND"))
				.andExpect(jsonPath("errorMessage").value("계좌가 없습니다"))
				.andExpect(status().isOk());
		
	}
	
	

}
