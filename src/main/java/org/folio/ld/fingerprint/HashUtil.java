package org.folio.ld.fingerprint;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import lombok.experimental.UtilityClass;

@UtilityClass
public class HashUtil {
  public static long hash(JsonNode node) {
    return Hashing.murmur3_128().hashString(node.toString(), StandardCharsets.UTF_8).padToLong();
  }
}
