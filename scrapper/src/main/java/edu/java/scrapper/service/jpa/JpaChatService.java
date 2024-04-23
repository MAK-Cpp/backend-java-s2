package edu.java.scrapper.service.jpa;

import edu.java.dto.exception.DTOException;
import edu.java.dto.exception.NonExistentChatException;
import edu.java.dto.exception.WrongParametersException;
import edu.java.dto.response.ChatResponse;
import edu.java.dto.response.LinkAliasResponse;
import edu.java.dto.response.ListChatResponse;
import edu.java.scrapper.repository.jpa.ChatEntity;
import edu.java.scrapper.repository.jpa.ChatsAndLinksEntity;
import edu.java.scrapper.service.AbstractService;
import edu.java.scrapper.service.ChatService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public class JpaChatService extends AbstractService implements ChatService {
    private final SessionFactory sessionFactory;

    public JpaChatService(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    @Transactional
    public void registerChat(Long chatId) throws DTOException {
        validateId(chatId);
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            if (session.get(ChatEntity.class, chatId) != null) {
                transaction.rollback();
                throw new WrongParametersException(CHAT_ALREADY_EXISTS_EXCEPTION);
            }
            session.persist(new ChatEntity(chatId));
            session.flush();
            log.info(String.format(SUCCESS_CHAT_REGISTER_FORMAT, chatId));
            transaction.commit();
        }
    }

    @Override
    @Transactional
    public void deleteChat(Long chatId) throws DTOException {
        validateId(chatId);
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            final ChatEntity chatEntity = session.get(ChatEntity.class, chatId);
            if (chatEntity == null) {
                transaction.rollback();
                throw new NonExistentChatException(String.format(NON_EXISTING_CHAT_EXCEPTION_FORMAT, chatId));
            }
            session.remove(chatEntity);
            session.flush();
            transaction.commit();
        }
    }

    @Override
    public ListChatResponse getAllChats() throws DTOException {
        try (Session session = sessionFactory.openSession()) {
            final CriteriaBuilder builder = session.getCriteriaBuilder();
            final CriteriaQuery<ChatEntity> criteria = builder.createQuery(ChatEntity.class);
            final Root<ChatEntity> chatRoot = criteria.from(ChatEntity.class);
            final CriteriaQuery<ChatEntity> all = criteria.select(chatRoot);

            final ChatResponse[] responses = session.createQuery(all)
                .getResultStream()
                .map(chatEntity -> new ChatResponse(chatEntity.getId()))
                .toArray(ChatResponse[]::new);

            return new ListChatResponse(responses, responses.length);
        }
    }

    @Override
    public ListChatResponse getAllChats(Long linkId) throws DTOException {
        validateId(linkId);
        try (Session session = sessionFactory.openSession()) {
            final CriteriaBuilder builder = session.getCriteriaBuilder();
            final CriteriaQuery<ChatsAndLinksEntity> criteria = builder.createQuery(ChatsAndLinksEntity.class);
            final Root<ChatsAndLinksEntity> chatsAndLinksEntityRoot = criteria.from(ChatsAndLinksEntity.class);
            final CriteriaQuery<ChatsAndLinksEntity> allWhere = criteria.select(chatsAndLinksEntityRoot)
                .where(builder.equal(chatsAndLinksEntityRoot.get("key").get("linkId"), linkId));

            final ChatResponse[] responses = session.createQuery(allWhere)
                .getResultStream()
                .map(chatEntity -> new ChatResponse(chatEntity.getChat().getId()))
                .toArray(ChatResponse[]::new);

            return new ListChatResponse(responses, responses.length);
        }
    }

    @Override
    public ChatResponse getChat(Long chatId) throws DTOException {
        validateId(chatId);
        try (Session session = sessionFactory.openSession()) {
            final ChatEntity chatEntity = session.get(ChatEntity.class, chatId);
            if (chatEntity == null) {
                throw new NonExistentChatException(String.format(NON_EXISTING_CHAT_EXCEPTION_FORMAT, chatId));
            }
            return new ChatResponse(chatEntity.getId());
        }
    }

    @Override
    public LinkAliasResponse getLinkAlias(Long chatId, Long linkId) throws DTOException {
        validateId(chatId);
        validateId(linkId);
        try (Session session = sessionFactory.openSession()) {
            final var key = new ChatsAndLinksEntity.ChatsAndLinksPK(chatId, linkId);
            final ChatsAndLinksEntity chatsAndLinksEntity = session.get(ChatsAndLinksEntity.class, key);
            if (chatsAndLinksEntity == null) {
                throw new WrongParametersException(String.format(LINK_IS_NOT_TRACKED_BY_CHAT_FORMAT, linkId, chatId));
            }
            return new LinkAliasResponse(chatsAndLinksEntity.getAlias());
        }
    }
}
