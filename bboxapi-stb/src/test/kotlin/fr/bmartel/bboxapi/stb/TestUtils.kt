package fr.bmartel.bboxapi.stb

import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import fr.bmartel.bboxapi.android.stb.TestCase
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Assert
import org.skyscreamer.jsonassert.JSONAssert
import java.io.File
import java.net.UnknownHostException

class TestUtils {

    companion object {
        fun getResTextFile(fileName: String): String {
            val classLoader = javaClass.classLoader
            return File(classLoader.getResource(fileName).file).readText()
        }

        fun getResBinaryFile(fileName: String): ByteArray {
            val classLoader = javaClass.classLoader
            return File(classLoader.getResource(fileName).file).readBytes()
        }

        fun <T, Y> executeAsyncOneParam(input: Y,
                                        testcase: TestCase,
                                        filename: String?,
                                        body: (input: Y, handler: (Request, Response, Result<*, FuelError>) -> Unit) -> T,
                                        expectedException: Exception? = null,
                                        json: Boolean = true) {
            var request: Request? = null
            var response: Response? = null
            var data: Any? = null
            var err: FuelError? = null
            body(input) { req, res, result ->
                request = req
                response = res
                val (d, e) = result
                data = d
                err = e
                testcase.lock.countDown()
            }
            testcase.await()
            checkAsyncResult(
                    filename = filename,
                    request = request,
                    response = response,
                    data = data,
                    err = err,
                    expectedException = expectedException,
                    json = json)
        }

        fun <T, A, B, C> executeAsyncThreeParam(input1: A,
                                                input2: B,
                                                input3: C,
                                                testcase: TestCase,
                                                filename: String?,
                                                body: (input1: A, input2: B, input3: C, handler: (Request, Response, Result<*, FuelError>) -> Unit) -> T,
                                                expectedException: Exception? = null,
                                                json: Boolean = true) {
            var request: Request? = null
            var response: Response? = null
            var data: Any? = null
            var err: FuelError? = null
            body(input1, input2, input3) { req, res, result ->
                request = req
                response = res
                val (d, e) = result
                data = d
                err = e
                testcase.lock.countDown()
            }
            testcase.await()
            checkAsyncResult(
                    filename = filename,
                    request = request,
                    response = response,
                    data = data,
                    err = err,
                    expectedException = expectedException,
                    json = json)
        }

        fun <T, Y> executeAsyncOneParamCb(input: Y,
                                          testcase: TestCase,
                                          filename: String?,
                                          body: (input: Y, handler: Handler<T>) -> T,
                                          expectedException: Exception? = null,
                                          json: Boolean = true) {
            var request: Request? = null
            var response: Response? = null
            var data: Any? = null
            var err: FuelError? = null

            body(input, object : Handler<T> {
                override fun failure(req: Request, res: Response, e: FuelError) {
                    request = req
                    response = res
                    err = e
                    testcase.lock.countDown()
                }

                override fun success(req: Request, res: Response, d: T) {
                    request = req
                    response = res
                    data = d
                    testcase.lock.countDown()
                }
            })
            testcase.await()
            checkAsyncResult(
                    filename = filename,
                    request = request,
                    response = response,
                    data = data,
                    err = err,
                    expectedException = expectedException,
                    json = json)
        }

        fun <T, A, B, C> executeAsyncThreeParamCb(input1: A,
                                                  input2: B,
                                                  input3: C,
                                                  testcase: TestCase,
                                                  filename: String?,
                                                  body: (input1: A, input2: B, input3: C, handler: Handler<T>) -> T,
                                                  expectedException: Exception? = null,
                                                  json: Boolean = true) {
            var request: Request? = null
            var response: Response? = null
            var data: Any? = null
            var err: FuelError? = null

            body(input1, input2, input3, object : Handler<T> {
                override fun failure(req: Request, res: Response, e: FuelError) {
                    request = req
                    response = res
                    err = e
                    testcase.lock.countDown()
                }

                override fun success(req: Request, res: Response, d: T) {
                    request = req
                    response = res
                    data = d
                    testcase.lock.countDown()
                }
            })
            testcase.await()
            checkAsyncResult(
                    filename = filename,
                    request = request,
                    response = response,
                    data = data,
                    err = err,
                    expectedException = expectedException,
                    json = json)
        }

        fun <T, Y> executeSyncOneParam(filename: String?, input: Y, body: (input: Y) -> T, expectedException: Exception? = null, json: Boolean = true) {
            val (_, response, result) = body(input) as Triple<Request, Response, *>
            val (data, err) = result as Result<*, FuelError>
            checkSyncResult(filename = filename, response = response, data = data, err = err, expectedException = expectedException, json = json)
        }

        fun <T, A, B, C> executeThreeParamsSync(filename: String?, input1: A, input2: B, input3: C, body: (input1: A, input2: B, input3: C) -> T, expectedException: Exception? = null) {
            val (_, response, result) = body(input1, input2, input3) as Triple<Request, Response, *>
            val (data, err) = result as Result<*, FuelError>
            checkSyncResult(filename = filename, response = response, data = data, err = err, expectedException = expectedException)
        }

        fun <T> executeSync(filename: String?, body: () -> T, expectedException: Exception? = null) {
            val (_, response, result) = body() as Triple<Request, Response, *>
            val (data, err) = result as Result<*, FuelError>
            checkSyncResult(filename = filename, response = response, data = data, err = err, expectedException = expectedException)
        }

        fun <T> executeAsyncCb(testcase: TestCase, filename: String?, body: (handler: Handler<T>) -> T, expectedException: Exception? = null) {
            var request: Request? = null
            var response: Response? = null
            var data: Any? = null
            var err: FuelError? = null

            body(object : Handler<T> {
                override fun failure(req: Request, res: Response, e: FuelError) {
                    request = req
                    response = res
                    err = e
                    testcase.lock.countDown()
                }

                override fun success(req: Request, res: Response, d: T) {
                    request = req
                    response = res
                    data = d
                    testcase.lock.countDown()
                }
            })
            testcase.await()
            checkAsyncResult(
                    filename = filename,
                    request = request,
                    response = response,
                    data = data,
                    err = err,
                    expectedException = expectedException)
        }

        fun <T> executeAsync(testcase: TestCase, filename: String?, body: (handler: (Request, Response, Result<*, FuelError>) -> Unit) -> T, expectedException: Exception? = null) {
            var request: Request? = null
            var response: Response? = null
            var data: Any? = null
            var err: FuelError? = null
            body { req, res, result ->
                request = req
                response = res
                val (d, e) = result
                data = d
                err = e
                testcase.lock.countDown()
            }
            testcase.await()
            checkAsyncResult(
                    filename = filename,
                    request = request,
                    response = response,
                    data = data,
                    err = err,
                    expectedException = expectedException)
        }

        private fun checkAsyncResult(filename: String?,
                                     request: Request?,
                                     response: Response?,
                                     data: Any?,
                                     err: FuelError?,
                                     expectedException: Exception?,
                                     json: Boolean = true) {
            MatcherAssert.assertThat(request, CoreMatchers.notNullValue())
            MatcherAssert.assertThat(response, CoreMatchers.notNullValue())

            if (expectedException != null) {
                checkFullError(fuelError = err, expectedException = expectedException)
            } else {
                Assert.assertEquals(200, response?.statusCode)
                MatcherAssert.assertThat(err?.exception, CoreMatchers.nullValue())
                MatcherAssert.assertThat(data, CoreMatchers.notNullValue())
                if (filename != null && json) {
                    JSONAssert.assertEquals(TestUtils.getResTextFile(fileName = filename), Gson().toJson(data), false)
                } else if (filename != null && !json) {
                    Assert.assertEquals(TestUtils.getResBinaryFile(fileName = filename).size, (data as ByteArray).size)
                }
            }
        }

        private fun <T> checkSyncResult(filename: String?,
                                        response: Response,
                                        data: T, err: FuelError?,
                                        expectedException: Exception?,
                                        json: Boolean = true) {
            if (expectedException != null) {
                checkFullError(fuelError = err, expectedException = expectedException)
            } else {
                Assert.assertEquals(200, response.statusCode)
                MatcherAssert.assertThat(err?.exception, CoreMatchers.nullValue())
                MatcherAssert.assertThat(data, CoreMatchers.notNullValue())
                if (filename != null && json) {
                    JSONAssert.assertEquals(TestUtils.getResTextFile(fileName = filename), Gson().toJson(data), false)
                } else if (filename != null && !json) {
                    Assert.assertEquals(TestUtils.getResBinaryFile(fileName = filename).size, (data as ByteArray).size)
                }
            }
        }

        fun checkFullError(fuelError: FuelError?, expectedException: Exception) {
            MatcherAssert.assertThat(fuelError, CoreMatchers.notNullValue())
            MatcherAssert.assertThat(fuelError?.exception, CoreMatchers.notNullValue())
            if (expectedException is UnknownHostException) {
                Assert.assertTrue(fuelError?.exception is UnknownHostException)
            } else if (expectedException is HttpException) {
                Assert.assertTrue(fuelError?.exception is HttpException)
                val exception = fuelError?.exception as HttpException
                Assert.assertEquals(exception.message, fuelError.exception.message)
            } else if (expectedException is JsonSyntaxException) {
                Assert.assertTrue(fuelError?.exception is JsonSyntaxException)
            } else {
                Assert.fail("unchecked exception : $fuelError")
            }
        }

        fun <T> sendNotificationAndWait(filename: String, response: T, error: Boolean = false) {
            Assert.assertNotNull(response)
            val data = JsonParser().parse(TestUtils.getResTextFile(fileName = filename)).asJsonObject
            if (error) {
                JSONAssert.assertEquals(data.toString(), Gson().toJson(response), false)

            } else {
                JSONAssert.assertEquals(data.getAsJsonObject("body").toString(), Gson().toJson(response), false)
            }
        }

        inline fun <reified U> checkCustomResponse(testcase: TestCase,
                                                   inputReq: Request,
                                                   filename: String?,
                                                   expectedException: Exception?,
                                                   body: (Request, handler: (Request, Response, Result<ByteArray, FuelError>) -> Unit) -> Unit) {
            var request: Request? = null
            var response: Response? = null
            var data: ByteArray? = null
            var err: FuelError? = null
            body(inputReq) { req, res, result ->
                request = req
                response = res
                val (d, e) = result
                data = d
                err = e
                testcase.lock.countDown()
            }
            testcase.await()
            checkFuelResponseResult<U>(filename = filename,
                    request = request,
                    response = response,
                    data = data ?: ByteArray(0),
                    err = err,
                    expectedException = expectedException)
        }


        inline fun <reified U> checkCustomResponseCb(testcase: TestCase,
                                                     inputReq: Request,
                                                     filename: String?,
                                                     expectedException: Exception?,
                                                     body: (Request, handler: Handler<ByteArray>) -> Unit) {
            var request: Request? = null
            var response: Response? = null
            var data: ByteArray? = null
            var err: FuelError? = null
            body(inputReq, object : Handler<ByteArray> {
                override fun failure(req: Request, res: Response, e: FuelError) {
                    request = req
                    response = res
                    err = e
                    testcase.lock.countDown()
                }

                override fun success(req: Request, res: Response, d: ByteArray) {
                    request = req
                    response = res
                    data = d
                    testcase.lock.countDown()
                }
            })
            testcase.await()
            checkFuelResponseResult<U>(filename = filename,
                    request = request,
                    response = response,
                    data = data ?: ByteArray(0),
                    err = err,
                    expectedException = expectedException)
        }

        inline fun <reified U> checkCustomResponseSync(
                inputReq: Request,
                filename: String?,
                expectedException: Exception?,
                body: (Request) -> Triple<Request, Response, *>) {
            val (request, response, result) = body(inputReq)
            val (data, err) = result as Result<ByteArray, FuelError>

            checkFuelResponseResult<U>(filename = filename,
                    request = request,
                    response = response,
                    data = data ?: ByteArray(0),
                    err = err,
                    expectedException = expectedException)
        }

        //https://stackoverflow.com/a/33381385/2614364
        inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object : TypeToken<T>() {}.type)

        inline fun <reified U> checkFuelResponseResult(filename: String?,
                                                       request: Request?,
                                                       response: Response?,
                                                       data: ByteArray,
                                                       err: FuelError?,
                                                       expectedException: Exception?) {
            Assert.assertNotNull(request)
            Assert.assertNotNull(response)
            if (expectedException != null) {
                Assert.assertNull(data)
                checkFullError(fuelError = err, expectedException = expectedException)
            } else {
                Assert.assertNull(err)
                Assert.assertNotNull(data)
                if (filename != null) {
                    JSONAssert.assertEquals(
                            TestUtils.getResTextFile(fileName = filename),
                            Gson().toJson(Gson().fromJson<U>(String(data))), false)
                }
            }
        }
    }
}