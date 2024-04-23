package edu.java.scrapper.repository.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chats_and_links")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatsAndLinksEntity {
    @EmbeddedId
    private ChatsAndLinksPK key;

    @Column(name = "alias", nullable = false)
    private String alias;

    @ManyToOne
    @MapsId("chatId")
    @JoinColumn(name = "chat_id")
    private ChatEntity chat;

    @ManyToOne
    @MapsId("linkId")
    @JoinColumn(name = "link_id")
    private LinkEntity link;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatsAndLinksPK implements Serializable {
        @Column(name = "chat_id")
        private Long chatId;

        @Column(name = "link_id")
        private Long linkId;
    }
}
