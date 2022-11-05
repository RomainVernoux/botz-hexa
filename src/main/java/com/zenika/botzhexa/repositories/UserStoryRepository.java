package com.zenika.botzhexa.repositories;

import com.zenika.botzhexa.model.UserStory;
import com.zenika.botzhexa.model.UserStoryStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface UserStoryRepository extends MongoRepository<UserStory, UUID> {

    List<UserStory> findAllByStatusNot(UserStoryStatus statusToExclude);
}
