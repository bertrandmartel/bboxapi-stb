# API reference

## Display Toast

> Asynchronous 

```kotlin
val toast = ToastRequest(message = "this is a message", pos_y = 500, pos_x = 300, color = "#FF0000")

bboxapi.displayToast(toast) { request, response, result ->
    when (result) {
        is Result.Failure -> {
            result.getException().printStackTrace()
        }
        is Result.Success -> {
            println(response.statusCode)
        }
    }
}
```

```java
ToastRequest toastRequest = new ToastRequest("this is a toast", "#FF0000", 500, 300);

bboxapi.displayToast(toastRequest, new Handler<byte[]>() {
    @Override
    public void success(Request request, Response response, byte[] bytes) {
        System.out.println(response.getStatusCode());
    }

    @Override
    public void failure(Request request, Response response, FuelError fuelError) {
        fuelError.printStackTrace();
    }
});
```

> Synchronous

```kotlin
val toast = ToastRequest(message = "this is a message", pos_y = 500, pos_x = 200, color = "#FF0000")

val (_, res, result) = bboxapi.displayToastSync(toast)
when (result) {
    is Result.Failure -> {
        result.getException().printStackTrace()
    }
    is Result.Success -> {
        println(res.statusCode)
    }
}
```

```java
ToastRequest toastRequest = new ToastRequest("this is a toast", "#FF0000", 500, 300);

Triple<Request, Response, Result<byte[], FuelError>> data = bboxapi.displayToastSync(toastRequest);
Request request = data.getFirst();
Response response = data.getSecond();
Result<byte[], FuelError> obj = data.getThird();
System.out.println(response.getStatusCode());
```

Display a Toast message

| Field | Type | default value | Description |
|-------|------|-------|-------------|
| message | string | `""` | toast message |
| color | string | `null` | toast text color in hex string format (ex: #FF0000) |
| pos_x | int | `null` | toast X position |
| pos_y | int | `null` | toast Y position |

## Get TV channel list

> Asynchronous 

```kotlin
bboxapi.getChannels { _, _, result ->
    when (result) {
        is Result.Failure -> {
            result.getException().printStackTrace()
        }
        is Result.Success -> {
            println(result.get())
        }
    }
}
```

```java
bboxapi.getChannels(new Handler<List<Channel>>() {
    @Override
    public void success(Request request, Response response, List<Channel> channels) {
        System.out.println(channels);
    }

    @Override
    public void failure(Request request, Response response, FuelError fuelError) {
        fuelError.printStackTrace();
    }
});
```

> Synchronous 

```kotlin
val (_, _, result) = bboxapi.getChannelsSync()
when (result) {
    is Result.Failure -> {
        result.getException().printStackTrace()
    }
    is Result.Success -> {
        println(result.get())
    }
}
```

```java
Triple<Request, Response, Result<List<Channel>, FuelError>> data = bboxapi.getChannelsSync();

Request request = data.getFirst();
Response response = data.getSecond();
Result<List<Channel>, FuelError> obj = data.getThird();
System.out.println(obj.get());
```

Get list of TV channel with the following information :

| Field | Type | Description |
|-------|------|-----------|
| mediaState | string | state of media `play` or `stop` |
| mediaTitle | string | channel name |
| positionId | int | channel position |

## Get Application list

> Asynchronous 

```kotlin
bboxapi.getApps { _, _, result ->
    when (result) {
        is Result.Failure -> {
            result.getException().printStackTrace()
        }
        is Result.Success -> {
            println(result.get())
        }
    }
}
```

```java
bboxapi.getApps(new Handler<List<Application>>() {
    @Override
    public void success(Request request, Response response, List<Application> channels) {
        System.out.println(channels);
    }

    @Override
    public void failure(Request request, Response response, FuelError fuelError) {
        fuelError.printStackTrace();
    }
});
```

> Synchronous

```kotlin
val (_, _, result) = bboxapi.getAppsSync()
when (result) {
    is Result.Failure -> {
        result.getException().printStackTrace()
    }
    is Result.Success -> {
        println(result.get())
    }
}
```

```java
Triple<Request, Response, Result<List<Application>, FuelError>> data = bboxapi.getAppsSync();

Request request = data.getFirst();
Response response = data.getSecond();
Result<List<Application>, FuelError> obj = data.getThird();
System.out.println(obj.get());
```

Get list of Android application installed on Bbox. The information includes the following :

| Field | Type | Description |
|-------|------|------------|
| appId | string | application id | 
| appName | string | application name (ex: Youtube) |
| packageName | string | application package name (ex: com.google.youtube) |
| component | string | component intent |
| appState | string | application state (stopped/foreground)
| data | string | data intent |
| leanback | boolean | if app is an Android TV app |
| logoUrl | string | path to getAppIcon for this package name | 

## Get Application info

> Asynchronous

```kotlin
bboxapi.getAppInfo(packageName = "com.google.android.youtube.tv") { _, _, result ->
    when (result) {
        is Result.Failure -> {
            val ex = result.getException()
            ex.printStackTrace()
        }
        is Result.Success -> {
            val data = result.get()
            println(data)
        }
    }
}
```

```java
bboxapi.getAppInfo("com.google.android.youtube.tv", new Handler<List<Application>>() {
    @Override
    public void success(Request request, Response response, List<Application> channels) {
        System.out.println(channels);
    }

    @Override
    public void failure(Request request, Response response, FuelError fuelError) {
        fuelError.printStackTrace();
    }
});
```

> Synchronous

```kotlin
val (_, _, result) = bboxapi.getAppInfoSync(packageName = "com.google.android.youtube.tv")
when (result) {
    is Result.Failure -> {
        result.getException().printStackTrace()
    }
    is Result.Success -> {
        println(result.get())
    }
}
```

```java
Triple<Request, Response, Result<List<Application>, FuelError>> data = bboxapi.getAppInfoSync("com.google.android.youtube.tv");

Request request = data.getFirst();
Response response = data.getSecond();
Result<List<Application>, FuelError> obj = data.getThird();
System.out.println(obj.get());
```

Get information about a specific application by package name :

| Field | Type | Description |
|-------|------|------------|
| appId | string | application id | 
| appName | string | application name (ex: Youtube) |
| packageName | string | application package name (ex: com.google.youtube) |
| component | string | component intent |
| appState | string | application state (stopped/foreground)
| data | string | data intent |
| leanback | boolean | if app is an Android TV app |
| logoUrl | string | path to getAppIcon for this package name | 

## Get Application icon

> Asynchronous

```kotlin
bboxapi.getAppIcon(packageName = "com.google.android.youtube.tv") { _, _, result ->
    when (result) {
        is Result.Failure -> {
            result.getException().printStackTrace()
        }
        is Result.Success -> {
            println(result.get().size)
        }
    }
}
```

```java
bboxapi.getAppIcon("com.google.android.youtube.tv", new Handler<byte[]>() {
    @Override
    public void success(Request request, Response response, byte[] image) {
        System.out.println(image.length);
    }

    @Override
    public void failure(Request request, Response response, FuelError fuelError) {
        fuelError.printStackTrace();
    }
});
```

> Synchronous

```kotlin
val (_, _, result) = bboxapi.getAppIconSync(packageName = "com.google.android.youtube.tv")
when (result) {
    is Result.Failure -> {
        result.getException().printStackTrace()
    }
    is Result.Success -> {
        println(result.get().size)
    }
}
```

```java
Triple<Request, Response, Result<byte[], FuelError>> data = bboxapi.getAppIconSync("com.google.android.youtube.tv");

Request request = data.getFirst();
Response response = data.getSecond();
Result<byte[], FuelError> obj = data.getThird();
System.out.println(obj.get().length);
```

Retrieve Android application icon for a specific package name

## Get current TV channel

> Asynchronous

```kotlin
bboxapi.getCurrentChannel { _, _, result ->
    when (result) {
        is Result.Failure -> {
            result.getException().printStackTrace()
        }
        is Result.Success -> {
            println(result.get())
        }
    }
}
```

```java
bboxapi.getCurrentChannel(new Handler<Media>() {
    @Override
    public void success(Request request, Response response, Media channel) {
        System.out.println(channel);
    }

    @Override
    public void failure(Request request, Response response, FuelError fuelError) {
        fuelError.printStackTrace();
    }
});
```

> Synchronous

```kotlin
val (_, _, result) = bboxapi.getCurrentChannelSync()
when (result) {
    is Result.Failure -> {
        result.getException().printStackTrace()
    }
    is Result.Success -> {
        println(result.get())
    }
}
```

```java
Triple<Request, Response, Result<Media, FuelError>> data = bboxapi.getCurrentChannelSync();

Request request = data.getFirst();
Response response = data.getSecond();
Result<Media, FuelError> obj = data.getThird();
System.out.println(obj.get());
```

Get current TV channel with the following information :

| Field | Type | Description |
|-------|------|------------|
| mediaService | string |  | 
| mediaState | string | media state (stop/play) |
| mediaTitle | string | channel name |

## Get volume

> Asynchronous

```kotlin
bboxapi.getVolume { _, _, result ->
    when (result) {
        is Result.Failure -> {
            result.getException().printStackTrace()
        }
        is Result.Success -> {
            println(result.get())
        }
    }
}
```

```java
bboxapi.getVolume(new Handler<Volume>() {
    @Override
    public void success(Request request, Response response, Volume volume) {
        System.out.println(volume);
    }

    @Override
    public void failure(Request request, Response response, FuelError fuelError) {
        fuelError.printStackTrace();
    }
});
```

> Synchronous

```kotlin
val (_, _, result) = bboxapi.getVolumeSync()
when (result) {
    is Result.Failure -> {
        result.getException().printStackTrace()
    }
    is Result.Success -> {
        println(result.get())
    }
}
```

```java
Triple<Request, Response, Result<Volume, FuelError>> data = bboxapi.getVolumeSync();

Request request = data.getFirst();
Response response = data.getSecond();
Result<Volume, FuelError> obj = data.getThird();
System.out.println(obj.get());
```

Get volume value


Get current TV channel with the following information :

| Field | Type | Description |
|-------|------|------------|
| volume | string | volume value (yes this is a string !?) | 

## Set volume

> Asynchronous

```kotlin
bboxapi.setVolume(volume = 10) { _, response, result ->
    when (result) {
        is Result.Failure -> {
            result.getException().printStackTrace()
        }
        is Result.Success -> {
            println(response.statusCode)
        }
    }
}
```

```java
bboxapi.setVolume(10, new Handler<byte[]>() {
    @Override
    public void success(Request request, Response response, byte[] body) {
        System.out.println(response.getStatusCode());
    }

    @Override
    public void failure(Request request, Response response, FuelError fuelError) {
        fuelError.printStackTrace();
    }
});
```

> Synchronous

```kotlin
val (_, response, result) = bboxapi.setVolumeSync(volume = 100)
when (result) {
    is Result.Failure -> {
        result.getException().printStackTrace()
    }
    is Result.Success -> {
        println(response.statusCode)
    }
}
```

```java
Triple<Request, Response, Result<byte[], FuelError>> data = bboxapi.setVolumeSync(100);

Request request = data.getFirst();
Response response = data.getSecond();
Result<byte[], FuelError> obj = data.getThird();
System.out.println(response.getStatusCode());
```

Set volume

| Field | Type | Description |
|-------|------|------------|
| volume | int | volume value | 

## Start application

> Asynchronous

```kotlin
bboxapi.startApp(packageName = "com.google.android.youtube.tv") { _, response, result ->
    when (result) {
        is Result.Failure -> {
            result.getException().printStackTrace()
        }
        is Result.Success -> {
            println(response.statusCode)
        }
    }
}
```

```java
bboxapi.startApp("com.google.android.youtube.tv", new Handler<byte[]>() {
    @Override
    public void success(Request request, Response response, byte[] body) {
        System.out.println(response.getStatusCode());
    }

    @Override
    public void failure(Request request, Response response, FuelError fuelError) {
        fuelError.printStackTrace();
    }
});
```

> Synchronous

```kotlin
val (_, response, result) = bboxapi.startAppSync(packageName = "com.google.android.youtube.tv")

when (result) {
    is Result.Failure -> {
        result.getException().printStackTrace()
    }
    is Result.Success -> {
        println(response.statusCode)
    }
}
```

```java
Triple<Request, Response, Result<byte[], FuelError>> data = bboxapi.startAppSync("com.google.android.youtube.tv");

Request request = data.getFirst();
Response response = data.getSecond();
Result<byte[], FuelError> obj = data.getThird();
System.out.println(response.getStatusCode());
```

Start Android application by package name

| Field | Type | Description |
|-------|------|------------|
| packageName | string | application package name (ex: com.google.youtube) | 

## Custom HTTP request

> Asynchronous

```kotlin
bboxapi.createCustomRequest(Fuel.get("/applications")) { _, _, result ->
    when (result) {
        is Result.Failure -> {
            result.getException().printStackTrace()
        }
        is Result.Success -> {
            println(String(result.get()))
        }
    }
}
```

```java
bboxapi.createCustomRequest(Fuel.get("/applications"), new Handler<byte[]>() {
    @Override
    public void success(Request request, Response response, byte[] data) {
        System.out.println(new String(data));
    }

    @Override
    public void failure(Request request, Response response, FuelError fuelError) {
        fuelError.printStackTrace();
    }
});
```

> Synchronous

```kotlin
val (_, _, result) = bboxapi.createCustomRequestSync(Fuel.get("/applications"))
when (result) {
    is Result.Failure -> {
        result.getException().printStackTrace()
    }
    is Result.Success -> {
        println(String(result.get()))
    }
}
```

```java
Triple<Request, Response, Result<byte[], FuelError>> data = bboxapi.createCustomRequestSync(Fuel.get("/applications"));

Request request = data.getFirst();
Response response = data.getSecond();
Result<byte[], FuelError> obj = data.getThird();
System.out.println(new String(obj.get()));
```


Create your own HTTP request, this can be useful for not relying on the library implementation

