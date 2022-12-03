package com.example.account.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

//AccountApplication 뜰 때 bean으로 등록되고 @EnableJpaAuditing을 통해 @CreatedData나 @LastModifiedDate가 붙은 값들에 대해 자동으로 저장함
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfiguration {
	
}
