# BboxApi STB client library #

[![Build Status](https://travis-ci.org/bertrandmartel/bboxapi-stb.svg?branch=master)](https://travis-ci.org/bertrandmartel/bboxapi-stb)
[![Download](https://api.bintray.com/packages/bertrandmartel/maven/bboxapi-stb/images/download.svg) ](https://bintray.com/bertrandmartel/maven/bboxapi-stb/_latestVersion)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/fr.bmartel/bboxapi-stb/badge.svg)](https://maven-badges.herokuapp.com/maven-central/fr.bmartel/bboxapi-stb)
[![Javadoc](http://javadoc-badge.appspot.com/fr.bmartel/bboxapi-stb.svg?label=javadoc)](http://javadoc-badge.appspot.com/fr.bmartel/bboxapi-stb)
[![codecov](https://codecov.io/gh/bertrandmartel/bboxapi-stb/branch/master/graph/badge.svg)](https://codecov.io/gh/bertrandmartel/bboxapi-stb)
[![License](http://img.shields.io/:license-mit-blue.svg)](LICENSE.md)

[Bbox Miami API](https://api.bbox.fr/doc/#API%20Box) client library for Kotlin/Java/Android

**[Go to Documentation](http://bertrandmartel.github.io/bboxapi-stb)**

## Features

### MDNS

- [x] discover Rest API service
- [x] discover Websocket API service (only for next version)

### REST

#### Actions & Resources

- [x] get channel list (`GET /media/tvchannellist`)
- [x] get list of Android apps installed (`GET /applications`)
- [x] get information about a specific installed Android app (`GET /applications/$packageName`)
- [x] get application icon  (`GET /applications/$packageName/image`)
- [x] get current channel (`GET /media`)
- [x] get volume (`GET /userinterface/volume`)
- [x] start application (`POST /applications/$packageName`)
- [x] display toast (`POST /userinterface/toast`)
- [x] set volume (`POST /userinterface/volume`)
- [x] get VOD catalog (`GET /media/vod`) (**cloud**)
- [x] get EPG catalog (`GET /media/live`) (**cloud**)

#### Notifications

- [x] register application for notifications (`POST /applications/register`)
- [x] get all opened notification channels (`GET /notification`)
- [x] subscribe notification with specific resource type (`POST /notification`)
- [x] unsubscribe notification channel (`DELETE /notification/$channelId`)
- [x] create a notification on a specific channel (`POST /notification/$channelId`)

### Websocket

- [x] open websocket stream for receiving notifications
- [x] parse notification messages : channel change, application state, error message

## Tests

Run test on mockserver :
```bash
./gradlew test
```

## External Library

* [Fuel](https://github.com/kittinunf/Fuel): Http client for REST API
* [Gson](https://github.com/google/gson) : deserialize JSON objects
* [RxBonjour](https://github.com/mannodermaus/RxBonjour) : MDNS discovery
* [OkHttp](https://github.com/square/okhttp) : websocket client

## API documentation

https://api.bbox.fr/doc/#API%20Box

## License

The MIT License (MIT) Copyright (c) 2018 Bertrand Martel
