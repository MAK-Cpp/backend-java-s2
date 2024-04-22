/*
 * This file is generated by jOOQ.
 */

package edu.java.scrapper.domain.jooq.tables.records;

import edu.java.scrapper.domain.jooq.tables.Chats;
import java.beans.ConstructorProperties;
import javax.annotation.processing.Generated;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record1;
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
public class ChatsRecord extends UpdatableRecordImpl<ChatsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>CHATS.CHAT_ID</code>.
     */
    public void setChatId(@NotNull Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>CHATS.CHAT_ID</code>.
     */
    @jakarta.validation.constraints.NotNull
    @NotNull
    public Long getChatId() {
        return (Long) get(0);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    @NotNull
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ChatsRecord
     */
    public ChatsRecord() {
        super(Chats.CHATS);
    }

    /**
     * Create a detached, initialised ChatsRecord
     */
    @ConstructorProperties({"chatId"})
    public ChatsRecord(@NotNull Long chatId) {
        super(Chats.CHATS);

        setChatId(chatId);
        resetChangedOnNotNull();
    }

    /**
     * Create a detached, initialised ChatsRecord
     */
    public ChatsRecord(edu.java.scrapper.domain.jooq.tables.pojos.Chats value) {
        super(Chats.CHATS);

        if (value != null) {
            setChatId(value.getChatId());
            resetChangedOnNotNull();
        }
    }
}
