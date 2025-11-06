package com.bank.account.infrastructure.output.adapter.jpa.repository;

import com.bank.account.infrastructure.output.adapter.jpa.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountSpringRepository extends JpaRepository<AccountEntity,Long> {

    Optional<AccountEntity> findByAccountNumber(String accountNumber);

    List<AccountEntity> findAllByStatusIsTrue();

    List<AccountEntity> findByCustomerId(Long customerId);

    Optional<AccountEntity> findByAccountNumberAndIdNot(String accountNumber, Long id);
}