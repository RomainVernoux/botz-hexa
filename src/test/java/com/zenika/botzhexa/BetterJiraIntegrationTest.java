package com.zenika.botzhexa;

import com.zenika.botzhexa.model.User;
import com.zenika.botzhexa.model.UserStory;
import com.zenika.botzhexa.model.UserStoryStatus;
import com.zenika.botzhexa.repositories.UserRepository;
import com.zenika.botzhexa.repositories.UserStoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.zenika.botzhexa.model.UserStoryStatus.*;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BetterJiraIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserStoryRepository userStoryRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void beforeEach() {
        mongoTemplate.dropCollection(User.class);
        mongoTemplate.dropCollection(UserStory.class);
    }

    @Test
    void get_all_non_closed_user_stories_with_owners() throws Exception {
        var user = aUser("John Doe");
        userRepository.save(user);
        userStoryRepository.save(aUserStory("us1", "desc1", TODO, user.getId()));
        userStoryRepository.save(aUserStory("us2", "desc2", WIP, null));
        userStoryRepository.save(aUserStory("us3", "desc3", CLOSED, null));

        mockMvc.perform(get("/user-stories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("us1"))
                .andExpect(jsonPath("$[0].description").value("desc1"))
                .andExpect(jsonPath("$[0].status").value("TODO"))
                .andExpect(jsonPath("$[0].user.username").value("John Doe"))
                .andExpect(jsonPath("$[1].title").value("us2"))
                .andExpect(jsonPath("$[1].description").value("desc2"))
                .andExpect(jsonPath("$[1].status").value("WIP"))
                .andExpect(jsonPath("$[1].user").doesNotExist());
    }

    @Test
    void change_status() throws Exception {
        var userStory = aUserStory("us1", "desc1", TODO, null);
        userStoryRepository.save(userStory);

        mockMvc.perform(post("/user-stories/" + userStory.getId() + "/change-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "newStatus": "WIP"
                        }
                        """));

        mockMvc.perform(get("/user-stories")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].status").value("WIP"));
    }

    @Test
    void reject_forbidden_status_change() throws Exception {
        var userStory = aUserStory("us1", "desc1", TODO, null);
        userStoryRepository.save(userStory);

        mockMvc.perform(post("/user-stories/" + userStory.getId() + "/change-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "newStatus": "DONE"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fail_for_unknown_user_story() throws Exception {
        mockMvc.perform(post("/user-stories/" + UUID.randomUUID() + "/change-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "newStatus": "WIP"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    private User aUser(String username) {
        var user = new User(UUID.randomUUID());
        user.setUsername(username);
        user.setPasswordHash("xyz");
        return user;
    }

    private UserStory aUserStory(String title, String description, UserStoryStatus status, UUID userId) {
        var userStory = new UserStory(UUID.randomUUID());
        userStory.setTitle(title);
        userStory.setDescription(description);
        userStory.setStatus(status);
        userStory.setUserId(userId);
        return userStory;
    }
}
