package org.folio.ld.fingerprint;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static org.folio.ld.fingerprint.service.FingerprintServiceImpl.FingerprintEntry;

import lombok.experimental.UtilityClass;
import org.folio.ld.fingerprint.config.FingerprintEntrySerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

@UtilityClass
public class JsonUtil {

  public static JsonMapper getJsonMapper() {
    return JsonMapper.builder()
      .addModule(new SimpleModule().addSerializer(FingerprintEntry.class, new FingerprintEntrySerializer()))
      .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(NON_NULL))
      .build();
  }
}
