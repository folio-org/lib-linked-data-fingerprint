package org.folio.ld.fingerprint.service;

import org.folio.ld.dictionary.model.Resource;

public interface FingerprintService {

  String fingerprint(Resource resource);

}
