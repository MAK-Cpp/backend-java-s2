package edu.java.scrapper.client;

import edu.java.scrapper.response.AnswerResponse;
import reactor.core.publisher.Mono;

public interface StackOverflowClient {
    Mono<AnswerResponse> getQuestionAnswers(int questionId);
}
