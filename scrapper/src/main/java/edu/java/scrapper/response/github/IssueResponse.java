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
public class IssueResponse implements Response {
    private int number;
    private String title;
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
}
