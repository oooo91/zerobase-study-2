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

import com.example.account.exception.AccountException;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//JPA 이용 시 엔티티(Entity) 객체들을 Builder 기반으로 생성하는 것이 기본 패턴 
//엔티티: 클래스 위에 선언하여 이 클래스가 엔티티임을 알려준다. 이렇게 되면 JPA에서 정의된 필드(column)들을 바탕으로 데이터베이스에 테이블(row)을 만들어준다.

//Builder: 엔티티 객체를 만들 때 빌더 패턴을 이용해서 만들 수 있도록 지정해주는 어노테이션
//Builder로 선언하면 예를 들어 Board.builder().{여러가지 필드의 초기값 선언}.build() 형태로 객체를 만들 수 있다.

@Getter
@Setter
@AllArgsConstructor //@AllArgsConstructor과 @NoArgsConstructor 같이 쓰게 되면 처음에 에러나는데, 필드 생성해주면 사라짐
@NoArgsConstructor
@Builder //하나의 return하는 방식이다. 필요한 것만 담아서 객체를 생성할 수 있다.
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Account {
	@Id //Account 테이블에 기본키(PK)를 지정함
	@GeneratedValue //생성 전략을 정의하기 위함, 없을 시 기본키가 바로 할당됨
	private Long id;
	
	/*
	 ManyToOne
	 	@Entity의 member, Article이 있음
	 	@ManyToOne : 다대일, 한 명의 회원이 여러 게시글을 작성할 수 있으므로 게시글(Article) 기준으로 @ManyToOne을 선언함
		@OneToMany : 일대다, 회원 한 명이 게시글을 여러 개 작성할 수 있으므로 회원(Member) 기준으로 @OneToMany를 선언함
	 */
	
	@ManyToOne
	private AccountUser accountUser;
	private String accountNumber;
	
	//@Enumerated = entity에서 enum을 사용할 때 쓰이는 어노테이션
	//EnumType.ORIGINAL = enum 순서값을 DB에 저장 (즉 숫자가 저장됨)
	//EnumType.STRING = enum 이름 값을 DB에 저장
	@Enumerated(EnumType.STRING)
	private AccountStatus accountStatus;
	private Long balance;
	
	private LocalDateTime registeredAt;
	private LocalDateTime unregiteredAt;	
	
	@CreatedDate //Entity가 생성되어 저장할 때 시간이 자동 저장
	private LocalDateTime createdAt;
	
	@LastModifiedDate //조회한 Entity의 값을 변경할 때 시간이 자동 저장
	private LocalDateTime updatedAt;
	
	//잔액 변경 (중요 로직은 객체 안에서 직접 수행하도록 함)
	public void useBalance(Long amount) {
		if(amount > balance) {
			throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
		}
		
		balance -= amount;
		
	}
	
	public void cancelBalance(Long amount) {
		if(amount < 0) {
			throw new AccountException(ErrorCode.INVALID_REQUEST);
		}
		
		balance += amount;
		
	}
	
	
}
