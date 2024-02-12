package edu.java.bot;

import lombok.Getter;
import lombok.Setter;
import java.util.HashMap;

@Getter
public class User {
    private final HashMap<String, String> links = new HashMap<>();
    @Setter
    private CommandFunction waitingFunction = CommandFunction.END;
}
