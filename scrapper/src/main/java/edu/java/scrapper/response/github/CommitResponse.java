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
    private static final String AUTHOR_FORMAT = "%s <%s>";
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

        @Override
        public String toString() {
            return String.format(AUTHOR_FORMAT, name, email);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Committer {
        private String name;
        private String email;
        private OffsetDateTime date;

        @Override
        public String toString() {
            return String.format(AUTHOR_FORMAT, name, email);
        }
    }
}
