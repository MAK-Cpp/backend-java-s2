package edu.java.bot;

import jakarta.validation.constraints.NotEmpty;
import org.jetbrains.annotations.NotNull;

public record Command(@NotEmpty String name, @NotEmpty String description, @NotNull CommandFunction function) {
    public static final Command START = new Command("start", "register a user", CommandFunction.START);
    public static final Command HELP = new Command("help", "display commands list", CommandFunction.HELP);
    public static final Command TRACK = new Command("track", "start tracking link(s)", CommandFunction.TRACK);
    public static final Command UNTRACK = new Command("untrack", "stop tracking link", CommandFunction.UNTRACK);
    public static final Command LIST = new Command("list", "show list of tracked links", CommandFunction.LIST);
}
