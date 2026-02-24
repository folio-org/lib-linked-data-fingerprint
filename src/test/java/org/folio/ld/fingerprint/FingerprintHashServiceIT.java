package org.folio.ld.fingerprint;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.ld.fingerprint.test.TestUtil.getResource;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.folio.ld.fingerprint.service.FingerprintDataMissingException;
import org.folio.ld.fingerprint.service.FingerprintHashServiceImpl;
import org.folio.ld.fingerprint.test.SpringTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@EnableConfigurationProperties
@SpringBootTest(classes = SpringTestConfig.class)
class FingerprintHashServiceIT {

  @Autowired
  private FingerprintHashServiceImpl fingerprintHashService;

  @Test
  void shouldKeepCurrentBehaviorByDefault_whenConfiguredFingerprintInputsAreMissing() {
    var hub1 = getResource(Map.of(NAME, List.of("Clubb, Angela. Mad about muffins")),
      Set.of(HUB), Collections.emptyMap());
    var hub2 = getResource(Map.of(NAME, List.of("Works of William Shapespeare")),
      Set.of(HUB), Collections.emptyMap());

    var hash1 = fingerprintHashService.hash(hub1);
    var hash2 = fingerprintHashService.hash(hub2);

    assertThat(hash1).isEqualTo(hash2);
  }

  @Test
  void shouldThrowInStrictMode_whenConfiguredFingerprintInputsAreMissing() {
    var hub = getResource(Map.of(NAME, List.of("Clubb, Angela. Mad about muffins")),
      Set.of(HUB), Collections.emptyMap());

    assertThatThrownBy(() -> fingerprintHashService.hash(hub, true))
      .isInstanceOf(FingerprintDataMissingException.class)
      .hasMessageContaining("resource:");
  }
}
