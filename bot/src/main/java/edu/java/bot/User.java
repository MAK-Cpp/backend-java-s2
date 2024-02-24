package edu.java.bot;

import edu.java.bot.commands.CommandFunction;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.Getter;
import lombok.Setter;
import static edu.java.bot.TelegramBotComponent.maybe;

public class User {
    private final ConcurrentMap<String, Link> links = new ConcurrentHashMap<>();
    @Setter @Getter private CommandFunction waitingFunction = CommandFunction.END;

    public User(Link... linksArray) {
        Arrays.stream(linksArray).forEach(link -> links.put(link.getAlias(), link));
    }

    public Collection<Link> allLinks() {
        return links.values();
    }

    public Optional<Link> addLink(final Link link) {
        return maybe(links.put(link.getAlias(), link));
    }

    public Optional<Link> getLink(final String alias) {
        return maybe(links.get(alias));
    }

    public Optional<Link> removeLink(final String alias) {
        return maybe(links.remove(alias));
    }

    public boolean containsLink(final String alias) {
        return links.containsKey(alias);
    }

    public boolean hasNoLinks() {
        return links.isEmpty();
    }

    public Set<String> aliasSet() {
        return links.keySet();
    }
}
