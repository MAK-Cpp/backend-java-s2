package edu.java.scrapper.response.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.java.scrapper.response.Response;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueResponse implements Response {
    private int number;
    private String title;
    private User user;
    @JsonProperty("html_url")
    private String htmlUrl;
    private String state;
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;
    @JsonProperty("closed_at")
    private OffsetDateTime closedAt;
    private String body;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class User {
        String login;
        String type;

        @Override
        public String toString() {
            return login + "(type: " + type + ")";
        }
    }
}
