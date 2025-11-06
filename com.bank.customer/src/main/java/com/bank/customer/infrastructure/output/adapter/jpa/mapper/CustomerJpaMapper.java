package com.bank.customer.infrastructure.output.adapter.jpa.mapper;

import com.bank.customer.domain.model.Customer;
import com.bank.customer.infrastructure.output.adapter.jpa.entity.CustomerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerJpaMapper {

    CustomerEntity toEntity(Customer customer);

    @Mapping(target = "password", ignore = true)
    Customer toDomain(CustomerEntity customerEntity);
}