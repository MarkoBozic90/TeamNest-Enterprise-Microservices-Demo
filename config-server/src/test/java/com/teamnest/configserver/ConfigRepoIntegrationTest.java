package com.teamnest.configserver;


import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConfigRepoIntegrationTest {

    private static File tempDir;
    private static String repoUri;

    @Autowired
    private TestRestTemplate rest;

    @BeforeAll
    static void setupRepo() throws IOException, GitAPIException {
        tempDir = Files.createTempDirectory("config-repo-").toFile();

        // init repo with main as default
        try (Git git = Git.init()
            // if your JGit supports it; otherwise create+checkout below
            .setInitialBranch("main")
            .setDirectory(tempDir)
            .call()) {

            // write config-service.yml at repo root
            File appYaml = new File(tempDir, "config-service.yml");
            try (FileWriter fw = new FileWriter(appYaml)) {
                fw.write("""
                        example:
                          message: "Hello from test repo"
                        """);
            }

            git.add().addFilepattern(".").call();
            git.commit().setMessage("initial commit").call();

            // For older JGit without setInitialBranch:
            // git.branchCreate().setName("main").call();
            // git.checkout().setName("main").call();
        }

        repoUri = "file:///" + tempDir.getAbsolutePath().replace("\\", "/");
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.cloud.config.server.git.uri", () -> repoUri);
        r.add("spring.cloud.config.server.git.default-label", () -> "main");
        r.add("spring.cloud.config.server.git.clone-on-start", () -> "true");
        r.add("spring.cloud.config.server.git.force-pull", () -> "true");

        // Basic auth for your SecurityConfig
        r.add("spring.security.user.name", () -> "config");
        r.add("spring.security.user.password", () -> "changeme");
        r.add("spring.security.user.roles", () -> "ADMIN");
    }

    @AfterAll
    static void cleanup() {
        if (tempDir != null) tempDir.deleteOnExit();
    }

    @Test
    void shouldServeConfigFromGitRepo() {
        TestRestTemplate admin = rest.withBasicAuth("config", "changeme");
        var resp = admin.getForEntity("/config-service/default", String.class);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).contains("example", "Hello from test repo");
    }
}
