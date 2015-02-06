OPFIab
======

## The Road Ahead
- [ ] OPFIab Core
  - [ ] Solid Library API
    - [x] Public ```OPFIab``` methods.
    - [ ] Public ```IabHelper``` methods.
    - [ ] Public models.
    - [x] Public listeners.
    - [ ] Configuration.
  - [ ] Solid internal architecture.
    - [x] Event based internal communication.
    - [x] Request handling.
    - [x] Response handling.
  - [ ] ```BillingProvider```
    - [x] ```BillingController```
    - [x] Abstract asyc based implementation.
    - [ ] Abstract aidl services (Google based) implementation.
    - [x] Sku resolver abstraction.
    - [ ] Transparent Sku mapping
    - [ ] PurchaseVerifier abstraction.
  - [ ] Store picking algorithm implementation. - **10%**
    - [x] Package installer based.
    - [x] Setup state handling.
    - [x] Authentication
    - [ ] Prefer last used provider.
    - [ ] Dynamically switch from stale provider.
  - [ ] Documentation - **0%**
    - [ ] Public API.
    - [ ] Internal impelentation comments.
    - [ ] Project Wiki. // TODO
  - [ ] QA - **30%**
    - [ ] Static Analyzers compliance
      - [ ] CheckStyle.
      - [ ] FindBug.
      - [ ] PMD.
      - [ ] Gradle plugin.
    - [ ] Test Coverage.
- [ ] BillingProvider implementations. - **10%**
  - [ ] Google InApp Billing
  - [x] Amazon InApp
  - [ ] Samsung
    - [ ] Abstract activity-dependant BilingProvider implementation.
  - [ ] Fortumo
    - [ ] Subscriptions [#3](https://github.com/onepf/OPFIab/issues/3)
- [ ] Sample project - **10%**
  - [ ] Trivial Drive
  - [ ] Google migration sample.
- [ ] CI server. - **0%**
  - [ ] Automated builds.
  - [ ] GitHub integration.