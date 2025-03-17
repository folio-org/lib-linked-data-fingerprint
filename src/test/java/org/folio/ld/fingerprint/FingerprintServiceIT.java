package org.folio.ld.fingerprint;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.folio.ld.fingerprint.test.TestUtil.category;
import static org.folio.ld.fingerprint.test.TestUtil.categorySet;
import static org.folio.ld.fingerprint.test.TestUtil.ddcClassification;
import static org.folio.ld.fingerprint.test.TestUtil.dissertation;
import static org.folio.ld.fingerprint.test.TestUtil.extent;
import static org.folio.ld.fingerprint.test.TestUtil.id_isbn;
import static org.folio.ld.fingerprint.test.TestUtil.id_lccn;
import static org.folio.ld.fingerprint.test.TestUtil.instance;
import static org.folio.ld.fingerprint.test.TestUtil.instanceTitle;
import static org.folio.ld.fingerprint.test.TestUtil.languageCategory;
import static org.folio.ld.fingerprint.test.TestUtil.lcClassification;
import static org.folio.ld.fingerprint.test.TestUtil.loadResourceAsString;
import static org.folio.ld.fingerprint.test.TestUtil.meetingConcept;
import static org.folio.ld.fingerprint.test.TestUtil.parallelTitle;
import static org.folio.ld.fingerprint.test.TestUtil.providerEvent;
import static org.folio.ld.fingerprint.test.TestUtil.providerPlace;
import static org.folio.ld.fingerprint.test.TestUtil.status;
import static org.folio.ld.fingerprint.test.TestUtil.variantTitle;
import static org.folio.ld.fingerprint.test.TestUtil.work;

import java.util.stream.Stream;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.fingerprint.service.FingerprintServiceImpl;
import org.folio.ld.fingerprint.test.SpringTestConfig;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@EnableConfigurationProperties
@SpringBootTest(classes = SpringTestConfig.class)
class FingerprintServiceIT {
  @Autowired
  private FingerprintServiceImpl fingerprintService;

  private static Stream<Arguments> provideResourceAndExpectedFingerprint() {
    return Stream.of(
      Arguments.of(category("category"), "category.json"),
      Arguments.of(languageCategory(), "languageCategory.json"),
      Arguments.of(categorySet(), "categorySet.json"),
      Arguments.of(dissertation(), "dissertation.json"),
      Arguments.of(meetingConcept(), "meetingConcept.json"),
      Arguments.of(id_lccn(), "id_lccn.json"),
      Arguments.of(id_isbn(), "id_isbn.json"),
      Arguments.of(instance(), "instance.json"),
      Arguments.of(instanceTitle(), "instanceTitle.json"),
      Arguments.of(parallelTitle(), "parallelTitle.json"),
      Arguments.of(variantTitle(), "variantTitle.json"),
      Arguments.of(lcClassification(), "lcClassification.json"),
      Arguments.of(ddcClassification(), "ddcClassification.json"),
      Arguments.of(providerEvent("providerEvent"), "providerEvent.json"),
      Arguments.of(providerPlace("providerEvent"), "providerPlace.json"),
      Arguments.of(status("some"), "status.json"),
      Arguments.of(work(), "work.json"),
      Arguments.of(extent(), "extent.json")
    );
  }

  @ParameterizedTest
  @MethodSource("provideResourceAndExpectedFingerprint")
  void shouldReturnCorrectFingerprint_forGivenResource(Resource resource, String expectedJson) {
    // given
    var expected = loadResourceAsString("fingerprints_expected/" + expectedJson);

    // when
    var fingerprint = fingerprintService.fingerprint(resource);

    // then
    assertThat(fingerprint).isEqualTo(expected);
  }
}
