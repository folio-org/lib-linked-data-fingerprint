package org.folio.ld.fingerprint.model;

import com.fasterxml.jackson.databind.JsonNode;

public record FingerprintedResource(JsonNode fingerprintedDoc, Long hash) {
}
