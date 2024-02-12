package edu.java.bot;

public record Command(String name, String description, CommandFunction function) {
    public static final Command START = new Command("start", "register a user", CommandFunction.START);
    public static final Command HELP = new Command("help", "display commands list", CommandFunction.HELP);
    public static final Command TRACK = new Command("track", "start tracking link(s)", CommandFunction.TRACK);
    public static final Command UNTRACK = new Command("untrack", "stop tracking link", CommandFunction.UNTRACK);
    public static final Command LIST = new Command("list", "show list of tracked links", CommandFunction.LIST);
}
