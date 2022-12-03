
/*
package com.example.account.domain;

import java.time.LocalDateTime;

import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "doesNotUseThisBuilder")
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class) //자동으로 생성일자, 수정일자 업데이트
public class BaseEntity { //공통부문

	@Id //Account 테이블에 기본키(PK)를 지정함
	@GeneratedValue //생성 전략을 정의하기 위함, 얘 없으면 기본키가 바로 할당됨
	private Long id;
	
	@CreatedDate //Entity가 생성되어 저장될 때 시간이 자동 저장
	private LocalDateTime createdAt;
	
	@LastModifiedDate //조회한 Entity의 값을 변경할 때 시간이 자동 저장
	private LocalDateTime updatedAt;
}
*/
