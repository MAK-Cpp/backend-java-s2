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
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public class JpaChatService extends AbstractService implements ChatService {
    public static final String KEY = "key";
    public static final String CHAT_ID = "chatId";
    private final EntityManager entityManager;

    public JpaChatService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void registerChat(Long chatId) throws DTOException {
        validateId(chatId);
        if (entityManager.find(ChatEntity.class, chatId) != null) {
            throw new WrongParametersException(String.format(CHAT_ALREADY_EXISTS_EXCEPTION_FORMAT, chatId));
        }
        entityManager.persist(new ChatEntity(chatId));
        entityManager.flush();
        log.info(String.format(SUCCESS_CHAT_REGISTER_FORMAT, chatId));
    }

    @Override
    @Transactional
    public void deleteChat(Long chatId) throws DTOException {
        validateId(chatId);
        final ChatEntity chatEntity = entityManager.find(ChatEntity.class, chatId);
        if (chatEntity == null) {
            throw new NonExistentChatException(String.format(NON_EXISTING_CHAT_EXCEPTION_FORMAT, chatId));
        }
        var allWhere = JpaLinkService.selectFromChatsAndLinksWhereChatIdEq(chatId, entityManager);
        entityManager.createQuery(allWhere).getResultStream().forEach(entityManager::remove);
        entityManager.remove(chatEntity);
        entityManager.flush();
    }

    @Override
    public ListChatResponse getAllChats() throws DTOException {
        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<ChatEntity> criteria = builder.createQuery(ChatEntity.class);
        final Root<ChatEntity> chatRoot = criteria.from(ChatEntity.class);
        final CriteriaQuery<ChatEntity> all = criteria.select(chatRoot);

        final ChatResponse[] responses = entityManager.createQuery(all)
            .getResultStream()
            .map(chatEntity -> new ChatResponse(chatEntity.getId()))
            .toArray(ChatResponse[]::new);

        return new ListChatResponse(responses, responses.length);
    }

    @Override
    public ListChatResponse getAllChats(Long linkId) throws DTOException {
        validateId(linkId);
        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<ChatsAndLinksEntity> criteria = builder.createQuery(ChatsAndLinksEntity.class);
        final Root<ChatsAndLinksEntity> chatsAndLinksEntityRoot = criteria.from(ChatsAndLinksEntity.class);
        final CriteriaQuery<ChatsAndLinksEntity> allWhere = criteria.select(chatsAndLinksEntityRoot)
            .where(builder.equal(chatsAndLinksEntityRoot.get(KEY).get("linkId"), linkId));

        final ChatResponse[] responses = entityManager.createQuery(allWhere)
            .getResultStream()
            .map(chatEntity -> new ChatResponse(chatEntity.getChat().getId()))
            .toArray(ChatResponse[]::new);

        return new ListChatResponse(responses, responses.length);
    }

    @Override
    public ChatResponse getChat(Long chatId) throws DTOException {
        validateId(chatId);
        final ChatEntity chatEntity = entityManager.find(ChatEntity.class, chatId);
        if (chatEntity == null) {
            throw new NonExistentChatException(String.format(NON_EXISTING_CHAT_EXCEPTION_FORMAT, chatId));
        }
        return new ChatResponse(chatEntity.getId());
    }

    @Override
    public LinkAliasResponse getLinkAlias(Long chatId, Long linkId) throws DTOException {
        validateId(chatId);
        validateId(linkId);
        final var key = new ChatsAndLinksEntity.ChatsAndLinksPK(chatId, linkId);
        final ChatsAndLinksEntity chatsAndLinksEntity = entityManager.find(ChatsAndLinksEntity.class, key);
        if (chatsAndLinksEntity == null) {
            throw new WrongParametersException(String.format(LINK_IS_NOT_TRACKED_BY_CHAT_FORMAT, linkId, chatId));
        }
        return new LinkAliasResponse(chatsAndLinksEntity.getAlias());
    }
}
