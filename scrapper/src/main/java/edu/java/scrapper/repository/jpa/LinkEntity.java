package edu.java.scrapper.repository.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.net.URI;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "links")
@NoArgsConstructor
public class LinkEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "link_id")
    @Getter
    private Long id;

    @Column(name = "uri", unique = true, nullable = false)
    private String uri;

    @Column(name = "last_update", nullable = false)
    private Timestamp lastUpdate = new Timestamp(System.currentTimeMillis());

    public LinkEntity(URI uri) {
        this.uri = uri.toString();
    }

    public OffsetDateTime getLastUpdate() {
        return OffsetDateTime.of(lastUpdate.toLocalDateTime(), ZoneOffset.UTC);
    }

    public void setLastUpdate(OffsetDateTime lastUpdate) {
        this.lastUpdate = Timestamp.from(lastUpdate.toInstant());
    }

    public URI getUri() {
        return URI.create(uri);
    }

    public void setUri(URI uri) {
        this.uri = uri.toString();
    }
}
