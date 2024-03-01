package org.folio.ld.fingerprint.service;

import static com.google.common.hash.Hashing.murmur3_128;

import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.model.Resource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HashServiceImpl implements HashService {

  private final FingerprintService fingerprintService;

  @Override
  public Long hash(Resource resource) {
    return murmur3_128().hashString(fingerprintService.fingerprint(resource), StandardCharsets.UTF_8).padToLong();
  }

}
