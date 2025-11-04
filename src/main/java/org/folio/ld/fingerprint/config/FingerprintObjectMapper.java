package org.folio.ld.fingerprint.config;

import static org.folio.ld.fingerprint.service.FingerprintServiceImpl.FingerprintEntry;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.stereotype.Component;

@Component
public class FingerprintObjectMapper extends ObjectMapper {

  public FingerprintObjectMapper() {
    var module = new SimpleModule();
    module.addSerializer(FingerprintEntry.class, new FingerprintEntrySerializer());
    super.registerModule(module)
      .setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

}
