package org.folio.ld.fingerprint.exception;

public class FingerprintException extends RuntimeException {

  public final String field;
  public final String value;

  public FingerprintException(String field, String value) {
    this.field = field;
    this.value = value;
  }
}
