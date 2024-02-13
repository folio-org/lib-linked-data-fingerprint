package org.folio.ld.fingerprint.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public record Resource(
  JsonNode doc,
  List<String> types) {
}

