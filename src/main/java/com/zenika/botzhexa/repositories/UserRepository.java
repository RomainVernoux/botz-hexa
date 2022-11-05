package com.zenika.botzhexa.repositories;

import com.zenika.botzhexa.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface UserRepository extends MongoRepository<User, UUID> {
}
