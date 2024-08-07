package edu.java.scrapper.service.jdbc;

import edu.java.dto.response.LinkResponse;
import edu.java.scrapper.repository.jdbc.JdbcLinkRepository;
import edu.java.scrapper.service.AbstractService;
import edu.java.scrapper.service.LinkUpdater;
import org.springframework.transaction.annotation.Transactional;

public class JdbcLinkUpdater extends AbstractService implements LinkUpdater {
    private final JdbcLinkRepository jdbcLinkRepository;

    public JdbcLinkUpdater(JdbcLinkRepository jdbcLinkRepository) {
        this.jdbcLinkRepository = jdbcLinkRepository;
    }

    @Override
    @Transactional
    public LinkResponse updateLink(Long linkId) {
        return jdbcLinkRepository.update(linkId).getFirst();
    }
}
