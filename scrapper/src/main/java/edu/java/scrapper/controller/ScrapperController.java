package edu.java.scrapper.controller;

import edu.java.dto.exception.LinkNotFoundException;
import edu.java.dto.exception.NonExistentChatException;
import edu.java.dto.exception.WrongParametersException;
import edu.java.dto.request.AddLinkRequest;
import edu.java.dto.request.RemoveLinkRequest;
import edu.java.dto.response.ApiErrorResponse;
import edu.java.dto.response.ChatResponse;
import edu.java.dto.response.ListUserLinkResponse;
import edu.java.dto.response.UserLinkResponse;
import edu.java.scrapper.service.ChatService;
import edu.java.scrapper.service.LinkService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@OpenAPIDefinition(info = @Info(
    title = "Scrapper API",
    version = "1.0.0",
    contact = @Contact(name = "Maxim Primakov", email = "spartmenik@gmail.com")
))
public class ScrapperController {
    private final LinkService linkService;
    private final ChatService chatService;

    @Autowired
    public ScrapperController(LinkService linkService, ChatService chatService) {
        this.linkService = linkService;
        this.chatService = chatService;
    }

    @PostMapping("/tg-chat/{id}")
    @Operation(summary = "Зарегистрировать чат", responses = {
        @ApiResponse(responseCode = "200",
                     description = "Чат зарегистрирован"),
        @ApiResponse(responseCode = "400",
                     description = "Некорректные параметры запроса",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> registerChat(@PathVariable long id) throws WrongParametersException {
        chatService.registerChat(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tg-chat/{id}")
    @Operation(summary = "Удалить чат", responses = {
        @ApiResponse(responseCode = "200",
                     description = "Чат успешно удалён"),
        @ApiResponse(responseCode = "400",
                     description = "Некорректные параметры запроса",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404",
                     description = "Чат не существует",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteChat(@PathVariable long id)
        throws WrongParametersException, NonExistentChatException {
        chatService.deleteChat(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tg-chat/{id}")
    @Operation(summary = "Получить данные о чате", responses = {
        @ApiResponse(responseCode = "200",
                     description = "Чат успешно получен",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = ChatResponse.class))),
        @ApiResponse(responseCode = "400",
                     description = "Некорректные параметры запроса",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404",
                     description = "Чат не существует",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ChatResponse> getChat(@PathVariable long id)
        throws WrongParametersException, NonExistentChatException {
        return ResponseEntity.ok(chatService.getChat(id));
    }

    @GetMapping("/links")
    @Operation(summary = "Получить все отслеживаемые ссылки", responses = {
        @ApiResponse(responseCode = "200",
                     description = "Ссылки успешно получены",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = ListUserLinkResponse.class))),
        @ApiResponse(responseCode = "400",
                     description = "Некорректные параметры запроса",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404",
                     description = "Чат не существует",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ListUserLinkResponse> getAllTrackingLinks(@RequestHeader long tgChatId)
        throws WrongParametersException, NonExistentChatException {
        return ResponseEntity.ok(linkService.getAllLinks(tgChatId));
    }

    @PostMapping("/links")
    @Operation(summary = "Добавить отслеживание ссылки", responses = {
        @ApiResponse(responseCode = "200",
                     description = "Ссылка успешно добавлена",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = UserLinkResponse.class))),
        @ApiResponse(responseCode = "400",
                     description = "Некорректные параметры запроса",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404",
                     description = "Чат не существует",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<UserLinkResponse> addLinkToTracking(
        @RequestBody AddLinkRequest request, @RequestHeader long tgChatId
    ) throws WrongParametersException, NonExistentChatException {
        return ResponseEntity
            .ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(linkService.addLink(tgChatId, request.getLink(), request.getAlias()));
    }

    @DeleteMapping("/links")
    @Operation(summary = "Убрать отслеживание ссылки", responses = {
        @ApiResponse(responseCode = "200",
                     description = "Ссылка успешно убрана",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = UserLinkResponse.class))),
        @ApiResponse(responseCode = "400",
                     description = "Некорректные параметры запроса",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404",
                     description = "Ссылка или чат не существуют",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<UserLinkResponse> removeLinkFromTracking(
        @RequestBody RemoveLinkRequest request, @RequestHeader long tgChatId
    ) throws WrongParametersException, NonExistentChatException, LinkNotFoundException {
        return ResponseEntity
            .ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(linkService.removeLink(tgChatId, request.getAlias()));
    }

    @GetMapping("/link")
    @Operation(summary = "Получить ссылку по id чата и ее синониму", responses = {
        @ApiResponse(responseCode = "200",
                     description = "Ссылка успешно получена",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = UserLinkResponse.class))),
        @ApiResponse(responseCode = "400",
                     description = "Некорректные параметры запроса",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404",
                     description = "Ссылка или чат не существуют",
                     content = @Content(mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<UserLinkResponse> getLinkByChatIdAndAlias(
        @RequestHeader long tgChatId, @RequestHeader String alias
    ) {
        return ResponseEntity
            .ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(linkService.getLink(tgChatId, alias));
    }
}
