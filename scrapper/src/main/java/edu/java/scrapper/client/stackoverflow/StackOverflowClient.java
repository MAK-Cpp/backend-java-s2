package edu.java.scrapper.client.stackoverflow;

import edu.java.scrapper.client.ExternalServiceClient;
import edu.java.scrapper.response.stackoverflow.AnswerResponse;

public interface StackOverflowClient extends ExternalServiceClient {
    AnswerResponse getQuestionAnswers(String id);
}
