package com.microservices.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.web.SecurityFilterChain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class WebClientSecurityConfig {
    private final ClientRegistrationRepository clientRegistrationRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(WebClientSecurityConfig.class);

    private static final String GROUPS_CLAIM = "groups";
    private static final String ROLE_PREFIX = "ROLE_%s";

    @Value("${security.logout-success-url}")
    private String logoutSuccessUrl;

    OidcClientInitiatedLogoutSuccessHandler oidcClientInitiatedLogoutSuccessHandler () {
        OidcClientInitiatedLogoutSuccessHandler successHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        successHandler.setPostLogoutRedirectUri(logoutSuccessUrl);

        return successHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(configurer -> configurer
                        .requestMatchers("/").permitAll()
                        .anyRequest()
                        .fullyAuthenticated())
                .logout(logout -> logout
                        .logoutSuccessHandler(oidcClientInitiatedLogoutSuccessHandler())
                )
                .oauth2Client(Customizer.withDefaults())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userAuthoritiesMapper(userAuthoritiesMapper())
                        )
                );

        return http.build();
    }

    private GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return authorities -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                if (authority instanceof OidcUserAuthority oidcUserAuthority) {
                    OidcIdToken oidcIdToken = oidcUserAuthority.getIdToken();

                    LOGGER.info("Username from id token: {}", oidcIdToken.getPreferredUsername());

                    OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();

                    List<String> groups = userInfo != null
                            ? userInfo.getClaimAsStringList(GROUPS_CLAIM)
                            : oidcIdToken.getClaimAsStringList(GROUPS_CLAIM);

                    if (groups != null) {
                        List<SimpleGrantedAuthority> groupAuthorities = groups.stream()
                                .map(group -> new SimpleGrantedAuthority(ROLE_PREFIX.formatted(group.toUpperCase())))
                                .toList();
                        mappedAuthorities.addAll(groupAuthorities);
                    }
                }
            });

            return mappedAuthorities;
        };
    }
}