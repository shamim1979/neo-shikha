package com.neoshikha.userservice.repository;

import com.neoshikha.sharedcore.repository.BaseRepository;
import com.neoshikha.userservice.entity.User;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends BaseRepository<User, UUID> {
}
