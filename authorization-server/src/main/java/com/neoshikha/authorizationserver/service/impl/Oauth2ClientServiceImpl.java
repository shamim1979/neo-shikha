package com.neoshikha.authorizationserver.service.impl;

import com.neoshikha.authorizationserver.repository.Oauth2ClientRepository;
import com.neoshikha.authorizationserver.service.Oauth2ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Oauth2ClientServiceImpl implements Oauth2ClientService {
    private final Oauth2ClientRepository oauth2ClientRepository;
}
