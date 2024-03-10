package edu.java.scrapper.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PullRequestResponse {
    @JsonProperty("html_url") private String htmlUrl;
    private String state;
    private String title;
    private String body;
    private int number;
    @JsonProperty("created_at") private OffsetDateTime createdAt;
    @JsonProperty("updated_at") private OffsetDateTime updatedAt;
    @JsonProperty("closed_at") private OffsetDateTime closedAt;
    @JsonProperty("merged_at") private OffsetDateTime mergedAt;
}
