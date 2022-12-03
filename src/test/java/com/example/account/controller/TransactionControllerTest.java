package com.example.account.controller;

import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.account.dto.CancelBalance;
import com.example.account.dto.TransactionDTO;
import com.example.account.dto.UseBalance;
import com.example.account.service.TransactionService;
import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;


@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

	@MockBean
	private TransactionService transactionService;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Test
	void successUseBalance() throws Exception {
		//given Mock 객체의 동작 정의, TransactionController에 따라 알아서 transactionService.useBalance 함수가 실행될 것이고, 그 함수는 위에 정의된 동작으로 실행한다.
		given(transactionService.useBalance(anyLong(), anyString(), anyLong())).willReturn(TransactionDTO.builder()
																							.accountNumber("1000000000")
																							.transactedAt(LocalDateTime.now())
																							.amount(12345L)
																							.transactionId("transactionId")
																							.transactionResultType(TransactionResultType.S)
																							.build());
		
		//when
		//then JSON으로 반환된 값이 예상한 값과 같은지를 검사한다.
		mockMvc.perform(post("/transaction/use")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new UseBalance.Request(1L, "2000000000", 3000L)))) //아무거나 요청
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accountNumber").value("1000000000"))
				.andExpect(jsonPath("$.transactionResultType").value("S"))
				.andExpect(jsonPath("$.transactionId").value("transactionId"))
				.andExpect(jsonPath("$.amount").value(12345));
	}
	
	
	@Test
	void successCancelBalance() throws Exception {
		//given
		given(transactionService.cancelBalance(anyString(), anyString(), anyLong())).willReturn(TransactionDTO.builder()
																							.accountNumber("1000000000")
																							.transactedAt(LocalDateTime.now())
																							.amount(54321L)
																							.transactionId("transactionIdForCancel")
																							.transactionResultType(TransactionResultType.S)
																							.build());
		
		//when
		//then
		mockMvc.perform(post("/transaction/cancel")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CancelBalance.Request("transactionId", "2000000000", 3000L)))) 
				.andDo(print()) 
				.andExpect(status().isOk()) 
				.andExpect(jsonPath("$.accountNumber").value("1000000000"))
				.andExpect(jsonPath("$.transactionResultType").value("S"))
				.andExpect(jsonPath("$.transactionId").value("transactionIdForCancel"))
				.andExpect(jsonPath("$.amount").value(54321));
	}
	
	@Test
	void successQueryTransaction() throws Exception {
		 
		//given
		given(transactionService.queryTransaction(anyString())).willReturn(TransactionDTO.builder()
				.accountNumber("1000000000")
				.transactionType(TransactionType.USE)
				.transactedAt(LocalDateTime.now())
				.amount(54321L)
				.transactionId("transactionIdForCancel")
				.transactionResultType(TransactionResultType.S)
				.build());
		
		//then
		mockMvc.perform(get("/transaction/12345"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.transactionType").value("USE"))
				.andExpect(jsonPath("$.accountNumber").value("1000000000"))
				.andExpect(jsonPath("$.transactionResultType").value("S"))
				.andExpect(jsonPath("$.transactionId").value("transactionIdForCancel"))
				.andExpect(jsonPath("$.amount").value(54321));
		
	}

}
