package com.example.account.service;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import com.example.account.exception.AccountException;
import com.example.account.type.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockService {
	private final RedissonClient redissonClient; //클라이언트 생성
	
	//자물쇠(lock) 생성
	public void lock(String accountNumber) { 
		RLock lock = redissonClient.getLock(getLockKey(accountNumber)); //계좌번호를 lock의 key로 삼음
		log.debug("Trying lock for accountNumber : {}", accountNumber);
		
		//tryLock(waitTime, leaseTime)
		try {
			boolean isLock = lock.tryLock(1, 15, TimeUnit.SECONDS); //success(lock 생성)였다가 5초 뒤 fail(lock 해제)
			
			//lock을 취득 못하였을 시 거래 실패
			if(!isLock) {
				log.error("====Lock acquisition failed====");
				throw new AccountException(ErrorCode.ACCOUNT_TRANSACTION_LOCK);
			}
		} catch (AccountException e) {
			throw e;
		} catch (Exception e) {
			log.error("Redis lock failed", e);
		}
		
	}
	
	public void unlock(String accountNumber) {
		log.debug("Unlock for accountNumber : {} ", accountNumber);
		redissonClient.getLock(getLockKey(accountNumber)).unlock(); //lock을 가져온 후 해제
	}
	
	

	private String getLockKey(String accountNumber) {
		return "ACLK:" + accountNumber;
	}
	
}
