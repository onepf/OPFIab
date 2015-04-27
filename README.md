# OPFIab
OPFIab is a next step from [OpenIAB](https://github.com/onepf/OpenIAB). It's an Android library intended to make in-app billing integration easy while supporting multiple billing providers (Appstores).

**This library is still under development and may contain bugs. Any feedback is deeply appreciated.**

## Dependencies
Library is designed to be extensible and cosists of separate modules.

Add core dependency:
```groovy
  dependencies {
    compile 'de.greenrobot:eventbus:2.4.0'
    compile 'org.onepf:opfutils:0.1.21'
    compile 'org.onepf:opfiab:0.2.0@aar'
  }
```
Or grab our latest [release](https://github.com/onepf/OPFIab/releases).

You'll also whant to add one or few `BillingProvider` modules. Check [supported providers](https://github.com/onepf/OPFIab/wiki#supported-billing-providers) for details. 

## Documentation
Full documentaion is available on our [wiki](https://github.com/onepf/OPFIab/wiki).

## Samples
* [TrivialDrive](https://github.com/onepf/OPFIab/tree/master/samples/trivialdrive)

## FAQ
Coming soon.

## Thanks
* [@greenbot](https://github.com/greenrobot) for awesome [EventBus](https://github.com/greenrobot/EventBus) library.

## License

    Copyright 2012-2015 One Platform Foundation

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
