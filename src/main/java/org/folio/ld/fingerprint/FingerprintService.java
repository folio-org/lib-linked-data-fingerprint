package org.folio.ld.fingerprint;

import static org.folio.ld.fingerprint.HashUtil.hash;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.folio.ld.fingerprint.config.FingerprintRules;
import org.folio.ld.fingerprint.model.FingerprintedResource;
import org.folio.ld.fingerprint.model.Resource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FingerprintService {
  // TODO move to lib-linked-data-dictionary
  private static final String TYPE_IRI = "http://bibfra.me/purl/versa/type";
  private static final String FINGERPRINT_IRI = "http://library.link/vocab/fingerprint";

  private final FingerprintRules rules;
  private final ObjectMapper mapper;


  /**
   * Fingerprints the given resource description and returns the fingerprinted resource back.
   *
   * @param resource Resource to fingerprint
   * @return The fingerprinted resource
   */
  public FingerprintedResource fingerprint(Resource resource) {
    var matchedRule = rules.getRules()
      .stream()
      .filter(rule -> new HashSet<>(resource.types()).containsAll(rule.types()))
      .findFirst();

    var fingerprint = matchedRule
      .map(FingerprintRules.FingerprintRule::fields)
      .map(fingerprintFields -> getFingerprint(resource, fingerprintFields))
      .orElseGet(() -> getFingerprint(resource)); // Fallback - use all properties for fingerprinting if no rule exists
    return getFingerprintedResource(fingerprint, resource.doc());
  }

  private FingerprintedResource getFingerprintedResource(List<List<String>> fingerprint, JsonNode sourceDoc) {
    var fingerprintDoc = mapper.valueToTree(fingerprint);
    var sourceDocWithFingerprint = ((ObjectNode) sourceDoc).set(FINGERPRINT_IRI, fingerprintDoc);
    return new FingerprintedResource(sourceDocWithFingerprint, hash(fingerprintDoc));
  }

  private List<List<String>> getFingerprint(Resource resource, Set<String> fingerprintFields) {
    var typesFingerprint = getKeyValuePairs(TYPE_IRI, resource.types());

    Map<String, List<String>> docMap = mapper.convertValue(resource.doc(),
      new TypeReference<>() {
      });

    var fieldValueFingerprint = docMap.keySet()
      .stream()
      .filter(fingerprintFields::contains)
      .sorted()
      .flatMap(key -> getKeyValuePairs(key, docMap.get(key)).stream())
      .toList();

    return Stream.concat(typesFingerprint.stream(), fieldValueFingerprint.stream())
      .toList();
  }

  private List<List<String>> getFingerprint(Resource resource) {
    var allFields = mapper.convertValue(resource.doc(), new TypeReference<Map<String, ?>>() {
    }).keySet();
    return getFingerprint(resource, allFields);
  }

  private List<List<String>> getKeyValuePairs(String key, List<String> values) {
    return values
      .stream()
      .sorted()
      .map(val -> List.of(key, val))
      .toList();
  }
}
