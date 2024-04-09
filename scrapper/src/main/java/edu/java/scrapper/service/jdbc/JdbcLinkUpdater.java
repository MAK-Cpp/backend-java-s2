package edu.java.scrapper.service.jdbc;

import edu.java.scrapper.repository.jdbc.JdbcLinkRepository;
import edu.java.scrapper.service.LinkUpdater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JdbcLinkUpdater implements LinkUpdater {
    private final JdbcLinkRepository jdbcLinkRepository;

    @Autowired
    public JdbcLinkUpdater(JdbcLinkRepository jdbcLinkRepository) {
        this.jdbcLinkRepository = jdbcLinkRepository;
    }

    @Override
    public void updateLink(Long linkId) {
        jdbcLinkRepository.update(linkId);
    }
}
