package com.oneid.qualityboard.security;

import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reads exact quality scopes from the mounted, deployment-owned access file.
 */
@Service
public class QualityScopeAccessService {

    private static final String ROLE_PREFIX = "ROLE_";

    private final Map<String, Set<String>> userScopes;
    private final Map<String, Set<String>> roleScopes;

    public QualityScopeAccessService(QualityScopeAccessProperties properties) {
        Map<String, Object> root = loadRules(properties.getFile());
        this.userScopes = readGrants(root, "user_ids");
        this.roleScopes = readGrants(root, "roles");
    }

    public Set<String> allowedScopes(Long userId, Set<String> roles) {
        Set<String> grantedScopes = new LinkedHashSet<>();
        if (userId != null) {
            grantedScopes.addAll(userScopes.getOrDefault(String.valueOf(userId), Collections.emptySet()));
        }
        if (roles != null) {
            roles.stream()
                    .filter(role -> role != null && !role.isBlank())
                    .map(QualityScopeAccessService::normalizeRole)
                    .forEach(role -> grantedScopes.addAll(roleScopes.getOrDefault(role, Collections.emptySet())));
        }
        return Collections.unmodifiableSet(grantedScopes);
    }

    private static String normalizeRole(String role) {
        return role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role;
    }

    private static Map<String, Object> loadRules(String configuredPath) {
        if (configuredPath == null || configuredPath.isBlank()) {
            throw new IllegalStateException("quality scope access file must be configured");
        }

        Path path = Path.of(configuredPath);
        if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
            throw new IllegalStateException("quality scope access file is unavailable: " + path);
        }

        try (InputStream inputStream = Files.newInputStream(path)) {
            Object document = new Yaml(new SafeConstructor(new LoaderOptions())).load(inputStream);
            if (document == null) {
                return Collections.emptyMap();
            }
            if (!(document instanceof Map<?, ?> rawRoot)) {
                throw new IllegalStateException("quality scope access file must contain a YAML map");
            }

            Map<String, Object> root = new LinkedHashMap<>();
            rawRoot.forEach((key, value) -> {
                if (!(key instanceof String stringKey) || stringKey.isBlank()) {
                    throw new IllegalStateException("quality scope access file contains an invalid key");
                }
                root.put(stringKey, value);
            });
            return root;
        } catch (IOException | YAMLException exception) {
            throw new IllegalStateException("failed to load quality scope access file: " + path, exception);
        }
    }

    private static Map<String, Set<String>> readGrants(Map<String, Object> root, String sectionName) {
        Object section = root.get(sectionName);
        if (section == null) {
            return Collections.emptyMap();
        }
        if (!(section instanceof Map<?, ?> rawGrants)) {
            throw new IllegalStateException("quality scope access section '" + sectionName + "' must be a map");
        }

        Map<String, Set<String>> grants = new LinkedHashMap<>();
        rawGrants.forEach((subject, rawScopes) -> {
            if (!(subject instanceof String subjectKey) || subjectKey.isBlank()) {
                throw new IllegalStateException("quality scope access section '" + sectionName + "' contains an invalid subject");
            }
            grants.put(subjectKey, readScopeList(rawScopes, sectionName, subjectKey));
        });
        return Collections.unmodifiableMap(grants);
    }

    private static Set<String> readScopeList(Object rawScopes, String sectionName, String subject) {
        if (!(rawScopes instanceof List<?> scopeList)) {
            throw new IllegalStateException("quality scope access grant for '" + subject + "' in '" + sectionName + "' must be a list");
        }

        Set<String> scopes = new LinkedHashSet<>();
        for (Object rawScope : scopeList) {
            if (!(rawScope instanceof String scope) || !isValidScopeKey(scope)) {
                throw new IllegalStateException("invalid quality scope entry: " + rawScope);
            }
            scopes.add(scope);
        }
        return Collections.unmodifiableSet(scopes);
    }

    private static boolean isValidScopeKey(String value) {
        int separator = value.indexOf(':');
        return separator > 0
                && separator == value.lastIndexOf(':')
                && separator < value.length() - 1
                && value.equals(value.trim())
                && !value.contains(" ");
    }
}
