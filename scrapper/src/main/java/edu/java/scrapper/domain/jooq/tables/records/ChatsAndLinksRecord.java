/*
 * This file is generated by jOOQ.
 */

package edu.java.scrapper.domain.jooq.tables.records;

import edu.java.scrapper.domain.jooq.tables.ChatsAndLinks;
import jakarta.validation.constraints.Size;
import java.beans.ConstructorProperties;
import javax.annotation.processing.Generated;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record2;
import org.jooq.impl.UpdatableRecordImpl;

/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "https://www.jooq.org",
        "jOOQ version:3.19.7"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({"all", "unchecked", "rawtypes", "this-escape"})
public class ChatsAndLinksRecord extends UpdatableRecordImpl<ChatsAndLinksRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>CHATS_AND_LINKS.CHAT_ID</code>.
     */
    public void setChatId(@NotNull Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>CHATS_AND_LINKS.CHAT_ID</code>.
     */
    @jakarta.validation.constraints.NotNull
    @NotNull
    public Long getChatId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>CHATS_AND_LINKS.LINK_ID</code>.
     */
    public void setLinkId(@NotNull Long value) {
        set(1, value);
    }

    /**
     * Getter for <code>CHATS_AND_LINKS.LINK_ID</code>.
     */
    @jakarta.validation.constraints.NotNull
    @NotNull
    public Long getLinkId() {
        return (Long) get(1);
    }

    /**
     * Setter for <code>CHATS_AND_LINKS.ALIAS</code>.
     */
    public void setAlias(@NotNull String value) {
        set(2, value);
    }

    /**
     * Getter for <code>CHATS_AND_LINKS.ALIAS</code>.
     */
    @jakarta.validation.constraints.NotNull
    @Size(max = 1000000000)
    @NotNull
    public String getAlias() {
        return (String) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    @NotNull
    public Record2<Long, Long> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ChatsAndLinksRecord
     */
    public ChatsAndLinksRecord() {
        super(ChatsAndLinks.CHATS_AND_LINKS);
    }

    /**
     * Create a detached, initialised ChatsAndLinksRecord
     */
    @ConstructorProperties({"chatId", "linkId", "alias"})
    public ChatsAndLinksRecord(@NotNull Long chatId, @NotNull Long linkId, @NotNull String alias) {
        super(ChatsAndLinks.CHATS_AND_LINKS);

        setChatId(chatId);
        setLinkId(linkId);
        setAlias(alias);
        resetChangedOnNotNull();
    }

    /**
     * Create a detached, initialised ChatsAndLinksRecord
     */
    public ChatsAndLinksRecord(edu.java.scrapper.domain.jooq.tables.pojos.ChatsAndLinks value) {
        super(ChatsAndLinks.CHATS_AND_LINKS);

        if (value != null) {
            setChatId(value.getChatId());
            setLinkId(value.getLinkId());
            setAlias(value.getAlias());
            resetChangedOnNotNull();
        }
    }
}
