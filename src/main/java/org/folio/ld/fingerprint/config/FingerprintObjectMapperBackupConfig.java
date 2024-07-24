package org.folio.ld.fingerprint.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.folio.ld.fingerprint.service.FingerprintServiceImpl.FingerprintEntry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FingerprintObjectMapperBackupConfig {

  @Bean
  @ConditionalOnMissingBean
  public ObjectMapper objectMapper() {
    var mapper = new ObjectMapper();
    return mapper
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
      .registerModule(new SimpleModule().addSerializer(FingerprintEntry.class, new FingerprintEntrySerializer()));
  }

}
