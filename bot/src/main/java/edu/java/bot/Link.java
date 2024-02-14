package edu.java.bot;

import lombok.Getter;
import java.net.URI;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Link {
    @Getter private final String alias;
    @Getter private final URI uri;
    private final String markdownFormat;
    private static final Pattern PATTERN = Pattern.compile(TelegramBotComponent.LINK_PARSE_REGEX);

    public Link(final String alias, final String uri) {
        this.alias = alias;
        this.uri = URI.create(uri);
        this.markdownFormat = String.format(TelegramBotComponent.LINK_MARKDOWN_FORMAT, alias, uri);
    }

    public static Optional<Link> parse(final String toParse) {
        Matcher matcher = PATTERN.matcher(toParse);
        if (matcher.find()) {
            return Optional.of(new Link(matcher.group(1), matcher.group(2)));
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return markdownFormat;
    }
}
