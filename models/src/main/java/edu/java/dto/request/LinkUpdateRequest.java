package edu.java.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkUpdateRequest {
    private long id;
    private String url;
    private String description;
    private long[] tgChatIds;
}
