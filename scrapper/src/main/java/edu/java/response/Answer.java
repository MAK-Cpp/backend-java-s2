package edu.java.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Answer {
    @JsonProperty("answer_id") private int answerId;
    @JsonProperty("question_id") private int questionId;
    @JsonProperty("body_markdown") private String message;
    @JsonProperty("creation_date") private OffsetDateTime creationDate;
    private AnswerOwner owner;
}
