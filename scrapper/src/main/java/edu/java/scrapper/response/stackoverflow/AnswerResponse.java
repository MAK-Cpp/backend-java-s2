package edu.java.scrapper.response.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.java.scrapper.response.Response;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResponse implements Response {
    @JsonProperty("items")
    private List<Answer> answers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Answer {
        @JsonProperty("answer_id")
        private int answerId;
        @JsonProperty("question_id")
        private int questionId;
        @JsonProperty("body_markdown")
        private String message;
        @JsonProperty("creation_date")
        private OffsetDateTime creationDate;
        private AnswerOwner owner;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerOwner {
        private String link;
        @JsonProperty("display_name")
        private String name;
        private int reputation;
        @JsonProperty("user_id")
        private int userId;

        @Override
        public String toString() {
           return "[" + name + "](" + link + "), reputation: " + reputation;
        }
    }
}
