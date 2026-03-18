package com.microservices.security;

import com.microservices.config.AnalyticsServiceConfigData;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Qualifier(value = "analytics-service-audience-validator")
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {
    private final AnalyticsServiceConfigData analyticsServiceConfigData;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (jwt.getAudience().contains(analyticsServiceConfigData.getCustomAudience())) {
            return OAuth2TokenValidatorResult.success();
        } else {
            OAuth2Error audienceError = new OAuth2Error(
                    "Invalid token",
                    "The required audience %s is missing".formatted(analyticsServiceConfigData.getCustomAudience()),
                    null
            );

            return OAuth2TokenValidatorResult.failure(audienceError);
        }
    }
}