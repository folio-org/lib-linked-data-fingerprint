package org.folio.ld.fingerprint.config;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.Data;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@ConfigurationProperties
@PropertySource(value = "classpath:fingerprint-rules.yml", factory = YamlPropertySourceFactory.class)
public class FingerprintRules {

  private List<FingerprintRule> rules;

  /**
   * Replace keys in the configuration with the corresponding IRI.
   */
  @PostConstruct
  public void replaceKeyWithIri() {
    this.rules = rules.stream()
      .map(this::replaceKeyWithIri)
      .toList();
  }

  private FingerprintRule replaceKeyWithIri(FingerprintRule rule) {
    Set<String> types = rule.types.stream()
      .map(ResourceTypeDictionary::valueOf)
      .map(ResourceTypeDictionary::getUri)
      .collect(Collectors.toSet());

    Set<String> fields = rule.fields.stream()
      .map(PropertyDictionary::valueOf)
      .map(PropertyDictionary::getValue)
      .collect(Collectors.toSet());

    return new FingerprintRule(types, fields);
  }

  public record FingerprintRule(
    Set<String> types,
    Set<String> fields) {
  }
}
