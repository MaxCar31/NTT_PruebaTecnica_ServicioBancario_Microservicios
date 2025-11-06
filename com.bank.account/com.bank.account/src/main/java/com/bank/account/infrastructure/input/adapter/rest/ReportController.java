package com.bank.account.infrastructure.input.adapter.rest.impl;

import com.bank.account.application.input.port.ReportUseCase;
import com.bank.account.domain.exception.FileGenerationException;
import com.bank.account.infrastructure.exception.ErrorResponse;
import com.bank.account.infrastructure.output.excel.ExcelReportGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Endpoints for generating reports")
public class ReportController {

    private final ReportUseCase reportUseCase;
    private final ExcelReportGenerator excelReportGenerator;

    @GetMapping
    @Operation(summary = "Generate Account Statement Report",
            description = "Generates an account statement report in Excel format. Requires at least 'clientId' or 'accountNumber'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report generated successfully.",
                    content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "400", description = "Invalid input data. 'clientId' or 'accountNumber' must be provided.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Client or Account not found for the given criteria.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "An unexpected error occurred while generating the file.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Customer service is unavailable.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Mono<Void> getAccountStatementReport(
            ServerHttpResponse response,

            @Parameter(description = "ID of the client for the report.", example = "1")
            @RequestParam(required = false) Long clientId,

            @Parameter(description = "Account number for the report.", example = "478758")
            @RequestParam(required = false) String accountNumber,

            @Parameter(description = "The start date and time for the report period (ISO 8601 format).", required = true, example = "2025-10-12T09:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "The end date and time for the report period (ISO 8601 format).", required = true, example = "2025-10-12T17:30:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        if (clientId == null && (accountNumber == null || accountNumber.isBlank())) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Either 'clientId' or 'accountNumber' must be provided."));
        }

        return reportUseCase.generateAccountStatement(clientId, accountNumber, startDate, endDate)
                .flatMap(statement -> {
                    try {
                        byte[] excelContent = excelReportGenerator.generateStatement(statement);
                        String filename = "statement_" + (clientId != null ? clientId : accountNumber) + "_" + startDate.toLocalDate() + "_to_" + endDate.toLocalDate() + ".xlsx";

                        response.getHeaders().setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
                        response.getHeaders().setContentDisposition(ContentDisposition.attachment().filename(filename).build());

                        DataBuffer buffer = response.bufferFactory().wrap(excelContent);
                        return response.writeWith(Mono.just(buffer));

                    } catch (IOException e) {
                        return Mono.error(new FileGenerationException("Failed to generate Excel report", e));
                    }
                });
    }
}