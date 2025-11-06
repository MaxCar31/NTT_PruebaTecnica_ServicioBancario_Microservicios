package com.bank.customer.infrastructure.input.adapter.rest.mapper;

import com.bank.customer.domain.model.Customer;
import com.bank.customer.infrastructure.input.adapter.rest.dto.request.CreateCustomerRequest;
import com.bank.customer.infrastructure.input.adapter.rest.dto.request.UpdateCustomerRequest;
import com.bank.customer.infrastructure.input.adapter.rest.dto.response.CustomerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerRestMapper {

    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "status", ignore = true)
    Customer toDomain(CreateCustomerRequest dto);

    @Mapping(target = "identification", ignore = true)
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "password", ignore = true)
    Customer toDomain(UpdateCustomerRequest dto);

    CustomerResponse toResponse(Customer domain);
}