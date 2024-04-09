package edu.java.scrapper.client;

import edu.java.scrapper.response.stackoverflow.AnswerResponse;
import reactor.core.publisher.Mono;

public interface StackOverflowClient {
    Mono<AnswerResponse> getQuestionAnswers(String questionId);
}
