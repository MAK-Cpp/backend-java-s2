package edu.java.scrapper.validator.stackoverflow;

import edu.java.scrapper.client.stackoverflow.StackOverflowClient;
import edu.java.scrapper.validator.AbstractLinkValidator;
import edu.java.scrapper.validator.ValidatorKey;

public abstract class AbstractStackOverflowLinkValidator extends AbstractLinkValidator {
    protected static final ValidatorKey ID = new ValidatorKey("id", "\\d+");
    protected static final ValidatorKey TITLE = new ValidatorKey("title", "[a-zA-Z0-9-]+");

    protected final StackOverflowClient stackOverflowClient;

    public AbstractStackOverflowLinkValidator(StackOverflowClient stackOverflowClient) {
        this.stackOverflowClient = stackOverflowClient;
    }
}
