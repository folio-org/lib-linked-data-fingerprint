package org.folio.ld.fingerprint.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.folio.ld.fingerprint.service.FingerprintServiceImpl.FingerprintEntry;


public class FingerprintEntrySerializer extends StdSerializer<FingerprintEntry> {

  public FingerprintEntrySerializer() {
    this(null);
  }

  protected FingerprintEntrySerializer(Class<FingerprintEntry> t) {
    super(t);
  }

  @Override
  public void serialize(FingerprintEntry value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeStartArray();
    gen.writeString(value.key());
    gen.writeString(value.value());
    gen.writeEndArray();
  }
}
