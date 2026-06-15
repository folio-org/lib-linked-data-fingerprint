package org.folio.ld.fingerprint.service.legacy;

import static org.folio.ld.dictionary.PropertyDictionary.ATTRIBUTION;
import static org.folio.ld.dictionary.PropertyDictionary.GEOGRAPHIC_COVERAGE;
import static org.folio.ld.dictionary.PropertyDictionary.MISC_INFO;
import static org.folio.ld.dictionary.PropertyDictionary.NUMERATION;
import static org.folio.ld.dictionary.PropertyDictionary.PLACE;
import static org.folio.ld.dictionary.PropertyDictionary.SUBORDINATE_UNIT;
import static org.folio.ld.dictionary.PropertyDictionary.TITLES;

import java.util.Set;
import lombok.experimental.UtilityClass;

@UtilityClass
public class LegacyProperties {

  private static final Set<String> LEGACY_PROPERTIES = Set.of(
    ATTRIBUTION.getValue(),
    GEOGRAPHIC_COVERAGE.getValue(),
    MISC_INFO.getValue(),
    NUMERATION.getValue(),
    PLACE.getValue(),
    SUBORDINATE_UNIT.getValue(),
    TITLES.getValue()
  );
  private static final String BACKSLASH = "/";
  private static final String CURRENT_SUBPATH = BACKSLASH + "library" + BACKSLASH;
  private static final String LEGACY_SUBPATH = BACKSLASH + "marc" + BACKSLASH;

  public static String get(String property) {
    return property.replace(CURRENT_SUBPATH, LEGACY_SUBPATH);
  }

  public static boolean isLegacy(String property) {
    return LEGACY_PROPERTIES.contains(property);
  }

}
