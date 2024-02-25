package edu.java.client;

import edu.java.response.AnswerResponse;
import reactor.core.publisher.Mono;

public interface StackOverflowClient {
    Mono<AnswerResponse> getQuestionAnswers(int questionId);
}
