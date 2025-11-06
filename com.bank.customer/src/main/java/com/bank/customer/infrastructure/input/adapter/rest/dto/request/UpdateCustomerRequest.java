package com.bank.customer.infrastructure.input.adapter.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCustomerRequest {

    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Name can only contain letters and spaces")
    private String name;

    @Pattern(regexp = "^[MF]$", message = "Gender must be M or F")
    private String gender;

    @NotBlank(message = "Address cannot be blank")
    private String address;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone must be a valid format")
    private String phone;

    @Schema(description = "Set customer status to true (active) or false (inactive).")
    private Boolean status;
}