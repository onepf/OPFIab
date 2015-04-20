# OPFIab
Android library intended to make In-App billing integration easy, while providing support for multiple App Stores.
## Usage
### Dependencies
Just add following to your gradle config:
```groovy
  dependencies {
    compile 'org.onepf:opfutils:0.1.20'
    compile 'org.onepf:opfiab:0.2.0@aar'
    compile 'org.onepf:opfiab-google:0.2.0@aar'
    compile 'org.onepf:opfiab-amazon:0.2.0@aar'
  }
```
This includes following external dependencies:
 - [EventBus](https://github.com/greenrobot/EventBus)
 - [OPFUtils](https://github.com/onepf/OPFUtils)

### Initialization & Setup
By far the best place for library initialization is ```Application.onCreate()``` callback:
```java
@Override
public void onCreate() {
  super.onCreate();
  // Define new library configuration
  Configuration configuration = new Configuration.Builder()
      // Add BillingProvider implementations in prefered order
      .addBillingProvider(new GoogleBillingProvider.Builder(this).build())
      .addBillingProvider(new AmazonBillingProvider.Builder(this).build())
      // Add global listener for all billing events
      .setBillingListener(new DefaultBillingListener() {
          // Handle desired callbacks
          @Override
          public void onSetupResponse(@NonNull final SetupResponse setupResponse) {
              super.onSetupResponse(setupResponse);
              if (setupResponse.isSuccessful()) {
                  // Hooray, we can purchase stuff!
              }
          }
          
          @Override
          public void onConsume(@NonNull final ConsumeResponse consumeResponse) {
              super.onConsume(consumeResponse);
              if (consumeResponse.isSuccessful()) {
                  // Award user with purchased item
                  MyPersistentStorage.add10Coins();
              } 
          }
      })
      .build();
  
  // Initialize library
  OPFIab.init(this, configuration);
  // Try to pick a suitable billing provider
  OPFIab.setup();
}

```
### Performing Billing Actions
So, library is set up and ready to go, what next?
<br>
Now you need a helper object to tell library to actually do something.
There's a variety of helpers available from ```OPFiab```, for more details see [Adanced Usage](https://github.com/onepf/OPFIab/wiki/Advanced-Usage) article or check out some [examples](./samples).

