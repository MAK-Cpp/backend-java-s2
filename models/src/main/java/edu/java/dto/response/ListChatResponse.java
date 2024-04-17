package edu.java.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ListChatResponse {
    private ChatResponse[] chats;
    private int size;
}
