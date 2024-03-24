package edu.java.scrapper.repository;

import edu.java.scrapper.dto.LinkDTO;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcLinkRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcLinkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void add(String uri) {
        jdbcTemplate.update("INSERT INTO links (uri) VALUES (?)", uri);
    }

    @Transactional
    public void remove(Long linkId) {
        jdbcTemplate.update("DELETE FROM links WHERE link_id = ?", linkId);
    }

    public List<LinkDTO> findAll() {
        return jdbcTemplate.query(
            "SELECT link_id, uri FROM links",
            (rs, rowNum) -> new LinkDTO(rs.getLong("link_id"), rs.getString("uri"))
        );
    }
}
