package com.example.account.service;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.example.account.aop.AccountLockIdInterface;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LockAopAspect {
	//lock service
	private final LockService lockService;
	
	
	@Around("@annotation(com.example.account.aop.AccountLock) && args(request)") //어떤 경우에 이 aspect를 적용할 것인지, Controller의 Request request를 가져옴
	public Object aroundMethod(ProceedingJoinPoint pjp, AccountLockIdInterface request) throws Throwable { //UseBalance, CancelBalance든 request 가져올 수 있어서 모두 적용 가능
		
		//lock 취득 시도
		lockService.lock(request.getAccountNumber());
		
		try {
			return pjp.proceed(); //AOP를 걸어줬던 동작을 실행, @Around는 before, after 전후로 동작을 넣음 
		} finally {
			lockService.unlock(request.getAccountNumber());//AOP가 성공했든 아니든 lock 해제
		}
	}
}
