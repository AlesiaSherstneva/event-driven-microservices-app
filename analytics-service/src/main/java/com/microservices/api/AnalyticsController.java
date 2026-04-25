package com.microservices.api;

import com.microservices.model.AnalyticsResponseModel;
import com.microservices.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping(value = "/", produces = "application/vnd.api.v1+json")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsController.class);

    @Operation(summary = "Get analytics by word")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success", content = {
                    @Content(mediaType = "application/vnd.api.v1+json",
                            schema = @Schema(implementation = AnalyticsResponseModel.class))
            }),
            @ApiResponse(responseCode = "400", description = "Not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping("get-word-count-by-word/{word}")
    public ResponseEntity<AnalyticsResponseModel> getWordCountByWord(@PathVariable @NotEmpty String word) {
        Optional<AnalyticsResponseModel> response = analyticsService.getWordAnalytics(word);

        if (response.isPresent()) {
            LOGGER.info("Analytics data returned with id {}", response.get().getId());

            return ResponseEntity.ok(response.get());
        }

        return ResponseEntity.ok(AnalyticsResponseModel.builder().build());
    }
}