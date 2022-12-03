package com.example.account.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;

/*
스프링부트는 Entity의 기본적인 CRUD가 가능하도록 JpaRepository 인터페이스를 제공한다.
JpaRepository를 상속한 인터페이스 -> Entity 하나에 대해 아래와 같은 기능을 제공한다.

save() -> 레코드 저장 (insert, update)
findOne() -> primary key로 레코드 한 건 찾음
findAll() -> 전체 레코드 불러옴 페이징, 정렬 등에 사용
count() -> 레코드 갯수
delete() -> 레코드 삭제
*/

@Repository //DAO 인터페이스
public interface AccountRepository extends JpaRepository<Account, Long> { //Long은 Account의 ID 값이다. entity들 구분짓는 타입이다.
	Optional<Account> findFirstByOrderByIdDesc(); //Optional : null이더라도 바로 NPE(예외)가 발생하지 않음
	
	//AccountUser을 쓸 수 있는 이유는 Account의 @ManyToOne
	Integer countByAccountUser(AccountUser accountUser); // COUNT(*) FROM ACCOUNTUSER 자동으로 쿼리 발생
	
	Optional<Account> findByAccountNumber(String AccountNumber);
	
	List<Account> findByAccountUser(AccountUser accountUser); //Account에 연관관계로 포함된 AccountUser가 있어서 이 메소드가 이 인터페이스를 통해 SQL을 자동 생성한다
}
