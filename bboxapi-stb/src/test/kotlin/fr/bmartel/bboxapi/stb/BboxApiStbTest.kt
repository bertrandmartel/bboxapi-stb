package fr.bmartel.bboxapi.stb

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.HttpException
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.gson.JsonParser
import de.mannodermaus.rxbonjour.BonjourBroadcastConfig
import de.mannodermaus.rxbonjour.RxBonjour
import de.mannodermaus.rxbonjour.drivers.jmdns.JmDNSDriver
import de.mannodermaus.rxbonjour.platforms.desktop.DesktopPlatform
import fr.bmartel.bboxapi.android.stb.TestCase
import fr.bmartel.bboxapi.stb.model.*
import io.reactivex.schedulers.Schedulers
import okhttp3.mockwebserver.MockWebServer
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.CountDownLatch

open class BboxApiStbTest : TestCase() {

    companion object {
        private val mockServer = MockWebServer()
        private val bboxApi = BboxApiStb(APP_ID, APP_SECRET, DesktopPlatform.create())
        @BeforeClass
        @JvmStatic
        fun initMockServer() {
            mockServer.start()
            mockServer.setDispatcher(MockDispatcher())
            bboxApi.cloudHost = mockServer.url("").toString().dropLast(n = 1)
            FuelManager.instance.basePath = "${mockServer.url("").toString().dropLast(n = 1)}/api.bbox.lan/v0"
        }
    }

    @Before
    fun setUp() {
        lock = CountDownLatch(1)
        bboxApi.cloudHost = mockServer.url("").toString().dropLast(n = 1)
        bboxApi.boxIp = mockServer.hostName
        FuelManager.instance.basePath = "${mockServer.url("").toString().dropLast(n = 1)}/api.bbox.lan/v0"
        bboxApi.hasSessionId = false
        bboxApi.tokenValidity = Date().time
        bboxApi.token = ""
        bboxApi.sessionIdValidity = Date().time
        bboxApi.sessionId = ""
        REGISTER_FAIL = false
        SUBSCRIBE_FAIL = false
    }

    @Test
    fun getChannels() {
        TestUtils.executeAsync(testcase = this, filename = "channels.json", body = bboxApi::getChannels)
        //following request doesn't call /sessionId or /token
        lock = CountDownLatch(1)
        TestUtils.executeAsync(testcase = this, filename = "channels.json", body = bboxApi::getChannels)
    }

    @Test
    fun getChannelsCb() {
        TestUtils.executeAsyncCb(testcase = this, filename = "channels.json", body = bboxApi::getChannels)
    }

    @Test
    fun getChannelsSync() {
        TestUtils.executeSync(filename = "channels.json", body = bboxApi::getChannelsSync)
    }

    @Test
    fun getApps() {
        TestUtils.executeAsync(testcase = this, filename = "apps.json", body = bboxApi::getApps)
    }

    @Test
    fun getAppsCb() {
        TestUtils.executeAsyncCb(testcase = this, filename = "apps.json", body = bboxApi::getApps)
    }

    @Test
    fun getAppsSync() {
        TestUtils.executeSync(filename = "apps.json", body = bboxApi::getAppsSync)
    }

    @Test
    fun getAppInfo() {
        TestUtils.executeAsyncOneParam(input = APP_TEST, testcase = this, filename = "app_info.json", body = bboxApi::getAppInfo)
    }

    @Test
    fun getAppInfoCb() {
        TestUtils.executeAsyncOneParamCb(input = APP_TEST, testcase = this, filename = "app_info.json", body = bboxApi::getAppInfo)
    }

    @Test
    fun getAppInfoSync() {
        TestUtils.executeSyncOneParam(input = APP_TEST, filename = "app_info.json", body = bboxApi::getAppInfoSync)
    }

    @Test
    fun getAppIcon() {
        TestUtils.executeAsyncOneParam(input = APP_TEST, testcase = this, filename = "ic_launcher.png", body = bboxApi::getAppIcon, json = false)
    }

    @Test
    fun getAppIconCb() {
        TestUtils.executeAsyncOneParamCb(input = APP_TEST, testcase = this, filename = "ic_launcher.png", body = bboxApi::getAppIcon, json = false)
    }

    @Test
    fun getAppIconSync() {
        TestUtils.executeSyncOneParam(input = APP_TEST, filename = "ic_launcher.png", body = bboxApi::getAppIconSync, json = false)
    }

    @Test
    fun getCurrentChannel() {
        TestUtils.executeAsync(testcase = this, filename = "current_channel.json", body = bboxApi::getCurrentChannel)
    }

    @Test
    fun getCurrentChannelCb() {
        TestUtils.executeAsyncCb(testcase = this, filename = "current_channel.json", body = bboxApi::getCurrentChannel)
    }

    @Test
    fun getCurrentChannelSync() {
        TestUtils.executeSync(filename = "current_channel.json", body = bboxApi::getCurrentChannelSync)
    }

    @Test
    fun getVolume() {
        TestUtils.executeAsync(testcase = this, filename = "volume.json", body = bboxApi::getVolume)
    }

    @Test
    fun getVolumeCb() {
        TestUtils.executeAsyncCb(testcase = this, filename = "volume.json", body = bboxApi::getVolume)
    }

    @Test
    fun getVolumeSync() {
        TestUtils.executeSync(filename = "volume.json", body = bboxApi::getVolumeSync)
    }

    @Test
    fun startApp() {
        TestUtils.executeAsyncOneParam(input = APP_TEST, testcase = this, filename = null, body = bboxApi::startApp)
    }

    @Test
    fun startAppCb() {
        TestUtils.executeAsyncOneParamCb(input = APP_TEST, testcase = this, filename = null, body = bboxApi::startApp)
    }

    @Test
    fun startAppSync() {
        TestUtils.executeSyncOneParam(input = APP_TEST, filename = null, body = bboxApi::startAppSync)
    }

    @Test
    fun displayToast() {
        TestUtils.executeAsyncOneParam(input = ToastRequest(
                message = "this is a toast",
                color = "#FF0000",
                pos_x = 100,
                pos_y = 500
        ), testcase = this, filename = null, body = bboxApi::displayToast)
    }

    @Test
    fun displayToastCb() {
        TestUtils.executeAsyncOneParamCb(input = ToastRequest(
                message = "this is a toast",
                color = "#FF0000",
                pos_x = 100,
                pos_y = 500
        ), testcase = this, filename = null, body = bboxApi::displayToast)
    }

    @Test
    fun displayToastSync() {
        TestUtils.executeSyncOneParam(input = ToastRequest(
                message = "this is a toast",
                color = "#FF0000",
                pos_x = 100,
                pos_y = 500
        ), filename = null, body = bboxApi::displayToastSync)
    }

    @Test
    fun setVolume() {
        TestUtils.executeAsyncOneParam(input = 100, testcase = this, filename = null, body = bboxApi::setVolume)
    }

    @Test
    fun setVolumeCb() {
        TestUtils.executeAsyncOneParamCb(input = 100, testcase = this, filename = null, body = bboxApi::setVolume)
    }

    @Test
    fun setVolumeSync() {
        TestUtils.executeSyncOneParam(input = 100, filename = null, body = bboxApi::setVolumeSync)
    }

    @Test
    fun registerApp() {
        TestUtils.executeAsyncOneParam(input = "myApp", testcase = this, filename = null, body = bboxApi::registerApp)
    }

    @Test
    fun registerAppCb() {
        TestUtils.executeAsyncOneParamCb(input = "myApp", testcase = this, filename = null, body = bboxApi::registerApp)
    }

    @Test
    fun registerAppSync() {
        TestUtils.executeSyncOneParam(input = "myApp", filename = null, body = bboxApi::registerAppSync)
    }

    @Test
    fun unsubscribe() {
        TestUtils.executeAsyncOneParam(input = CHANNELS[0], testcase = this, filename = null, body = bboxApi::unsubscribe)
    }

    @Test
    fun unsubscribeCb() {
        TestUtils.executeAsyncOneParamCb(input = CHANNELS[0], testcase = this, filename = null, body = bboxApi::unsubscribe)
    }

    @Test
    fun unsubscribeSync() {
        TestUtils.executeSyncOneParam(input = CHANNELS[0], filename = null, body = bboxApi::unsubscribeSync)
    }

    @Test
    fun getOpenedChannels() {
        TestUtils.executeAsync(testcase = this, filename = "opened_channels.json", body = bboxApi::getOpenedChannels)
    }

    @Test
    fun getOpenedChannelsCb() {
        TestUtils.executeAsyncCb(testcase = this, filename = "opened_channels.json", body = bboxApi::getOpenedChannels)
    }

    @Test
    fun getOpenedChannelsSync() {
        TestUtils.executeSync(filename = "opened_channels.json", body = bboxApi::getOpenedChannelsSync)
    }

    @Test
    fun unsubscribeAllSync() {
        TestUtils.executeSync(filename = "opened_channels.json", body = bboxApi::unsubscribeAllSync)
    }

    @Test
    fun noHostGetToken() {
        bboxApi.cloudHost = "http://testsetsetset"
        TestUtils.executeAsync(testcase = this, filename = "channels.json", body = bboxApi::getChannels, expectedException = UnknownHostException())
    }

    @Test
    fun noHostSyncGetToken() {
        bboxApi.cloudHost = "http://testsetsetset"
        TestUtils.executeSync(filename = "channels.json", body = bboxApi::getChannelsSync, expectedException = UnknownHostException())
    }

    @Test
    fun notFoundGetToken() {
        bboxApi.cloudHost = mockServer.url("").toString().dropLast(n = 1) + "/test"
        TestUtils.executeAsync(testcase = this, filename = "channels.json", body = bboxApi::getChannels, expectedException = HttpException(httpCode = 404, httpMessage = "Client Error"))
    }

    @Test
    fun notFoundGetTokenSync() {
        bboxApi.cloudHost = mockServer.url("").toString().dropLast(n = 1) + "/test"
        TestUtils.executeSync(filename = "channels.json", body = bboxApi::getChannelsSync, expectedException = HttpException(httpCode = 404, httpMessage = "Client Error"))
    }

    @Test
    fun noHostGetSessionId() {
        FuelManager.instance.basePath = "http://testsetsetset/api.bbox.lan/v0"
        TestUtils.executeAsync(testcase = this, filename = "channels.json", body = bboxApi::getChannels, expectedException = UnknownHostException())
    }

    @Test
    fun noHostGetSessionIdSync() {
        FuelManager.instance.basePath = "http://testsetsetset/api.bbox.lan/v0"
        TestUtils.executeSync(filename = "channels.json", body = bboxApi::getChannelsSync, expectedException = UnknownHostException())
    }

    @Test
    fun notFoundGetSessionId() {
        FuelManager.instance.basePath = "${mockServer.url("").toString().dropLast(n = 1)}/test/api.bbox.lan/v0"
        TestUtils.executeAsync(testcase = this, filename = "channels.json", body = bboxApi::getChannels, expectedException = HttpException(httpCode = 404, httpMessage = "Client Error"))
    }

    @Test
    fun notFoundGetSessionIdSync() {
        FuelManager.instance.basePath = "${mockServer.url("").toString().dropLast(n = 1)}/test/api.bbox.lan/v0"
        TestUtils.executeSync(filename = "channels.json", body = bboxApi::getChannelsSync, expectedException = HttpException(httpCode = 404, httpMessage = "Client Error"))
    }

    @Test
    fun getChannelWithTokenValidityOk() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.HOUR_OF_DAY, 1)
        bboxApi.tokenValidity = cal.time.time
        TestUtils.executeAsync(testcase = this, filename = "channels.json", body = bboxApi::getChannels)
    }

    @Test
    fun getChannelWithTokenValidityOkSync() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.HOUR_OF_DAY, 1)
        bboxApi.tokenValidity = cal.time.time
        TestUtils.executeSync(filename = "channels.json", body = bboxApi::getChannelsSync)
    }

    @Test
    fun getChannelWithSessionIdValidityOk() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.HOUR_OF_DAY, 1)
        bboxApi.sessionIdValidity = cal.time.time
        TestUtils.executeAsync(testcase = this, filename = "channels.json", body = bboxApi::getChannels)
    }

    @Test
    fun getChannelWithSessionIdValidityOkSync() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.HOUR_OF_DAY, 1)
        bboxApi.sessionIdValidity = cal.time.time
        TestUtils.executeSync(filename = "channels.json", body = bboxApi::getChannelsSync)
    }

    @Test
    fun getChannelWithSessionIdAndTokenValidityOk() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.HOUR_OF_DAY, 1)
        bboxApi.sessionIdValidity = cal.time.time
        bboxApi.tokenValidity = cal.time.time
        TestUtils.executeAsync(testcase = this, filename = "channels.json", body = bboxApi::getChannels)
    }

    @Test
    fun getChannelWithSessionIdAndTokenValidityOkSync() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.HOUR_OF_DAY, 1)
        bboxApi.sessionIdValidity = cal.time.time
        bboxApi.tokenValidity = cal.time.time
        TestUtils.executeSync(filename = "channels.json", body = bboxApi::getChannelsSync)
    }

    @Test
    fun getChannelWithTokenValidityOkButNoProblem() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.HOUR_OF_DAY, 1)
        bboxApi.tokenValidity = cal.time.time
        bboxApi.token = TOKEN
        TestUtils.executeAsync(testcase = this, filename = "channels.json", body = bboxApi::getChannels)
    }

    @Test
    fun getChannelWithTokenValidityOkButNoProblemSync() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.HOUR_OF_DAY, 1)
        bboxApi.tokenValidity = cal.time.time
        bboxApi.token = TOKEN
        TestUtils.executeSync(filename = "channels.json", body = bboxApi::getChannelsSync)
    }

    @Test
    fun getChannelWithSessionValidityOkButNoProblem() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.HOUR_OF_DAY, 1)
        bboxApi.sessionIdValidity = cal.time.time
        bboxApi.sessionId = SESSION_ID
        TestUtils.executeAsync(testcase = this, filename = "channels.json", body = bboxApi::getChannels)
    }

    @Test
    fun getChannelWithSessionValidityOkButNoProblemSync() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.HOUR_OF_DAY, 1)
        bboxApi.sessionIdValidity = cal.time.time
        bboxApi.sessionId = SESSION_ID
        TestUtils.executeSync(filename = "channels.json", body = bboxApi::getChannelsSync)
    }

    @Test
    fun testModel() {
        val channel = Channel(mediaState = "stop", mediaTitle = "TF1", positionId = "1")
        Assert.assertEquals("stop", channel.mediaState)
        Assert.assertEquals("TF1", channel.mediaTitle)
        Assert.assertEquals("1", channel.positionId)

        val application = Application(
                appId = "15224372559840.1372093189580732",
                appName = "6play",
                appState = "foreground",
                component = "fr.m6.m6replay.by/fr.m6.m6replay.tv.activity.SplashActivity",
                data = "",
                leanback = true,
                logoUrl = "/applications/fr.m6.m6replay.by/image",
                packageName = "fr.m6.m6replay.by"
        )
        Assert.assertEquals("15224372559840.1372093189580732", application.appId)
        Assert.assertEquals("6play", application.appName)
        Assert.assertEquals("foreground", application.appState)
        Assert.assertEquals("fr.m6.m6replay.by/fr.m6.m6replay.tv.activity.SplashActivity", application.component)
        Assert.assertEquals("", application.data)
        Assert.assertEquals(true, application.leanback)
        Assert.assertEquals("/applications/fr.m6.m6replay.by/image", application.logoUrl)
        Assert.assertEquals("fr.m6.m6replay.by", application.packageName)
        val media = Media(mediaService = "Live", mediaState = "play", mediaTitle = "TF1", positionId = "1")
        Assert.assertEquals("Live", media.mediaService)
        Assert.assertEquals("play", media.mediaState)
        Assert.assertEquals("TF1", media.mediaTitle)
        Assert.assertEquals("1", media.positionId)
        val volume = Volume("100")
        Assert.assertEquals("100", volume.volume)
    }

    @Test
    fun startDiscovery() {
        val rxBonjour = RxBonjour.Builder()
                .platform(DesktopPlatform.create())
                .driver(JmDNSDriver.create())
                .create()

        val broadcastConfig = BonjourBroadcastConfig(
                type = BBOXAPI_REST_SERVICE_TYPE,
                name = BBOXAPI_REST_SERVICE_NAME,
                address = null,
                port = 13337)

        val disposable = rxBonjour.newBroadcast(broadcastConfig)
                .subscribeOn(Schedulers.io())
                .subscribe()

        var found = false

        bboxApi.startRestDiscovery(findOneAndExit = true, maxDuration = 10000) { eventType, service, error ->
            Assert.assertNotNull(service)
            Assert.assertNull(error)
            Assert.assertEquals(eventType, StbServiceEvent.SERVICE_FOUND)
            Assert.assertEquals(service?.port, 13337)
            Assert.assertEquals(service?.ip, "127.0.0.1")
            found = true
            lock.countDown()
        }
        lock.await()
        Assert.assertTrue(found)
        Assert.assertNotNull(bboxApi.serviceDiscovery)
        Assert.assertTrue(bboxApi.serviceDiscovery?.isDisposed ?: false)

        //wait for 2 seconds for discovery stopped event
        found = false
        lock = CountDownLatch(1)
        disposable.dispose()
        bboxApi.startRestDiscovery(findOneAndExit = true, maxDuration = 2000) { eventType, service, error ->
            Assert.assertNull(service)
            Assert.assertNull(error)
            Assert.assertEquals(eventType, StbServiceEvent.DISCOVERY_STOPPED)
            found = true
            lock.countDown()
        }
        lock.await()
        Assert.assertTrue(found)
        Assert.assertNotNull(bboxApi.serviceDiscovery)
        Assert.assertTrue(bboxApi.serviceDiscovery?.isDisposed ?: false)
    }

    @Test
    fun subscribeNotification() {
        var throwable: Throwable? = null

        var clientOpened = false
        var clientClosed = false
        var mediaMessageReceived: MediaEvent? = null
        var appMessageReceived: AppEvent? = null
        var errorMessageReceived: BboxApiError? = null

        var serverOpened = false
        var serverClosed = false
        var serverMessageReceived: String? = null
        var serverError: Exception? = null

        var serverStarted = false
        var connection: WebSocket? = null

        val websocketServer = object : WebSocketServer(InetSocketAddress(0)) {
            override fun onError(conn: WebSocket?, ex: Exception?) {
                ex?.printStackTrace()
                serverError = ex
                lock.countDown()
            }

            override fun onStart() {
                serverStarted = true
                lock.countDown()
            }

            override fun onMessage(conn: WebSocket?, message: String?) {
                serverMessageReceived = message
                connection = conn
                lock.countDown()
            }

            override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
                serverClosed = true
            }

            override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
                serverOpened = true
            }
        }
        websocketServer.start()
        lock.await()
        bboxApi.boxWebsocketPort = websocketServer.port
        Assert.assertTrue(serverStarted)

        lock = CountDownLatch(1)
        val notificationChannel = bboxApi.subscribeNotification("appName",
                listOf(Resource.Application, Resource.Media, Resource.Message),
                object : BboxApiStb.WebSocketListener {

                    override fun onOpen() {
                        clientOpened = true
                    }

                    override fun onClose() {
                        clientClosed = true
                        lock.countDown()
                    }

                    override fun onFailure(t: Throwable?) {
                        t?.printStackTrace()
                        throwable = t
                        lock.countDown()
                    }

                    override fun onApp(app: AppEvent) {
                        appMessageReceived = app
                        lock.countDown()
                    }

                    override fun onMedia(media: MediaEvent) {
                        mediaMessageReceived = media
                        lock.countDown()
                    }

                    override fun onError(error: BboxApiError) {
                        errorMessageReceived = error
                        lock.countDown()
                    }
                })
        Assert.assertNotNull(notificationChannel)
        Assert.assertEquals(CHANNEL_ID, notificationChannel.channelId)
        Assert.assertTrue(notificationChannel.subscribeResult.third is Result.Success)

        //wait for server to receive channel ID
        lock.await()

        //client
        Assert.assertTrue(clientOpened)
        Assert.assertFalse(clientClosed)
        Assert.assertNull(mediaMessageReceived)
        Assert.assertNull(appMessageReceived)
        Assert.assertNull(errorMessageReceived)
        Assert.assertNull(throwable)

        //server
        Assert.assertTrue(serverOpened)
        Assert.assertFalse(serverClosed)
        Assert.assertEquals(CHANNEL_ID, serverMessageReceived)
        Assert.assertNull(serverError)

        Assert.assertNotNull(connection)
        Assert.assertNotNull(bboxApi.websocket)

        clientOpened = false
        clientClosed = false
        serverMessageReceived = null

        //send Media notification
        lock = CountDownLatch(1)
        connection?.send(TestUtils.getResTextFile("websocket_media.json"))
        lock.await()
        TestUtils.sendNotificationAndWait(filename = "websocket_media.json", response = mediaMessageReceived)

        //send App notification
        lock = CountDownLatch(1)
        connection?.send(TestUtils.getResTextFile("websocket_application.json"))
        lock.await()
        TestUtils.sendNotificationAndWait(filename = "websocket_application.json", response = appMessageReceived)

        //send Error notification
        lock = CountDownLatch(1)
        connection?.send(TestUtils.getResTextFile("websocket_error_appname.json"))
        lock.await()
        TestUtils.sendNotificationAndWait(filename = "websocket_error_appname.json", response = errorMessageReceived, error = true)

        //unknown notification format (not JSON)
        lock = CountDownLatch(1)
        connection?.send("{something unexpected}")
        lock.await()
        Assert.assertNotNull(errorMessageReceived)
        JSONAssert.assertEquals(Gson().toJson(BboxApiError("can't parse event : {something unexpected}")), Gson().toJson(errorMessageReceived), false)

        //unknown notification format (JSON)
        lock = CountDownLatch(1)
        connection?.send("{\"some\":\"thing\"}")
        lock.await()
        Assert.assertNotNull(errorMessageReceived)
        JSONAssert.assertEquals(Gson().toJson(BboxApiError("can't parse event : {\"some\":\"thing\"}")), Gson().toJson(errorMessageReceived), false)

        //unknown resourceId)
        lock = CountDownLatch(1)
        connection?.send("{\"resourceId\":\"something\"}")
        lock.await()
        Assert.assertNotNull(errorMessageReceived)
        JSONAssert.assertEquals(Gson().toJson(BboxApiError("can't parse event : {\"resourceId\":\"something\"}")), Gson().toJson(errorMessageReceived), false)

        lock = CountDownLatch(1)
        bboxApi.closeWebsocket()
        //wait for closing
        lock.await()
        Assert.assertNull(serverMessageReceived)
        Assert.assertNull(throwable)
        Assert.assertTrue(clientClosed)
        Assert.assertFalse(clientOpened)
    }

    @Test
    fun subscribeNotificationRegisterFail() {
        REGISTER_FAIL = true
        val notificationChannel = bboxApi.subscribeNotification("appName",
                listOf(Resource.Application, Resource.Media, Resource.Message),
                object : BboxApiStb.WebSocketListener {
                    override fun onApp(app: AppEvent) {
                    }

                    override fun onMedia(media: MediaEvent) {
                    }

                    override fun onError(error: BboxApiError) {
                    }

                    override fun onOpen() {
                    }

                    override fun onClose() {
                    }

                    override fun onFailure(t: Throwable?) {
                    }
                })
        Assert.assertNotNull(notificationChannel)
        Assert.assertNull(notificationChannel.channelId)
        Assert.assertTrue(notificationChannel.subscribeResult.third is Result.Failure)
    }

    @Test
    fun subscribeNotificationSubscribeFail() {
        SUBSCRIBE_FAIL = true
        val notificationChannel = bboxApi.subscribeNotification("appName",
                listOf(Resource.Application, Resource.Media, Resource.Message),
                object : BboxApiStb.WebSocketListener {
                    override fun onApp(app: AppEvent) {
                    }

                    override fun onMedia(media: MediaEvent) {
                    }

                    override fun onError(error: BboxApiError) {
                    }

                    override fun onOpen() {
                    }

                    override fun onClose() {
                    }

                    override fun onFailure(t: Throwable?) {
                    }
                })
        Assert.assertNotNull(notificationChannel)
        Assert.assertNull(notificationChannel.channelId)
        Assert.assertTrue(notificationChannel.subscribeResult.third is Result.Failure)
    }
}