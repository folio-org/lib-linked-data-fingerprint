package org.folio.ld.fingerprint;

import static org.folio.ld.fingerprint.test.TestUtil.family;
import static org.folio.ld.fingerprint.test.TestUtil.form;
import static org.folio.ld.fingerprint.test.TestUtil.jurisdiction;
import static org.folio.ld.fingerprint.test.TestUtil.meeting;
import static org.folio.ld.fingerprint.test.TestUtil.organization;
import static org.folio.ld.fingerprint.test.TestUtil.person;
import static org.folio.ld.fingerprint.test.TestUtil.place;
import static org.folio.ld.fingerprint.test.TestUtil.temporal;
import static org.folio.ld.fingerprint.test.TestUtil.topic;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.fingerprint.service.FingerprintHashServiceImpl;
import org.folio.ld.fingerprint.test.SpringTestConfig;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@EnableConfigurationProperties
@SpringBootTest(classes = SpringTestConfig.class)
class FingerprintHashIT {

  @Autowired
  private FingerprintHashServiceImpl fingerprintHashService;

  private static Stream<Arguments> provideResourceAndExpectedLegacyHash() {
    return Stream.of(
      Arguments.of(family(), "jn6Zj1ZKlyM"),
      Arguments.of(form(), "qltONOF0dsk"),
      Arguments.of(jurisdiction(), "r6tXEArUyN4"),
      Arguments.of(meeting(), "OuXkFaFFUww"),
      Arguments.of(organization(), "0Y52jqlXs-Q"),
      Arguments.of(person(), "WFcTSHUJd0E"),
      Arguments.of(place(), "sljlirD2NAk"),
      Arguments.of(topic(), "qDzeEji3Cj4"),
      Arguments.of(temporal(), "pmdE_1EWFaU")
    );
  }

  @ParameterizedTest
  @MethodSource("provideResourceAndExpectedLegacyHash")
  void testLegacyFingerprintHash(Resource resource, String expectedHash) {
    // when
    var result = fingerprintHashService.base64UrlLegacy(resource);

    // then
    assertEquals(expectedHash, result);
  }
}
