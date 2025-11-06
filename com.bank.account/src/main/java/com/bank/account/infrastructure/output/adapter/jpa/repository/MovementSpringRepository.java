package com.bank.account.infrastructure.output.adapter.jpa.repository;

import com.bank.account.infrastructure.output.adapter.jpa.entity.MovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MovementSpringRepository extends JpaRepository<MovementEntity, Long> {

    List<MovementEntity> findByAccountId(Long accountId);

    List<MovementEntity> findByAccountIdInAndDateBetween(List<Long> accountIds, LocalDateTime startDate, LocalDateTime endDate);
}