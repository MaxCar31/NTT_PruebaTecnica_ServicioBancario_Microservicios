package com.bank.customer.infrastructure.output.adapter.jpa.repository;

import com.bank.customer.infrastructure.output.adapter.jpa.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;


public interface CustomerSpringRepository extends JpaRepository<CustomerEntity, Long> {

    List<CustomerEntity> findAllByStatusIsTrue();

    Optional<CustomerEntity> findByIdentification(String identification);

    Optional<CustomerEntity> findByIdentificationAndCustomerIdNot(String identification, Long customerId);
}