# lib-linked-data-fingerprint

© 2024 EBSCO Information Services.

This software is distributed under the terms of the Apache License, Version 2.0.
See the file "[LICENSE](LICENSE)" for more information.

## Introduction
Fingerprint hash the unique identifier for a resource in Linked Data Graph.

lib-linked-data-fingerprint Java Spring library is responsible for generating fingerprint hash for a resource.

## Usage

```java
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.fingerprint.service.FingerprintService;
import org.springframework.stereotype.Service;

@Service
public class YourService {

  private final FingerprintService fingerprintService;

  public YourService(FingerprintService fingerprintService) {
    this.fingerprintService = fingerprintService;
  }

  public void yourMethod(Resource resource) {
    String fingerprint = fingerprintService.fingerprint(resource);
    // ...
    // ...
  }
}
```

### Dependencies
- [lib-linked-data-dictionary](https://github.com/folio-org/lib-linked-data-dictionary)
