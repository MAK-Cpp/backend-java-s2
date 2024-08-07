package edu.java.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkUpdateRequest {
    private Long id;
    private String url;
    private String description;
    private ChatAndAlias[] chatsAndAliases;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatAndAlias {
        Long id;
        String alias;
    }
}
