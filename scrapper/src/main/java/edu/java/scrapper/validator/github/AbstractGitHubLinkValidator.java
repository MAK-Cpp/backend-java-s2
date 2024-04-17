package edu.java.scrapper.validator.github;

import edu.java.scrapper.client.github.GithubClient;
import edu.java.scrapper.validator.AbstractLinkValidator;
import edu.java.scrapper.validator.ValidatorKey;

public abstract class AbstractGitHubLinkValidator extends AbstractLinkValidator {
    public static final String NAME_REGEX = "[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*";
    public static final String NUMBER_REGEX = "\\d+";
    public static final ValidatorKey OWNER = new ValidatorKey("owner", NAME_REGEX);
    public static final ValidatorKey REPO = new ValidatorKey("repo", NAME_REGEX);
    public static final ValidatorKey PULL_NUMBER = new ValidatorKey("pull_number", NUMBER_REGEX);
    public static final ValidatorKey ISSUE_NUMBER = new ValidatorKey("issue_number", NUMBER_REGEX);

    protected final GithubClient githubClient;

    public AbstractGitHubLinkValidator(GithubClient githubClient) {
        this.githubClient = githubClient;
    }
}
