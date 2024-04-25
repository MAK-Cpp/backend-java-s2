/*
 * This file is generated by jOOQ.
 */

package edu.java.scrapper.domain.jooq;

import edu.java.scrapper.domain.jooq.tables.Chats;
import edu.java.scrapper.domain.jooq.tables.ChatsAndLinks;
import edu.java.scrapper.domain.jooq.tables.Links;
import edu.java.scrapper.domain.jooq.tables.records.ChatsAndLinksRecord;
import edu.java.scrapper.domain.jooq.tables.records.ChatsRecord;
import edu.java.scrapper.domain.jooq.tables.records.LinksRecord;
import javax.annotation.processing.Generated;
import org.jooq.ForeignKey;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;

/**
 * A class modelling foreign key relationships and constraints of tables in the
 * default schema.
 */
@Generated(
    value = {
        "https://www.jooq.org",
        "jOOQ version:3.19.7"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({"all", "unchecked", "rawtypes", "this-escape"})
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<ChatsRecord> CONSTRAINT_3 =
        Internal.createUniqueKey(Chats.CHATS, DSL.name("CONSTRAINT_3"), new TableField[] {Chats.CHATS.CHAT_ID}, true);
    public static final UniqueKey<ChatsAndLinksRecord> CHAT_ID_ALIAS_UNIQUE =
        Internal.createUniqueKey(ChatsAndLinks.CHATS_AND_LINKS,
            DSL.name("CHAT_ID_ALIAS_UNIQUE"),
            new TableField[] {ChatsAndLinks.CHATS_AND_LINKS.CHAT_ID, ChatsAndLinks.CHATS_AND_LINKS.ALIAS},
            true
        );
    public static final UniqueKey<ChatsAndLinksRecord> CONSTRAINT_D =
        Internal.createUniqueKey(ChatsAndLinks.CHATS_AND_LINKS,
            DSL.name("CONSTRAINT_D"),
            new TableField[] {ChatsAndLinks.CHATS_AND_LINKS.CHAT_ID, ChatsAndLinks.CHATS_AND_LINKS.LINK_ID},
            true
        );
    public static final UniqueKey<LinksRecord> CONSTRAINT_4 =
        Internal.createUniqueKey(Links.LINKS, DSL.name("CONSTRAINT_4"), new TableField[] {Links.LINKS.LINK_ID}, true);
    public static final UniqueKey<LinksRecord> CONSTRAINT_45 =
        Internal.createUniqueKey(Links.LINKS, DSL.name("CONSTRAINT_45"), new TableField[] {Links.LINKS.URI}, true);

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<ChatsAndLinksRecord, ChatsRecord> CONSTRAINT_D5 = Internal.createForeignKey(
        ChatsAndLinks.CHATS_AND_LINKS,
        DSL.name("CONSTRAINT_D5"),
        new TableField[] {ChatsAndLinks.CHATS_AND_LINKS.CHAT_ID},
        Keys.CONSTRAINT_3,
        new TableField[] {Chats.CHATS.CHAT_ID},
        true
    );
    public static final ForeignKey<ChatsAndLinksRecord, LinksRecord> CONSTRAINT_D54 = Internal.createForeignKey(
        ChatsAndLinks.CHATS_AND_LINKS,
        DSL.name("CONSTRAINT_D54"),
        new TableField[] {ChatsAndLinks.CHATS_AND_LINKS.LINK_ID},
        Keys.CONSTRAINT_4,
        new TableField[] {Links.LINKS.LINK_ID},
        true
    );
}