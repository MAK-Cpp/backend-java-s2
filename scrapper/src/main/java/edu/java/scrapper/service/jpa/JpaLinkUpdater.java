package edu.java.scrapper.service.jpa;

import edu.java.dto.response.LinkResponse;
import edu.java.scrapper.repository.jpa.LinkEntity;
import edu.java.scrapper.service.AbstractService;
import edu.java.scrapper.service.LinkUpdater;
import java.time.OffsetDateTime;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;

public class JpaLinkUpdater extends AbstractService implements LinkUpdater {
    private final EntityManager entityManager;

    public JpaLinkUpdater(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public LinkResponse updateLink(Long linkId) {
        validateId(linkId);
        final LinkEntity link = entityManager.find(LinkEntity.class, linkId);
        entityManager.detach(link);
        link.setLastUpdate(OffsetDateTime.now());
        entityManager.merge(link);
        entityManager.flush();
        return JpaLinkService.LINK_RESPONSE_MAPPER.apply(link);
    }
}
