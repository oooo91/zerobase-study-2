package com.example.account.config;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

//레디스(캐시 서버) 서버 설정파일
@Configuration
public class LocalRedisConfig {
	
	@Value("${spring.redis.port}") //value: 프로퍼티 파일(설정 파일)을 불러들여서 값을 지정함
	private int redisPort;

	private RedisServer redisServer;
	
	//Spring Boot가 기동하면서 Bean을 등록할 때 레디스를 실행하고, 종료되면서 Bean을 삭제할 때 레디스를 종료하도록 설정

	//@PostConstruct : bean 생성된 후 수행 (의존성 주입이 끝난 후, service를 수행하기 전에 발생함) 
	@PostConstruct
	public void startRedis() {
		redisServer = new RedisServer(redisPort); //래디스 서버 생성
		redisServer.start(); //래디스 서버 실행
	}
	
	//@PreDestroy : bean 소멸 되기 전 수행
	@PreDestroy
	public void stopRedis() {
		if(redisServer != null) {
			redisServer.stop(); //레디스 서버 종료
		}
	}
}
