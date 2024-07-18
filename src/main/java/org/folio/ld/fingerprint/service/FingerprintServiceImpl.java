package org.folio.ld.fingerprint.service;

import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.ld.fingerprint.config.FingerprintRules;
import org.folio.ld.fingerprint.config.FingerprintRules.FingerprintRule;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class FingerprintServiceImpl implements FingerprintService {
  private static final String TYPE_IRI = "http://bibfra.me/purl/versa/type";
  private final FingerprintRules rules;
  private final ObjectMapper mapper;

  @Override
  public String fingerprint(Resource resource) {
    var matchedRule = rules.getRules()
      .stream()
      .filter(rule -> resource.getTypeNames().equals(rule.types()))
      .findFirst();
    var fingerprintMap = getFingerprint(resource, matchedRule.orElse(null));
    return mapper.valueToTree(fingerprintMap).toString();
  }

  private List<List<String>> getFingerprint(Resource resource, FingerprintRule rule) {
    if (isNull(rule)) {
      log.info("No rule is defined for resource type [{}], fingerprinting all properties and no edges",
        resource.getTypes());
    }
    var fingerprint = new ArrayList<>(collectTypes(resource));
    ofNullable(rule).flatMap(r -> collectLabel(resource, r.label())).ifPresent(fingerprint::add);
    fingerprint.addAll(collectProperties(resource, rule));
    fingerprint.addAll(collectEdges(resource, rule));
    return fingerprint;
  }

  private List<List<String>> collectTypes(Resource resource) {
    return resource.getTypes().stream().map(ResourceTypeDictionary::getUri).sorted()
      .map(type -> List.of(TYPE_IRI, type))
      .toList();
  }

  private Optional<List<String>> collectLabel(Resource resource, boolean includeLabel) {
    if (includeLabel && nonNull(resource.getLabel())) {
      return Optional.of(List.of(LABEL.getValue(), resource.getLabel()));
    } else {
      return Optional.empty();
    }
  }

  private List<List<String>> collectProperties(Resource resource, FingerprintRule rule) {
    var propertiesFp = new ArrayList<List<String>>();
    if (isNull(resource.getDoc())) {
      return propertiesFp;
    }
    mapper.convertValue(resource.getDoc(), new TypeReference<Map<String, List<String>>>() {
      }).entrySet().stream()
      .filter(e -> isNull(rule) || nonNull(rule.properties()) && PropertyDictionary.fromValue(e.getKey())
        .map(Enum::name)
        .map(p -> rule.properties().contains(p))
        .orElse(false)
      )
      .sorted(comparing(Map.Entry<String, List<String>>::getKey).thenComparing(map -> String.join(",", map.getValue())))
      .forEach(e -> e.getValue().stream().sorted().forEach(v -> propertiesFp.add(List.of(e.getKey(), v))));
    return propertiesFp;
  }

  private List<List<String>> collectEdges(Resource resource, FingerprintRule rule) {
    var edgesFp = new ArrayList<List<String>>();
    if (nonNull(rule) && nonNull(rule.edges())) {
      ofNullable(resource.getOutgoingEdges())
        .ifPresent(edges -> edges.forEach(
          oe -> rule.edges().stream()
            .filter(r -> filterEdgeRule(oe, r))
            .findFirst()
            .ifPresent(edgeRule -> edgesFp.addAll(getEdgeFingerprint(oe.getTarget(), edgeRule))))
        );
    }
    return edgesFp.stream()
      .sorted(comparing((List<String> pair) -> pair.get(0)).thenComparing(pair -> pair.get(1)))
      .toList();
  }

  private boolean filterEdgeRule(ResourceEdge oe, FingerprintRules.EdgeRule edgeRule) {
    return (isNull(edgeRule.types()) || edgeRule.types().equals(oe.getTarget().getTypeNames()))
      && (isNull(edgeRule.predicate()) || edgeRule.predicate().equals(oe.getPredicate().name()));
  }

  private List<List<String>> getEdgeFingerprint(Resource resource, FingerprintRules.EdgeRule edgeRule) {
    var fingerprint = new ArrayList<List<String>>();
    collectLabel(resource, edgeRule.label()).ifPresent(fingerprint::add);
    fingerprint.addAll(collectEdgeProperties(resource, edgeRule));
    return fingerprint;
  }

  private List<List<String>> collectEdgeProperties(Resource resource, FingerprintRules.EdgeRule rule) {
    var propertiesFp = new ArrayList<List<String>>();
    if (isNull(rule.edgeProperties()) || isNull(resource.getDoc())) {
      return propertiesFp;
    }
    mapper.convertValue(resource.getDoc(), new TypeReference<Map<String, List<String>>>() {
      }).entrySet().stream()
      .flatMap(e -> PropertyDictionary.fromValue(e.getKey())
        .map(Enum::name)
        .filter(rule.edgeProperties()::containsKey)
        .stream()
        .flatMap(p -> e.getValue().stream().map(v -> List.of(rule.edgeProperties().get(p), v)).toList().stream())
      )
      .forEach(propertiesFp::add);
    return propertiesFp;
  }

}
