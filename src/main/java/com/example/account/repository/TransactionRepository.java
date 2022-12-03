package com.example.account.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.account.domain.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> { //구현체를 만드는 인터페이스

	Optional<Transaction> findByTransactionId(String transationId);
}
