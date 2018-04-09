# Errors

> check exception type & response status code on failure :

```kotlin
bboxapi.getApps { _, response, result ->
    when (result) {
        is Result.Failure -> {
            val ex = result.getException()
            when {
                ex.exception is HttpException -> println("http error : ${response.statusCode}")
                else -> ex.printStackTrace()
            }
        }
        is Result.Success -> {
            val data = result.get()
            println(data)
        }
    }
}
```

```java
bboxapi.getApps(new Handler<List<Application>>() {
    @Override
    public void failure(Request request, Response response, FuelError error) {
        if (error.getException() instanceof HttpException) {
            System.out.println("http error : " + response.getStatusCode());
        } else {
            error.printStackTrace();
        }
    }

    @Override
    public void success(Request request, Response response, List<Application> data) {
        System.out.println(data);
    }
});
```

`FuelError` can be checked for exceptions, for instance : 

| Exception             |  description   |
|-----------------------|------------------------------------------|
| HttpException         | a non 2XX HTTP response was received, check the status code from the response |

Among `HttpException`, you can find the following :

Error Code | Meaning
---------- | -------
400 | Bad Request -- request format is invalid
404 | Not Found -- endpoint doesn't exist (check it starts with http://$IP:$PORT/api.bbox.lan/v0)