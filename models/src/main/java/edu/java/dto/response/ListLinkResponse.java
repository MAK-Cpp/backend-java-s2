package edu.java.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ListLinkResponse {
    private LinkResponse[] links;
    private int size;
}
