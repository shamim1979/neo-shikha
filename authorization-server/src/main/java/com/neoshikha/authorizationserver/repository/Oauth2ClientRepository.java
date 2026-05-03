package com.neoshikha.authorizationserver.repository;

import com.neoshikha.authorizationserver.entity.Oauth2Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface Oauth2ClientRepository extends JpaRepository<Oauth2Client, UUID> {
    Optional<Oauth2Client> findByClientId(String clientId);
}