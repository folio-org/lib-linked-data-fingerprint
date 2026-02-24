package org.folio.ld.fingerprint.service;

public class FingerprintDataMissingException extends IllegalStateException {

  public FingerprintDataMissingException(String message) {
    super(message);
  }
}
