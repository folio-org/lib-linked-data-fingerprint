package org.folio.ld.fingerprint.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.folio.ld.fingerprint.service.FingerprintServiceImpl.FingerprintEntry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class FingerprintObjectMapperBeanPostProcessor implements BeanPostProcessor {

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if (bean instanceof ObjectMapper mapper) {
      mapper.registerModule(new SimpleModule().addSerializer(FingerprintEntry.class, new FingerprintEntrySerializer()));
    }
    return bean;
  }
}
