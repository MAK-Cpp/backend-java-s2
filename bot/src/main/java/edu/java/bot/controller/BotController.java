package edu.java.bot.controller;

import edu.java.bot.request.LinkUpdateRequest;
import edu.java.bot.service.BotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BotController {
    private final BotService botService;
    private static final String UPDATE_PROCESSED = "Обновление обработано";

    @Autowired
    public BotController(BotService botService) {
        this.botService = botService;
    }

    @PostMapping("/updates")
    public ResponseEntity<String> sendUpdate(@RequestBody LinkUpdateRequest request) {
        botService.updateLink(request);
        return ResponseEntity.ok(UPDATE_PROCESSED);
    }
}
