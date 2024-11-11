# lib-linked-data-fingerprint

© 2024 EBSCO Information Services.

This software is distributed under the terms of the Apache License, Version 2.0.
See the file "[LICENSE](LICENSE)" for more information.
## Purpose
Fingerprint hash is the unique identifier for a resource in Linked Data Graph.
This library provides a function for a fingerprint hash generation.
## Compiling
```bash
mvn clean install
```
## Using the library
The library can be used as a source of Linked Data resource hash:
```java
import org.folio.ld.dictionary.model.Resource;
import org.folio.ld.fingerprint.service.FingerprintHashService;
import org.springframework.stereotype.Service;

@Service
public class YourService {

  private final FingerprintHashService fingerprintHashService;

  public YourService(FingerprintHashService fingerprintHashService) {
    this.fingerprintHashService = fingerprintHashService;
  }

  public void yourMethod(Resource resource) {
    Long hash = fingerprintHashService.hash(resource);
    // ...
    // ...
  }
}
```
## Dependencies
- [lib-linked-data-dictionary](https://github.com/folio-org/lib-linked-data-dictionary)
## Download and configuration
The built artifacts for this module are available. See [configuration](https://dev.folio.org/download/artifacts/) for repository access.

