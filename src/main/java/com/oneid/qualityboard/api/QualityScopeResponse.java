package com.oneid.qualityboard.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A quality-data scope the current caller is allowed to read.
 */
public record QualityScopeResponse(
        String scope,
        @JsonProperty("scope_id") String scopeId,
        String label
) {

    static QualityScopeResponse fromScopeKey(String scopeKey) {
        int separator = scopeKey.indexOf(':');
        String scope = scopeKey.substring(0, separator);
        String scopeId = scopeKey.substring(separator + 1);
        return new QualityScopeResponse(scope, scopeId, scope + " " + scopeId);
    }
}
