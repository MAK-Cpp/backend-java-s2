package edu.java.scrapper.service.jpa;

import edu.java.dto.exception.AliasAlreadyTakenException;
import edu.java.dto.exception.DTOException;
import edu.java.dto.exception.LinkAlreadyTrackedException;
import edu.java.dto.exception.LinkNotFoundException;
import edu.java.dto.exception.NonExistentChatException;
import edu.java.dto.exception.NonExistentLinkAliasException;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinkResponse;
import edu.java.dto.response.ListUserLinkResponse;
import edu.java.dto.response.UserLinkResponse;
import edu.java.scrapper.repository.jpa.ChatEntity;
import edu.java.scrapper.repository.jpa.ChatsAndLinksEntity;
import edu.java.scrapper.repository.jpa.LinkEntity;
import edu.java.scrapper.service.AbstractService;
import edu.java.scrapper.service.LinkService;
import edu.java.scrapper.validator.LinkValidator;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.net.URI;
import java.util.List;
import java.util.function.Function;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.transaction.annotation.Transactional;

public class JpaLinkService extends AbstractService implements LinkService {
    /*package-private*/ static final Function<? super LinkEntity, LinkResponse> LINK_RESPONSE_MAPPER =
        linkEntity -> new LinkResponse(
            linkEntity.getId(),
            linkEntity.getUri(),
            linkEntity.getLastUpdate()
        );
    private static final Function<? super ChatsAndLinksEntity, UserLinkResponse> USER_LINK_RESPONSE_MAPPER =
        chatsAndLinksEntity -> new UserLinkResponse(
            LINK_RESPONSE_MAPPER.apply(chatsAndLinksEntity.getLink()),
            chatsAndLinksEntity.getAlias()
        );
    private final SessionFactory sessionFactory;
    private final List<LinkValidator> linkValidators;

    public JpaLinkService(SessionFactory sessionFactory, List<LinkValidator> linkValidators) {
        this.sessionFactory = sessionFactory;
        this.linkValidators = linkValidators;
    }

    private static ChatEntity validateChatId(Session session, Long chatId) throws DTOException {
        validateId(chatId);
        final ChatEntity chatEntity = session.get(ChatEntity.class, chatId);
        if (chatEntity == null) {
            throw new NonExistentChatException(String.format(NON_EXISTING_CHAT_EXCEPTION_FORMAT, chatId));
        }
        return chatEntity;
    }

    @Override
    public ListLinkResponse getAllLinks() throws DTOException {
        try (Session session = sessionFactory.openSession()) {
            final CriteriaBuilder builder = session.getCriteriaBuilder();
            final CriteriaQuery<LinkEntity> criteria = builder.createQuery(LinkEntity.class);
            final Root<LinkEntity> linkRoot = criteria.from(LinkEntity.class);
            final CriteriaQuery<LinkEntity> all = criteria.select(linkRoot);

            final LinkResponse[] responses = session.createQuery(all)
                .getResultStream()
                .map(LINK_RESPONSE_MAPPER)
                .toArray(LinkResponse[]::new);

            return new ListLinkResponse(responses, responses.length);
        }
    }

    @Override
    public ListUserLinkResponse getAllLinks(Long chatId) throws DTOException {
        try (Session session = sessionFactory.openSession()) {
            validateChatId(session, chatId);
            final CriteriaBuilder builder = session.getCriteriaBuilder();
            final CriteriaQuery<ChatsAndLinksEntity> criteria = builder.createQuery(ChatsAndLinksEntity.class);
            final Root<ChatsAndLinksEntity> chatsAndLinksRoot = criteria.from(ChatsAndLinksEntity.class);
            final CriteriaQuery<ChatsAndLinksEntity> allWhere = criteria.select(chatsAndLinksRoot)
                .where(builder.equal(chatsAndLinksRoot.get("key").get("chatId"), chatId));

            final UserLinkResponse[] responses = session.createQuery(allWhere)
                .getResultStream()
                .map(USER_LINK_RESPONSE_MAPPER)
                .toArray(UserLinkResponse[]::new);

            return new ListUserLinkResponse(responses, responses.length);
        }
    }

    private static CriteriaQuery<ChatsAndLinksEntity> chatsAndLinksWhereEqChatIdAndAlias(
        CriteriaBuilder builder,
        Long chatId,
        String alias
    ) {
        final CriteriaQuery<ChatsAndLinksEntity> criteria = builder.createQuery(ChatsAndLinksEntity.class);
        final Root<ChatsAndLinksEntity> chatsAndLinksRoot = criteria.from(ChatsAndLinksEntity.class);
        return criteria.select(chatsAndLinksRoot)
            .where(builder.equal(chatsAndLinksRoot.get("key").get("chatId"), chatId))
            .where(builder.equal(chatsAndLinksRoot.get("alias"), alias));
    }

    @Override
    public UserLinkResponse getLink(Long chatId, String alias) throws DTOException {
        try (Session session = sessionFactory.openSession()) {
            validateChatId(session, chatId);
            final CriteriaBuilder builder = session.getCriteriaBuilder();

            UserLinkResponse[] responses =
                session.createQuery(chatsAndLinksWhereEqChatIdAndAlias(builder, chatId, alias))
                    .getResultStream()
                    .map(USER_LINK_RESPONSE_MAPPER)
                    .toArray(UserLinkResponse[]::new);

            if (responses.length == 0) {
                throw new NonExistentLinkAliasException(String.format(
                    NON_EXISTENT_LINK_ALIAS_EXCEPTION_FORMAT,
                    alias,
                    chatId
                ));
            }

            return responses[0];
        }
    }

    @Override
    @Transactional
    public UserLinkResponse addLink(Long chatId, String link, String alias) throws DTOException {
        final URI uri = validateLink(link, linkValidators);
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            final ChatEntity chatEntity = validateChatId(session, chatId);
            transaction = session.beginTransaction();
            final CriteriaBuilder builder = session.getCriteriaBuilder();
            final CriteriaQuery<LinkEntity> linkCriteria = builder.createQuery(LinkEntity.class);
            final Root<LinkEntity> linkRoot = linkCriteria.from(LinkEntity.class);
            final CriteriaQuery<LinkEntity> linkEntityAllWhere = linkCriteria.select(linkRoot)
                .where(builder.equal(linkRoot.get("uri"), uri.toString()));

            final List<LinkEntity> responses = session.createQuery(linkEntityAllWhere).getResultList();
            final LinkEntity linkEntity;
            if (responses.isEmpty()) {
                linkEntity = new LinkEntity(uri);
                session.persist(linkEntity);
            } else {
                linkEntity = responses.getFirst();
            }
            final var key = new ChatsAndLinksEntity.ChatsAndLinksPK(chatId, linkEntity.getId());
            if (session.get(ChatsAndLinksEntity.class, key) != null) {
                transaction.rollback();
                throw new LinkAlreadyTrackedException(
                    String.format(LINK_ALREADY_TRACKED_EXCEPTION_FORMAT, linkEntity.getId(), chatId)
                );
            }
            final List<ChatsAndLinksEntity> chatsAndLinksEntities =
                session.createQuery(chatsAndLinksWhereEqChatIdAndAlias(builder, chatId, alias))
                    .getResultList();
            if (!chatsAndLinksEntities.isEmpty()) {
                transaction.rollback();
                throw new AliasAlreadyTakenException(
                    String.format(ALIAS_ALREADY_TAKEN_EXCEPTION_FORMAT, alias, chatId)
                );
            }
            final ChatsAndLinksEntity chatsAndLinksEntity = new ChatsAndLinksEntity(key, alias, chatEntity, linkEntity);
            session.persist(chatsAndLinksEntity);
            session.flush();
            transaction.commit();
            return USER_LINK_RESPONSE_MAPPER.apply(chatsAndLinksEntity);
        }
    }

    @Override
    @Transactional
    public UserLinkResponse removeLink(Long chatId, String alias) throws DTOException {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            validateChatId(session, chatId);
            transaction = session.beginTransaction();
            final CriteriaBuilder builder = session.getCriteriaBuilder();
            final List<ChatsAndLinksEntity> chatsAndLinksEntities =
                session.createQuery(chatsAndLinksWhereEqChatIdAndAlias(builder, chatId, alias))
                    .getResultList();
            if (chatsAndLinksEntities.isEmpty()) {
                transaction.rollback();
                throw new LinkNotFoundException(String.format(NON_EXISTENT_LINK_ALIAS_EXCEPTION_FORMAT, alias, chatId));
            }
            session.remove(chatsAndLinksEntities.getFirst());
            session.flush();
            transaction.commit();
            return USER_LINK_RESPONSE_MAPPER.apply(chatsAndLinksEntities.getFirst());
        }
    }
}
