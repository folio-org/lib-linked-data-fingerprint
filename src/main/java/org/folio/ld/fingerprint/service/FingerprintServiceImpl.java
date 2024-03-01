package org.folio.ld.fingerprint.service;

import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.fingerprint.config.FingerprintRules.FingerprintRule;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.ld.fingerprint.config.FingerprintRules;
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
    return mapper.valueToTree(fingerprintMap).toPrettyString();
  }

  private List<SimpleEntry<String, Object>> getFingerprint(Resource resource, FingerprintRule rule) {
    if (isNull(rule)) {
      log.info("No rule is defined for resource type [{}], fingerprinting all properties and no edges",
        resource.getTypes());
    }
    var fingerprint = new ArrayList<SimpleEntry<String, Object>>();
    fingerprint.add(collectTypes(resource));
    collectLabel(resource, rule).ifPresent(fingerprint::add);
    fingerprint.addAll(collectProperties(resource, rule));
    fingerprint.addAll(collectEdges(resource, rule));
    return fingerprint;
  }

  private SimpleEntry<String, Object> collectTypes(Resource resource) {
    var typeUris = resource.getTypes().stream().map(ResourceTypeDictionary::getUri).sorted().toList();
    return new SimpleEntry<>(TYPE_IRI, typeUris);
  }

  private Optional<SimpleEntry<String, Object>> collectLabel(Resource resource, FingerprintRule rule) {
    if (nonNull(rule) && rule.label() && nonNull(resource.getLabel())) {
      return Optional.of(new SimpleEntry<>(LABEL.getValue(), List.of(resource.getLabel())));
    } else {
      return Optional.empty();
    }
  }

  private List<SimpleEntry<String, Object>> collectProperties(Resource resource, FingerprintRule rule) {
    var propertiesFp = new ArrayList<SimpleEntry<String, Object>>();
    mapper.convertValue(resource.getDoc(), new TypeReference<Map<String, List<String>>>() {
      }).entrySet().stream()
      .filter(e -> isNull(rule) || nonNull(rule.properties()) && PropertyDictionary.fromValue(e.getKey())
        .map(Enum::name)
        .map(p -> rule.properties().contains(p))
        .orElse(false)
      )
      .sorted(comparing(Map.Entry<String, List<String>>::getKey).thenComparing(map -> String.join(",", map.getValue())))
      .forEach(e -> propertiesFp.add(new SimpleEntry<>(e.getKey(), e.getValue().stream().sorted().toList())));
    return propertiesFp;
  }

  private List<SimpleEntry<String, Object>> collectEdges(Resource resource, FingerprintRule rule) {
    var edgesFp = new ArrayList<SimpleEntry<String, Object>>();
    if (nonNull(rule) && nonNull(rule.edges())) {
      ofNullable(resource.getOutgoingEdges())
        .ifPresent(edges -> edges.forEach(
          oe -> rule.edges().stream()
            .filter(r -> filterEdgeRule(oe, r))
            .findFirst()
            .map(edgeRule -> {
              var edgeFp = getFingerprint(oe.getTarget(), edgeRule);
              var typeFp = edgeFp.stream().filter(se -> se.getKey().equals(TYPE_IRI)).findFirst().orElseThrow();
              edgeFp.remove(typeFp);
              return new SimpleEntry<String, Object>(
                oe.getPredicate().getUri() + " -> " + String.join(",", (List<String>) typeFp.getValue()), edgeFp);
            })
            .ifPresent(edgesFp::add)));
    }
    return edgesFp.stream()
      .filter(se -> !((List<SimpleEntry<String, List<SimpleEntry<String, List<String>>>>>) se.getValue()).isEmpty())
      .sorted(comparing(SimpleEntry<String, Object>::getKey)
        .thenComparing(se -> {
          var value = (List<SimpleEntry<String, List<SimpleEntry<String, List<String>>>>>) se.getValue();
          return String.join(",", value.stream().map(v -> v.getKey() + v.getValue()).collect(Collectors.joining(",")));
        })).collect(Collectors.toList());
  }

  private boolean filterEdgeRule(ResourceEdge oe, FingerprintRule fr) {
    return (isNull(fr.types()) || fr.types().equals(oe.getTarget().getTypeNames()))
      && (isNull(fr.predicate()) || fr.predicate().equals(oe.getPredicate().name()));
  }

}
