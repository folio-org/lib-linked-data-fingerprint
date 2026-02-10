package org.folio.ld.fingerprint.config;

import org.folio.ld.fingerprint.service.FingerprintServiceImpl.FingerprintEntry;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class FingerprintEntrySerializer extends StdSerializer<FingerprintEntry> {

  public FingerprintEntrySerializer() {
    this(null);
  }

  protected FingerprintEntrySerializer(Class<FingerprintEntry> t) {
    super(t);
  }

  @Override
  public void serialize(FingerprintEntry value, JsonGenerator gen, SerializationContext provider) {
    gen.writeStartArray();
    gen.writeString(value.key());
    gen.writeString(value.value());
    gen.writeEndArray();
  }
}
