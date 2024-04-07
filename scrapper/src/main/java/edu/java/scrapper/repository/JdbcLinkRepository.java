package edu.java.scrapper.repository;

import edu.java.dto.response.LinkResponse;
import edu.java.scrapper.exception.UnexpectedValuesCountException;
import java.net.URI;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class JdbcLinkRepository extends JdbcTemplate {
    public static final RowMapper<LinkResponse> LINK_DTO_ROW_MAPPER =
        (rs, rowNum) -> new LinkResponse(rs.getLong("link_id"), URI.create(rs.getString("uri")));

    @Autowired
    public JdbcLinkRepository(DataSource dataSource) {
        super(dataSource);
    }

    public Long add(URI uri) {
        List<LinkResponse> isLinkIn = findAll(uri);
        return switch (isLinkIn.size()) {
            case 1 -> isLinkIn.getFirst().getId();
            case 0 -> query(
                "INSERT INTO links (uri) VALUES (?) RETURNING link_id, uri",
                LINK_DTO_ROW_MAPPER,
                uri.toString()
            ).getFirst().getId();
            default ->
                throw new UnexpectedValuesCountException("Expected <= 1 links " + uri + ", got " + isLinkIn.size());
        };
    }

    public void remove(Long linkId) {
        update("DELETE FROM links WHERE link_id = ?", linkId);
    }

    public List<LinkResponse> findAll() {
        return query(
            "SELECT link_id, uri FROM links",
            LINK_DTO_ROW_MAPPER
        );
    }

    public List<LinkResponse> findAll(URI uri) {
        return query(
            "SELECT link_id, uri FROM links WHERE uri = ?",
            LINK_DTO_ROW_MAPPER,
            uri.toString()
        );
    }
}
