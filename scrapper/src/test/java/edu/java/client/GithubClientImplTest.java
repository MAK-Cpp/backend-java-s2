package edu.java.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import edu.java.response.Author;
import edu.java.response.Commit;
import edu.java.response.CommitResponse;
import edu.java.response.Committer;
import edu.java.response.IssueResponse;
import edu.java.response.PullRequestResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
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
    private static final GithubClient githubClient = new GithubClientImpl(URL);

    @BeforeEach
    public void beforeEach() {
        wireMockServer = new WireMockServer(HTTP_ENDPOINT_PORT);
        wireMockServer.start();
        configureFor("localhost", HTTP_ENDPOINT_PORT);
    }

    public static Stream<Arguments> testGetCommits() {
        return Stream.of(
            Arguments.of(
                "octocat",
                "Hello-World",
                "[\n" +
                    "    {\n" +
                    "        \"sha\": \"7fd1a60b01f91b314f59955a4e4d4e80d8edf11d\",\n" +
                    "        \"node_id\": \"MDY6Q29tbWl0MTI5NjI2OTo3ZmQxYTYwYjAxZjkxYjMxNGY1OTk1NWE0ZTRkNGU4MGQ4ZWRmMTFk\",\n" +
                    "        \"commit\": {\n" +
                    "            \"author\": {\n" +
                    "                \"name\": \"The Octocat\",\n" +
                    "                \"email\": \"octocat@nowhere.com\",\n" +
                    "                \"date\": \"2012-03-06T23:06:50Z\"\n" +
                    "            },\n" +
                    "            \"committer\": {\n" +
                    "                \"name\": \"The Octocat\",\n" +
                    "                \"email\": \"octocat@nowhere.com\",\n" +
                    "                \"date\": \"2012-03-06T23:06:50Z\"\n" +
                    "            },\n" +
                    "            \"message\": \"Merge pull request #6 from Spaceghost/patch-1\\n\\nNew line at end of file.\",\n" +
                    "            \"tree\": {\n" +
                    "                \"sha\": \"b4eecafa9be2f2006ce1b709d6857b07069b4608\",\n" +
                    "                \"url\": \"https://api.github.com/repos/octocat/Hello-World/git/trees/b4eecafa9be2f2006ce1b709d6857b07069b4608\"\n" +
                    "            },\n" +
                    "            \"url\": \"https://api.github.com/repos/octocat/Hello-World/git/commits/7fd1a60b01f91b314f59955a4e4d4e80d8edf11d\",\n" +
                    "            \"comment_count\": 88,\n" +
                    "            \"verification\": {\n" +
                    "                \"verified\": false,\n" +
                    "                \"reason\": \"unsigned\",\n" +
                    "                \"signature\": null,\n" +
                    "                \"payload\": null\n" +
                    "            }\n" +
                    "        },\n" +
                    "        \"url\": \"https://api.github.com/repos/octocat/Hello-World/commits/7fd1a60b01f91b314f59955a4e4d4e80d8edf11d\",\n" +
                    "        \"html_url\": \"https://github.com/octocat/Hello-World/commit/7fd1a60b01f91b314f59955a4e4d4e80d8edf11d\",\n" +
                    "        \"comments_url\": \"https://api.github.com/repos/octocat/Hello-World/commits/7fd1a60b01f91b314f59955a4e4d4e80d8edf11d/comments\",\n" +
                    "        \"author\": {\n" +
                    "            \"login\": \"octocat\",\n" +
                    "            \"id\": 583231,\n" +
                    "            \"node_id\": \"MDQ6VXNlcjU4MzIzMQ==\",\n" +
                    "            \"avatar_url\": \"https://avatars.githubusercontent.com/u/583231?v=4\",\n" +
                    "            \"gravatar_id\": \"\",\n" +
                    "            \"url\": \"https://api.github.com/users/octocat\",\n" +
                    "            \"html_url\": \"https://github.com/octocat\",\n" +
                    "            \"followers_url\": \"https://api.github.com/users/octocat/followers\",\n" +
                    "            \"following_url\": \"https://api.github.com/users/octocat/following{/other_user}\",\n" +
                    "            \"gists_url\": \"https://api.github.com/users/octocat/gists{/gist_id}\",\n" +
                    "            \"starred_url\": \"https://api.github.com/users/octocat/starred{/owner}{/repo}\",\n" +
                    "            \"subscriptions_url\": \"https://api.github.com/users/octocat/subscriptions\",\n" +
                    "            \"organizations_url\": \"https://api.github.com/users/octocat/orgs\",\n" +
                    "            \"repos_url\": \"https://api.github.com/users/octocat/repos\",\n" +
                    "            \"events_url\": \"https://api.github.com/users/octocat/events{/privacy}\",\n" +
                    "            \"received_events_url\": \"https://api.github.com/users/octocat/received_events\",\n" +
                    "            \"type\": \"User\",\n" +
                    "            \"site_admin\": false\n" +
                    "        },\n" +
                    "        \"committer\": {\n" +
                    "            \"login\": \"octocat\",\n" +
                    "            \"id\": 583231,\n" +
                    "            \"node_id\": \"MDQ6VXNlcjU4MzIzMQ==\",\n" +
                    "            \"avatar_url\": \"https://avatars.githubusercontent.com/u/583231?v=4\",\n" +
                    "            \"gravatar_id\": \"\",\n" +
                    "            \"url\": \"https://api.github.com/users/octocat\",\n" +
                    "            \"html_url\": \"https://github.com/octocat\",\n" +
                    "            \"followers_url\": \"https://api.github.com/users/octocat/followers\",\n" +
                    "            \"following_url\": \"https://api.github.com/users/octocat/following{/other_user}\",\n" +
                    "            \"gists_url\": \"https://api.github.com/users/octocat/gists{/gist_id}\",\n" +
                    "            \"starred_url\": \"https://api.github.com/users/octocat/starred{/owner}{/repo}\",\n" +
                    "            \"subscriptions_url\": \"https://api.github.com/users/octocat/subscriptions\",\n" +
                    "            \"organizations_url\": \"https://api.github.com/users/octocat/orgs\",\n" +
                    "            \"repos_url\": \"https://api.github.com/users/octocat/repos\",\n" +
                    "            \"events_url\": \"https://api.github.com/users/octocat/events{/privacy}\",\n" +
                    "            \"received_events_url\": \"https://api.github.com/users/octocat/received_events\",\n" +
                    "            \"type\": \"User\",\n" +
                    "            \"site_admin\": false\n" +
                    "        },\n" +
                    "        \"parents\": [\n" +
                    "            {\n" +
                    "                \"sha\": \"553c2077f0edc3d5dc5d17262f6aa498e69d6f8e\",\n" +
                    "                \"url\": \"https://api.github.com/repos/octocat/Hello-World/commits/553c2077f0edc3d5dc5d17262f6aa498e69d6f8e\",\n" +
                    "                \"html_url\": \"https://github.com/octocat/Hello-World/commit/553c2077f0edc3d5dc5d17262f6aa498e69d6f8e\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"sha\": \"762941318ee16e59dabbacb1b4049eec22f0d303\",\n" +
                    "                \"url\": \"https://api.github.com/repos/octocat/Hello-World/commits/762941318ee16e59dabbacb1b4049eec22f0d303\",\n" +
                    "                \"html_url\": \"https://github.com/octocat/Hello-World/commit/762941318ee16e59dabbacb1b4049eec22f0d303\"\n" +
                    "            }\n" +
                    "        ]\n" +
                    "    }\n" +
                    "]",
                List.of(
                    new CommitResponse(
                        new Commit(
                            new Author(
                                "The Octocat",
                                "octocat@nowhere.com",
                                OffsetDateTime.parse("2012-03-06T23:06:50Z")
                            ),
                            new Committer(
                                "The Octocat",
                                "octocat@nowhere.com",
                                OffsetDateTime.parse("2012-03-06T23:06:50Z")
                            ),
                            "Merge pull request #6 from Spaceghost/patch-1\n\nNew line at end of file."
                        )
                    )
                )
            )
        );
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

    void testCommand(String owner, String repo, GithubClientCommands command, String body, List<?> result) {
        MappingBuilder builder = get("/repos/" + owner + "/" + repo + "/" + command.command);
        stubFor(builder.willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(body)));
        List<?> output = switch (command) {
            case COMMITS -> githubClient.getCommits(owner, repo).block();
            case PULL_REQUESTS -> githubClient.getPullRequests(owner, repo).block();
            case ISSUES -> githubClient.getIssues(owner, repo).block();
        };
        assertThat(output).isEqualTo(result);
    }

    @ParameterizedTest
    @MethodSource
    void testGetCommits(String owner, String repo, String body, List<CommitResponse> result) {
        testCommand(owner, repo, GithubClientCommands.COMMITS, body, result);
    }

    @ParameterizedTest
    @MethodSource
    void testGetPullRequests(String owner, String repo, String body, List<PullRequestResponse> result) {
        testCommand(owner, repo, GithubClientCommands.PULL_REQUESTS, body, result);
    }

    @ParameterizedTest
    @MethodSource
    void testGetIssues(String owner, String repo, String body, List<IssueResponse> result) {
        testCommand(owner, repo, GithubClientCommands.ISSUES, body, result);
    }

    @AfterEach
    public void afterEach() {
        wireMockServer.stop();
    }

    private enum GithubClientCommands {
        COMMITS("commits"), PULL_REQUESTS("pulls"), ISSUES("issues");
        private final String command;

        GithubClientCommands(String command) {
            this.command = command;
        }
    }
}
