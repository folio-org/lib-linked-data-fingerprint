package org.folio.ld.fingerprint.service;

import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.model.ResourceEdge;
import org.folio.ld.dictionary.model.title.PrimaryTitle;
import org.folio.ld.fingerprint.config.FingerprintRules;
import org.folio.ld.fingerprint.config.FingerprintRules.FingerprintRule;
import org.folio.ld.fingerprint.exception.FingerprintException;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class FingerprintServiceImpl implements FingerprintService {
  private static final String TYPE_URI = "http://bibfra.me/purl/versa/type";
  private static final String MAIN_TITLE_KEY = "http://bibfra.me/vocab/marc/mainHeading";
  private final FingerprintRules rules;
  private final ObjectMapper mapper;

  @SneakyThrows
  @Override
  public String fingerprint(Resource resource) {
    var matchedRule = rules.getRules()
      .stream()
      .filter(rule -> resource.getTypeNames().equals(rule.types()))
      .findFirst();
    var fingerprintMap = getFingerprint(resource, matchedRule.orElse(null));
    return mapper.writeValueAsString(fingerprintMap);
  }

  private List<FingerprintEntry> getFingerprint(Resource resource, FingerprintRule rule) {
    if (isNull(rule)) {
      log.info("No rule is defined for resource type [{}], fingerprinting all properties and no edges",
        resource.getTypes());
    }
    var fingerprint = new ArrayList<>(collectTypes(resource));
    ofNullable(rule).flatMap(r -> collectLabel(resource, r.label())).ifPresent(fingerprint::add);
    fingerprint.addAll(collectProperties(resource, rule));
    fingerprint.addAll(collectEdges(resource, rule));
    return fingerprint.stream()
      .sorted(comparing(FingerprintEntry::key).thenComparing(FingerprintEntry::value))
      .toList();
  }

  private List<FingerprintEntry> collectTypes(Resource resource) {
    return resource.getTypes()
      .stream()
      .map(ResourceTypeDictionary::getUri)
      .map(type -> new FingerprintEntry(TYPE_URI, type))
      .toList();
  }

  private Optional<FingerprintEntry> collectLabel(Resource resource, boolean includeLabel) {
    if (includeLabel && nonNull(resource.getLabel())) {
      return Optional.of(new FingerprintEntry(LABEL.getValue(), resource.getLabel()));
    } else {
      return Optional.empty();
    }
  }

  private List<FingerprintEntry> collectProperties(Resource resource, FingerprintRule rule) {
    var propertiesFp = new ArrayList<FingerprintEntry>();
    if (isNull(resource.getDoc())) {
      return propertiesFp;
    }
    var doc = collectAndRemoveTitle(resource, nonNull(rule) && rule.title(), propertiesFp);
    mapper.convertValue(doc, new TypeReference<Map<String, List<String>>>() {
      }).entrySet().stream()
      .filter(e -> nonNull(rule.properties()) && PropertyDictionary.fromValue(e.getKey())
        .map(Enum::name)
        .map(p -> rule.properties().contains(p))
        .orElse(false)
      )
      .forEach(e -> e.getValue().forEach(v -> propertiesFp.add(new FingerprintEntry(e.getKey(), v))));
    return propertiesFp;
  }

  private ObjectNode collectAndRemoveTitle(Resource resource, boolean includeTitle, List<FingerprintEntry> fpEntry) {
    if (resource.getDoc().has(TITLE.getUri())) {
      if (includeTitle) {
        try {
          resource.getTitles(mapper)
            .stream()
            .filter(t -> t instanceof PrimaryTitle)
            .flatMap(t -> t.mainTitles.stream())
            .forEach(mt -> fpEntry.add(new FingerprintEntry(MAIN_TITLE_KEY, mt)));
        } catch (JsonProcessingException e) {
          throw new FingerprintException(TITLE.getUri(), resource.getDoc().textValue());
        }
      }
    }
    return resource.getDocWithoutTitles();
  }

  private List<FingerprintEntry> collectEdges(Resource resource, FingerprintRule rule) {
    var edgesFp = new ArrayList<FingerprintEntry>();
    if (nonNull(rule) && nonNull(rule.edges())) {
      ofNullable(resource.getOutgoingEdges())
        .ifPresent(edges -> edges.forEach(
          oe -> rule.edges().stream()
            .filter(r -> filterEdgeRule(oe, r))
            .findFirst()
            .ifPresent(edgeRule -> edgesFp.addAll(getEdgeFingerprint(oe.getTarget(), edgeRule))))
        );
    }
    return edgesFp;
  }

  private boolean filterEdgeRule(ResourceEdge oe, FingerprintRules.EdgeRule edgeRule) {
    return (isNull(edgeRule.types()) || edgeRule.types().equals(oe.getTarget().getTypeNames()))
      && (isNull(edgeRule.predicate()) || edgeRule.predicate().equals(oe.getPredicate().name()));
  }

  private List<FingerprintEntry> getEdgeFingerprint(Resource resource, FingerprintRules.EdgeRule edgeRule) {
    var fingerprint = new ArrayList<FingerprintEntry>();
    collectLabel(resource, edgeRule.label()).ifPresent(fingerprint::add);
    fingerprint.addAll(collectEdgeProperties(resource, edgeRule));
    return fingerprint;
  }

  private List<FingerprintEntry> collectEdgeProperties(Resource resource, FingerprintRules.EdgeRule rule) {
    var propertiesFp = new ArrayList<FingerprintEntry>();
    if (!rule.title() && isNull(rule.edgeProperties()) || isNull(resource.getDoc())) {
      return propertiesFp;
    }
    var doc = collectAndRemoveTitle(resource, rule.title(), propertiesFp);
    if (isNull(rule.edgeProperties())) {
      return propertiesFp;
    }
    mapper.convertValue(doc, new TypeReference<Map<String, List<String>>>() {
      }).entrySet().stream()
      .flatMap(e -> PropertyDictionary.fromValue(e.getKey())
        .map(Enum::name)
        .filter(rule.edgeProperties()::containsKey)
        .stream()
        .flatMap(p -> e.getValue().stream().map(v -> new FingerprintEntry(rule.edgeProperties().get(p), v)))
      )
      .forEach(propertiesFp::add);
    return propertiesFp;
  }

  public record FingerprintEntry(String key, String value) {
  }
}
