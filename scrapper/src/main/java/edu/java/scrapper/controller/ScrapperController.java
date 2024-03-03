package edu.java.scrapper.controller;

import edu.java.scrapper.request.AddLinkRequest;
import edu.java.scrapper.request.RemoveLinkRequest;
import edu.java.scrapper.response.LinkResponse;
import edu.java.scrapper.service.ScrapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScrapperController {
    private final ScrapperService scrapperService;
    private static final String CHAT_REGISTER_SUCCESS = "Чат зарегистрирован";
    private static final String CHAT_DELETE_SUCCESS = "Чат успешно удалён";

    @Autowired
    public ScrapperController(ScrapperService scrapperService) {
        this.scrapperService = scrapperService;
    }

    @PostMapping("/tg-chat/{id}")
    public ResponseEntity<String> registerChat(@PathVariable int id) {
        scrapperService.registerChat(id);
        return ResponseEntity.ok(CHAT_REGISTER_SUCCESS);
    }

    @DeleteMapping("/tg-chat/{id}")
    public ResponseEntity<String> deleteChat(@PathVariable int id) {
        scrapperService.deleteChat(id);
        return ResponseEntity.ok(CHAT_DELETE_SUCCESS);
    }

    @GetMapping("/links")
    public ResponseEntity<LinkResponse[]> getAllTrackingLinks(@RequestParam int tgChatId) {
        return ResponseEntity.ok(scrapperService.getAllLinks(tgChatId));
    }

    @PostMapping("/links")
    public ResponseEntity<LinkResponse> addLinkToTracking(
        @RequestBody AddLinkRequest request, @RequestParam int tgChatId
    ) {
        return ResponseEntity.ok(scrapperService.addLink(tgChatId, request.getLink()));
    }

    @DeleteMapping("/links")
    public ResponseEntity<LinkResponse> removeLinkFromTracking(
        @RequestBody RemoveLinkRequest request, @RequestParam int tgChatId
    ) {
        return ResponseEntity.ok(scrapperService.removeLink(tgChatId, request.getLink()));
    }
}
