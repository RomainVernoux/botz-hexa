package com.zenika.botzhexa.api;

import com.zenika.botzhexa.model.UserStoryStatus;

public record ChangeStatusDTO(UserStoryStatus newStatus) {
}
