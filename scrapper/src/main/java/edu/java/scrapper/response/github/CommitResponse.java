package edu.java.scrapper.response.github;

import edu.java.scrapper.response.Response;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommitResponse implements Response {
    private Commit commit;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Commit {
        private Author author;
        private Committer committer;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Author {
        private String name;
        private String email;
        private OffsetDateTime date;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Committer {
        private String name;
        private String email;
        private OffsetDateTime date;
    }
}
