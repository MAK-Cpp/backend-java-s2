package edu.java.scrapper.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import edu.java.scrapper.client.github.GithubClient;
import edu.java.scrapper.client.github.GithubClientImpl;
import edu.java.scrapper.response.Response;
import edu.java.scrapper.response.github.CommitResponse;
import edu.java.scrapper.response.github.IssueCommentResponse;
import edu.java.scrapper.response.github.IssueResponse;
import edu.java.scrapper.response.github.PullRequestResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class GithubClientImplTest {
    private WireMockServer wireMockServer;
    private static final int HTTP_ENDPOINT_PORT = 8123;
    private static final String URL = "http://localhost:" + HTTP_ENDPOINT_PORT;
    private static final GithubClient githubClient = new GithubClientImpl(WebClient.builder(), URL);

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
                yield githubClient.getPullRequests(owner, repo);
            }
            case ISSUES -> {
                MappingBuilder builder = get("/repos/" + owner + "/" + repo + "/issues");
                stubFor(builder.willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(body)));
                yield githubClient.getIssues(owner, repo);
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
                yield githubClient.getListCommitsOnPullRequest(owner, repo, number);
            }
            case ISSUES -> {
                MappingBuilder builder = get("/repos/" + owner + "/" + repo + "/issues/" + number + "/comments");
                stubFor(builder.willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(body)));
                yield githubClient.getListIssueComments(owner, repo, number);
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

    public static Stream<Arguments> testGetPullRequests() {
        return Stream.of(
            Arguments.of(
                "octocat",
                "Hello-World",
                "[\n" +
                    "    {\n" +
                    "        \"url\": \"https://api.github.com/repos/octocat/Hello-World/pulls/2988\",\n" +
                    "        \"id\": 1740663394,\n" +
                    "        \"node_id\": \"PR_kwDOABPHjc5nwGpi\",\n" +
                    "        \"html_url\": \"https://github.com/octocat/Hello-World/pull/2988\",\n" +
                    "        \"diff_url\": \"https://github.com/octocat/Hello-World/pull/2988.diff\",\n" +
                    "        \"patch_url\": \"https://github.com/octocat/Hello-World/pull/2988.patch\",\n" +
                    "        \"issue_url\": \"https://api.github.com/repos/octocat/Hello-World/issues/2988\",\n" +
                    "        \"number\": 2988,\n" +
                    "        \"state\": \"open\",\n" +
                    "        \"locked\": false,\n" +
                    "        \"title\": \"Create codeql.yml\",\n" +
                    "        \"user\": {\n" +
                    "            \"login\": \"didar72ahmadi\",\n" +
                    "            \"id\": 159153880,\n" +
                    "            \"node_id\": \"U_kgDOCXx-2A\",\n" +
                    "            \"avatar_url\": \"https://avatars.githubusercontent.com/u/159153880?v=4\",\n" +
                    "            \"gravatar_id\": \"\",\n" +
                    "            \"url\": \"https://api.github.com/users/didar72ahmadi\",\n" +
                    "            \"html_url\": \"https://github.com/didar72ahmadi\",\n" +
                    "            \"followers_url\": \"https://api.github.com/users/didar72ahmadi/followers\",\n" +
                    "            \"following_url\": \"https://api.github.com/users/didar72ahmadi/following{/other_user}\",\n" +
                    "            \"gists_url\": \"https://api.github.com/users/didar72ahmadi/gists{/gist_id}\",\n" +
                    "            \"starred_url\": \"https://api.github.com/users/didar72ahmadi/starred{/owner}{/repo}\",\n" +
                    "            \"subscriptions_url\": \"https://api.github.com/users/didar72ahmadi/subscriptions\",\n" +
                    "            \"organizations_url\": \"https://api.github.com/users/didar72ahmadi/orgs\",\n" +
                    "            \"repos_url\": \"https://api.github.com/users/didar72ahmadi/repos\",\n" +
                    "            \"events_url\": \"https://api.github.com/users/didar72ahmadi/events{/privacy}\",\n" +
                    "            \"received_events_url\": \"https://api.github.com/users/didar72ahmadi/received_events\",\n" +
                    "            \"type\": \"User\",\n" +
                    "            \"site_admin\": false\n" +
                    "        },\n" +
                    "        \"body\": \"com.google.android.permission \\r\\n\",\n" +
                    "        \"created_at\": \"2024-02-23T12:43:58Z\",\n" +
                    "        \"updated_at\": \"2024-02-23T12:43:58Z\",\n" +
                    "        \"closed_at\": null,\n" +
                    "        \"merged_at\": null,\n" +
                    "        \"merge_commit_sha\": \"57905d6b33372540f02c9c87db6d27fa0063991f\",\n" +
                    "        \"assignee\": null,\n" +
                    "        \"assignees\": [\n" +
                    "\n" +
                    "        ],\n" +
                    "        \"requested_reviewers\": [\n" +
                    "\n" +
                    "        ],\n" +
                    "        \"requested_teams\": [\n" +
                    "\n" +
                    "        ],\n" +
                    "        \"labels\": [\n" +
                    "\n" +
                    "        ],\n" +
                    "        \"milestone\": null,\n" +
                    "        \"draft\": false,\n" +
                    "        \"commits_url\": \"https://api.github.com/repos/octocat/Hello-World/pulls/2988/commits\",\n" +
                    "        \"review_comments_url\": \"https://api.github.com/repos/octocat/Hello-World/pulls/2988/comments\",\n" +
                    "        \"review_comment_url\": \"https://api.github.com/repos/octocat/Hello-World/pulls/comments{/number}\",\n" +
                    "        \"comments_url\": \"https://api.github.com/repos/octocat/Hello-World/issues/2988/comments\",\n" +
                    "        \"statuses_url\": \"https://api.github.com/repos/octocat/Hello-World/statuses/772e2d7bd9fce08b2a2a7a1a9502d89776752a4e\",\n" +
                    "        \"head\": {\n" +
                    "            \"label\": \"didar72ahmadi:master\",\n" +
                    "            \"ref\": \"master\",\n" +
                    "            \"sha\": \"772e2d7bd9fce08b2a2a7a1a9502d89776752a4e\",\n" +
                    "            \"user\": {\n" +
                    "                \"login\": \"didar72ahmadi\",\n" +
                    "                \"id\": 159153880,\n" +
                    "                \"node_id\": \"U_kgDOCXx-2A\",\n" +
                    "                \"avatar_url\": \"https://avatars.githubusercontent.com/u/159153880?v=4\",\n" +
                    "                \"gravatar_id\": \"\",\n" +
                    "                \"url\": \"https://api.github.com/users/didar72ahmadi\",\n" +
                    "                \"html_url\": \"https://github.com/didar72ahmadi\",\n" +
                    "                \"followers_url\": \"https://api.github.com/users/didar72ahmadi/followers\",\n" +
                    "                \"following_url\": \"https://api.github.com/users/didar72ahmadi/following{/other_user}\",\n" +
                    "                \"gists_url\": \"https://api.github.com/users/didar72ahmadi/gists{/gist_id}\",\n" +
                    "                \"starred_url\": \"https://api.github.com/users/didar72ahmadi/starred{/owner}{/repo}\",\n" +
                    "                \"subscriptions_url\": \"https://api.github.com/users/didar72ahmadi/subscriptions\",\n" +
                    "                \"organizations_url\": \"https://api.github.com/users/didar72ahmadi/orgs\",\n" +
                    "                \"repos_url\": \"https://api.github.com/users/didar72ahmadi/repos\",\n" +
                    "                \"events_url\": \"https://api.github.com/users/didar72ahmadi/events{/privacy}\",\n" +
                    "                \"received_events_url\": \"https://api.github.com/users/didar72ahmadi/received_events\",\n" +
                    "                \"type\": \"User\",\n" +
                    "                \"site_admin\": false\n" +
                    "            },\n" +
                    "            \"repo\": {\n" +
                    "                \"id\": 754781609,\n" +
                    "                \"node_id\": \"R_kgDOLP0NqQ\",\n" +
                    "                \"name\": \"Hello-World\",\n" +
                    "                \"full_name\": \"didar72ahmadi/Hello-World\",\n" +
                    "                \"private\": false,\n" +
                    "                \"owner\": {\n" +
                    "                    \"login\": \"didar72ahmadi\",\n" +
                    "                    \"id\": 159153880,\n" +
                    "                    \"node_id\": \"U_kgDOCXx-2A\",\n" +
                    "                    \"avatar_url\": \"https://avatars.githubusercontent.com/u/159153880?v=4\",\n" +
                    "                    \"gravatar_id\": \"\",\n" +
                    "                    \"url\": \"https://api.github.com/users/didar72ahmadi\",\n" +
                    "                    \"html_url\": \"https://github.com/didar72ahmadi\",\n" +
                    "                    \"followers_url\": \"https://api.github.com/users/didar72ahmadi/followers\",\n" +
                    "                    \"following_url\": \"https://api.github.com/users/didar72ahmadi/following{/other_user}\",\n" +
                    "                    \"gists_url\": \"https://api.github.com/users/didar72ahmadi/gists{/gist_id}\",\n" +
                    "                    \"starred_url\": \"https://api.github.com/users/didar72ahmadi/starred{/owner}{/repo}\",\n" +
                    "                    \"subscriptions_url\": \"https://api.github.com/users/didar72ahmadi/subscriptions\",\n" +
                    "                    \"organizations_url\": \"https://api.github.com/users/didar72ahmadi/orgs\",\n" +
                    "                    \"repos_url\": \"https://api.github.com/users/didar72ahmadi/repos\",\n" +
                    "                    \"events_url\": \"https://api.github.com/users/didar72ahmadi/events{/privacy}\",\n" +
                    "                    \"received_events_url\": \"https://api.github.com/users/didar72ahmadi/received_events\",\n" +
                    "                    \"type\": \"User\",\n" +
                    "                    \"site_admin\": false\n" +
                    "                },\n" +
                    "                \"html_url\": \"https://github.com/didar72ahmadi/Hello-World\",\n" +
                    "                \"description\": \"My first repository on GitHub!\",\n" +
                    "                \"fork\": true,\n" +
                    "                \"url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World\",\n" +
                    "                \"forks_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/forks\",\n" +
                    "                \"keys_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/keys{/key_id}\",\n" +
                    "                \"collaborators_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/collaborators{/collaborator}\",\n" +
                    "                \"teams_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/teams\",\n" +
                    "                \"hooks_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/hooks\",\n" +
                    "                \"issue_events_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/issues/events{/number}\",\n" +
                    "                \"events_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/events\",\n" +
                    "                \"assignees_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/assignees{/user}\",\n" +
                    "                \"branches_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/branches{/branch}\",\n" +
                    "                \"tags_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/tags\",\n" +
                    "                \"blobs_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/git/blobs{/sha}\",\n" +
                    "                \"git_tags_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/git/tags{/sha}\",\n" +
                    "                \"git_refs_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/git/refs{/sha}\",\n" +
                    "                \"trees_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/git/trees{/sha}\",\n" +
                    "                \"statuses_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/statuses/{sha}\",\n" +
                    "                \"languages_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/languages\",\n" +
                    "                \"stargazers_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/stargazers\",\n" +
                    "                \"contributors_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/contributors\",\n" +
                    "                \"subscribers_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/subscribers\",\n" +
                    "                \"subscription_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/subscription\",\n" +
                    "                \"commits_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/commits{/sha}\",\n" +
                    "                \"git_commits_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/git/commits{/sha}\",\n" +
                    "                \"comments_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/comments{/number}\",\n" +
                    "                \"issue_comment_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/issues/comments{/number}\",\n" +
                    "                \"contents_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/contents/{+path}\",\n" +
                    "                \"compare_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/compare/{base}...{head}\",\n" +
                    "                \"merges_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/merges\",\n" +
                    "                \"archive_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/{archive_format}{/ref}\",\n" +
                    "                \"downloads_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/downloads\",\n" +
                    "                \"issues_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/issues{/number}\",\n" +
                    "                \"pulls_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/pulls{/number}\",\n" +
                    "                \"milestones_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/milestones{/number}\",\n" +
                    "                \"notifications_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/notifications{?since,all,participating}\",\n" +
                    "                \"labels_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/labels{/name}\",\n" +
                    "                \"releases_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/releases{/id}\",\n" +
                    "                \"deployments_url\": \"https://api.github.com/repos/didar72ahmadi/Hello-World/deployments\",\n" +
                    "                \"created_at\": \"2024-02-08T18:55:06Z\",\n" +
                    "                \"updated_at\": \"2024-02-08T18:55:06Z\",\n" +
                    "                \"pushed_at\": \"2024-02-08T18:59:28Z\",\n" +
                    "                \"git_url\": \"git://github.com/didar72ahmadi/Hello-World.git\",\n" +
                    "                \"ssh_url\": \"git@github.com:didar72ahmadi/Hello-World.git\",\n" +
                    "                \"clone_url\": \"https://github.com/didar72ahmadi/Hello-World.git\",\n" +
                    "                \"svn_url\": \"https://github.com/didar72ahmadi/Hello-World\",\n" +
                    "                \"homepage\": \"\",\n" +
                    "                \"size\": 4,\n" +
                    "                \"stargazers_count\": 0,\n" +
                    "                \"watchers_count\": 0,\n" +
                    "                \"language\": null,\n" +
                    "                \"has_issues\": false,\n" +
                    "                \"has_projects\": true,\n" +
                    "                \"has_downloads\": true,\n" +
                    "                \"has_wiki\": true,\n" +
                    "                \"has_pages\": false,\n" +
                    "                \"has_discussions\": false,\n" +
                    "                \"forks_count\": 0,\n" +
                    "                \"mirror_url\": null,\n" +
                    "                \"archived\": false,\n" +
                    "                \"disabled\": false,\n" +
                    "                \"open_issues_count\": 0,\n" +
                    "                \"license\": null,\n" +
                    "                \"allow_forking\": true,\n" +
                    "                \"is_template\": false,\n" +
                    "                \"web_commit_signoff_required\": false,\n" +
                    "                \"topics\": [\n" +
                    "\n" +
                    "                ],\n" +
                    "                \"visibility\": \"public\",\n" +
                    "                \"forks\": 0,\n" +
                    "                \"open_issues\": 0,\n" +
                    "                \"watchers\": 0,\n" +
                    "                \"default_branch\": \"master\"\n" +
                    "            }\n" +
                    "        },\n" +
                    "        \"base\": {\n" +
                    "            \"label\": \"octocat:master\",\n" +
                    "            \"ref\": \"master\",\n" +
                    "            \"sha\": \"7fd1a60b01f91b314f59955a4e4d4e80d8edf11d\",\n" +
                    "            \"user\": {\n" +
                    "                \"login\": \"octocat\",\n" +
                    "                \"id\": 583231,\n" +
                    "                \"node_id\": \"MDQ6VXNlcjU4MzIzMQ==\",\n" +
                    "                \"avatar_url\": \"https://avatars.githubusercontent.com/u/583231?v=4\",\n" +
                    "                \"gravatar_id\": \"\",\n" +
                    "                \"url\": \"https://api.github.com/users/octocat\",\n" +
                    "                \"html_url\": \"https://github.com/octocat\",\n" +
                    "                \"followers_url\": \"https://api.github.com/users/octocat/followers\",\n" +
                    "                \"following_url\": \"https://api.github.com/users/octocat/following{/other_user}\",\n" +
                    "                \"gists_url\": \"https://api.github.com/users/octocat/gists{/gist_id}\",\n" +
                    "                \"starred_url\": \"https://api.github.com/users/octocat/starred{/owner}{/repo}\",\n" +
                    "                \"subscriptions_url\": \"https://api.github.com/users/octocat/subscriptions\",\n" +
                    "                \"organizations_url\": \"https://api.github.com/users/octocat/orgs\",\n" +
                    "                \"repos_url\": \"https://api.github.com/users/octocat/repos\",\n" +
                    "                \"events_url\": \"https://api.github.com/users/octocat/events{/privacy}\",\n" +
                    "                \"received_events_url\": \"https://api.github.com/users/octocat/received_events\",\n" +
                    "                \"type\": \"User\",\n" +
                    "                \"site_admin\": false\n" +
                    "            },\n" +
                    "            \"repo\": {\n" +
                    "                \"id\": 1296269,\n" +
                    "                \"node_id\": \"MDEwOlJlcG9zaXRvcnkxMjk2MjY5\",\n" +
                    "                \"name\": \"Hello-World\",\n" +
                    "                \"full_name\": \"octocat/Hello-World\",\n" +
                    "                \"private\": false,\n" +
                    "                \"owner\": {\n" +
                    "                    \"login\": \"octocat\",\n" +
                    "                    \"id\": 583231,\n" +
                    "                    \"node_id\": \"MDQ6VXNlcjU4MzIzMQ==\",\n" +
                    "                    \"avatar_url\": \"https://avatars.githubusercontent.com/u/583231?v=4\",\n" +
                    "                    \"gravatar_id\": \"\",\n" +
                    "                    \"url\": \"https://api.github.com/users/octocat\",\n" +
                    "                    \"html_url\": \"https://github.com/octocat\",\n" +
                    "                    \"followers_url\": \"https://api.github.com/users/octocat/followers\",\n" +
                    "                    \"following_url\": \"https://api.github.com/users/octocat/following{/other_user}\",\n" +
                    "                    \"gists_url\": \"https://api.github.com/users/octocat/gists{/gist_id}\",\n" +
                    "                    \"starred_url\": \"https://api.github.com/users/octocat/starred{/owner}{/repo}\",\n" +
                    "                    \"subscriptions_url\": \"https://api.github.com/users/octocat/subscriptions\",\n" +
                    "                    \"organizations_url\": \"https://api.github.com/users/octocat/orgs\",\n" +
                    "                    \"repos_url\": \"https://api.github.com/users/octocat/repos\",\n" +
                    "                    \"events_url\": \"https://api.github.com/users/octocat/events{/privacy}\",\n" +
                    "                    \"received_events_url\": \"https://api.github.com/users/octocat/received_events\",\n" +
                    "                    \"type\": \"User\",\n" +
                    "                    \"site_admin\": false\n" +
                    "                },\n" +
                    "                \"html_url\": \"https://github.com/octocat/Hello-World\",\n" +
                    "                \"description\": \"My first repository on GitHub!\",\n" +
                    "                \"fork\": false,\n" +
                    "                \"url\": \"https://api.github.com/repos/octocat/Hello-World\",\n" +
                    "                \"forks_url\": \"https://api.github.com/repos/octocat/Hello-World/forks\",\n" +
                    "                \"keys_url\": \"https://api.github.com/repos/octocat/Hello-World/keys{/key_id}\",\n" +
                    "                \"collaborators_url\": \"https://api.github.com/repos/octocat/Hello-World/collaborators{/collaborator}\",\n" +
                    "                \"teams_url\": \"https://api.github.com/repos/octocat/Hello-World/teams\",\n" +
                    "                \"hooks_url\": \"https://api.github.com/repos/octocat/Hello-World/hooks\",\n" +
                    "                \"issue_events_url\": \"https://api.github.com/repos/octocat/Hello-World/issues/events{/number}\",\n" +
                    "                \"events_url\": \"https://api.github.com/repos/octocat/Hello-World/events\",\n" +
                    "                \"assignees_url\": \"https://api.github.com/repos/octocat/Hello-World/assignees{/user}\",\n" +
                    "                \"branches_url\": \"https://api.github.com/repos/octocat/Hello-World/branches{/branch}\",\n" +
                    "                \"tags_url\": \"https://api.github.com/repos/octocat/Hello-World/tags\",\n" +
                    "                \"blobs_url\": \"https://api.github.com/repos/octocat/Hello-World/git/blobs{/sha}\",\n" +
                    "                \"git_tags_url\": \"https://api.github.com/repos/octocat/Hello-World/git/tags{/sha}\",\n" +
                    "                \"git_refs_url\": \"https://api.github.com/repos/octocat/Hello-World/git/refs{/sha}\",\n" +
                    "                \"trees_url\": \"https://api.github.com/repos/octocat/Hello-World/git/trees{/sha}\",\n" +
                    "                \"statuses_url\": \"https://api.github.com/repos/octocat/Hello-World/statuses/{sha}\",\n" +
                    "                \"languages_url\": \"https://api.github.com/repos/octocat/Hello-World/languages\",\n" +
                    "                \"stargazers_url\": \"https://api.github.com/repos/octocat/Hello-World/stargazers\",\n" +
                    "                \"contributors_url\": \"https://api.github.com/repos/octocat/Hello-World/contributors\",\n" +
                    "                \"subscribers_url\": \"https://api.github.com/repos/octocat/Hello-World/subscribers\",\n" +
                    "                \"subscription_url\": \"https://api.github.com/repos/octocat/Hello-World/subscription\",\n" +
                    "                \"commits_url\": \"https://api.github.com/repos/octocat/Hello-World/commits{/sha}\",\n" +
                    "                \"git_commits_url\": \"https://api.github.com/repos/octocat/Hello-World/git/commits{/sha}\",\n" +
                    "                \"comments_url\": \"https://api.github.com/repos/octocat/Hello-World/comments{/number}\",\n" +
                    "                \"issue_comment_url\": \"https://api.github.com/repos/octocat/Hello-World/issues/comments{/number}\",\n" +
                    "                \"contents_url\": \"https://api.github.com/repos/octocat/Hello-World/contents/{+path}\",\n" +
                    "                \"compare_url\": \"https://api.github.com/repos/octocat/Hello-World/compare/{base}...{head}\",\n" +
                    "                \"merges_url\": \"https://api.github.com/repos/octocat/Hello-World/merges\",\n" +
                    "                \"archive_url\": \"https://api.github.com/repos/octocat/Hello-World/{archive_format}{/ref}\",\n" +
                    "                \"downloads_url\": \"https://api.github.com/repos/octocat/Hello-World/downloads\",\n" +
                    "                \"issues_url\": \"https://api.github.com/repos/octocat/Hello-World/issues{/number}\",\n" +
                    "                \"pulls_url\": \"https://api.github.com/repos/octocat/Hello-World/pulls{/number}\",\n" +
                    "                \"milestones_url\": \"https://api.github.com/repos/octocat/Hello-World/milestones{/number}\",\n" +
                    "                \"notifications_url\": \"https://api.github.com/repos/octocat/Hello-World/notifications{?since,all,participating}\",\n" +
                    "                \"labels_url\": \"https://api.github.com/repos/octocat/Hello-World/labels{/name}\",\n" +
                    "                \"releases_url\": \"https://api.github.com/repos/octocat/Hello-World/releases{/id}\",\n" +
                    "                \"deployments_url\": \"https://api.github.com/repos/octocat/Hello-World/deployments\",\n" +
                    "                \"created_at\": \"2011-01-26T19:01:12Z\",\n" +
                    "                \"updated_at\": \"2024-02-25T13:57:13Z\",\n" +
                    "                \"pushed_at\": \"2024-02-23T12:43:59Z\",\n" +
                    "                \"git_url\": \"git://github.com/octocat/Hello-World.git\",\n" +
                    "                \"ssh_url\": \"git@github.com:octocat/Hello-World.git\",\n" +
                    "                \"clone_url\": \"https://github.com/octocat/Hello-World.git\",\n" +
                    "                \"svn_url\": \"https://github.com/octocat/Hello-World\",\n" +
                    "                \"homepage\": \"\",\n" +
                    "                \"size\": 1,\n" +
                    "                \"stargazers_count\": 2459,\n" +
                    "                \"watchers_count\": 2459,\n" +
                    "                \"language\": null,\n" +
                    "                \"has_issues\": true,\n" +
                    "                \"has_projects\": true,\n" +
                    "                \"has_downloads\": true,\n" +
                    "                \"has_wiki\": true,\n" +
                    "                \"has_pages\": false,\n" +
                    "                \"has_discussions\": false,\n" +
                    "                \"forks_count\": 2153,\n" +
                    "                \"mirror_url\": null,\n" +
                    "                \"archived\": false,\n" +
                    "                \"disabled\": false,\n" +
                    "                \"open_issues_count\": 1277,\n" +
                    "                \"license\": null,\n" +
                    "                \"allow_forking\": true,\n" +
                    "                \"is_template\": false,\n" +
                    "                \"web_commit_signoff_required\": false,\n" +
                    "                \"topics\": [\n" +
                    "\n" +
                    "                ],\n" +
                    "                \"visibility\": \"public\",\n" +
                    "                \"forks\": 2153,\n" +
                    "                \"open_issues\": 1277,\n" +
                    "                \"watchers\": 2459,\n" +
                    "                \"default_branch\": \"master\"\n" +
                    "            }\n" +
                    "        },\n" +
                    "        \"_links\": {\n" +
                    "            \"self\": {\n" +
                    "                \"href\": \"https://api.github.com/repos/octocat/Hello-World/pulls/2988\"\n" +
                    "            },\n" +
                    "            \"html\": {\n" +
                    "                \"href\": \"https://github.com/octocat/Hello-World/pull/2988\"\n" +
                    "            },\n" +
                    "            \"issue\": {\n" +
                    "                \"href\": \"https://api.github.com/repos/octocat/Hello-World/issues/2988\"\n" +
                    "            },\n" +
                    "            \"comments\": {\n" +
                    "                \"href\": \"https://api.github.com/repos/octocat/Hello-World/issues/2988/comments\"\n" +
                    "            },\n" +
                    "            \"review_comments\": {\n" +
                    "                \"href\": \"https://api.github.com/repos/octocat/Hello-World/pulls/2988/comments\"\n" +
                    "            },\n" +
                    "            \"review_comment\": {\n" +
                    "                \"href\": \"https://api.github.com/repos/octocat/Hello-World/pulls/comments{/number}\"\n" +
                    "            },\n" +
                    "            \"commits\": {\n" +
                    "                \"href\": \"https://api.github.com/repos/octocat/Hello-World/pulls/2988/commits\"\n" +
                    "            },\n" +
                    "            \"statuses\": {\n" +
                    "                \"href\": \"https://api.github.com/repos/octocat/Hello-World/statuses/772e2d7bd9fce08b2a2a7a1a9502d89776752a4e\"\n" +
                    "            }\n" +
                    "        },\n" +
                    "        \"author_association\": \"NONE\",\n" +
                    "        \"auto_merge\": null,\n" +
                    "        \"active_lock_reason\": null\n" +
                    "    }\n" +
                    "]",
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
                "[\n" +
                    "    {\n" +
                    "        \"url\": \"https://api.github.com/repos/octocat/Hello-World/issues/2988\",\n" +
                    "        \"repository_url\": \"https://api.github.com/repos/octocat/Hello-World\",\n" +
                    "        \"labels_url\": \"https://api.github.com/repos/octocat/Hello-World/issues/2988/labels{/name}\",\n" +
                    "        \"comments_url\": \"https://api.github.com/repos/octocat/Hello-World/issues/2988/comments\",\n" +
                    "        \"events_url\": \"https://api.github.com/repos/octocat/Hello-World/issues/2988/events\",\n" +
                    "        \"html_url\": \"https://github.com/octocat/Hello-World/pull/2988\",\n" +
                    "        \"id\": 2151012041,\n" +
                    "        \"node_id\": \"PR_kwDOABPHjc5nwGpi\",\n" +
                    "        \"number\": 2988,\n" +
                    "        \"title\": \"Create codeql.yml\",\n" +
                    "        \"user\": {\n" +
                    "            \"login\": \"didar72ahmadi\",\n" +
                    "            \"id\": 159153880,\n" +
                    "            \"node_id\": \"U_kgDOCXx-2A\",\n" +
                    "            \"avatar_url\": \"https://avatars.githubusercontent.com/u/159153880?v=4\",\n" +
                    "            \"gravatar_id\": \"\",\n" +
                    "            \"url\": \"https://api.github.com/users/didar72ahmadi\",\n" +
                    "            \"html_url\": \"https://github.com/didar72ahmadi\",\n" +
                    "            \"followers_url\": \"https://api.github.com/users/didar72ahmadi/followers\",\n" +
                    "            \"following_url\": \"https://api.github.com/users/didar72ahmadi/following{/other_user}\",\n" +
                    "            \"gists_url\": \"https://api.github.com/users/didar72ahmadi/gists{/gist_id}\",\n" +
                    "            \"starred_url\": \"https://api.github.com/users/didar72ahmadi/starred{/owner}{/repo}\",\n" +
                    "            \"subscriptions_url\": \"https://api.github.com/users/didar72ahmadi/subscriptions\",\n" +
                    "            \"organizations_url\": \"https://api.github.com/users/didar72ahmadi/orgs\",\n" +
                    "            \"repos_url\": \"https://api.github.com/users/didar72ahmadi/repos\",\n" +
                    "            \"events_url\": \"https://api.github.com/users/didar72ahmadi/events{/privacy}\",\n" +
                    "            \"received_events_url\": \"https://api.github.com/users/didar72ahmadi/received_events\",\n" +
                    "            \"type\": \"User\",\n" +
                    "            \"site_admin\": false\n" +
                    "        },\n" +
                    "        \"labels\": [\n" +
                    "\n" +
                    "        ],\n" +
                    "        \"state\": \"open\",\n" +
                    "        \"locked\": false,\n" +
                    "        \"assignee\": null,\n" +
                    "        \"assignees\": [\n" +
                    "\n" +
                    "        ],\n" +
                    "        \"milestone\": null,\n" +
                    "        \"comments\": 0,\n" +
                    "        \"created_at\": \"2024-02-23T12:43:58Z\",\n" +
                    "        \"updated_at\": \"2024-02-23T12:43:58Z\",\n" +
                    "        \"closed_at\": null,\n" +
                    "        \"author_association\": \"NONE\",\n" +
                    "        \"active_lock_reason\": null,\n" +
                    "        \"draft\": false,\n" +
                    "        \"pull_request\": {\n" +
                    "            \"url\": \"https://api.github.com/repos/octocat/Hello-World/pulls/2988\",\n" +
                    "            \"html_url\": \"https://github.com/octocat/Hello-World/pull/2988\",\n" +
                    "            \"diff_url\": \"https://github.com/octocat/Hello-World/pull/2988.diff\",\n" +
                    "            \"patch_url\": \"https://github.com/octocat/Hello-World/pull/2988.patch\",\n" +
                    "            \"merged_at\": null\n" +
                    "        },\n" +
                    "        \"body\": \"com.google.android.permission \\r\\n\",\n" +
                    "        \"reactions\": {\n" +
                    "            \"url\": \"https://api.github.com/repos/octocat/Hello-World/issues/2988/reactions\",\n" +
                    "            \"total_count\": 0,\n" +
                    "            \"+1\": 0,\n" +
                    "            \"-1\": 0,\n" +
                    "            \"laugh\": 0,\n" +
                    "            \"hooray\": 0,\n" +
                    "            \"confused\": 0,\n" +
                    "            \"heart\": 0,\n" +
                    "            \"rocket\": 0,\n" +
                    "            \"eyes\": 0\n" +
                    "        },\n" +
                    "        \"timeline_url\": \"https://api.github.com/repos/octocat/Hello-World/issues/2988/timeline\",\n" +
                    "        \"performed_via_github_app\": null,\n" +
                    "        \"state_reason\": null\n" +
                    "    }\n" +
                    "]\n",
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
                "[\n" +
                    "    {\n" +
                    "        \"sha\": \"55fc82e1f766bd43a02291e33b208e0dd59847db\",\n" +
                    "        \"node_id\": \"C_kwDOLO2f3NoAKDU1ZmM4MmUxZjc2NmJkNDNhMDIyOTFlMzNiMjA4ZTBkZDU5ODQ3ZGI\",\n" +
                    "        \"commit\": {\n" +
                    "            \"author\": {\n" +
                    "                \"name\": \"Maxim Primakov\",\n" +
                    "                \"email\": \"spartmenik@gmail.com\",\n" +
                    "                \"date\": \"2024-03-08T11:42:02Z\"\n" +
                    "            },\n" +
                    "            \"committer\": {\n" +
                    "                \"name\": \"Maxim Primakov\",\n" +
                    "                \"email\": \"spartmenik@gmail.com\",\n" +
                    "                \"date\": \"2024-04-07T16:24:08Z\"\n" +
                    "            },\n" +
                    "            \"message\": \"hw4 init commit\",\n" +
                    "            \"tree\": {\n" +
                    "                \"sha\": \"a18367b84fef4d8bdc2bd6d615c5ebcc2a090142\",\n" +
                    "                \"url\": \"https://api.github.com/repos/MAK-Cpp/backend-java-s2/git/trees/a18367b84fef4d8bdc2bd6d615c5ebcc2a090142\"\n" +
                    "            },\n" +
                    "            \"url\": \"https://api.github.com/repos/MAK-Cpp/backend-java-s2/git/commits/55fc82e1f766bd43a02291e33b208e0dd59847db\",\n" +
                    "            \"comment_count\": 0,\n" +
                    "            \"verification\": {\n" +
                    "                \"verified\": false,\n" +
                    "                \"reason\": \"unsigned\",\n" +
                    "                \"signature\": null,\n" +
                    "                \"payload\": null\n" +
                    "            }\n" +
                    "        },\n" +
                    "        \"url\": \"https://api.github.com/repos/MAK-Cpp/backend-java-s2/commits/55fc82e1f766bd43a02291e33b208e0dd59847db\",\n" +
                    "        \"html_url\": \"https://github.com/MAK-Cpp/backend-java-s2/commit/55fc82e1f766bd43a02291e33b208e0dd59847db\",\n" +
                    "        \"comments_url\": \"https://api.github.com/repos/MAK-Cpp/backend-java-s2/commits/55fc82e1f766bd43a02291e33b208e0dd59847db/comments\",\n" +
                    "        \"author\": {\n" +
                    "            \"login\": \"MAK-Cpp\",\n" +
                    "            \"id\": 75676696,\n" +
                    "            \"node_id\": \"MDQ6VXNlcjc1Njc2Njk2\",\n" +
                    "            \"avatar_url\": \"https://avatars.githubusercontent.com/u/75676696?v=4\",\n" +
                    "            \"gravatar_id\": \"\",\n" +
                    "            \"url\": \"https://api.github.com/users/MAK-Cpp\",\n" +
                    "            \"html_url\": \"https://github.com/MAK-Cpp\",\n" +
                    "            \"followers_url\": \"https://api.github.com/users/MAK-Cpp/followers\",\n" +
                    "            \"following_url\": \"https://api.github.com/users/MAK-Cpp/following{/other_user}\",\n" +
                    "            \"gists_url\": \"https://api.github.com/users/MAK-Cpp/gists{/gist_id}\",\n" +
                    "            \"starred_url\": \"https://api.github.com/users/MAK-Cpp/starred{/owner}{/repo}\",\n" +
                    "            \"subscriptions_url\": \"https://api.github.com/users/MAK-Cpp/subscriptions\",\n" +
                    "            \"organizations_url\": \"https://api.github.com/users/MAK-Cpp/orgs\",\n" +
                    "            \"repos_url\": \"https://api.github.com/users/MAK-Cpp/repos\",\n" +
                    "            \"events_url\": \"https://api.github.com/users/MAK-Cpp/events{/privacy}\",\n" +
                    "            \"received_events_url\": \"https://api.github.com/users/MAK-Cpp/received_events\",\n" +
                    "            \"type\": \"User\",\n" +
                    "            \"site_admin\": false\n" +
                    "        },\n" +
                    "        \"committer\": {\n" +
                    "            \"login\": \"MAK-Cpp\",\n" +
                    "            \"id\": 75676696,\n" +
                    "            \"node_id\": \"MDQ6VXNlcjc1Njc2Njk2\",\n" +
                    "            \"avatar_url\": \"https://avatars.githubusercontent.com/u/75676696?v=4\",\n" +
                    "            \"gravatar_id\": \"\",\n" +
                    "            \"url\": \"https://api.github.com/users/MAK-Cpp\",\n" +
                    "            \"html_url\": \"https://github.com/MAK-Cpp\",\n" +
                    "            \"followers_url\": \"https://api.github.com/users/MAK-Cpp/followers\",\n" +
                    "            \"following_url\": \"https://api.github.com/users/MAK-Cpp/following{/other_user}\",\n" +
                    "            \"gists_url\": \"https://api.github.com/users/MAK-Cpp/gists{/gist_id}\",\n" +
                    "            \"starred_url\": \"https://api.github.com/users/MAK-Cpp/starred{/owner}{/repo}\",\n" +
                    "            \"subscriptions_url\": \"https://api.github.com/users/MAK-Cpp/subscriptions\",\n" +
                    "            \"organizations_url\": \"https://api.github.com/users/MAK-Cpp/orgs\",\n" +
                    "            \"repos_url\": \"https://api.github.com/users/MAK-Cpp/repos\",\n" +
                    "            \"events_url\": \"https://api.github.com/users/MAK-Cpp/events{/privacy}\",\n" +
                    "            \"received_events_url\": \"https://api.github.com/users/MAK-Cpp/received_events\",\n" +
                    "            \"type\": \"User\",\n" +
                    "            \"site_admin\": false\n" +
                    "        },\n" +
                    "        \"parents\": [\n" +
                    "            {\n" +
                    "                \"sha\": \"de43706948325d80beb0a9107af7e5b67f5dc9ec\",\n" +
                    "                \"url\": \"https://api.github.com/repos/MAK-Cpp/backend-java-s2/commits/de43706948325d80beb0a9107af7e5b67f5dc9ec\",\n" +
                    "                \"html_url\": \"https://github.com/MAK-Cpp/backend-java-s2/commit/de43706948325d80beb0a9107af7e5b67f5dc9ec\"\n" +
                    "            }\n" +
                    "        ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"sha\": \"e954e25349ef58b0e73fed4b03f01dec5939e59f\",\n" +
                    "        \"node_id\": \"C_kwDOLO2f3NoAKGU5NTRlMjUzNDllZjU4YjBlNzNmZWQ0YjAzZjAxZGVjNTkzOWU1OWY\",\n" +
                    "        \"commit\": {\n" +
                    "            \"author\": {\n" +
                    "                \"name\": \"Maxim Primakov\",\n" +
                    "                \"email\": \"spartmenik@gmail.com\",\n" +
                    "                \"date\": \"2024-03-08T14:47:13Z\"\n" +
                    "            },\n" +
                    "            \"committer\": {\n" +
                    "                \"name\": \"Maxim Primakov\",\n" +
                    "                \"email\": \"spartmenik@gmail.com\",\n" +
                    "                \"date\": \"2024-04-07T16:24:08Z\"\n" +
                    "            },\n" +
                    "            \"message\": \"written tasks No. 1-3\",\n" +
                    "            \"tree\": {\n" +
                    "                \"sha\": \"b3595657fa91a6c0b39d0d0a863cacc9ed255eee\",\n" +
                    "                \"url\": \"https://api.github.com/repos/MAK-Cpp/backend-java-s2/git/trees/b3595657fa91a6c0b39d0d0a863cacc9ed255eee\"\n" +
                    "            },\n" +
                    "            \"url\": \"https://api.github.com/repos/MAK-Cpp/backend-java-s2/git/commits/e954e25349ef58b0e73fed4b03f01dec5939e59f\",\n" +
                    "            \"comment_count\": 0,\n" +
                    "            \"verification\": {\n" +
                    "                \"verified\": false,\n" +
                    "                \"reason\": \"unsigned\",\n" +
                    "                \"signature\": null,\n" +
                    "                \"payload\": null\n" +
                    "            }\n" +
                    "        },\n" +
                    "        \"url\": \"https://api.github.com/repos/MAK-Cpp/backend-java-s2/commits/e954e25349ef58b0e73fed4b03f01dec5939e59f\",\n" +
                    "        \"html_url\": \"https://github.com/MAK-Cpp/backend-java-s2/commit/e954e25349ef58b0e73fed4b03f01dec5939e59f\",\n" +
                    "        \"comments_url\": \"https://api.github.com/repos/MAK-Cpp/backend-java-s2/commits/e954e25349ef58b0e73fed4b03f01dec5939e59f/comments\",\n" +
                    "        \"author\": {\n" +
                    "            \"login\": \"MAK-Cpp\",\n" +
                    "            \"id\": 75676696,\n" +
                    "            \"node_id\": \"MDQ6VXNlcjc1Njc2Njk2\",\n" +
                    "            \"avatar_url\": \"https://avatars.githubusercontent.com/u/75676696?v=4\",\n" +
                    "            \"gravatar_id\": \"\",\n" +
                    "            \"url\": \"https://api.github.com/users/MAK-Cpp\",\n" +
                    "            \"html_url\": \"https://github.com/MAK-Cpp\",\n" +
                    "            \"followers_url\": \"https://api.github.com/users/MAK-Cpp/followers\",\n" +
                    "            \"following_url\": \"https://api.github.com/users/MAK-Cpp/following{/other_user}\",\n" +
                    "            \"gists_url\": \"https://api.github.com/users/MAK-Cpp/gists{/gist_id}\",\n" +
                    "            \"starred_url\": \"https://api.github.com/users/MAK-Cpp/starred{/owner}{/repo}\",\n" +
                    "            \"subscriptions_url\": \"https://api.github.com/users/MAK-Cpp/subscriptions\",\n" +
                    "            \"organizations_url\": \"https://api.github.com/users/MAK-Cpp/orgs\",\n" +
                    "            \"repos_url\": \"https://api.github.com/users/MAK-Cpp/repos\",\n" +
                    "            \"events_url\": \"https://api.github.com/users/MAK-Cpp/events{/privacy}\",\n" +
                    "            \"received_events_url\": \"https://api.github.com/users/MAK-Cpp/received_events\",\n" +
                    "            \"type\": \"User\",\n" +
                    "            \"site_admin\": false\n" +
                    "        },\n" +
                    "        \"committer\": {\n" +
                    "            \"login\": \"MAK-Cpp\",\n" +
                    "            \"id\": 75676696,\n" +
                    "            \"node_id\": \"MDQ6VXNlcjc1Njc2Njk2\",\n" +
                    "            \"avatar_url\": \"https://avatars.githubusercontent.com/u/75676696?v=4\",\n" +
                    "            \"gravatar_id\": \"\",\n" +
                    "            \"url\": \"https://api.github.com/users/MAK-Cpp\",\n" +
                    "            \"html_url\": \"https://github.com/MAK-Cpp\",\n" +
                    "            \"followers_url\": \"https://api.github.com/users/MAK-Cpp/followers\",\n" +
                    "            \"following_url\": \"https://api.github.com/users/MAK-Cpp/following{/other_user}\",\n" +
                    "            \"gists_url\": \"https://api.github.com/users/MAK-Cpp/gists{/gist_id}\",\n" +
                    "            \"starred_url\": \"https://api.github.com/users/MAK-Cpp/starred{/owner}{/repo}\",\n" +
                    "            \"subscriptions_url\": \"https://api.github.com/users/MAK-Cpp/subscriptions\",\n" +
                    "            \"organizations_url\": \"https://api.github.com/users/MAK-Cpp/orgs\",\n" +
                    "            \"repos_url\": \"https://api.github.com/users/MAK-Cpp/repos\",\n" +
                    "            \"events_url\": \"https://api.github.com/users/MAK-Cpp/events{/privacy}\",\n" +
                    "            \"received_events_url\": \"https://api.github.com/users/MAK-Cpp/received_events\",\n" +
                    "            \"type\": \"User\",\n" +
                    "            \"site_admin\": false\n" +
                    "        },\n" +
                    "        \"parents\": [\n" +
                    "            {\n" +
                    "                \"sha\": \"55fc82e1f766bd43a02291e33b208e0dd59847db\",\n" +
                    "                \"url\": \"https://api.github.com/repos/MAK-Cpp/backend-java-s2/commits/55fc82e1f766bd43a02291e33b208e0dd59847db\",\n" +
                    "                \"html_url\": \"https://github.com/MAK-Cpp/backend-java-s2/commit/55fc82e1f766bd43a02291e33b208e0dd59847db\"\n" +
                    "            }\n" +
                    "        ]\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"sha\": \"f3f539338bc981e0a09772f0dd1f3d801eb7596f\",\n" +
                    "        \"node_id\": \"C_kwDOLO2f3NoAKGYzZjUzOTMzOGJjOTgxZTBhMDk3NzJmMGRkMWYzZDgwMWViNzU5NmY\",\n" +
                    "        \"commit\": {\n" +
                    "            \"author\": {\n" +
                    "                \"name\": \"Maxim Primakov\",\n" +
                    "                \"email\": \"spartmenik@gmail.com\",\n" +
                    "                \"date\": \"2024-03-09T12:09:04Z\"\n" +
                    "            },\n" +
                    "            \"committer\": {\n" +
                    "                \"name\": \"Maxim Primakov\",\n" +
                    "                \"email\": \"spartmenik@gmail.com\",\n" +
                    "                \"date\": \"2024-04-07T16:24:08Z\"\n" +
                    "            },\n" +
                    "            \"message\": \"written task No. 4\",\n" +
                    "            \"tree\": {\n" +
                    "                \"sha\": \"fb431809ce61479c5005638d2ed2ced7e9f2ba67\",\n" +
                    "                \"url\": \"https://api.github.com/repos/MAK-Cpp/backend-java-s2/git/trees/fb431809ce61479c5005638d2ed2ced7e9f2ba67\"\n" +
                    "            },\n" +
                    "            \"url\": \"https://api.github.com/repos/MAK-Cpp/backend-java-s2/git/commits/f3f539338bc981e0a09772f0dd1f3d801eb7596f\",\n" +
                    "            \"comment_count\": 0,\n" +
                    "            \"verification\": {\n" +
                    "                \"verified\": false,\n" +
                    "                \"reason\": \"unsigned\",\n" +
                    "                \"signature\": null,\n" +
                    "                \"payload\": null\n" +
                    "            }\n" +
                    "        },\n" +
                    "        \"url\": \"https://api.github.com/repos/MAK-Cpp/backend-java-s2/commits/f3f539338bc981e0a09772f0dd1f3d801eb7596f\",\n" +
                    "        \"html_url\": \"https://github.com/MAK-Cpp/backend-java-s2/commit/f3f539338bc981e0a09772f0dd1f3d801eb7596f\",\n" +
                    "        \"comments_url\": \"https://api.github.com/repos/MAK-Cpp/backend-java-s2/commits/f3f539338bc981e0a09772f0dd1f3d801eb7596f/comments\",\n" +
                    "        \"author\": {\n" +
                    "            \"login\": \"MAK-Cpp\",\n" +
                    "            \"id\": 75676696,\n" +
                    "            \"node_id\": \"MDQ6VXNlcjc1Njc2Njk2\",\n" +
                    "            \"avatar_url\": \"https://avatars.githubusercontent.com/u/75676696?v=4\",\n" +
                    "            \"gravatar_id\": \"\",\n" +
                    "            \"url\": \"https://api.github.com/users/MAK-Cpp\",\n" +
                    "            \"html_url\": \"https://github.com/MAK-Cpp\",\n" +
                    "            \"followers_url\": \"https://api.github.com/users/MAK-Cpp/followers\",\n" +
                    "            \"following_url\": \"https://api.github.com/users/MAK-Cpp/following{/other_user}\",\n" +
                    "            \"gists_url\": \"https://api.github.com/users/MAK-Cpp/gists{/gist_id}\",\n" +
                    "            \"starred_url\": \"https://api.github.com/users/MAK-Cpp/starred{/owner}{/repo}\",\n" +
                    "            \"subscriptions_url\": \"https://api.github.com/users/MAK-Cpp/subscriptions\",\n" +
                    "            \"organizations_url\": \"https://api.github.com/users/MAK-Cpp/orgs\",\n" +
                    "            \"repos_url\": \"https://api.github.com/users/MAK-Cpp/repos\",\n" +
                    "            \"events_url\": \"https://api.github.com/users/MAK-Cpp/events{/privacy}\",\n" +
                    "            \"received_events_url\": \"https://api.github.com/users/MAK-Cpp/received_events\",\n" +
                    "            \"type\": \"User\",\n" +
                    "            \"site_admin\": false\n" +
                    "        },\n" +
                    "        \"committer\": {\n" +
                    "            \"login\": \"MAK-Cpp\",\n" +
                    "            \"id\": 75676696,\n" +
                    "            \"node_id\": \"MDQ6VXNlcjc1Njc2Njk2\",\n" +
                    "            \"avatar_url\": \"https://avatars.githubusercontent.com/u/75676696?v=4\",\n" +
                    "            \"gravatar_id\": \"\",\n" +
                    "            \"url\": \"https://api.github.com/users/MAK-Cpp\",\n" +
                    "            \"html_url\": \"https://github.com/MAK-Cpp\",\n" +
                    "            \"followers_url\": \"https://api.github.com/users/MAK-Cpp/followers\",\n" +
                    "            \"following_url\": \"https://api.github.com/users/MAK-Cpp/following{/other_user}\",\n" +
                    "            \"gists_url\": \"https://api.github.com/users/MAK-Cpp/gists{/gist_id}\",\n" +
                    "            \"starred_url\": \"https://api.github.com/users/MAK-Cpp/starred{/owner}{/repo}\",\n" +
                    "            \"subscriptions_url\": \"https://api.github.com/users/MAK-Cpp/subscriptions\",\n" +
                    "            \"organizations_url\": \"https://api.github.com/users/MAK-Cpp/orgs\",\n" +
                    "            \"repos_url\": \"https://api.github.com/users/MAK-Cpp/repos\",\n" +
                    "            \"events_url\": \"https://api.github.com/users/MAK-Cpp/events{/privacy}\",\n" +
                    "            \"received_events_url\": \"https://api.github.com/users/MAK-Cpp/received_events\",\n" +
                    "            \"type\": \"User\",\n" +
                    "            \"site_admin\": false\n" +
                    "        },\n" +
                    "        \"parents\": [\n" +
                    "            {\n" +
                    "                \"sha\": \"e954e25349ef58b0e73fed4b03f01dec5939e59f\",\n" +
                    "                \"url\": \"https://api.github.com/repos/MAK-Cpp/backend-java-s2/commits/e954e25349ef58b0e73fed4b03f01dec5939e59f\",\n" +
                    "                \"html_url\": \"https://github.com/MAK-Cpp/backend-java-s2/commit/e954e25349ef58b0e73fed4b03f01dec5939e59f\"\n" +
                    "            }\n" +
                    "        ]\n" +
                    "    }\n" +
                    "]",
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
                "[\n" +
                    "    {\n" +
                    "        \"url\": \"https://api.github.com/repos/testcontainers/testcontainers-java/issues/comments/1959081453\",\n" +
                    "        \"html_url\": \"https://github.com/testcontainers/testcontainers-java/issues/8338#issuecomment-1959081453\",\n" +
                    "        \"issue_url\": \"https://api.github.com/repos/testcontainers/testcontainers-java/issues/8338\",\n" +
                    "        \"id\": 1959081453,\n" +
                    "        \"node_id\": \"IC_kwDOAgP_mc50xTXt\",\n" +
                    "        \"user\": {\n" +
                    "            \"login\": \"marcelstoer\",\n" +
                    "            \"id\": 624195,\n" +
                    "            \"node_id\": \"MDQ6VXNlcjYyNDE5NQ==\",\n" +
                    "            \"avatar_url\": \"https://avatars.githubusercontent.com/u/624195?v=4\",\n" +
                    "            \"gravatar_id\": \"\",\n" +
                    "            \"url\": \"https://api.github.com/users/marcelstoer\",\n" +
                    "            \"html_url\": \"https://github.com/marcelstoer\",\n" +
                    "            \"followers_url\": \"https://api.github.com/users/marcelstoer/followers\",\n" +
                    "            \"following_url\": \"https://api.github.com/users/marcelstoer/following{/other_user}\",\n" +
                    "            \"gists_url\": \"https://api.github.com/users/marcelstoer/gists{/gist_id}\",\n" +
                    "            \"starred_url\": \"https://api.github.com/users/marcelstoer/starred{/owner}{/repo}\",\n" +
                    "            \"subscriptions_url\": \"https://api.github.com/users/marcelstoer/subscriptions\",\n" +
                    "            \"organizations_url\": \"https://api.github.com/users/marcelstoer/orgs\",\n" +
                    "            \"repos_url\": \"https://api.github.com/users/marcelstoer/repos\",\n" +
                    "            \"events_url\": \"https://api.github.com/users/marcelstoer/events{/privacy}\",\n" +
                    "            \"received_events_url\": \"https://api.github.com/users/marcelstoer/received_events\",\n" +
                    "            \"type\": \"User\",\n" +
                    "            \"site_admin\": false\n" +
                    "        },\n" +
                    "        \"created_at\": \"2024-02-22T09:53:55Z\",\n" +
                    "        \"updated_at\": \"2024-02-22T09:53:55Z\",\n" +
                    "        \"author_association\": \"NONE\",\n" +
                    "        \"body\": \"Anyone coming across this, please follow the discussion at #8354. The `commons-compress` dependency won't be updated here for now.\",\n" +
                    "        \"reactions\": {\n" +
                    "            \"url\": \"https://api.github.com/repos/testcontainers/testcontainers-java/issues/comments/1959081453/reactions\",\n" +
                    "            \"total_count\": 2,\n" +
                    "            \"+1\": 0,\n" +
                    "            \"-1\": 0,\n" +
                    "            \"laugh\": 0,\n" +
                    "            \"hooray\": 0,\n" +
                    "            \"confused\": 2,\n" +
                    "            \"heart\": 0,\n" +
                    "            \"rocket\": 0,\n" +
                    "            \"eyes\": 0\n" +
                    "        },\n" +
                    "        \"performed_via_github_app\": null\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"url\": \"https://api.github.com/repos/testcontainers/testcontainers-java/issues/comments/1992649517\",\n" +
                    "        \"html_url\": \"https://github.com/testcontainers/testcontainers-java/issues/8338#issuecomment-1992649517\",\n" +
                    "        \"issue_url\": \"https://api.github.com/repos/testcontainers/testcontainers-java/issues/8338\",\n" +
                    "        \"id\": 1992649517,\n" +
                    "        \"node_id\": \"IC_kwDOAgP_mc52xWst\",\n" +
                    "        \"user\": {\n" +
                    "            \"login\": \"hailuand\",\n" +
                    "            \"id\": 6646502,\n" +
                    "            \"node_id\": \"MDQ6VXNlcjY2NDY1MDI=\",\n" +
                    "            \"avatar_url\": \"https://avatars.githubusercontent.com/u/6646502?v=4\",\n" +
                    "            \"gravatar_id\": \"\",\n" +
                    "            \"url\": \"https://api.github.com/users/hailuand\",\n" +
                    "            \"html_url\": \"https://github.com/hailuand\",\n" +
                    "            \"followers_url\": \"https://api.github.com/users/hailuand/followers\",\n" +
                    "            \"following_url\": \"https://api.github.com/users/hailuand/following{/other_user}\",\n" +
                    "            \"gists_url\": \"https://api.github.com/users/hailuand/gists{/gist_id}\",\n" +
                    "            \"starred_url\": \"https://api.github.com/users/hailuand/starred{/owner}{/repo}\",\n" +
                    "            \"subscriptions_url\": \"https://api.github.com/users/hailuand/subscriptions\",\n" +
                    "            \"organizations_url\": \"https://api.github.com/users/hailuand/orgs\",\n" +
                    "            \"repos_url\": \"https://api.github.com/users/hailuand/repos\",\n" +
                    "            \"events_url\": \"https://api.github.com/users/hailuand/events{/privacy}\",\n" +
                    "            \"received_events_url\": \"https://api.github.com/users/hailuand/received_events\",\n" +
                    "            \"type\": \"User\",\n" +
                    "            \"site_admin\": false\n" +
                    "        },\n" +
                    "        \"created_at\": \"2024-03-12T21:53:58Z\",\n" +
                    "        \"updated_at\": \"2024-03-12T21:53:58Z\",\n" +
                    "        \"author_association\": \"NONE\",\n" +
                    "        \"body\": \"Apache have released patch version 1.26.1 of commons-compress last week that may address this?\\r\\n\\r\\n> [COMPRESS-659:  TarArchiveOutputStream should use Commons IO Charsets instead of Commons Codec Charsets.](https://github.com/apache/commons-compress/blob/master/RELEASE-NOTES.txt#L25)\\r\\n\\r\\nI was able to successfully upgrade the commons-compress version in a project of mine to 1.26.1 that was previously failing on 1.26.0 with:\\r\\n```java\\r\\njava.lang.NoClassDefFoundError: org/apache/commons/codec/Charsets\\r\\n\\r\\n\\tat org.apache.commons.compress.archivers.tar.TarArchiveOutputStream.<init>(TarArchiveOutputStream.java:212)\\r\\n\\tat org.apache.commons.compress.archivers.tar.TarArchiveOutputStream.<init>(TarArchiveOutputStream.java:157)\\r\\n\\tat org.apache.commons.compress.archivers.tar.TarArchiveOutputStream.<init>(TarArchiveOutputStream.java:147)\\r\\n\\tat org.testcontainers.containers.ContainerState.copyFileToContainer(ContainerState.java:350)\\r\\n\\tat org.testcontainers.containers.ContainerState.copyFileToContainer(ContainerState.java:331)\\r\\n\\tat java.base/java.util.LinkedHashMap.forEach(LinkedHashMap.java:986)\\r\\n\\tat org.testcontainers.containers.GenericContainer.tryStart(GenericContainer.java:441)\\r\\n```\",\n" +
                    "        \"reactions\": {\n" +
                    "            \"url\": \"https://api.github.com/repos/testcontainers/testcontainers-java/issues/comments/1992649517/reactions\",\n" +
                    "            \"total_count\": 0,\n" +
                    "            \"+1\": 0,\n" +
                    "            \"-1\": 0,\n" +
                    "            \"laugh\": 0,\n" +
                    "            \"hooray\": 0,\n" +
                    "            \"confused\": 0,\n" +
                    "            \"heart\": 0,\n" +
                    "            \"rocket\": 0,\n" +
                    "            \"eyes\": 0\n" +
                    "        },\n" +
                    "        \"performed_via_github_app\": null\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"url\": \"https://api.github.com/repos/testcontainers/testcontainers-java/issues/comments/2041414862\",\n" +
                    "        \"html_url\": \"https://github.com/testcontainers/testcontainers-java/issues/8338#issuecomment-2041414862\",\n" +
                    "        \"issue_url\": \"https://api.github.com/repos/testcontainers/testcontainers-java/issues/8338\",\n" +
                    "        \"id\": 2041414862,\n" +
                    "        \"node_id\": \"IC_kwDOAgP_mc55rYTO\",\n" +
                    "        \"user\": {\n" +
                    "            \"login\": \"blommish\",\n" +
                    "            \"id\": 937168,\n" +
                    "            \"node_id\": \"MDQ6VXNlcjkzNzE2OA==\",\n" +
                    "            \"avatar_url\": \"https://avatars.githubusercontent.com/u/937168?v=4\",\n" +
                    "            \"gravatar_id\": \"\",\n" +
                    "            \"url\": \"https://api.github.com/users/blommish\",\n" +
                    "            \"html_url\": \"https://github.com/blommish\",\n" +
                    "            \"followers_url\": \"https://api.github.com/users/blommish/followers\",\n" +
                    "            \"following_url\": \"https://api.github.com/users/blommish/following{/other_user}\",\n" +
                    "            \"gists_url\": \"https://api.github.com/users/blommish/gists{/gist_id}\",\n" +
                    "            \"starred_url\": \"https://api.github.com/users/blommish/starred{/owner}{/repo}\",\n" +
                    "            \"subscriptions_url\": \"https://api.github.com/users/blommish/subscriptions\",\n" +
                    "            \"organizations_url\": \"https://api.github.com/users/blommish/orgs\",\n" +
                    "            \"repos_url\": \"https://api.github.com/users/blommish/repos\",\n" +
                    "            \"events_url\": \"https://api.github.com/users/blommish/events{/privacy}\",\n" +
                    "            \"received_events_url\": \"https://api.github.com/users/blommish/received_events\",\n" +
                    "            \"type\": \"User\",\n" +
                    "            \"site_admin\": false\n" +
                    "        },\n" +
                    "        \"created_at\": \"2024-04-07T10:35:03Z\",\n" +
                    "        \"updated_at\": \"2024-04-07T10:35:03Z\",\n" +
                    "        \"author_association\": \"NONE\",\n" +
                    "        \"body\": \"Do I understand this correctly, it will probably not be updated before spring has updated it to 1.26.1? \",\n" +
                    "        \"reactions\": {\n" +
                    "            \"url\": \"https://api.github.com/repos/testcontainers/testcontainers-java/issues/comments/2041414862/reactions\",\n" +
                    "            \"total_count\": 0,\n" +
                    "            \"+1\": 0,\n" +
                    "            \"-1\": 0,\n" +
                    "            \"laugh\": 0,\n" +
                    "            \"hooray\": 0,\n" +
                    "            \"confused\": 0,\n" +
                    "            \"heart\": 0,\n" +
                    "            \"rocket\": 0,\n" +
                    "            \"eyes\": 0\n" +
                    "        },\n" +
                    "        \"performed_via_github_app\": null\n" +
                    "    }\n" +
                    "]",
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

    private enum GithubClientCommands {
        PULLS, ISSUES
    }
}
