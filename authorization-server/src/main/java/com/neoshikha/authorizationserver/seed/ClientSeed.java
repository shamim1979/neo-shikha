package com.neoshikha.authorizationserver.seed;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neoshikha.authorizationserver.entity.Oauth2Client;
import com.neoshikha.authorizationserver.repository.Oauth2ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.settings.ConfigurationSettingNames;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ClientSeed implements CommandLineRunner {

    private final Oauth2ClientRepository oauth2ClientRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) {
        if (oauth2ClientRepository.findByClientId("neo-client").isPresent()) {
            return;
        }

        Oauth2Client client = Oauth2Client.builder()
                .clientId("neo-client")
                .clientSecret("{noop}secret")
                .clientAuthenticationMethods(ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue())
                .authorizationGrantTypes(String.join(",",
                        AuthorizationGrantType.CLIENT_CREDENTIALS.getValue(),
                        AuthorizationGrantType.AUTHORIZATION_CODE.getValue(),
                        AuthorizationGrantType.REFRESH_TOKEN.getValue()
                ))
                .redirectUris("http://127.0.0.1:8080/login/oauth2/code/neo-client")
                .scopes(String.join(",",
                        OidcScopes.OPENID,
                        "read",
                        "write"
                ))
                .clientSettings(toJson(Map.of(
                        ConfigurationSettingNames.Client.REQUIRE_AUTHORIZATION_CONSENT, false
                )))
                .tokenSettings(toJson(Map.of(
                        ConfigurationSettingNames.Token.ACCESS_TOKEN_TIME_TO_LIVE, 30 * 60,
                        ConfigurationSettingNames.Token.REFRESH_TOKEN_TIME_TO_LIVE, 7 * 24 * 60 * 60
                )))
                .build();

        oauth2ClientRepository.save(client);
    }

    private String toJson(Map<String, Object> settings) {
        try {
            return objectMapper.writeValueAsString(settings);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize OAuth2 client settings", e);
        }
    }
}
