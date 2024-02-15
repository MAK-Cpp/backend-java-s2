package edu.java.bot;

import edu.java.bot.commands.CommandFunction;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

public class User {
    private final Map<String, Link> links = new HashMap<>();
    @Setter @Getter private CommandFunction waitingFunction = CommandFunction.END;

    public Collection<Link> allLinks() {
        return links.values();
    }

    public Link addLink(final Link link) {
        return links.put(link.getAlias(), link);
    }

    public Link getLink(final String alias) {
        return links.get(alias);
    }

    public Link removeLink(final String alias) {
        return links.remove(alias);
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
