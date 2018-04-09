# Overview

Bbox Miami Api are composed of 

* REST API used to :
 * get TV channel list
 * get list of Android applications installed
 * get information about a specific installed Android app
 * get Android application icon
 * get current TV channel
 * get volume value
 * start an Android app
 * display a Toast message
 * set volume 

* Websocket event used to :
 * get notified when a TV channel change is detected
 * get notified when an Android application state change 
 * receive custom message from clients

Box IP/port is broadcasted on local network via MDNS (Multicast DNS) on local network

![architecture](images/overview.png)

# Security

These API are secured using : 

* a token which is exchanged for an AppId/AppSecret
* a sessionId which is exchanged for the previous token. This sessionId will be passed in a custom header for each subsequent API call

For more information check API Security section of [this page](https://api.bbox.fr/doc/#Getting%20started)

**An AppId & AppSecret are necessary to use this library, you can get them by contacting Bouygues Télécom via [this contact page](https://dev.bouyguestelecom.fr/dev/?page_id=51)**