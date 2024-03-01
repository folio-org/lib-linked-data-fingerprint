package org.folio.ld.fingerprint.config;

import java.util.List;
import java.util.Set;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@ConfigurationProperties
@PropertySource(value = "classpath:fingerprint-rules.yml", factory = YamlPropertySourceFactory.class)
public class FingerprintRules {

  private List<FingerprintRule> rules;

  public record FingerprintRule(Set<String> types,
                                boolean label,
                                Set<String> properties,
                                String predicate,
                                Set<FingerprintRule> edges) {
  }

}
