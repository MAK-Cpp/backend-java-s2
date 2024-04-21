package edu.java.scrapper.repository.jdbc;

import edu.java.dto.response.LinkResponse;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Slf4j
public class JdbcLinkRepository extends JdbcTemplate {
    public static final RowMapper<LinkResponse> LINK_DTO_ROW_MAPPER =
        (rs, rowNum) -> new LinkResponse(
            rs.getLong("link_id"),
            URI.create(rs.getString("uri")),
            rs.getTimestamp("last_update").toInstant().atOffset(ZoneOffset.UTC)
        );
    public static final String GET_LINK_BY_URI = "SELECT link_id, uri, last_update FROM links WHERE uri = ?";
    @SuppressWarnings("checkstyle:LineLength")
    public static final String ADD_AND_GET_UNIQUE_LINK =
        "INSERT INTO links (uri) VALUES (?) ON CONFLICT (uri) DO UPDATE SET last_update = EXCLUDED.last_update RETURNING link_id, uri, last_update";

    @Autowired
    public JdbcLinkRepository(DataSource dataSource) {
        super(dataSource);
    }

    @Transactional
    public LinkResponse add(URI uri) {
        return query(
            ADD_AND_GET_UNIQUE_LINK,
            LINK_DTO_ROW_MAPPER,
            uri.toString()
        ).getFirst();
    }

    @Transactional
    public void remove(Long linkId) {
        update("DELETE FROM links WHERE link_id = ?", linkId);
    }

    @Transactional
    public void update(Long linkId) {
        update("UPDATE links SET last_update = ? WHERE link_id = ?", OffsetDateTime.now(), linkId);
    }

    public List<LinkResponse> findAll() {
        return query(
            "SELECT link_id, uri, last_update FROM links",
            LINK_DTO_ROW_MAPPER
        );
    }

    public List<LinkResponse> findAll(URI uri) {
        return query(
            GET_LINK_BY_URI,
            LINK_DTO_ROW_MAPPER,
            uri.toString()
        );
    }

    public List<LinkResponse> findAll(Long linkId) {
        return query(
            "SELECT link_id, uri, last_update FROM links WHERE link_id = ?",
            LINK_DTO_ROW_MAPPER,
            linkId
        );
    }
}
