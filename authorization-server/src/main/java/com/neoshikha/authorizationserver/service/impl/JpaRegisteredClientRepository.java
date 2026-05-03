package com.neoshikha.authorizationserver.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neoshikha.authorizationserver.entity.Oauth2Client;
import com.neoshikha.authorizationserver.repository.Oauth2ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.ConfigurationSettingNames;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JpaRegisteredClientRepository implements RegisteredClientRepository {

    private final Oauth2ClientRepository clientRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void save(RegisteredClient registeredClient) {
        Oauth2Client client = toEntity(registeredClient);
        clientRepository.save(client);
    }

    private Oauth2Client toEntity(RegisteredClient rc) {
        Oauth2Client client = new Oauth2Client();

        client.setId(UUID.fromString(rc.getId()));
        client.setClientId(rc.getClientId());
        client.setClientIdIssuedAt(rc.getClientIdIssuedAt());
        client.setClientSecret(rc.getClientSecret());
        client.setClientSecretExpiresAt(rc.getClientSecretExpiresAt());
        client.setClientName(rc.getClientName());

        client.setClientAuthenticationMethods(
                rc.getClientAuthenticationMethods()
                        .stream()
                        .map(ClientAuthenticationMethod::getValue)
                        .collect(Collectors.joining(","))
        );

        client.setAuthorizationGrantTypes(
                rc.getAuthorizationGrantTypes()
                        .stream()
                        .map(AuthorizationGrantType::getValue)
                        .collect(Collectors.joining(","))
        );

        client.setRedirectUris(String.join(",", rc.getRedirectUris()));
        client.setPostLogoutRedirectUris(String.join(",", rc.getPostLogoutRedirectUris()));
        client.setScopes(String.join(",", rc.getScopes()));

        client.setClientSettings(writeSettings(rc.getClientSettings().getSettings()));
        client.setTokenSettings(writeSettings(rc.getTokenSettings().getSettings()));

        return client;
    }

    private RegisteredClient toObject(Oauth2Client client) {
        RegisteredClient.Builder builder = RegisteredClient.withId(client.getId().toString())
                .clientId(client.getClientId())
                .clientIdIssuedAt(client.getClientIdIssuedAt())
                .clientSecret(client.getClientSecret())
                .clientSecretExpiresAt(client.getClientSecretExpiresAt())
                .clientName(client.getClientName());

        split(client.getClientAuthenticationMethods())
                .forEach(method -> builder.clientAuthenticationMethod(
                        new ClientAuthenticationMethod(method)
                ));

        split(client.getAuthorizationGrantTypes())
                .forEach(grant -> builder.authorizationGrantType(
                        new AuthorizationGrantType(grant)
                ));

        split(client.getRedirectUris()).forEach(builder::redirectUri);
        split(client.getPostLogoutRedirectUris()).forEach(builder::postLogoutRedirectUri);
        split(client.getScopes()).forEach(builder::scope);

        builder.clientSettings(readClientSettings(client.getClientSettings()));
        builder.tokenSettings(readTokenSettings(client.getTokenSettings()));

        return builder.build();
    }

    private ClientSettings readClientSettings(String value) {
        Map<String, Object> settings = readSettings(value);
        ClientSettings.Builder builder = ClientSettings.builder();

        asBoolean(settings.get(ConfigurationSettingNames.Client.REQUIRE_AUTHORIZATION_CONSENT))
                .ifPresent(builder::requireAuthorizationConsent);
        asBoolean(settings.get(ConfigurationSettingNames.Client.REQUIRE_PROOF_KEY))
                .ifPresent(builder::requireProofKey);

        return builder.build();
    }

    private TokenSettings readTokenSettings(String value) {
        Map<String, Object> settings = readSettings(value);
        TokenSettings.Builder builder = TokenSettings.builder();

        asDuration(settings.get(ConfigurationSettingNames.Token.ACCESS_TOKEN_TIME_TO_LIVE))
                .ifPresent(builder::accessTokenTimeToLive);
        asDuration(settings.get(ConfigurationSettingNames.Token.REFRESH_TOKEN_TIME_TO_LIVE))
                .ifPresent(builder::refreshTokenTimeToLive);
        asDuration(settings.get(ConfigurationSettingNames.Token.AUTHORIZATION_CODE_TIME_TO_LIVE))
                .ifPresent(builder::authorizationCodeTimeToLive);
        asDuration(settings.get(ConfigurationSettingNames.Token.DEVICE_CODE_TIME_TO_LIVE))
                .ifPresent(builder::deviceCodeTimeToLive);
        asBoolean(settings.get(ConfigurationSettingNames.Token.REUSE_REFRESH_TOKENS))
                .ifPresent(builder::reuseRefreshTokens);

        return builder.build();
    }

    private Map<String, Object> readSettings(String value) {
        if (value == null || value.isBlank()) {
            return Map.of();
        }

        try {
            return objectMapper.readValue(value, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    private String writeSettings(Map<String, Object> settings) {
        Map<String, Object> serializableSettings = new HashMap<>();
        settings.forEach((key, value) -> serializableSettings.put(key, toSerializableValue(value)));

        try {
            return objectMapper.writeValueAsString(serializableSettings);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize OAuth2 client settings", e);
        }
    }

    private Object toSerializableValue(Object value) {
        if (value instanceof Duration duration) {
            return duration.toSeconds();
        }
        return value;
    }

    private Optional<Boolean> asBoolean(Object value) {
        if (value instanceof Boolean booleanValue) {
            return Optional.of(booleanValue);
        }
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            return Optional.of(Boolean.parseBoolean(stringValue));
        }
        return Optional.empty();
    }

    private Optional<Duration> asDuration(Object value) {
        if (value instanceof Duration duration) {
            return Optional.of(duration);
        }
        if (value instanceof Number number) {
            return Optional.of(Duration.ofSeconds(number.longValue()));
        }
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            try {
                return Optional.of(Duration.parse(stringValue));
            } catch (Exception ignored) {
                try {
                    return Optional.of(Duration.ofSeconds(Long.parseLong(stringValue)));
                } catch (NumberFormatException ignoredNumberFormat) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    private List<String> split(String value) {
        if (value == null || value.isBlank()) return List.of();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    @Override
    public RegisteredClient findById(String id) {
        return clientRepository.findById(UUID.fromString(id))
                .map(this::toObject)
                .orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return clientRepository.findByClientId(clientId)
                .map(this::toObject)
                .orElse(null);
    }
}
