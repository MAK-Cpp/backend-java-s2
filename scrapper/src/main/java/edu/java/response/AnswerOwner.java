package edu.java.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerOwner {
    private String link;
    @JsonProperty("display_name") private String name;
    private int reputation;
    @JsonProperty("user_id") private int userId;
}
