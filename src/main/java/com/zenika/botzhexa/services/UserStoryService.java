package com.zenika.botzhexa.services;

import com.zenika.botzhexa.model.UserStory;
import com.zenika.botzhexa.model.UserStoryStatus;
import com.zenika.botzhexa.repositories.UserRepository;
import com.zenika.botzhexa.repositories.UserStoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static com.zenika.botzhexa.model.UserStoryStatus.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class UserStoryService {

    private final UserStoryRepository userStoryRepository;
    private final UserRepository userRepository;

    public UserStoryService(UserStoryRepository userStoryRepository, UserRepository userRepository) {
        this.userStoryRepository = userStoryRepository;
        this.userRepository = userRepository;
    }

    public List<UserStory> getAll() {
        var userStories = userStoryRepository.findAllByStatusNot(CLOSED);
        userStories.forEach(userStory -> {
            if (userStory.getUserId() != null) {
                var user = userRepository.findById(userStory.getUserId()).orElseThrow(IllegalStateException::new);
                userStory.setUser(user);
            }
        });
        return userStories;
    }

    public void updateStatus(UUID id, UserStoryStatus newStatus) {
        var userStory = userStoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No user story with id " + id));
        var oldStatus = userStory.getStatus();
        if (newStatus == TODO || newStatus == WIP && oldStatus != TODO || newStatus == DONE && oldStatus != WIP) {
            throw new ResponseStatusException(BAD_REQUEST, "Cannot change state to " + newStatus);
        }
        userStory.setStatus(newStatus);
        userStoryRepository.save(userStory);
    }
}
