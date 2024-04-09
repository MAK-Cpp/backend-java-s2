package edu.java.dto.response;

import java.net.URI;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LinkResponse {
    private Long id;
    private URI uri;
    private OffsetDateTime lastUpdate;
}
