package com.oneid.qualityboard.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QualityScopeAccessServiceTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void combinesUserAndNormalizedRoleGrants() throws IOException {
        QualityScopeAccessService service = serviceFor("""
                user_ids:
                  \"10001\": [\"brand:1001\"]
                roles:
                  ROLE_QUALITY_READER: [\"brand:1001\", \"brand:1002\"]
                """);

        assertThat(service.allowedScopes(10001L, Set.of("QUALITY_READER")))
                .containsExactlyInAnyOrder("brand:1001", "brand:1002");
        assertThat(service.allowedScopes(10001L, Set.of("ROLE_QUALITY_READER")))
                .containsExactlyInAnyOrder("brand:1001", "brand:1002");
    }

    @Test
    void returnsNoScopesWhenUserAndRolesHaveNoGrant() throws IOException {
        QualityScopeAccessService service = serviceFor("""
                user_ids:
                  \"10001\": [\"brand:1001\"]
                roles:
                  ROLE_QUALITY_READER: [\"brand:1002\"]
                """);

        assertThat(service.allowedScopes(99999L, Set.of())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "version: 1\n"})
    void emptyOrSectionlessRulesFailClosed(String yaml) throws IOException {
        QualityScopeAccessService service = serviceFor(yaml);

        assertThat(service.allowedScopes(10001L, Set.of("QUALITY_READER"))).isEmpty();
    }

    @Test
    void rejectsAnInvalidScopeEntryAtStartup() throws IOException {
        Path config = writeConfig("""
                user_ids:
                  \"10001\": [\"brand:\"]
                roles: {}
                """);

        assertThatThrownBy(() -> new QualityScopeAccessService(propertiesFor(config)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("brand:");
    }

    @Test
    void rejectsAnUnavailableConfigurationFileAtStartup() {
        Path missingConfig = temporaryDirectory.resolve("missing.yml");

        assertThatThrownBy(() -> new QualityScopeAccessService(propertiesFor(missingConfig)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("quality scope access file");
    }

    private QualityScopeAccessService serviceFor(String yaml) throws IOException {
        return new QualityScopeAccessService(propertiesFor(writeConfig(yaml)));
    }

    private Path writeConfig(String yaml) throws IOException {
        Path config = temporaryDirectory.resolve("quality-scope-access.yml");
        return Files.writeString(config, yaml);
    }

    private QualityScopeAccessProperties propertiesFor(Path config) {
        QualityScopeAccessProperties properties = new QualityScopeAccessProperties();
        properties.setFile(config.toString());
        return properties;
    }
}
