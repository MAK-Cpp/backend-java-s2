package edu.java.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ListUserLinkResponse {
    private UserLinkResponse[] links;
    private int size;
}
