package edu.java.scrapper.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RemoveLinkRequest {
    private String link;
}
