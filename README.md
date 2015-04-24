# OPFIab
OPFIab is a next step from [OpenIAB](https://github.com/onepf/OpenIAB). It's an Android library intended to make in-app billing integration easy while supporting multiple billing providers (Appstores).

**This library is still under development and may contain bugs. Any feedback is deeply apreciated.*

## Dependencies
Library is designed to be extensible and cosist of separate modules.

First you need to add core dependecy:
```groovy
  dependencies {
    compile 'de.greenrobot:eventbus:2.4.0'
    compile 'org.onepf:opfutils:0.1.21'
    compile 'org.onepf:opfiab:0.2.0@aar'
  }
```
Then you have to add one or few `BillingProvider` modules. Check [supported providers](https://github.com/onepf/OPFIab/wiki#supported-billing-providers) for details. 

## Documentation
Full documentaion is available on our [wiki](https://github.com/onepf/OPFIab/wiki).

Also check out our [sample](https://github.com/onepf/OPFIab/tree/master/samples/trivialdrive):
* [Google Play](https://play.google.com/store/apps/details?id=org.onepf.opfiab.trivialdrive)
* [Amazon](http://www.amazon.com/OPF-Test-Account-OPFIab-Trivial/dp/B00W9TY70E/)

**Please be carefull, you might actually get charged.**

## FAQ
Coming soon.

## Thanks
* [@greenbot](https://github.com/greenrobot) for awesome [EventBus](https://github.com/greenrobot/EventBus) library.

