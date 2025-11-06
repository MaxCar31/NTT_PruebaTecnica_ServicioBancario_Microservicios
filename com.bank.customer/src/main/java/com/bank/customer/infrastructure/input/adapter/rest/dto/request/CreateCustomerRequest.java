package com.bank.customer.infrastructure.input.adapter.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCustomerRequest {

    @NotBlank(message = "Name cannot be blank")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Name can only contain letters and spaces")
    private String name;

    @NotBlank(message = "Gender cannot be blank")
    @Pattern(regexp = "^[MF]$", message = "Gender must be M or F")
    private String gender;

    @NotBlank(message = "Identification cannot be blank")
    @Pattern(regexp = "^[0-9]{8,12}$", message = "Identification must be 8-12 digits")
    private String identification;

    @NotBlank(message = "Address cannot be blank")
    private String address;

    @NotBlank(message = "Phone cannot be blank")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone must be a valid format")
    private String phone;

    @NotBlank(message = "Password cannot be blank")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%]).{8,}$",
            message = "Password must have uppercase, number, special char and min 8 chars")
    private String password;
}