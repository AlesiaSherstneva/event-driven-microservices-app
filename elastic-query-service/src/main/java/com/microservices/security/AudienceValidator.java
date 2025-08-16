package com.microservices.security;

import com.microservices.config.ElasticQueryServiceConfigData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@Qualifier("elastic-query-service-audience-validator")
@RequiredArgsConstructor
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {
    private final ElasticQueryServiceConfigData configData;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (jwt.getAudience().contains(configData.getCustomAudience())) {
            return OAuth2TokenValidatorResult.success();
        } else {
            OAuth2Error audienceError = new OAuth2Error(
                    "Invalid token",
                    String.format("The required audience %s is missing!", configData.getCustomAudience()),
                    null
            );
            return OAuth2TokenValidatorResult.failure(audienceError);
        }
    }
}