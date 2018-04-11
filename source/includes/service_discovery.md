# Service discovery


> Desktop platform

```kotlin
val bboxapi = BboxApiStb(appId = "YourAppId", appSecret = "YourAppSecret")

bboxapi.startRestDiscovery(findOneAndExit = true, maxDuration = 10000, platform = DesktopPlatform.create()) { eventType, service, error ->
    when (eventType) {
        StbServiceEvent.SERVICE_FOUND -> println("service found : ${service?.ip}:${service?.port}")
        StbServiceEvent.DISCOVERY_STOPPED -> println("end of discovery")
        StbServiceEvent.DISCOVERY_ERROR -> error?.printStackTrace()
    }
}
```

```java
BboxApiStb bboxapi = new BboxApiStb("YourAppId", "YourAppSecret");

bboxapi.startRestDiscovery(true, DesktopPlatform.create(), 10000, (stbServiceEvent, stbService, throwable) -> {
    switch (stbServiceEvent) {
        case SERVICE_FOUND:
            System.out.println("service found : " + stbService.getIp() + ":" + stbService.getPort());
            break;
        case DISCOVERY_STOPPED:
            System.out.println("end of discovery");
            break;
        case DISCOVERY_ERROR:
            throwable.printStackTrace();
            break;
    }
    return Unit.INSTANCE;
});
```

> Android platform 

```kotlin
val bboxapi = BboxApiStb(appId = "YourAppId", appSecret = "YourAppSecret")

bboxapi.startRestDiscovery(findOneAndExit = true, maxDuration = 10000, platform = AndroidPlatform.create()) { eventType, service, error ->
    when (eventType) {
        StbServiceEvent.SERVICE_FOUND -> println("service found : ${service?.ip}:${service?.port}")
        StbServiceEvent.DISCOVERY_STOPPED -> println("end of discovery")
        StbServiceEvent.DISCOVERY_ERROR -> error?.printStackTrace()
    }
}
```

```java
BboxApiStb bboxapi = new BboxApiStb("YourAppId", "YourAppSecret");

bboxapi.startRestDiscovery(true, AndroidPlatform.create(), 10000, (stbServiceEvent, stbService, throwable) -> {
    switch (stbServiceEvent) {
        case SERVICE_FOUND:
            System.out.println("service found : " + stbService.getIp() + ":" + stbService.getPort());
            break;
        case DISCOVERY_STOPPED:
            System.out.println("end of discovery");
            break;
        case DISCOVERY_ERROR:
            throwable.printStackTrace();
            break;
    }
    return Unit.INSTANCE;
});
```

In order to find Bbox Miami IP adress, use the mDNS broadcast feature exposed by BboxAPI Miami service

You have to add the following dependency depending on platform : 

* Desktop : `implementation "de.mannodermaus.rxjava2:rxbonjour-platform-desktop:2.0.0-RC1"`
* Android : `implementation "de.mannodermaus.rxjava2:rxbonjour-platform-android:2.0.0-RC1"`

Also using `startRestDiscovery` :

* setting `findOneAndExit` to `true` will automatically end the discovery when one service is found
* setting `maxDuration` parameter will set a max duration in milliseconds for service discovery

The last service found is automatically chosen and set in variable `bboxapi.restService`.

The list of services found is available in `bboxapi.restServiceList`. In special environments where there are multiple Bbox Miami or multiple Android TV device with Bbox API STB service installed on the same network, there can be more than one service found if `findOneAndExit` is set to `false`.