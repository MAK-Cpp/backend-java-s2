package edu.java.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkUpdateRequest {
    private int id;
    private String url;
    private String description;
    private int[] tgChatIds;
}
