package edu.java.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserLinkResponse {
    private LinkResponse link;
    private String alias;
}
