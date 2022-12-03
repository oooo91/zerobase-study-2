package com.example.account.domain;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;

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
@Entity
@EntityListeners(AuditingEntityListener.class) //자동으로 생성일자, 수정일자 업데이트
public class Transaction {
	@Id 
	@GeneratedValue 
	private Long id;
	
	@Enumerated(EnumType.STRING)
	private TransactionType transactionType;
	
	@Enumerated(EnumType.STRING)
	private TransactionResultType transactionResultType;
	
	@ManyToOne
	private Account account; //transaction의 N개가 특정 account에 연결이 되도록
	private Long amount;
	private Long balanceSnapshot;
	
	private String transactionId;
	private LocalDateTime transactedAt;
	
	@CreatedDate 
	private LocalDateTime createdAt;
	
	@LastModifiedDate 
	private LocalDateTime updatedAt;
	
}
