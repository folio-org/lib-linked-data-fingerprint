package org.folio.ld.fingerprint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.PropertyDictionary;
import org.folio.ld.dictionary.ResourceTypeDictionary;
import org.folio.ld.fingerprint.config.FingerprintRules;
import org.folio.ld.fingerprint.config.ObjectMapperBackupConfig;
import org.folio.ld.fingerprint.config.YamlPropertySourceFactory;
import org.folio.ld.fingerprint.model.FingerprintedResource;
import org.folio.ld.fingerprint.model.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@AutoConfigureMockMvc
@EnableConfigurationProperties
@SpringBootTest(classes = {FingerprintService.class, FingerprintRules.class,
  ObjectMapperBackupConfig.class, YamlPropertySourceFactory.class})
@Log4j2
class FingerprintServiceTest {

  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private FingerprintService fingerprintService;

  @Test
  // TODO add assertions
  void testFingerprint() {
    Map<String, List<String>> doc = Map.of(
      PropertyDictionary.PART_NUMBER.getValue(), List.of("2"),
      PropertyDictionary.MAIN_TITLE.getValue(), List.of("main title"),
      PropertyDictionary.SUBTITLE.getValue(), List.of("sub title"),
      PropertyDictionary.PART_NAME.getValue(), List.of("part name")
    );

    List<String> types = List.of(ResourceTypeDictionary.TITLE.getUri());

    Resource resource = new Resource(objectMapper.convertValue(doc, JsonNode.class), types);
    FingerprintedResource fingerprintedResource = fingerprintService.fingerprint(resource);

    log.info(fingerprintedResource);
  }
}
