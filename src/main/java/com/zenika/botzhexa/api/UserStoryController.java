package com.zenika.botzhexa.api;

import com.zenika.botzhexa.model.UserStory;
import com.zenika.botzhexa.services.UserStoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user-stories")
public class UserStoryController {

    private final UserStoryService userStoryService;

    public UserStoryController(UserStoryService userStoryService) {
        this.userStoryService = userStoryService;
    }

    @GetMapping
    public List<UserStory> getAll() {
        return userStoryService.getAll();
    }

    @PostMapping("/{id}/change-status")
    public void updateStatus(@PathVariable UUID id, @RequestBody ChangeStatusDTO dto) {
        userStoryService.updateStatus(id, dto.newStatus());
    }
}
