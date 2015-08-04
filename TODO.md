Arranged according to priority:

- [ ] Update documentation to reflect all latest changes
- [ ] General enchancements: #21 #22 #23 #24 #25
- [ ] Amazon enchancements
    * Subscriptions duration: #20
    * Blocking requst handling: #29
- [ ] API
    * Rename setup(): #26
    * Requst data in response: #30

- [ ] **Introduce percistant storage concept**
    * Add appropriate interfaces
    * Update API 
    * Update SamsungBillingProvider implementation to make use of persistant storage
    * Default implementation backed by local database 
    * Separate module with persistant storage backed by Facebook

Unity plugin

* Make Configuration object JSON compatible
  * All providers\sku resolvers\purchase verifiers can be specified via full class name
  * Support limited configuration
* Java layer
  * Translate all API calls from Unity to native
  * Convert all models to JSON and vise versa
  * Conveys library event to Unity
* Unity sample  

iOS and WP is out of scop
