package edu.java.scrapper.service.jpa;

import edu.java.dto.response.LinkResponse;
import edu.java.scrapper.repository.jpa.LinkEntity;
import edu.java.scrapper.service.AbstractService;
import edu.java.scrapper.service.LinkUpdater;
import java.time.OffsetDateTime;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class JpaLinkUpdater extends AbstractService implements LinkUpdater {
    private final SessionFactory sessionFactory;

    public JpaLinkUpdater(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public LinkResponse updateLink(Long linkId) {
        validateId(linkId);
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            final LinkEntity link = session.get(LinkEntity.class, linkId);
            session.detach(link);
            link.setLastUpdate(OffsetDateTime.now());
            session.merge(link);
            session.flush();
            session.getTransaction().commit();
            return JpaLinkService.LINK_RESPONSE_MAPPER.apply(link);
        }
    }
}
