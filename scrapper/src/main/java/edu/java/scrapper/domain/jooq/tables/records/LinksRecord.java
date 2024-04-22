/*
 * This file is generated by jOOQ.
 */

package edu.java.scrapper.domain.jooq.tables.records;

import edu.java.scrapper.domain.jooq.tables.Links;
import jakarta.validation.constraints.Size;
import java.beans.ConstructorProperties;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
public class LinksRecord extends UpdatableRecordImpl<LinksRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>LINKS.LINK_ID</code>.
     */
    public void setLinkId(@Nullable Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>LINKS.LINK_ID</code>.
     */
    @Nullable
    public Long getLinkId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>LINKS.URI</code>.
     */
    public void setUri(@NotNull String value) {
        set(1, value);
    }

    /**
     * Getter for <code>LINKS.URI</code>.
     */
    @jakarta.validation.constraints.NotNull
    @Size(max = 1000000000)
    @NotNull
    public String getUri() {
        return (String) get(1);
    }

    /**
     * Setter for <code>LINKS.LAST_UPDATE</code>.
     */
    public void setLastUpdate(@Nullable LocalDateTime value) {
        set(2, value);
    }

    /**
     * Getter for <code>LINKS.LAST_UPDATE</code>.
     */
    @Nullable
    public LocalDateTime getLastUpdate() {
        return (LocalDateTime) get(2);
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
     * Create a detached LinksRecord
     */
    public LinksRecord() {
        super(Links.LINKS);
    }

    /**
     * Create a detached, initialised LinksRecord
     */
    @ConstructorProperties({"linkId", "uri", "lastUpdate"})
    public LinksRecord(@Nullable Long linkId, @NotNull String uri, @Nullable LocalDateTime lastUpdate) {
        super(Links.LINKS);

        setLinkId(linkId);
        setUri(uri);
        setLastUpdate(lastUpdate);
        resetChangedOnNotNull();
    }

    /**
     * Create a detached, initialised LinksRecord
     */
    public LinksRecord(edu.java.scrapper.domain.jooq.tables.pojos.Links value) {
        super(Links.LINKS);

        if (value != null) {
            setLinkId(value.getLinkId());
            setUri(value.getUri());
            setLastUpdate(value.getLastUpdate());
            resetChangedOnNotNull();
        }
    }
}
