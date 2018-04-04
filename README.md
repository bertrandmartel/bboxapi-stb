# BboxApi STB client library #

[![Build Status](https://travis-ci.org/bertrandmartel/bboxapi-stb.svg)](https://travis-ci.org/bertrandmartel/bboxapi-stb)
[![Download](https://api.bintray.com/packages/bertrandmartel/maven/bboxapi-stb/images/download.svg) ](https://bintray.com/bertrandmartel/maven/bboxapi-stb/_latestVersion)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/fr.bmartel/bboxapi-stb-android/badge.svg)](https://maven-badges.herokuapp.com/maven-central/fr.bmartel/bboxapi-stb-android)
[![Javadoc](http://javadoc-badge.appspot.com/fr.bmartel/bboxapi-stb-android.svg?label=javadoc)](http://javadoc-badge.appspot.com/fr.bmartel/bboxapi-stb-android)
[![codecov](https://codecov.io/gh/bertrandmartel/bboxapi-stb/branch/master/graph/badge.svg)](https://codecov.io/gh/bertrandmartel/bboxapi-stb)
[![License](http://img.shields.io/:license-mit-blue.svg)](LICENSE.md)

[Bbox Miami API](https://api.bbox.fr/doc/#API%20Box) client library for Kotlin/Java/Android

:construction: Documentation coming soon :construction:

## Features

### MDNS

- [x] discover Bbox API service

### REST

- [x] get channel list (`GET /media/tvchannellist`)
- [x] get list of Android apps installed (`GET /applications`)
- [x] get information about a specific installed Android app (`GET /applications/$packageName`)
- [x] get application icon  (`GET /applications/$packageName/image`)
- [x] get current channel (`GET /media`)
- [x] get volume (`GET /userinterface/volume`)
- [x] start application (`POST /applications/$packageName`)
- [x] display toast (`POST /userinterface/toast`)
- [x] set volume (`POST /userinterface/volume`)
- [x] register application for notifications (`POST /applications/register`)

### Websocket

- [ ] open websocket stream for receiving notifications
- [ ] get all opened channel
- [ ] create a notification on a specific channel
- [ ] subscribe notification
- [ ] unsubscribe notifications

## Tests

Run test on mockserver :
```bash
./gradlew test
```

## External Library

* [Fuel](https://github.com/kittinunf/Fuel)
* [Gson](https://github.com/google/gson)
* [RxBonjour](https://github.com/mannodermaus/RxBonjour)

## API documentation

https://api.bbox.fr/doc/#API%20Box

## License

The MIT License (MIT) Copyright (c) 2018 Bertrand Martel
