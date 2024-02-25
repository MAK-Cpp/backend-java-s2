package edu.java.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueResponse {
    private int number;
    private String title;
    @JsonProperty("html_url") private String htmlUrl;
    private String state;
    @JsonProperty("created_at") private OffsetDateTime createdAt;
    @JsonProperty("updated_at") private OffsetDateTime updatedAt;
    @JsonProperty("closed_at") private OffsetDateTime closedAt;
    private String body;
}
