package org.folio.ld.fingerprint;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Set;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.fingerprint.config.FingerprintRules;
import org.folio.ld.fingerprint.test.SpringTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@EnableConfigurationProperties
@SpringBootTest(classes = SpringTestConfig.class)
class FingerprintRulesValidationIT {

  @Autowired
  private FingerprintRules rules;

  @Test
  void validateFingerprintRules() {
    validateTypes();
    validateProperties();
    validatePredicates();
  }

  private void validateTypes() {
    var types = rules.getRules()
      .stream()
      .map(FingerprintRules.FingerprintRule::types)
      .flatMap(Set::stream)
      .collect(toSet());

    var edgeTypes =  rules.getRules().stream()
      .filter(rule -> nonNull(rule.edges()))
      .flatMap(rule -> rule.edges().stream())
      .filter(edgeRule -> nonNull(edgeRule.types()))
      .flatMap(edgeRule ->  edgeRule.types().stream())
      .collect(toSet());

    assertThat(types).isNotEmpty();
    assertThat(edgeTypes).isNotEmpty();

    concat(types.stream(), edgeTypes.stream())
      .forEach(
        type -> assertThatCode(() -> ResourceTypeDictionary.valueOf(type)).doesNotThrowAnyException()
      );
  }

  private void validateProperties() {
    var properties = rules.getRules()
      .stream()
      .filter(rule -> nonNull(rule.properties()))
      .flatMap(rule -> rule.properties().stream())
      .collect(toSet());

    var edgeProperties = rules.getRules().stream()
      .filter(rule -> nonNull(rule.edges()))
      .flatMap(rule -> rule.edges().stream())
      .filter(edgeRule -> nonNull(edgeRule.edgeProperties()))
      .flatMap(edgeRule -> edgeRule.edgeProperties().keySet().stream())
      .collect(toSet());

    assertThat(properties).isNotEmpty();
    assertThat(edgeProperties).isNotEmpty();

    concat(properties.stream(), edgeProperties.stream())
      .forEach(
        property -> assertThatCode(() -> PropertyDictionary.valueOf(property)).doesNotThrowAnyException()
      );
  }

  private void validatePredicates() {
    var predicates = rules.getRules().stream()
      .filter(rule -> nonNull(rule.edges()))
      .flatMap(rule -> rule.edges().stream())
      .map(FingerprintRules.EdgeRule::predicate)
      .collect(toSet());

    assertThat(predicates).isNotEmpty();

    predicates.forEach(
      predicate -> assertThatCode(() -> PredicateDictionary.valueOf(predicate)).doesNotThrowAnyException()
    );
  }
}
