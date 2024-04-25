/*
 * This file is generated by jOOQ.
 */

package edu.java.scrapper.domain.jooq.tables;

import edu.java.scrapper.domain.jooq.DefaultSchema;
import edu.java.scrapper.domain.jooq.Keys;
import edu.java.scrapper.domain.jooq.tables.ChatsAndLinks.ChatsAndLinksPath;
import edu.java.scrapper.domain.jooq.tables.Links.LinksPath;
import edu.java.scrapper.domain.jooq.tables.records.ChatsRecord;
import java.util.Collection;
import javax.annotation.processing.Generated;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.InverseForeignKey;
import org.jooq.Name;
import org.jooq.Path;
import org.jooq.PlainSQL;
import org.jooq.QueryPart;
import org.jooq.Record;
import org.jooq.SQL;
import org.jooq.Schema;
import org.jooq.Select;
import org.jooq.Stringly;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

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
public class Chats extends TableImpl<ChatsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>CHATS</code>
     */
    public static final Chats CHATS = new Chats();

    /**
     * The class holding records for this type
     */
    @Override
    @NotNull
    public Class<ChatsRecord> getRecordType() {
        return ChatsRecord.class;
    }

    /**
     * The column <code>CHATS.CHAT_ID</code>.
     */
    public final TableField<ChatsRecord, Long> CHAT_ID =
        createField(DSL.name("CHAT_ID"), SQLDataType.BIGINT.nullable(false), this, "");

    private Chats(Name alias, Table<ChatsRecord> aliased) {
        this(alias, aliased, (Field<?>[]) null, null);
    }

    private Chats(Name alias, Table<ChatsRecord> aliased, Field<?>[] parameters, Condition where) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table(), where);
    }

    /**
     * Create an aliased <code>CHATS</code> table reference
     */
    public Chats(String alias) {
        this(DSL.name(alias), CHATS);
    }

    /**
     * Create an aliased <code>CHATS</code> table reference
     */
    public Chats(Name alias) {
        this(alias, CHATS);
    }

    /**
     * Create a <code>CHATS</code> table reference
     */
    public Chats() {
        this(DSL.name("CHATS"), null);
    }

    public <O extends Record> Chats(
        Table<O> path,
        ForeignKey<O, ChatsRecord> childPath,
        InverseForeignKey<O, ChatsRecord> parentPath
    ) {
        super(path, childPath, parentPath, CHATS);
    }

    /**
     * A subtype implementing {@link Path} for simplified path-based joins.
     */
    public static class ChatsPath extends Chats implements Path<ChatsRecord> {

        private static final long serialVersionUID = 1L;

        public <O extends Record> ChatsPath(
            Table<O> path,
            ForeignKey<O, ChatsRecord> childPath,
            InverseForeignKey<O, ChatsRecord> parentPath
        ) {
            super(path, childPath, parentPath);
        }

        private ChatsPath(Name alias, Table<ChatsRecord> aliased) {
            super(alias, aliased);
        }

        @Override
        public ChatsPath as(String alias) {
            return new ChatsPath(DSL.name(alias), this);
        }

        @Override
        public ChatsPath as(Name alias) {
            return new ChatsPath(alias, this);
        }

        @Override
        public ChatsPath as(Table<?> alias) {
            return new ChatsPath(alias.getQualifiedName(), this);
        }
    }

    @Override
    @Nullable
    public Schema getSchema() {
        return aliased() ? null : DefaultSchema.DEFAULT_SCHEMA;
    }

    @Override
    @NotNull
    public UniqueKey<ChatsRecord> getPrimaryKey() {
        return Keys.CONSTRAINT_3;
    }

    private transient ChatsAndLinksPath _chatsAndLinks;

    /**
     * Get the implicit to-many join path to the
     * <code>PUBLIC.CHATS_AND_LINKS</code> table
     */
    public ChatsAndLinksPath chatsAndLinks() {
        if (_chatsAndLinks == null) {
            _chatsAndLinks = new ChatsAndLinksPath(this, null, Keys.CONSTRAINT_D5.getInverseKey());
        }

        return _chatsAndLinks;
    }

    /**
     * Get the implicit many-to-many join path to the <code>PUBLIC.LINKS</code>
     * table
     */
    public LinksPath links() {
        return chatsAndLinks().links();
    }

    @Override
    @NotNull
    public Chats as(String alias) {
        return new Chats(DSL.name(alias), this);
    }

    @Override
    @NotNull
    public Chats as(Name alias) {
        return new Chats(alias, this);
    }

    @Override
    @NotNull
    public Chats as(Table<?> alias) {
        return new Chats(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    @NotNull
    public Chats rename(String name) {
        return new Chats(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    @NotNull
    public Chats rename(Name name) {
        return new Chats(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    @NotNull
    public Chats rename(Table<?> name) {
        return new Chats(name.getQualifiedName(), null);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @NotNull
    public Chats where(Condition condition) {
        return new Chats(getQualifiedName(), aliased() ? this : null, null, condition);
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @NotNull
    public Chats where(Collection<? extends Condition> conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @NotNull
    public Chats where(Condition... conditions) {
        return where(DSL.and(conditions));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @NotNull
    public Chats where(Field<Boolean> condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @NotNull
    @PlainSQL
    public Chats where(SQL condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @NotNull
    @PlainSQL
    public Chats where(@Stringly.SQL String condition) {
        return where(DSL.condition(condition));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @NotNull
    @PlainSQL
    public Chats where(@Stringly.SQL String condition, Object... binds) {
        return where(DSL.condition(condition, binds));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @NotNull
    @PlainSQL
    public Chats where(@Stringly.SQL String condition, QueryPart... parts) {
        return where(DSL.condition(condition, parts));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @NotNull
    public Chats whereExists(Select<?> select) {
        return where(DSL.exists(select));
    }

    /**
     * Create an inline derived table from this table
     */
    @Override
    @NotNull
    public Chats whereNotExists(Select<?> select) {
        return where(DSL.notExists(select));
    }
}