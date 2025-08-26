package org.folio.ld.fingerprint;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.folio.ld.dictionary.ResourceTypeDictionary.BOOKS;
import static org.folio.ld.dictionary.ResourceTypeDictionary.CONTINUING_RESOURCES;
import static org.folio.ld.fingerprint.test.TestUtil.adminMetadataAnnotation;
import static org.folio.ld.fingerprint.test.TestUtil.annotation;
import static org.folio.ld.fingerprint.test.TestUtil.category;
import static org.folio.ld.fingerprint.test.TestUtil.categorySet;
import static org.folio.ld.fingerprint.test.TestUtil.classificationDdc;
import static org.folio.ld.fingerprint.test.TestUtil.classificationLc;
import static org.folio.ld.fingerprint.test.TestUtil.conceptMeeting;
import static org.folio.ld.fingerprint.test.TestUtil.conceptTemporal;
import static org.folio.ld.fingerprint.test.TestUtil.dissertation;
import static org.folio.ld.fingerprint.test.TestUtil.extent;
import static org.folio.ld.fingerprint.test.TestUtil.id_ian;
import static org.folio.ld.fingerprint.test.TestUtil.id_isbn;
import static org.folio.ld.fingerprint.test.TestUtil.id_lccn;
import static org.folio.ld.fingerprint.test.TestUtil.id_unknown;
import static org.folio.ld.fingerprint.test.TestUtil.instance;
import static org.folio.ld.fingerprint.test.TestUtil.languageCategory;
import static org.folio.ld.fingerprint.test.TestUtil.loadResourceAsString;
import static org.folio.ld.fingerprint.test.TestUtil.providerEvent;
import static org.folio.ld.fingerprint.test.TestUtil.providerPlace;
import static org.folio.ld.fingerprint.test.TestUtil.status;
import static org.folio.ld.fingerprint.test.TestUtil.title;
import static org.folio.ld.fingerprint.test.TestUtil.titleParallel;
import static org.folio.ld.fingerprint.test.TestUtil.titleVariant;
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
      Arguments.of(adminMetadataAnnotation(), "adminMetadataAnnotation.json"),
      Arguments.of(annotation("annotation"), "annotation.json"),
      Arguments.of(category("category"), "category.json"),
      Arguments.of(categorySet(), "categorySet.json"),
      Arguments.of(classificationDdc(), "ddcClassification.json"),
      Arguments.of(classificationLc(), "lcClassification.json"),
      Arguments.of(conceptMeeting(), "meetingConcept.json"),
      Arguments.of(conceptTemporal(), "temporalConcept.json"),
      Arguments.of(dissertation(), "dissertation.json"),
      Arguments.of(extent(), "extent.json"),
      Arguments.of(id_ian(), "id_ian.json"),
      Arguments.of(id_isbn(), "id_isbn.json"),
      Arguments.of(id_lccn(), "id_lccn.json"),
      Arguments.of(id_unknown(), "id_unknown.json"),
      Arguments.of(instance(), "instance.json"),
      Arguments.of(languageCategory(), "languageCategory.json"),
      Arguments.of(providerEvent("providerEvent"), "providerEvent.json"),
      Arguments.of(providerPlace("providerEvent"), "providerPlace.json"),
      Arguments.of(status("some"), "status.json"),
      Arguments.of(title(), "instanceTitle.json"),
      Arguments.of(titleParallel(), "parallelTitle.json"),
      Arguments.of(titleVariant(), "variantTitle.json"),
      Arguments.of(work(BOOKS), "work_book.json"),
      Arguments.of(work(CONTINUING_RESOURCES), "work_serial.json")
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
