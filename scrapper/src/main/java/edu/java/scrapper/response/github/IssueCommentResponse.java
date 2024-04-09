package edu.java.scrapper.response.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import edu.java.scrapper.response.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueCommentResponse implements Response {
    private User user;
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;
    private String body;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class User {
        private String login;
        private String type;
    }
}
