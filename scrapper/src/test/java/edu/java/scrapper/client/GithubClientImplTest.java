package edu.java.scrapper.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import edu.java.configuration.HttpClientConfig;
import edu.java.scrapper.client.github.GithubClient;
import edu.java.scrapper.client.github.GithubClientImpl;
import edu.java.scrapper.response.Response;
import edu.java.scrapper.response.github.CommitResponse;
import edu.java.scrapper.response.github.IssueCommentResponse;
import edu.java.scrapper.response.github.IssueResponse;
import edu.java.scrapper.response.github.PullRequestResponse;
import edu.java.test.client.ClientTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;

public class GithubClientImplTest extends ClientTest {
    private WireMockServer wireMockServer;
    private static final int HTTP_ENDPOINT_PORT = getPort();
    private static final String URL = "http://localhost:" + HTTP_ENDPOINT_PORT;
    private static final GithubClient GITHUB_CLIENT = new GithubClientImpl(WebClient.builder(), URL, RETRY);

    @BeforeEach
    public void beforeEach() {
        wireMockServer = new WireMockServer(HTTP_ENDPOINT_PORT);
        wireMockServer.start();
        configureFor("localhost", HTTP_ENDPOINT_PORT);
    }

    @AfterEach
    public void afterEach() {
        wireMockServer.stop();
    }

    private <T extends Response> void testCommand(
        String owner,
        String repo,
        GithubClientCommands command,
        String body,
        List<T> result
    ) {
        List<? extends Response> output = switch (command) {
            case PULLS -> {
                MappingBuilder builder = get("/repos/" + owner + "/" + repo + "/pulls");
                stubFor(builder.willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(body)));
                yield GITHUB_CLIENT.getPullRequests(owner, repo);
            }
            case ISSUES -> {
                MappingBuilder builder = get("/repos/" + owner + "/" + repo + "/issues");
                stubFor(builder.willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(body)));
                yield GITHUB_CLIENT.getIssues(owner, repo);
            }
        };
        assertThat(output).isEqualTo(result);
    }

    private <T extends Response> void testCommand(
        String owner,
        String repo,
        String number,
        GithubClientCommands command,
        String body,
        List<T> result
    ) {
        List<? extends Response> output = switch (command) {
            case PULLS -> {
                MappingBuilder builder = get("/repos/" + owner + "/" + repo + "/pulls/" + number + "/commits");
                stubFor(builder.willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(body)));
                yield GITHUB_CLIENT.getListCommitsOnPullRequest(owner, repo, number);
            }
            case ISSUES -> {
                MappingBuilder builder = get("/repos/" + owner + "/" + repo + "/issues/" + number + "/comments");
                stubFor(builder.willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(body)));
                yield GITHUB_CLIENT.getListIssueComments(owner, repo, number);
            }
        };
        assertThat(output).isEqualTo(result);
    }

    @ParameterizedTest
    @MethodSource
    void testGetPullRequests(String owner, String repo, String body, List<PullRequestResponse> result) {
        testCommand(owner, repo, GithubClientCommands.PULLS, body, result);
    }

    @ParameterizedTest
    @MethodSource
    void testGetIssues(String owner, String repo, String body, List<IssueResponse> result) {
        testCommand(owner, repo, GithubClientCommands.ISSUES, body, result);
    }

    @ParameterizedTest
    @MethodSource
    void testGetListCommitsOnPullRequest(
        String owner,
        String repo,
        String number,
        String body,
        List<CommitResponse> result
    ) {
        testCommand(owner, repo, number, GithubClientCommands.PULLS, body, result);
    }

    @ParameterizedTest
    @MethodSource
    void testGetListIssueComments(String owner, String repo, String number, String body, List<IssueResponse> result) {
        testCommand(owner, repo, number, GithubClientCommands.ISSUES, body, result);
    }

    @ParameterizedTest
    @MethodSource("testRetryStream")
    public void testRetryGetPullRequests(
        HttpClientConfig httpClientConfig,
        int serverNotWorkingDuration,
        boolean enoughTime,
        HttpStatus failStatus
    ) {
        final String owner = "octocat";
        final String repo = "Hello-World";
        final String body = OCTOCAT_HELLO_WORLD_GET_PULL_REQUESTS_RESPONSE;
        final GithubClient githubClient = new GithubClientImpl(WebClient.builder(), URL, httpClientConfig.retry());
        testRetry(
            wireMockServer,
            get("/repos/" + owner + "/" + repo + "/pulls"),
            response -> response.withHeader("Content-Type", "application/json").withBody(body),
            serverNotWorkingDuration,
            enoughTime,
            httpClientConfig.codes().contains(failStatus),
            () -> githubClient.getPullRequests(owner, repo),
            failStatus
        );
    }

    @ParameterizedTest
    @MethodSource("testRetryStream")
    public void testRetryGetIssues(
        HttpClientConfig httpClientConfig,
        int serverNotWorkingDuration,
        boolean enoughTime,
        HttpStatus failStatus
    ) {
        final String owner = "octocat";
        final String repo = "Hello-World";
        final String body = OCTOCAT_HELLO_WORLD_GET_ISSUES_RESPONSE;
        final GithubClient githubClient = new GithubClientImpl(WebClient.builder(), URL, httpClientConfig.retry());
        testRetry(
            wireMockServer,
            get("/repos/" + owner + "/" + repo + "/issues"),
            response -> response.withHeader("Content-Type", "application/json").withBody(body),
            serverNotWorkingDuration,
            enoughTime,
            httpClientConfig.codes().contains(failStatus),
            () -> githubClient.getIssues(owner, repo),
            failStatus
        );
    }

    @ParameterizedTest
    @MethodSource("testRetryStream")
    public void testRetryGetListCommitsOnPullRequest(
        HttpClientConfig httpClientConfig,
        int serverNotWorkingDuration,
        boolean enoughTime,
        HttpStatus failStatus
    ) {
        final String owner = "MAK-Cpp";
        final String repo = "backend-java-s2";
        final String number = "6";
        final String body = MAK_CPP_BACKEND_JAVA_S2_6_GET_LIST_COMMITS_ON_PULL_REQUEST_RESPONSE;
        final GithubClient githubClient = new GithubClientImpl(WebClient.builder(), URL, httpClientConfig.retry());
        testRetry(
            wireMockServer,
            get("/repos/" + owner + "/" + repo + "/pulls/" + number + "/commits"),
            response -> response.withHeader("Content-Type", "application/json").withBody(body),
            serverNotWorkingDuration,
            enoughTime,
            httpClientConfig.codes().contains(failStatus),
            () -> githubClient.getListCommitsOnPullRequest(owner, repo, number),
            failStatus
        );
    }

    @ParameterizedTest
    @MethodSource("testRetryStream")
    public void testRetryGetListIssueComments(
        HttpClientConfig httpClientConfig,
        int serverNotWorkingDuration,
        boolean enoughTime,
        HttpStatus failStatus
    ) {
        final String owner = "testcontainers";
        final String repo = "testcontainers-java";
        final String number = "8338";
        final String body = TESTCONTAINERS_TESTCONTAINER_JAVA_8338_GET_LIST_ISSUE_COMMENTS_RESPONSE;
        final GithubClient githubClient = new GithubClientImpl(WebClient.builder(), URL, httpClientConfig.retry());
        testRetry(
            wireMockServer,
            get("/repos/" + owner + "/" + repo + "/issues/" + number + "/comments"),
            response -> response.withHeader("Content-Type", "application/json").withBody(body),
            serverNotWorkingDuration,
            enoughTime,
            httpClientConfig.codes().contains(failStatus),
            () -> githubClient.getListIssueComments(owner, repo, number),
            failStatus
        );
    }

    public static Stream<Arguments> testGetPullRequests() {
        return Stream.of(
            Arguments.of(
                "octocat",
                "Hello-World",
                OCTOCAT_HELLO_WORLD_GET_PULL_REQUESTS_RESPONSE,
                List.of(
                    new PullRequestResponse(
                        "https://github.com/octocat/Hello-World/pull/2988",
                        "open",
                        "Create codeql.yml",
                        new PullRequestResponse.User("didar72ahmadi", "User"),
                        "com.google.android.permission \r\n",
                        2988,
                        OffsetDateTime.parse("2024-02-23T12:43:58Z"),
                        OffsetDateTime.parse("2024-02-23T12:43:58Z"),
                        null,
                        null
                    )
                )
            )
        );
    }

    public static Stream<Arguments> testGetIssues() {
        return Stream.of(
            Arguments.of(
                "octocat",
                "Hello-World",
                OCTOCAT_HELLO_WORLD_GET_ISSUES_RESPONSE,
                List.of(
                    new IssueResponse(
                        2988,
                        "Create codeql.yml",
                        new IssueResponse.User("didar72ahmadi", "User"),
                        "https://github.com/octocat/Hello-World/pull/2988",
                        "open",
                        OffsetDateTime.parse("2024-02-23T12:43:58Z"),
                        OffsetDateTime.parse("2024-02-23T12:43:58Z"),
                        null,
                        "com.google.android.permission \r\n"
                    )
                )
            )
        );
    }

    public static Stream<Arguments> testGetListCommitsOnPullRequest() {
        return Stream.of(
            Arguments.of(
                "MAK-Cpp",
                "backend-java-s2",
                "6",
                MAK_CPP_BACKEND_JAVA_S2_6_GET_LIST_COMMITS_ON_PULL_REQUEST_RESPONSE,
                List.of(
                    new CommitResponse(
                        new CommitResponse.Commit(
                            new CommitResponse.Author(
                                "Maxim Primakov",
                                "spartmenik@gmail.com",
                                OffsetDateTime.parse("2024-03-08T11:42:02Z")
                            ),
                            new CommitResponse.Committer(
                                "Maxim Primakov",
                                "spartmenik@gmail.com",
                                OffsetDateTime.parse("2024-04-07T16:24:08Z")
                            ),
                            "hw4 init commit"
                        )
                    ),
                    new CommitResponse(
                        new CommitResponse.Commit(
                            new CommitResponse.Author(
                                "Maxim Primakov",
                                "spartmenik@gmail.com",
                                OffsetDateTime.parse("2024-03-08T14:47:13Z")
                            ),
                            new CommitResponse.Committer(
                                "Maxim Primakov",
                                "spartmenik@gmail.com",
                                OffsetDateTime.parse("2024-04-07T16:24:08Z")
                            ),
                            "written tasks No. 1-3"
                        )
                    ),
                    new CommitResponse(
                        new CommitResponse.Commit(
                            new CommitResponse.Author(
                                "Maxim Primakov",
                                "spartmenik@gmail.com",
                                OffsetDateTime.parse("2024-03-09T12:09:04Z")
                            ),
                            new CommitResponse.Committer(
                                "Maxim Primakov",
                                "spartmenik@gmail.com",
                                OffsetDateTime.parse("2024-04-07T16:24:08Z")
                            ),
                            "written task No. 4"
                        )
                    )
                )
            )
        );
    }

    public static Stream<Arguments> testGetListIssueComments() {
        return Stream.of(
            Arguments.of(
                "testcontainers",
                "testcontainers-java",
                "8338",
                TESTCONTAINERS_TESTCONTAINER_JAVA_8338_GET_LIST_ISSUE_COMMENTS_RESPONSE,
                List.of(
                    new IssueCommentResponse(
                        new IssueCommentResponse.User("marcelstoer", "User"),
                        OffsetDateTime.parse("2024-02-22T09:53:55Z"),
                        OffsetDateTime.parse("2024-02-22T09:53:55Z"),
                        "Anyone coming across this, please follow the discussion at #8354. The `commons-compress` dependency won't be updated here for now."
                    ),
                    new IssueCommentResponse(
                        new IssueCommentResponse.User("hailuand", "User"),
                        OffsetDateTime.parse("2024-03-12T21:53:58Z"),
                        OffsetDateTime.parse("2024-03-12T21:53:58Z"),
                        "Apache have released patch version 1.26.1 of commons-compress last week that may address this?\r\n\r\n> [COMPRESS-659:  TarArchiveOutputStream should use Commons IO Charsets instead of Commons Codec Charsets.](https://github.com/apache/commons-compress/blob/master/RELEASE-NOTES.txt#L25)\r\n\r\nI was able to successfully upgrade the commons-compress version in a project of mine to 1.26.1 that was previously failing on 1.26.0 with:\r\n```java\r\njava.lang.NoClassDefFoundError: org/apache/commons/codec/Charsets\r\n\r\n\tat org.apache.commons.compress.archivers.tar.TarArchiveOutputStream.<init>(TarArchiveOutputStream.java:212)\r\n\tat org.apache.commons.compress.archivers.tar.TarArchiveOutputStream.<init>(TarArchiveOutputStream.java:157)\r\n\tat org.apache.commons.compress.archivers.tar.TarArchiveOutputStream.<init>(TarArchiveOutputStream.java:147)\r\n\tat org.testcontainers.containers.ContainerState.copyFileToContainer(ContainerState.java:350)\r\n\tat org.testcontainers.containers.ContainerState.copyFileToContainer(ContainerState.java:331)\r\n\tat java.base/java.util.LinkedHashMap.forEach(LinkedHashMap.java:986)\r\n\tat org.testcontainers.containers.GenericContainer.tryStart(GenericContainer.java:441)\r\n```"
                    ),
                    new IssueCommentResponse(
                        new IssueCommentResponse.User("blommish", "User"),
                        OffsetDateTime.parse("2024-04-07T10:35:03Z"),
                        OffsetDateTime.parse("2024-04-07T10:35:03Z"),
                        "Do I understand this correctly, it will probably not be updated before spring has updated it to 1.26.1? "
                    )
                )
            )
        );
    }

    public static final String OCTOCAT_HELLO_WORLD_GET_PULL_REQUESTS_RESPONSE =
        """
            [
                {
                    "url": "https://api.github.com/repos/octocat/Hello-World/pulls/2988",
                    "id": 1740663394,
                    "node_id": "PR_kwDOABPHjc5nwGpi",
                    "html_url": "https://github.com/octocat/Hello-World/pull/2988",
                    "diff_url": "https://github.com/octocat/Hello-World/pull/2988.diff",
                    "patch_url": "https://github.com/octocat/Hello-World/pull/2988.patch",
                    "issue_url": "https://api.github.com/repos/octocat/Hello-World/issues/2988",
                    "number": 2988,
                    "state": "open",
                    "locked": false,
                    "title": "Create codeql.yml",
                    "user": {
                        "login": "didar72ahmadi",
                        "id": 159153880,
                        "node_id": "U_kgDOCXx-2A",
                        "avatar_url": "https://avatars.githubusercontent.com/u/159153880?v=4",
                        "gravatar_id": "",
                        "url": "https://api.github.com/users/didar72ahmadi",
                        "html_url": "https://github.com/didar72ahmadi",
                        "followers_url": "https://api.github.com/users/didar72ahmadi/followers",
                        "following_url": "https://api.github.com/users/didar72ahmadi/following{/other_user}",
                        "gists_url": "https://api.github.com/users/didar72ahmadi/gists{/gist_id}",
                        "starred_url": "https://api.github.com/users/didar72ahmadi/starred{/owner}{/repo}",
                        "subscriptions_url": "https://api.github.com/users/didar72ahmadi/subscriptions",
                        "organizations_url": "https://api.github.com/users/didar72ahmadi/orgs",
                        "repos_url": "https://api.github.com/users/didar72ahmadi/repos",
                        "events_url": "https://api.github.com/users/didar72ahmadi/events{/privacy}",
                        "received_events_url": "https://api.github.com/users/didar72ahmadi/received_events",
                        "type": "User",
                        "site_admin": false
                    },
                    "body": "com.google.android.permission \\r\\n",
                    "created_at": "2024-02-23T12:43:58Z",
                    "updated_at": "2024-02-23T12:43:58Z",
                    "closed_at": null,
                    "merged_at": null,
                    "merge_commit_sha": "57905d6b33372540f02c9c87db6d27fa0063991f",
                    "assignee": null,
                    "assignees": [

                    ],
                    "requested_reviewers": [

                    ],
                    "requested_teams": [

                    ],
                    "labels": [

                    ],
                    "milestone": null,
                    "draft": false,
                    "commits_url": "https://api.github.com/repos/octocat/Hello-World/pulls/2988/commits",
                    "review_comments_url": "https://api.github.com/repos/octocat/Hello-World/pulls/2988/comments",
                    "review_comment_url": "https://api.github.com/repos/octocat/Hello-World/pulls/comments{/number}",
                    "comments_url": "https://api.github.com/repos/octocat/Hello-World/issues/2988/comments",
                    "statuses_url": "https://api.github.com/repos/octocat/Hello-World/statuses/772e2d7bd9fce08b2a2a7a1a9502d89776752a4e",
                    "head": {
                        "label": "didar72ahmadi:master",
                        "ref": "master",
                        "sha": "772e2d7bd9fce08b2a2a7a1a9502d89776752a4e",
                        "user": {
                            "login": "didar72ahmadi",
                            "id": 159153880,
                            "node_id": "U_kgDOCXx-2A",
                            "avatar_url": "https://avatars.githubusercontent.com/u/159153880?v=4",
                            "gravatar_id": "",
                            "url": "https://api.github.com/users/didar72ahmadi",
                            "html_url": "https://github.com/didar72ahmadi",
                            "followers_url": "https://api.github.com/users/didar72ahmadi/followers",
                            "following_url": "https://api.github.com/users/didar72ahmadi/following{/other_user}",
                            "gists_url": "https://api.github.com/users/didar72ahmadi/gists{/gist_id}",
                            "starred_url": "https://api.github.com/users/didar72ahmadi/starred{/owner}{/repo}",
                            "subscriptions_url": "https://api.github.com/users/didar72ahmadi/subscriptions",
                            "organizations_url": "https://api.github.com/users/didar72ahmadi/orgs",
                            "repos_url": "https://api.github.com/users/didar72ahmadi/repos",
                            "events_url": "https://api.github.com/users/didar72ahmadi/events{/privacy}",
                            "received_events_url": "https://api.github.com/users/didar72ahmadi/received_events",
                            "type": "User",
                            "site_admin": false
                        },
                        "repo": {
                            "id": 754781609,
                            "node_id": "R_kgDOLP0NqQ",
                            "name": "Hello-World",
                            "full_name": "didar72ahmadi/Hello-World",
                            "private": false,
                            "owner": {
                                "login": "didar72ahmadi",
                                "id": 159153880,
                                "node_id": "U_kgDOCXx-2A",
                                "avatar_url": "https://avatars.githubusercontent.com/u/159153880?v=4",
                                "gravatar_id": "",
                                "url": "https://api.github.com/users/didar72ahmadi",
                                "html_url": "https://github.com/didar72ahmadi",
                                "followers_url": "https://api.github.com/users/didar72ahmadi/followers",
                                "following_url": "https://api.github.com/users/didar72ahmadi/following{/other_user}",
                                "gists_url": "https://api.github.com/users/didar72ahmadi/gists{/gist_id}",
                                "starred_url": "https://api.github.com/users/didar72ahmadi/starred{/owner}{/repo}",
                                "subscriptions_url": "https://api.github.com/users/didar72ahmadi/subscriptions",
                                "organizations_url": "https://api.github.com/users/didar72ahmadi/orgs",
                                "repos_url": "https://api.github.com/users/didar72ahmadi/repos",
                                "events_url": "https://api.github.com/users/didar72ahmadi/events{/privacy}",
                                "received_events_url": "https://api.github.com/users/didar72ahmadi/received_events",
                                "type": "User",
                                "site_admin": false
                            },
                            "html_url": "https://github.com/didar72ahmadi/Hello-World",
                            "description": "My first repository on GitHub!",
                            "fork": true,
                            "url": "https://api.github.com/repos/didar72ahmadi/Hello-World",
                            "forks_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/forks",
                            "keys_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/keys{/key_id}",
                            "collaborators_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/collaborators{/collaborator}",
                            "teams_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/teams",
                            "hooks_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/hooks",
                            "issue_events_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/issues/events{/number}",
                            "events_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/events",
                            "assignees_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/assignees{/user}",
                            "branches_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/branches{/branch}",
                            "tags_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/tags",
                            "blobs_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/git/blobs{/sha}",
                            "git_tags_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/git/tags{/sha}",
                            "git_refs_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/git/refs{/sha}",
                            "trees_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/git/trees{/sha}",
                            "statuses_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/statuses/{sha}",
                            "languages_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/languages",
                            "stargazers_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/stargazers",
                            "contributors_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/contributors",
                            "subscribers_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/subscribers",
                            "subscription_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/subscription",
                            "commits_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/commits{/sha}",
                            "git_commits_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/git/commits{/sha}",
                            "comments_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/comments{/number}",
                            "issue_comment_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/issues/comments{/number}",
                            "contents_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/contents/{+path}",
                            "compare_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/compare/{base}...{head}",
                            "merges_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/merges",
                            "archive_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/{archive_format}{/ref}",
                            "downloads_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/downloads",
                            "issues_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/issues{/number}",
                            "pulls_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/pulls{/number}",
                            "milestones_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/milestones{/number}",
                            "notifications_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/notifications{?since,all,participating}",
                            "labels_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/labels{/name}",
                            "releases_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/releases{/id}",
                            "deployments_url": "https://api.github.com/repos/didar72ahmadi/Hello-World/deployments",
                            "created_at": "2024-02-08T18:55:06Z",
                            "updated_at": "2024-02-08T18:55:06Z",
                            "pushed_at": "2024-02-08T18:59:28Z",
                            "git_url": "git://github.com/didar72ahmadi/Hello-World.git",
                            "ssh_url": "git@github.com:didar72ahmadi/Hello-World.git",
                            "clone_url": "https://github.com/didar72ahmadi/Hello-World.git",
                            "svn_url": "https://github.com/didar72ahmadi/Hello-World",
                            "homepage": "",
                            "size": 4,
                            "stargazers_count": 0,
                            "watchers_count": 0,
                            "language": null,
                            "has_issues": false,
                            "has_projects": true,
                            "has_downloads": true,
                            "has_wiki": true,
                            "has_pages": false,
                            "has_discussions": false,
                            "forks_count": 0,
                            "mirror_url": null,
                            "archived": false,
                            "disabled": false,
                            "open_issues_count": 0,
                            "license": null,
                            "allow_forking": true,
                            "is_template": false,
                            "web_commit_signoff_required": false,
                            "topics": [

                            ],
                            "visibility": "public",
                            "forks": 0,
                            "open_issues": 0,
                            "watchers": 0,
                            "default_branch": "master"
                        }
                    },
                    "base": {
                        "label": "octocat:master",
                        "ref": "master",
                        "sha": "7fd1a60b01f91b314f59955a4e4d4e80d8edf11d",
                        "user": {
                            "login": "octocat",
                            "id": 583231,
                            "node_id": "MDQ6VXNlcjU4MzIzMQ==",
                            "avatar_url": "https://avatars.githubusercontent.com/u/583231?v=4",
                            "gravatar_id": "",
                            "url": "https://api.github.com/users/octocat",
                            "html_url": "https://github.com/octocat",
                            "followers_url": "https://api.github.com/users/octocat/followers",
                            "following_url": "https://api.github.com/users/octocat/following{/other_user}",
                            "gists_url": "https://api.github.com/users/octocat/gists{/gist_id}",
                            "starred_url": "https://api.github.com/users/octocat/starred{/owner}{/repo}",
                            "subscriptions_url": "https://api.github.com/users/octocat/subscriptions",
                            "organizations_url": "https://api.github.com/users/octocat/orgs",
                            "repos_url": "https://api.github.com/users/octocat/repos",
                            "events_url": "https://api.github.com/users/octocat/events{/privacy}",
                            "received_events_url": "https://api.github.com/users/octocat/received_events",
                            "type": "User",
                            "site_admin": false
                        },
                        "repo": {
                            "id": 1296269,
                            "node_id": "MDEwOlJlcG9zaXRvcnkxMjk2MjY5",
                            "name": "Hello-World",
                            "full_name": "octocat/Hello-World",
                            "private": false,
                            "owner": {
                                "login": "octocat",
                                "id": 583231,
                                "node_id": "MDQ6VXNlcjU4MzIzMQ==",
                                "avatar_url": "https://avatars.githubusercontent.com/u/583231?v=4",
                                "gravatar_id": "",
                                "url": "https://api.github.com/users/octocat",
                                "html_url": "https://github.com/octocat",
                                "followers_url": "https://api.github.com/users/octocat/followers",
                                "following_url": "https://api.github.com/users/octocat/following{/other_user}",
                                "gists_url": "https://api.github.com/users/octocat/gists{/gist_id}",
                                "starred_url": "https://api.github.com/users/octocat/starred{/owner}{/repo}",
                                "subscriptions_url": "https://api.github.com/users/octocat/subscriptions",
                                "organizations_url": "https://api.github.com/users/octocat/orgs",
                                "repos_url": "https://api.github.com/users/octocat/repos",
                                "events_url": "https://api.github.com/users/octocat/events{/privacy}",
                                "received_events_url": "https://api.github.com/users/octocat/received_events",
                                "type": "User",
                                "site_admin": false
                            },
                            "html_url": "https://github.com/octocat/Hello-World",
                            "description": "My first repository on GitHub!",
                            "fork": false,
                            "url": "https://api.github.com/repos/octocat/Hello-World",
                            "forks_url": "https://api.github.com/repos/octocat/Hello-World/forks",
                            "keys_url": "https://api.github.com/repos/octocat/Hello-World/keys{/key_id}",
                            "collaborators_url": "https://api.github.com/repos/octocat/Hello-World/collaborators{/collaborator}",
                            "teams_url": "https://api.github.com/repos/octocat/Hello-World/teams",
                            "hooks_url": "https://api.github.com/repos/octocat/Hello-World/hooks",
                            "issue_events_url": "https://api.github.com/repos/octocat/Hello-World/issues/events{/number}",
                            "events_url": "https://api.github.com/repos/octocat/Hello-World/events",
                            "assignees_url": "https://api.github.com/repos/octocat/Hello-World/assignees{/user}",
                            "branches_url": "https://api.github.com/repos/octocat/Hello-World/branches{/branch}",
                            "tags_url": "https://api.github.com/repos/octocat/Hello-World/tags",
                            "blobs_url": "https://api.github.com/repos/octocat/Hello-World/git/blobs{/sha}",
                            "git_tags_url": "https://api.github.com/repos/octocat/Hello-World/git/tags{/sha}",
                            "git_refs_url": "https://api.github.com/repos/octocat/Hello-World/git/refs{/sha}",
                            "trees_url": "https://api.github.com/repos/octocat/Hello-World/git/trees{/sha}",
                            "statuses_url": "https://api.github.com/repos/octocat/Hello-World/statuses/{sha}",
                            "languages_url": "https://api.github.com/repos/octocat/Hello-World/languages",
                            "stargazers_url": "https://api.github.com/repos/octocat/Hello-World/stargazers",
                            "contributors_url": "https://api.github.com/repos/octocat/Hello-World/contributors",
                            "subscribers_url": "https://api.github.com/repos/octocat/Hello-World/subscribers",
                            "subscription_url": "https://api.github.com/repos/octocat/Hello-World/subscription",
                            "commits_url": "https://api.github.com/repos/octocat/Hello-World/commits{/sha}",
                            "git_commits_url": "https://api.github.com/repos/octocat/Hello-World/git/commits{/sha}",
                            "comments_url": "https://api.github.com/repos/octocat/Hello-World/comments{/number}",
                            "issue_comment_url": "https://api.github.com/repos/octocat/Hello-World/issues/comments{/number}",
                            "contents_url": "https://api.github.com/repos/octocat/Hello-World/contents/{+path}",
                            "compare_url": "https://api.github.com/repos/octocat/Hello-World/compare/{base}...{head}",
                            "merges_url": "https://api.github.com/repos/octocat/Hello-World/merges",
                            "archive_url": "https://api.github.com/repos/octocat/Hello-World/{archive_format}{/ref}",
                            "downloads_url": "https://api.github.com/repos/octocat/Hello-World/downloads",
                            "issues_url": "https://api.github.com/repos/octocat/Hello-World/issues{/number}",
                            "pulls_url": "https://api.github.com/repos/octocat/Hello-World/pulls{/number}",
                            "milestones_url": "https://api.github.com/repos/octocat/Hello-World/milestones{/number}",
                            "notifications_url": "https://api.github.com/repos/octocat/Hello-World/notifications{?since,all,participating}",
                            "labels_url": "https://api.github.com/repos/octocat/Hello-World/labels{/name}",
                            "releases_url": "https://api.github.com/repos/octocat/Hello-World/releases{/id}",
                            "deployments_url": "https://api.github.com/repos/octocat/Hello-World/deployments",
                            "created_at": "2011-01-26T19:01:12Z",
                            "updated_at": "2024-02-25T13:57:13Z",
                            "pushed_at": "2024-02-23T12:43:59Z",
                            "git_url": "git://github.com/octocat/Hello-World.git",
                            "ssh_url": "git@github.com:octocat/Hello-World.git",
                            "clone_url": "https://github.com/octocat/Hello-World.git",
                            "svn_url": "https://github.com/octocat/Hello-World",
                            "homepage": "",
                            "size": 1,
                            "stargazers_count": 2459,
                            "watchers_count": 2459,
                            "language": null,
                            "has_issues": true,
                            "has_projects": true,
                            "has_downloads": true,
                            "has_wiki": true,
                            "has_pages": false,
                            "has_discussions": false,
                            "forks_count": 2153,
                            "mirror_url": null,
                            "archived": false,
                            "disabled": false,
                            "open_issues_count": 1277,
                            "license": null,
                            "allow_forking": true,
                            "is_template": false,
                            "web_commit_signoff_required": false,
                            "topics": [

                            ],
                            "visibility": "public",
                            "forks": 2153,
                            "open_issues": 1277,
                            "watchers": 2459,
                            "default_branch": "master"
                        }
                    },
                    "_links": {
                        "self": {
                            "href": "https://api.github.com/repos/octocat/Hello-World/pulls/2988"
                        },
                        "html": {
                            "href": "https://github.com/octocat/Hello-World/pull/2988"
                        },
                        "issue": {
                            "href": "https://api.github.com/repos/octocat/Hello-World/issues/2988"
                        },
                        "comments": {
                            "href": "https://api.github.com/repos/octocat/Hello-World/issues/2988/comments"
                        },
                        "review_comments": {
                            "href": "https://api.github.com/repos/octocat/Hello-World/pulls/2988/comments"
                        },
                        "review_comment": {
                            "href": "https://api.github.com/repos/octocat/Hello-World/pulls/comments{/number}"
                        },
                        "commits": {
                            "href": "https://api.github.com/repos/octocat/Hello-World/pulls/2988/commits"
                        },
                        "statuses": {
                            "href": "https://api.github.com/repos/octocat/Hello-World/statuses/772e2d7bd9fce08b2a2a7a1a9502d89776752a4e"
                        }
                    },
                    "author_association": "NONE",
                    "auto_merge": null,
                    "active_lock_reason": null
                }
            ]""";
    public static final String OCTOCAT_HELLO_WORLD_GET_ISSUES_RESPONSE =
        """
            [
                {
                    "url": "https://api.github.com/repos/octocat/Hello-World/issues/2988",
                    "repository_url": "https://api.github.com/repos/octocat/Hello-World",
                    "labels_url": "https://api.github.com/repos/octocat/Hello-World/issues/2988/labels{/name}",
                    "comments_url": "https://api.github.com/repos/octocat/Hello-World/issues/2988/comments",
                    "events_url": "https://api.github.com/repos/octocat/Hello-World/issues/2988/events",
                    "html_url": "https://github.com/octocat/Hello-World/pull/2988",
                    "id": 2151012041,
                    "node_id": "PR_kwDOABPHjc5nwGpi",
                    "number": 2988,
                    "title": "Create codeql.yml",
                    "user": {
                        "login": "didar72ahmadi",
                        "id": 159153880,
                        "node_id": "U_kgDOCXx-2A",
                        "avatar_url": "https://avatars.githubusercontent.com/u/159153880?v=4",
                        "gravatar_id": "",
                        "url": "https://api.github.com/users/didar72ahmadi",
                        "html_url": "https://github.com/didar72ahmadi",
                        "followers_url": "https://api.github.com/users/didar72ahmadi/followers",
                        "following_url": "https://api.github.com/users/didar72ahmadi/following{/other_user}",
                        "gists_url": "https://api.github.com/users/didar72ahmadi/gists{/gist_id}",
                        "starred_url": "https://api.github.com/users/didar72ahmadi/starred{/owner}{/repo}",
                        "subscriptions_url": "https://api.github.com/users/didar72ahmadi/subscriptions",
                        "organizations_url": "https://api.github.com/users/didar72ahmadi/orgs",
                        "repos_url": "https://api.github.com/users/didar72ahmadi/repos",
                        "events_url": "https://api.github.com/users/didar72ahmadi/events{/privacy}",
                        "received_events_url": "https://api.github.com/users/didar72ahmadi/received_events",
                        "type": "User",
                        "site_admin": false
                    },
                    "labels": [

                    ],
                    "state": "open",
                    "locked": false,
                    "assignee": null,
                    "assignees": [

                    ],
                    "milestone": null,
                    "comments": 0,
                    "created_at": "2024-02-23T12:43:58Z",
                    "updated_at": "2024-02-23T12:43:58Z",
                    "closed_at": null,
                    "author_association": "NONE",
                    "active_lock_reason": null,
                    "draft": false,
                    "pull_request": {
                        "url": "https://api.github.com/repos/octocat/Hello-World/pulls/2988",
                        "html_url": "https://github.com/octocat/Hello-World/pull/2988",
                        "diff_url": "https://github.com/octocat/Hello-World/pull/2988.diff",
                        "patch_url": "https://github.com/octocat/Hello-World/pull/2988.patch",
                        "merged_at": null
                    },
                    "body": "com.google.android.permission \\r\\n",
                    "reactions": {
                        "url": "https://api.github.com/repos/octocat/Hello-World/issues/2988/reactions",
                        "total_count": 0,
                        "+1": 0,
                        "-1": 0,
                        "laugh": 0,
                        "hooray": 0,
                        "confused": 0,
                        "heart": 0,
                        "rocket": 0,
                        "eyes": 0
                    },
                    "timeline_url": "https://api.github.com/repos/octocat/Hello-World/issues/2988/timeline",
                    "performed_via_github_app": null,
                    "state_reason": null
                }
            ]
            """;
    public static final String MAK_CPP_BACKEND_JAVA_S2_6_GET_LIST_COMMITS_ON_PULL_REQUEST_RESPONSE = """
        [
            {
                "sha": "55fc82e1f766bd43a02291e33b208e0dd59847db",
                "node_id": "C_kwDOLO2f3NoAKDU1ZmM4MmUxZjc2NmJkNDNhMDIyOTFlMzNiMjA4ZTBkZDU5ODQ3ZGI",
                "commit": {
                    "author": {
                        "name": "Maxim Primakov",
                        "email": "spartmenik@gmail.com",
                        "date": "2024-03-08T11:42:02Z"
                    },
                    "committer": {
                        "name": "Maxim Primakov",
                        "email": "spartmenik@gmail.com",
                        "date": "2024-04-07T16:24:08Z"
                    },
                    "message": "hw4 init commit",
                    "tree": {
                        "sha": "a18367b84fef4d8bdc2bd6d615c5ebcc2a090142",
                        "url": "https://api.github.com/repos/MAK-Cpp/backend-java-s2/git/trees/a18367b84fef4d8bdc2bd6d615c5ebcc2a090142"
                    },
                    "url": "https://api.github.com/repos/MAK-Cpp/backend-java-s2/git/commits/55fc82e1f766bd43a02291e33b208e0dd59847db",
                    "comment_count": 0,
                    "verification": {
                        "verified": false,
                        "reason": "unsigned",
                        "signature": null,
                        "payload": null
                    }
                },
                "url": "https://api.github.com/repos/MAK-Cpp/backend-java-s2/commits/55fc82e1f766bd43a02291e33b208e0dd59847db",
                "html_url": "https://github.com/MAK-Cpp/backend-java-s2/commit/55fc82e1f766bd43a02291e33b208e0dd59847db",
                "comments_url": "https://api.github.com/repos/MAK-Cpp/backend-java-s2/commits/55fc82e1f766bd43a02291e33b208e0dd59847db/comments",
                "author": {
                    "login": "MAK-Cpp",
                    "id": 75676696,
                    "node_id": "MDQ6VXNlcjc1Njc2Njk2",
                    "avatar_url": "https://avatars.githubusercontent.com/u/75676696?v=4",
                    "gravatar_id": "",
                    "url": "https://api.github.com/users/MAK-Cpp",
                    "html_url": "https://github.com/MAK-Cpp",
                    "followers_url": "https://api.github.com/users/MAK-Cpp/followers",
                    "following_url": "https://api.github.com/users/MAK-Cpp/following{/other_user}",
                    "gists_url": "https://api.github.com/users/MAK-Cpp/gists{/gist_id}",
                    "starred_url": "https://api.github.com/users/MAK-Cpp/starred{/owner}{/repo}",
                    "subscriptions_url": "https://api.github.com/users/MAK-Cpp/subscriptions",
                    "organizations_url": "https://api.github.com/users/MAK-Cpp/orgs",
                    "repos_url": "https://api.github.com/users/MAK-Cpp/repos",
                    "events_url": "https://api.github.com/users/MAK-Cpp/events{/privacy}",
                    "received_events_url": "https://api.github.com/users/MAK-Cpp/received_events",
                    "type": "User",
                    "site_admin": false
                },
                "committer": {
                    "login": "MAK-Cpp",
                    "id": 75676696,
                    "node_id": "MDQ6VXNlcjc1Njc2Njk2",
                    "avatar_url": "https://avatars.githubusercontent.com/u/75676696?v=4",
                    "gravatar_id": "",
                    "url": "https://api.github.com/users/MAK-Cpp",
                    "html_url": "https://github.com/MAK-Cpp",
                    "followers_url": "https://api.github.com/users/MAK-Cpp/followers",
                    "following_url": "https://api.github.com/users/MAK-Cpp/following{/other_user}",
                    "gists_url": "https://api.github.com/users/MAK-Cpp/gists{/gist_id}",
                    "starred_url": "https://api.github.com/users/MAK-Cpp/starred{/owner}{/repo}",
                    "subscriptions_url": "https://api.github.com/users/MAK-Cpp/subscriptions",
                    "organizations_url": "https://api.github.com/users/MAK-Cpp/orgs",
                    "repos_url": "https://api.github.com/users/MAK-Cpp/repos",
                    "events_url": "https://api.github.com/users/MAK-Cpp/events{/privacy}",
                    "received_events_url": "https://api.github.com/users/MAK-Cpp/received_events",
                    "type": "User",
                    "site_admin": false
                },
                "parents": [
                    {
                        "sha": "de43706948325d80beb0a9107af7e5b67f5dc9ec",
                        "url": "https://api.github.com/repos/MAK-Cpp/backend-java-s2/commits/de43706948325d80beb0a9107af7e5b67f5dc9ec",
                        "html_url": "https://github.com/MAK-Cpp/backend-java-s2/commit/de43706948325d80beb0a9107af7e5b67f5dc9ec"
                    }
                ]
            },
            {
                "sha": "e954e25349ef58b0e73fed4b03f01dec5939e59f",
                "node_id": "C_kwDOLO2f3NoAKGU5NTRlMjUzNDllZjU4YjBlNzNmZWQ0YjAzZjAxZGVjNTkzOWU1OWY",
                "commit": {
                    "author": {
                        "name": "Maxim Primakov",
                        "email": "spartmenik@gmail.com",
                        "date": "2024-03-08T14:47:13Z"
                    },
                    "committer": {
                        "name": "Maxim Primakov",
                        "email": "spartmenik@gmail.com",
                        "date": "2024-04-07T16:24:08Z"
                    },
                    "message": "written tasks No. 1-3",
                    "tree": {
                        "sha": "b3595657fa91a6c0b39d0d0a863cacc9ed255eee",
                        "url": "https://api.github.com/repos/MAK-Cpp/backend-java-s2/git/trees/b3595657fa91a6c0b39d0d0a863cacc9ed255eee"
                    },
                    "url": "https://api.github.com/repos/MAK-Cpp/backend-java-s2/git/commits/e954e25349ef58b0e73fed4b03f01dec5939e59f",
                    "comment_count": 0,
                    "verification": {
                        "verified": false,
                        "reason": "unsigned",
                        "signature": null,
                        "payload": null
                    }
                },
                "url": "https://api.github.com/repos/MAK-Cpp/backend-java-s2/commits/e954e25349ef58b0e73fed4b03f01dec5939e59f",
                "html_url": "https://github.com/MAK-Cpp/backend-java-s2/commit/e954e25349ef58b0e73fed4b03f01dec5939e59f",
                "comments_url": "https://api.github.com/repos/MAK-Cpp/backend-java-s2/commits/e954e25349ef58b0e73fed4b03f01dec5939e59f/comments",
                "author": {
                    "login": "MAK-Cpp",
                    "id": 75676696,
                    "node_id": "MDQ6VXNlcjc1Njc2Njk2",
                    "avatar_url": "https://avatars.githubusercontent.com/u/75676696?v=4",
                    "gravatar_id": "",
                    "url": "https://api.github.com/users/MAK-Cpp",
                    "html_url": "https://github.com/MAK-Cpp",
                    "followers_url": "https://api.github.com/users/MAK-Cpp/followers",
                    "following_url": "https://api.github.com/users/MAK-Cpp/following{/other_user}",
                    "gists_url": "https://api.github.com/users/MAK-Cpp/gists{/gist_id}",
                    "starred_url": "https://api.github.com/users/MAK-Cpp/starred{/owner}{/repo}",
                    "subscriptions_url": "https://api.github.com/users/MAK-Cpp/subscriptions",
                    "organizations_url": "https://api.github.com/users/MAK-Cpp/orgs",
                    "repos_url": "https://api.github.com/users/MAK-Cpp/repos",
                    "events_url": "https://api.github.com/users/MAK-Cpp/events{/privacy}",
                    "received_events_url": "https://api.github.com/users/MAK-Cpp/received_events",
                    "type": "User",
                    "site_admin": false
                },
                "committer": {
                    "login": "MAK-Cpp",
                    "id": 75676696,
                    "node_id": "MDQ6VXNlcjc1Njc2Njk2",
                    "avatar_url": "https://avatars.githubusercontent.com/u/75676696?v=4",
                    "gravatar_id": "",
                    "url": "https://api.github.com/users/MAK-Cpp",
                    "html_url": "https://github.com/MAK-Cpp",
                    "followers_url": "https://api.github.com/users/MAK-Cpp/followers",
                    "following_url": "https://api.github.com/users/MAK-Cpp/following{/other_user}",
                    "gists_url": "https://api.github.com/users/MAK-Cpp/gists{/gist_id}",
                    "starred_url": "https://api.github.com/users/MAK-Cpp/starred{/owner}{/repo}",
                    "subscriptions_url": "https://api.github.com/users/MAK-Cpp/subscriptions",
                    "organizations_url": "https://api.github.com/users/MAK-Cpp/orgs",
                    "repos_url": "https://api.github.com/users/MAK-Cpp/repos",
                    "events_url": "https://api.github.com/users/MAK-Cpp/events{/privacy}",
                    "received_events_url": "https://api.github.com/users/MAK-Cpp/received_events",
                    "type": "User",
                    "site_admin": false
                },
                "parents": [
                    {
                        "sha": "55fc82e1f766bd43a02291e33b208e0dd59847db",
                        "url": "https://api.github.com/repos/MAK-Cpp/backend-java-s2/commits/55fc82e1f766bd43a02291e33b208e0dd59847db",
                        "html_url": "https://github.com/MAK-Cpp/backend-java-s2/commit/55fc82e1f766bd43a02291e33b208e0dd59847db"
                    }
                ]
            },
            {
                "sha": "f3f539338bc981e0a09772f0dd1f3d801eb7596f",
                "node_id": "C_kwDOLO2f3NoAKGYzZjUzOTMzOGJjOTgxZTBhMDk3NzJmMGRkMWYzZDgwMWViNzU5NmY",
                "commit": {
                    "author": {
                        "name": "Maxim Primakov",
                        "email": "spartmenik@gmail.com",
                        "date": "2024-03-09T12:09:04Z"
                    },
                    "committer": {
                        "name": "Maxim Primakov",
                        "email": "spartmenik@gmail.com",
                        "date": "2024-04-07T16:24:08Z"
                    },
                    "message": "written task No. 4",
                    "tree": {
                        "sha": "fb431809ce61479c5005638d2ed2ced7e9f2ba67",
                        "url": "https://api.github.com/repos/MAK-Cpp/backend-java-s2/git/trees/fb431809ce61479c5005638d2ed2ced7e9f2ba67"
                    },
                    "url": "https://api.github.com/repos/MAK-Cpp/backend-java-s2/git/commits/f3f539338bc981e0a09772f0dd1f3d801eb7596f",
                    "comment_count": 0,
                    "verification": {
                        "verified": false,
                        "reason": "unsigned",
                        "signature": null,
                        "payload": null
                    }
                },
                "url": "https://api.github.com/repos/MAK-Cpp/backend-java-s2/commits/f3f539338bc981e0a09772f0dd1f3d801eb7596f",
                "html_url": "https://github.com/MAK-Cpp/backend-java-s2/commit/f3f539338bc981e0a09772f0dd1f3d801eb7596f",
                "comments_url": "https://api.github.com/repos/MAK-Cpp/backend-java-s2/commits/f3f539338bc981e0a09772f0dd1f3d801eb7596f/comments",
                "author": {
                    "login": "MAK-Cpp",
                    "id": 75676696,
                    "node_id": "MDQ6VXNlcjc1Njc2Njk2",
                    "avatar_url": "https://avatars.githubusercontent.com/u/75676696?v=4",
                    "gravatar_id": "",
                    "url": "https://api.github.com/users/MAK-Cpp",
                    "html_url": "https://github.com/MAK-Cpp",
                    "followers_url": "https://api.github.com/users/MAK-Cpp/followers",
                    "following_url": "https://api.github.com/users/MAK-Cpp/following{/other_user}",
                    "gists_url": "https://api.github.com/users/MAK-Cpp/gists{/gist_id}",
                    "starred_url": "https://api.github.com/users/MAK-Cpp/starred{/owner}{/repo}",
                    "subscriptions_url": "https://api.github.com/users/MAK-Cpp/subscriptions",
                    "organizations_url": "https://api.github.com/users/MAK-Cpp/orgs",
                    "repos_url": "https://api.github.com/users/MAK-Cpp/repos",
                    "events_url": "https://api.github.com/users/MAK-Cpp/events{/privacy}",
                    "received_events_url": "https://api.github.com/users/MAK-Cpp/received_events",
                    "type": "User",
                    "site_admin": false
                },
                "committer": {
                    "login": "MAK-Cpp",
                    "id": 75676696,
                    "node_id": "MDQ6VXNlcjc1Njc2Njk2",
                    "avatar_url": "https://avatars.githubusercontent.com/u/75676696?v=4",
                    "gravatar_id": "",
                    "url": "https://api.github.com/users/MAK-Cpp",
                    "html_url": "https://github.com/MAK-Cpp",
                    "followers_url": "https://api.github.com/users/MAK-Cpp/followers",
                    "following_url": "https://api.github.com/users/MAK-Cpp/following{/other_user}",
                    "gists_url": "https://api.github.com/users/MAK-Cpp/gists{/gist_id}",
                    "starred_url": "https://api.github.com/users/MAK-Cpp/starred{/owner}{/repo}",
                    "subscriptions_url": "https://api.github.com/users/MAK-Cpp/subscriptions",
                    "organizations_url": "https://api.github.com/users/MAK-Cpp/orgs",
                    "repos_url": "https://api.github.com/users/MAK-Cpp/repos",
                    "events_url": "https://api.github.com/users/MAK-Cpp/events{/privacy}",
                    "received_events_url": "https://api.github.com/users/MAK-Cpp/received_events",
                    "type": "User",
                    "site_admin": false
                },
                "parents": [
                    {
                        "sha": "e954e25349ef58b0e73fed4b03f01dec5939e59f",
                        "url": "https://api.github.com/repos/MAK-Cpp/backend-java-s2/commits/e954e25349ef58b0e73fed4b03f01dec5939e59f",
                        "html_url": "https://github.com/MAK-Cpp/backend-java-s2/commit/e954e25349ef58b0e73fed4b03f01dec5939e59f"
                    }
                ]
            }
        ]""";
    public static final String TESTCONTAINERS_TESTCONTAINER_JAVA_8338_GET_LIST_ISSUE_COMMENTS_RESPONSE = """
        [
            {
                "url": "https://api.github.com/repos/testcontainers/testcontainers-java/issues/comments/1959081453",
                "html_url": "https://github.com/testcontainers/testcontainers-java/issues/8338#issuecomment-1959081453",
                "issue_url": "https://api.github.com/repos/testcontainers/testcontainers-java/issues/8338",
                "id": 1959081453,
                "node_id": "IC_kwDOAgP_mc50xTXt",
                "user": {
                    "login": "marcelstoer",
                    "id": 624195,
                    "node_id": "MDQ6VXNlcjYyNDE5NQ==",
                    "avatar_url": "https://avatars.githubusercontent.com/u/624195?v=4",
                    "gravatar_id": "",
                    "url": "https://api.github.com/users/marcelstoer",
                    "html_url": "https://github.com/marcelstoer",
                    "followers_url": "https://api.github.com/users/marcelstoer/followers",
                    "following_url": "https://api.github.com/users/marcelstoer/following{/other_user}",
                    "gists_url": "https://api.github.com/users/marcelstoer/gists{/gist_id}",
                    "starred_url": "https://api.github.com/users/marcelstoer/starred{/owner}{/repo}",
                    "subscriptions_url": "https://api.github.com/users/marcelstoer/subscriptions",
                    "organizations_url": "https://api.github.com/users/marcelstoer/orgs",
                    "repos_url": "https://api.github.com/users/marcelstoer/repos",
                    "events_url": "https://api.github.com/users/marcelstoer/events{/privacy}",
                    "received_events_url": "https://api.github.com/users/marcelstoer/received_events",
                    "type": "User",
                    "site_admin": false
                },
                "created_at": "2024-02-22T09:53:55Z",
                "updated_at": "2024-02-22T09:53:55Z",
                "author_association": "NONE",
                "body": "Anyone coming across this, please follow the discussion at #8354. The `commons-compress` dependency won't be updated here for now.",
                "reactions": {
                    "url": "https://api.github.com/repos/testcontainers/testcontainers-java/issues/comments/1959081453/reactions",
                    "total_count": 2,
                    "+1": 0,
                    "-1": 0,
                    "laugh": 0,
                    "hooray": 0,
                    "confused": 2,
                    "heart": 0,
                    "rocket": 0,
                    "eyes": 0
                },
                "performed_via_github_app": null
            },
            {
                "url": "https://api.github.com/repos/testcontainers/testcontainers-java/issues/comments/1992649517",
                "html_url": "https://github.com/testcontainers/testcontainers-java/issues/8338#issuecomment-1992649517",
                "issue_url": "https://api.github.com/repos/testcontainers/testcontainers-java/issues/8338",
                "id": 1992649517,
                "node_id": "IC_kwDOAgP_mc52xWst",
                "user": {
                    "login": "hailuand",
                    "id": 6646502,
                    "node_id": "MDQ6VXNlcjY2NDY1MDI=",
                    "avatar_url": "https://avatars.githubusercontent.com/u/6646502?v=4",
                    "gravatar_id": "",
                    "url": "https://api.github.com/users/hailuand",
                    "html_url": "https://github.com/hailuand",
                    "followers_url": "https://api.github.com/users/hailuand/followers",
                    "following_url": "https://api.github.com/users/hailuand/following{/other_user}",
                    "gists_url": "https://api.github.com/users/hailuand/gists{/gist_id}",
                    "starred_url": "https://api.github.com/users/hailuand/starred{/owner}{/repo}",
                    "subscriptions_url": "https://api.github.com/users/hailuand/subscriptions",
                    "organizations_url": "https://api.github.com/users/hailuand/orgs",
                    "repos_url": "https://api.github.com/users/hailuand/repos",
                    "events_url": "https://api.github.com/users/hailuand/events{/privacy}",
                    "received_events_url": "https://api.github.com/users/hailuand/received_events",
                    "type": "User",
                    "site_admin": false
                },
                "created_at": "2024-03-12T21:53:58Z",
                "updated_at": "2024-03-12T21:53:58Z",
                "author_association": "NONE",
                "body": "Apache have released patch version 1.26.1 of commons-compress last week that may address this?\\r\\n\\r\\n> [COMPRESS-659:  TarArchiveOutputStream should use Commons IO Charsets instead of Commons Codec Charsets.](https://github.com/apache/commons-compress/blob/master/RELEASE-NOTES.txt#L25)\\r\\n\\r\\nI was able to successfully upgrade the commons-compress version in a project of mine to 1.26.1 that was previously failing on 1.26.0 with:\\r\\n```java\\r\\njava.lang.NoClassDefFoundError: org/apache/commons/codec/Charsets\\r\\n\\r\\n\\tat org.apache.commons.compress.archivers.tar.TarArchiveOutputStream.<init>(TarArchiveOutputStream.java:212)\\r\\n\\tat org.apache.commons.compress.archivers.tar.TarArchiveOutputStream.<init>(TarArchiveOutputStream.java:157)\\r\\n\\tat org.apache.commons.compress.archivers.tar.TarArchiveOutputStream.<init>(TarArchiveOutputStream.java:147)\\r\\n\\tat org.testcontainers.containers.ContainerState.copyFileToContainer(ContainerState.java:350)\\r\\n\\tat org.testcontainers.containers.ContainerState.copyFileToContainer(ContainerState.java:331)\\r\\n\\tat java.base/java.util.LinkedHashMap.forEach(LinkedHashMap.java:986)\\r\\n\\tat org.testcontainers.containers.GenericContainer.tryStart(GenericContainer.java:441)\\r\\n```",
                "reactions": {
                    "url": "https://api.github.com/repos/testcontainers/testcontainers-java/issues/comments/1992649517/reactions",
                    "total_count": 0,
                    "+1": 0,
                    "-1": 0,
                    "laugh": 0,
                    "hooray": 0,
                    "confused": 0,
                    "heart": 0,
                    "rocket": 0,
                    "eyes": 0
                },
                "performed_via_github_app": null
            },
            {
                "url": "https://api.github.com/repos/testcontainers/testcontainers-java/issues/comments/2041414862",
                "html_url": "https://github.com/testcontainers/testcontainers-java/issues/8338#issuecomment-2041414862",
                "issue_url": "https://api.github.com/repos/testcontainers/testcontainers-java/issues/8338",
                "id": 2041414862,
                "node_id": "IC_kwDOAgP_mc55rYTO",
                "user": {
                    "login": "blommish",
                    "id": 937168,
                    "node_id": "MDQ6VXNlcjkzNzE2OA==",
                    "avatar_url": "https://avatars.githubusercontent.com/u/937168?v=4",
                    "gravatar_id": "",
                    "url": "https://api.github.com/users/blommish",
                    "html_url": "https://github.com/blommish",
                    "followers_url": "https://api.github.com/users/blommish/followers",
                    "following_url": "https://api.github.com/users/blommish/following{/other_user}",
                    "gists_url": "https://api.github.com/users/blommish/gists{/gist_id}",
                    "starred_url": "https://api.github.com/users/blommish/starred{/owner}{/repo}",
                    "subscriptions_url": "https://api.github.com/users/blommish/subscriptions",
                    "organizations_url": "https://api.github.com/users/blommish/orgs",
                    "repos_url": "https://api.github.com/users/blommish/repos",
                    "events_url": "https://api.github.com/users/blommish/events{/privacy}",
                    "received_events_url": "https://api.github.com/users/blommish/received_events",
                    "type": "User",
                    "site_admin": false
                },
                "created_at": "2024-04-07T10:35:03Z",
                "updated_at": "2024-04-07T10:35:03Z",
                "author_association": "NONE",
                "body": "Do I understand this correctly, it will probably not be updated before spring has updated it to 1.26.1? ",
                "reactions": {
                    "url": "https://api.github.com/repos/testcontainers/testcontainers-java/issues/comments/2041414862/reactions",
                    "total_count": 0,
                    "+1": 0,
                    "-1": 0,
                    "laugh": 0,
                    "hooray": 0,
                    "confused": 0,
                    "heart": 0,
                    "rocket": 0,
                    "eyes": 0
                },
                "performed_via_github_app": null
            }
        ]""";

    private enum GithubClientCommands {
        PULLS, ISSUES
    }
}
