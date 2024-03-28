package org.folio.ld.fingerprint.service;

import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.dictionary.util.HashUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FingerprintHashServiceImpl implements FingerprintHashService {

  private final FingerprintService fingerprintService;

  @Override
  public Long hash(Resource resource) {
    return HashUtils.hash(fingerprintService.fingerprint(resource));
  }
}
