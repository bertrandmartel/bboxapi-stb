# Notifications

BboxAPI Miami service dispatch notifications via WebSocket on port 9090. This library abstracts all the flow involving app registering, subscribing to events & opening websocket. This flow is described [here](https://api.bbox.fr/doc/#SDK%20Mobile)

## Subscribe notifications

```kotlin
val notificationChannel = bboxapi.subscribeNotification(
        appName = "myApplication",
        resourceList = listOf(Resource.Application, Resource.Media, Resource.Message),
        listener = object : BboxApiStb.WebSocketListener {

            override fun onOpen() {
                println("websocket opened")
            }

            override fun onClose() {
                println("websocket closed")
            }

            override fun onApp(app: AppEvent) {
                println("application event : $app")
            }

            override fun onMedia(media: MediaEvent) {
                println("channel change event : $media")
            }

            override fun onMessage(message: MessageEvent) {
                println("message event : $message")
            }

            override fun onError(error: BboxApiError) {
                println("error : $error")
            }

            override fun onFailure(throwable: Throwable?) {
                throwable?.printStackTrace()
            }
        })
val (_, _, result) = notificationChannel.subscribeResult
if (result is Result.Failure) {
    result.error.printStackTrace()
} else {
    println("subscribed on channelId ${notificationChannel.channelId} & appId ${notificationChannel.appId}")
}
```

```java
List<Resource> resourceList = new ArrayList<>();
resourceList.add(Resource.Application);
resourceList.add(Resource.Media);
resourceList.add(Resource.Message);

NotificationChannel notificationChannel = bboxapi.subscribeNotification(
        "myApplication",
        resourceList,
        new BboxApiStb.WebSocketListener() {
            @Override
            public void onOpen() {
                System.out.println("websocket opened");
            }

            @Override
            public void onClose() {
                System.out.println("websocket closed");
            }

            @Override
            public void onError(@NotNull BboxApiError error) {
                System.out.println("error : " + error);
            }

            @Override
            public void onMedia(@NotNull MediaEvent media) {
                System.out.println("channel change event : " + media);
            }

            @Override
            public void onApp(@NotNull AppEvent app) {
                System.out.println("application event : " + app);
            }

            @Override
            public void onMessage(@NotNull MessageEvent message) {
                System.out.println("message event : " + message);
            }

            @Override
            public void onFailure(@Nullable Throwable throwable) {
                throwable.printStackTrace();
            }
        });

Triple<Request, Response, Result<byte[], FuelError>> result = notificationChannel.getSubscribeResult();

if (result.component3().component2() != null) {
    result.component3().component2().printStackTrace();
} else {
    System.out.println("subscribed with resource on channelId " +
            notificationChannel.getChannelId() +
            " & appId " + notificationChannel.getAppId());
}
```

To listen for notifications use `subscribeNotification` with a list of `Resource` including the following : 

* `Resource.Application` to receive application state change (when an application is going background/foreground)
* `Resource.Media` to receive TV channel change events
* `Resource.Message` to receive messages sent by other BboxAPI STB client

| Field | Type | Description |
|-------|------|------------|
| appName | string | a name for your application | 
| resourceList | List<Resource> | list of resources to subscribe | 
| listener | BboxApiStb.WebSocketListener | websocket event listener | 


The underlying flow registers an "application" & subscribes notifications, an appID/channelID is returned from : 

* `appID` is the ID returned by registering an "application" for notifications
* `channelID` is the ID retuned by subscribing the previous "application" for specific resources

These appID/channelID can be used to send notification, check "Send notification" section

When you are done listening to notification call `closeWebsocket` to close the websocket. 

<aside class="success">
websocket is automatically closed before subscribing, thus no need to call <code>closeWebsocket</code> before <code>subscribeNotification</code>
</aside>

## Unsubscribe all channels

To unsubscribe all channels ID call `unsubscribeAllSync`. This will get all distinct opened channels & unsubscribe each of them

```kotlin
bboxapi.unsubscribeAllSync()
```

```java
bboxapi.unsubscribeAllSync();
```

## Send notification

> Asynchronous 

```kotlin
bboxapi.sendNotification(
		channelId = "15233003603520.7238189308864336-0.0718767910445014",
        appId = "15233003603520.7238189308864336",
        message = "some message") { _, response, result ->
    when (result) {
        is Result.Failure -> {
            result.getException().printStackTrace()
        }
        is Result.Success -> {
            println("message sent")
        }
    }
}
```

```java
bboxapi.sendNotification(
        "15233003603520.7238189308864336-0.0718767910445014",
        "15233003603520.7238189308864336",
        "some message", new Handler<byte[]>() {
            @Override
            public void success(Request request, Response response, byte[] bytes) {
                System.out.println("message sent");
            }

            @Override
            public void failure(Request request, Response response, FuelError fuelError) {
                fuelError.printStackTrace();
            }
        }
);
```

> Synchronous 

```kotlin
val (_, _, result) = bboxapi.sendNotificationSync(
        channelId = "15233003603520.7238189308864336-0.0718767910445014",
        appId = "15233003603520.7238189308864336",
        message = "some message")
when (result) {
    is Result.Failure -> {
        result.getException().printStackTrace()
    }
    is Result.Success -> {
        println("message sent")
    }
}
```

```java
Triple<Request, Response, Result<byte[], FuelError>> result = bboxapi.sendNotificationSync(
        "15233003603520.7238189308864336-0.0718767910445014",
        "15233003603520.7238189308864336",
        "some message"
);
if (result.component3().component2() != null) {
    result.component3().component2().printStackTrace();
} else {
    System.out.println("message sent");
}
```

You can send a notification to a pair channelId / appId.

| Field | Type | Description |
|-------|------|------------|
| channelId | string | ID retuned by subscribing the previous "application" for specific resources | 
| appId | string | ID returned by registering an "application" for notifications | 
| message | string | message to send | 