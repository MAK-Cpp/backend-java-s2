/*
 * This file is generated by jOOQ.
 */

package edu.java.scrapper.domain.jooq.tables.pojos;

import jakarta.validation.constraints.Size;
import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
public class Links implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long linkId;
    private String uri;
    private LocalDateTime lastUpdate;

    public Links() {
    }

    public Links(Links value) {
        this.linkId = value.linkId;
        this.uri = value.uri;
        this.lastUpdate = value.lastUpdate;
    }

    @ConstructorProperties({"linkId", "uri", "lastUpdate"})
    public Links(
        @Nullable Long linkId,
        @NotNull String uri,
        @Nullable LocalDateTime lastUpdate
    ) {
        this.linkId = linkId;
        this.uri = uri;
        this.lastUpdate = lastUpdate;
    }

    /**
     * Getter for <code>LINKS.LINK_ID</code>.
     */
    @Nullable
    public Long getLinkId() {
        return this.linkId;
    }

    /**
     * Setter for <code>LINKS.LINK_ID</code>.
     */
    public void setLinkId(@Nullable Long linkId) {
        this.linkId = linkId;
    }

    /**
     * Getter for <code>LINKS.URI</code>.
     */
    @jakarta.validation.constraints.NotNull
    @Size(max = 1000000000)
    @NotNull
    public String getUri() {
        return this.uri;
    }

    /**
     * Setter for <code>LINKS.URI</code>.
     */
    public void setUri(@NotNull String uri) {
        this.uri = uri;
    }

    /**
     * Getter for <code>LINKS.LAST_UPDATE</code>.
     */
    @Nullable
    public LocalDateTime getLastUpdate() {
        return this.lastUpdate;
    }

    /**
     * Setter for <code>LINKS.LAST_UPDATE</code>.
     */
    public void setLastUpdate(@Nullable LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Links other = (Links) obj;
        if (this.linkId == null) {
            if (other.linkId != null) {
                return false;
            }
        } else if (!this.linkId.equals(other.linkId)) {
            return false;
        }
        if (this.uri == null) {
            if (other.uri != null) {
                return false;
            }
        } else if (!this.uri.equals(other.uri)) {
            return false;
        }
        if (this.lastUpdate == null) {
            if (other.lastUpdate != null) {
                return false;
            }
        } else if (!this.lastUpdate.equals(other.lastUpdate)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.linkId == null) ? 0 : this.linkId.hashCode());
        result = prime * result + ((this.uri == null) ? 0 : this.uri.hashCode());
        result = prime * result + ((this.lastUpdate == null) ? 0 : this.lastUpdate.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Links (");

        sb.append(linkId);
        sb.append(", ").append(uri);
        sb.append(", ").append(lastUpdate);

        sb.append(")");
        return sb.toString();
    }
}
