package fr.bmartel.bboxapi.stb

import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.gson.gsonDeserializerOf
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMapError
import com.google.gson.*
import de.mannodermaus.rxbonjour.Platform
import de.mannodermaus.rxbonjour.RxBonjour
import de.mannodermaus.rxbonjour.drivers.jmdns.JmDNSDriver
import fr.bmartel.bboxapi.stb.model.*
import fr.bmartel.bboxapi.stb.utils.NetworkUtils
import io.reactivex.disposables.Disposable
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import java.util.*
import kotlin.concurrent.schedule

const val BBOXAPI_REST_SERVICE_TYPE = "_http._tcp"
const val BBOXAPI_REST_SERVICE_NAME = "Bboxapi"
const val BBOXAPI_WS_SERVICE_TYPE = "_ws._tcp"
const val BBOXAPI_WS_SERVICE_NAME = "Bboxapi"

class BboxApiStb(val appId: String, val appSecret: String) {

    var hasSessionId = false

    var cloudHost: String = "https://api.bbox.fr"

    var tokenValidity: Long = Date().time
    var token: String = ""

    var sessionIdValidity: Long = Date().time
    var sessionId: String = ""

    var serviceRestDiscovery: Disposable? = null
    var serviceWsDiscovery: Disposable? = null

    var discoveryRestTimer: Timer? = null
    var discoveryWsTimer: Timer? = null

    var httpClient = OkHttpClient()

    var websocket: WebSocket? = null

    val manager = FuelManager()

    var discoveringRest = false
    var discoveringWs = false

    var preferredIp: String? = null
    var preferredPort: Int? = null
    /**
     * list of Bbox API STB Rest services found on network.
     */
    var restServiceList = mutableListOf<StbService>()

    /**
     * list of Bbox API STB Websocket services found on network.
     */
    var wsServiceList = mutableListOf<StbService>()

    /**
     * the selected Bbox API STB Rest service.
     */
    var restService: StbService? = null

    /**
     * the selected Bbox API STB Websocket service.
     */
    var wsService: StbService? = null

    init {
        manager.basePath = "http://localhost"
    }

    fun setBasePath(basePath: String) {
        manager.basePath = basePath
    }

    fun selectRestService(service: StbService) {
        restService = service
        setBasePath(basePath = "http://${service.ip}:${service.port}/api.bbox.lan/v0")
    }

    fun selectWsService(service: StbService) {
        wsService = service
    }

    private fun buildTokenRequest(): Request {
        return manager.request(method = Method.POST, path = "$cloudHost/v1.3/security/token")
                .header(mapOf("Content-Type" to "application/json"))
                .body(GsonBuilder().disableHtmlEscaping().create().toJson(TokenRequest(appId = appId, appSecret = appSecret)))
    }

    private fun buildSessionIdRequest(token: String): Request {
        return manager.request(method = Method.POST, path = "/security/sessionId")
                .header(mapOf("Content-Type" to "application/json"))
                .body(GsonBuilder().disableHtmlEscaping().create().toJson(SessionIdRequest(token = token)))
    }

    private fun buildChannelListRequest(): Request {
        return manager.request(method = Method.GET, path = "/media/tvchannellist")
    }

    private fun buildAppsRequest(): Request {
        return manager.request(method = Method.GET, path = "/applications")
    }

    private fun buildAppInfoRequest(packageName: String): Request {
        return manager.request(method = Method.GET, path = "/applications/$packageName")
    }

    private fun buildAppIconRequest(packageName: String): Request {
        return manager.request(method = Method.GET, path = "/applications/$packageName/image")
    }

    private fun buildCurrentChannelRequest(): Request {
        return manager.request(method = Method.GET, path = "/media")
    }

    private fun buildVolumeRequest(): Request {
        return manager.request(method = Method.GET, path = "/userinterface/volume")
    }

    private fun buildStartAppRequest(packageName: String): Request {
        return manager.request(method = Method.POST, path = "/applications/$packageName")
    }

    private fun buildDisplayToastRequest(toast: ToastRequest): Request {
        return manager.request(method = Method.POST, path = "/userinterface/toast")
                .header(mapOf("Content-Type" to "application/json"))
                .body(GsonBuilder().disableHtmlEscaping().create().toJson(toast))
    }

    private fun buildSetVolumeRequest(volume: Int): Request {
        return manager.request(method = Method.POST, path = "/userinterface/volume")
                .header(mapOf("Content-Type" to "application/json"))
                .body(GsonBuilder().disableHtmlEscaping().create().toJson(VolumeRequest(volume)))
    }

    private fun buildRegisterAppRequest(appName: String): Request {
        return manager.request(method = Method.POST, path = "/applications/register")
                .header(mapOf("Content-Type" to "application/json"))
                .body(GsonBuilder().disableHtmlEscaping().create().toJson(RegisterRequest(appName)))
    }

    private fun buildSubscribeNotifRequest(appId: String, resourceList: List<Resource>): Request {
        val list = mutableListOf<ResourceItem>()
        resourceList.forEach { it ->
            list.add(ResourceItem(it.name))
        }
        return manager.request(method = Method.POST, path = "/notification")
                .header(mapOf("Content-Type" to "application/json"))
                .body(GsonBuilder().disableHtmlEscaping().create().toJson(SubscribeRequest(appId, list)))
    }

    private fun buildUnsubscribeNotifRequest(channelId: String): Request {
        return manager.request(method = Method.DELETE, path = "/notification/$channelId")
    }

    private fun buildGetOpenedChannelRequest(): Request {
        return manager.request(method = Method.GET, path = "/notification")
    }

    private fun buildPostNotification(channelId: String, appId: String, message: String): Request {
        return manager.request(method = Method.POST, path = "/notification/$channelId")
                .header(mapOf("Content-Type" to "application/json"))
                .body(GsonBuilder().disableHtmlEscaping().create().toJson(NotificationRequest(appId, message)))
    }

    /**
     * discover Bbox API Rest service.
     */
    fun startRestDiscovery(findOneAndExit: Boolean = false, platform: Platform, maxDuration: Int = 0, handler: (StbServiceEvent, StbService?, Boolean, Throwable?) -> Unit): Boolean {
        if (!discoveringRest) {
            discoveringRest = true
            val rxBonjour = RxBonjour.Builder()
                    .platform(platform)
                    .driver(JmDNSDriver.create())
                    .create()

            //empty service list before discovery
            restServiceList.clear()
            restService = null
            wsService = null
            val obs = rxBonjour.newDiscovery(type = BBOXAPI_REST_SERVICE_TYPE)
            serviceRestDiscovery = obs.subscribe(
                    { event ->
                        if (event.service.name.startsWith(BBOXAPI_REST_SERVICE_NAME)) {
                            if (findOneAndExit) {
                                stopDiscoveryRest()
                            }
                            val stbService = StbService(event.service.host.hostAddress, event.service.port)
                            restServiceList.add(stbService)
                            val changed = chooseRestService(stbService)

                            handler(StbServiceEvent.SERVICE_FOUND, stbService, changed, null)
                        }
                    },
                    { error ->
                        discoveringRest = false
                        handler(StbServiceEvent.DISCOVERY_ERROR, null, false, error)
                    })
            if (maxDuration > 0) {
                discoveryRestTimer = Timer()
                discoveryRestTimer?.schedule(delay = maxDuration.toLong()) {
                    stopDiscoveryRest()
                    handler(StbServiceEvent.DISCOVERY_STOPPED, null, false, null)
                    discoveryRestTimer?.cancel()
                }
            }
            return true
        }
        return false
    }

    private fun chooseRestService(stbService: StbService): Boolean {
        val currentIp = NetworkUtils.getIPAddress(true)
        var ipFound = false
        val restPreferredIp = mutableListOf<StbService>()
        val restCurrentIp = mutableListOf<StbService>()
        var changed = false
        for (service in restServiceList) {
            if (service.ip == currentIp) {
                ipFound = true
                restCurrentIp.add(service)
            }
        }
        if (preferredIp != null && preferredIp != "" && preferredPort != null) {
            var ipPreferred = false
            for (service in restServiceList) {
                if (service.ip == preferredIp && service.port == preferredPort) {
                    ipPreferred = true
                    restPreferredIp.add(service)
                }
            }
            when {
                ipPreferred -> {
                    for (service in restPreferredIp) {
                        if (service.port == 8080) {
                            selectRestService(service)
                            return true
                        }
                    }
                    for (service in restPreferredIp) {
                        if (service.port != 8080) {
                            selectRestService(service)
                            return true
                        }
                    }
                }
                ipFound -> {
                    for (service in restCurrentIp) {
                        if (service.port == 8080) {
                            selectRestService(service)
                            return true
                        }
                    }
                    for (service in restCurrentIp) {
                        if (service.port != 8080) {
                            selectRestService(service)
                            return true
                        }
                    }
                }
                restService == null -> {
                    selectRestService(stbService)
                    changed = true
                }
            }
        } else {
            when {
                ipFound -> {
                    for (service in restCurrentIp) {
                        if (service.port == 8080) {
                            selectRestService(service)
                            return true
                        }
                    }
                    for (service in restCurrentIp) {
                        if (service.port != 8080) {
                            selectRestService(service)
                            return true
                        }
                    }
                }
                restService == null -> {
                    selectRestService(stbService)
                    changed = true
                }
            }
        }
        return changed
    }

    private fun chooseWsService(stbService: StbService): Boolean {
        val currentIp = NetworkUtils.getIPAddress(true)
        var ipFound = false
        var wsCurrentIp: StbService? = null
        var changed = false
        for (service in wsServiceList) {
            if (service.ip == currentIp) {
                ipFound = true
                wsCurrentIp = service
            }
        }
        if (preferredIp != null) {
            var ipPreferred = false
            var wsIpPreferred: StbService? = null
            for (service in wsServiceList) {
                if (service.ip == preferredIp) {
                    ipPreferred = true
                    wsIpPreferred = service
                }
            }
            when {
                ipPreferred -> {
                    selectWsService(wsIpPreferred!!)
                    changed = true
                }
                ipFound -> {
                    selectWsService(wsCurrentIp!!)
                    changed = true
                }
                wsService == null -> {
                    selectWsService(stbService)
                    changed = true
                }
            }
        } else {
            when {
                ipFound -> {
                    selectWsService(wsCurrentIp!!)
                    changed = true
                }
                wsService == null -> {
                    selectWsService(stbService)
                    changed = true
                }
            }
        }
        return changed
    }

    /**
     * discover Bbox API Rest service.
     */
    fun startWebsocketDiscovery(findOneAndExit: Boolean = false, platform: Platform, maxDuration: Int = 0, handler: (StbServiceEvent, StbService?, Boolean, Throwable?) -> Unit): Boolean {
        if (!discoveringWs) {
            discoveringWs = true
            val rxBonjour = RxBonjour.Builder()
                    .platform(platform)
                    .driver(JmDNSDriver.create())
                    .create()

            //empty service list before discovery
            wsServiceList.clear()

            val obs = rxBonjour.newDiscovery(type = BBOXAPI_WS_SERVICE_TYPE)
            serviceWsDiscovery = obs.subscribe(
                    { event ->
                        if (event.service.name.startsWith(BBOXAPI_WS_SERVICE_NAME)) {
                            if (findOneAndExit) {
                                stopDiscoveryWs()
                            }
                            val stbService = StbService(event.service.host.hostAddress, event.service.port)
                            wsServiceList.add(stbService)
                            val changed = chooseWsService(stbService)

                            handler(StbServiceEvent.SERVICE_FOUND, stbService, changed, null)
                        }
                    },
                    { error ->
                        discoveringWs = false
                        handler(StbServiceEvent.DISCOVERY_ERROR, null, false, error)
                    })
            if (maxDuration > 0) {
                discoveryWsTimer = Timer()
                discoveryWsTimer?.schedule(delay = maxDuration.toLong()) {
                    stopDiscoveryWs()
                    handler(StbServiceEvent.DISCOVERY_STOPPED, null, false, null)
                    discoveryWsTimer?.cancel()
                }
            }
            return true
        }
        return false
    }

    fun subscribeNotification(appName: String, resourceList: List<Resource>, listener: IWebsocketListener): NotificationChannel? {
        closeWebsocket()
        if (restService == null) {
            return null
        }
        var port = 9090
        var ip = restService?.ip!!
        if (restService != null && restService!!.port != 8080 && wsService == null) {
            return null
        } else if (restService != null && restService!!.port != 8080) {
            port = wsService?.port!!
            ip = wsService?.ip!!
        }
        val registerRes = registerAppSync(appName)
        if (registerRes.third is Result.Failure) {
            return NotificationChannel(null, null, registerRes)
        }

        val location = registerRes.second.headers["Location"]?.get(0) ?: ""
        val appId = location.substring(location.lastIndexOf('/') + 1)

        val subscribeRes = processSessionIdSync<ByteArray>(request = buildSubscribeNotifRequest(appId, resourceList), json = false)
        if (subscribeRes.third is Result.Failure) {
            return NotificationChannel(null, null, subscribeRes)
        }
        val locationSubscribe = subscribeRes.second.headers["Location"]?.get(0) ?: ""
        val channelId = locationSubscribe.substring(locationSubscribe.lastIndexOf('/') + 1)

        openWebsocket(ip, port, object : okhttp3.WebSocketListener() {
            override fun onOpen(webSocket: WebSocket?, response: okhttp3.Response?) {
                webSocket?.send(appId)
                listener.onOpen()
            }

            override fun onClosed(webSocket: WebSocket?, code: Int, reason: String?) {
                listener.onClose()
            }

            override fun onMessage(webSocket: WebSocket?, text: String?) {
                if (text != null) {
                    try {
                        val data = JsonParser().parse(text).asJsonObject
                        when {
                            data.has("error") -> listener.onError(Gson().fromJson(text, BboxApiError::class.java))
                            data.has("resourceId") -> {
                                when {
                                    data.get("resourceId").asString == "Media" -> listener.onMedia(Gson().fromJson(data.get("body").asString, MediaEvent::class.java))
                                    data.get("resourceId").asString == "Application" -> listener.onApp(Gson().fromJson(data.getAsJsonObject("body"), AppEvent::class.java))
                                    data.get("resourceId").asString == "Message" -> listener.onMessage(Gson().fromJson(data.getAsJsonObject("body"), MessageEvent::class.java))
                                    else -> listener.onError(BboxApiError("can't parse event : $text"))
                                }
                            }
                            else -> listener.onError(BboxApiError("can't parse event : $text"))
                        }
                    } catch (e: JsonSyntaxException) {
                        listener.onError(BboxApiError("can't parse event : $text"))
                    } catch (e: ClassCastException) {
                        listener.onError(BboxApiError("can't parse event : $text"))
                    } catch (e: UnsupportedOperationException) {
                        listener.onError(BboxApiError("can't parse event : $text"))
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket?, t: Throwable?, response: okhttp3.Response?) {
                listener.onFailure(t)
            }
        })
        return NotificationChannel(appId, channelId, registerRes)
    }

    fun openWebsocket(ip: String, port: Int, listener: okhttp3.WebSocketListener) {
        httpClient = OkHttpClient()
        websocket = httpClient.newWebSocket(okhttp3.Request.Builder().url("ws://$ip:$port").build(), listener)
    }

    fun closeWebsocket() {
        websocket?.close(1000, null)
        httpClient.dispatcher().executorService().shutdown()
        httpClient.connectionPool().evictAll()
    }

    fun stopDiscoveryRest() {
        discoveringRest = false
        if (serviceRestDiscovery?.isDisposed == false) {
            serviceRestDiscovery?.dispose()
            discoveryRestTimer?.cancel()
        }
    }

    fun stopDiscoveryWs() {
        discoveringWs = false
        if (serviceWsDiscovery?.isDisposed == false) {
            serviceWsDiscovery?.dispose()
            discoveryWsTimer?.cancel()
        }
    }

    private fun getTokenAndExecute(handler: Handler<String>) {
        buildTokenRequest().responseString { req, res, result ->
            result.fold({ _ ->
                token = res.headers["x-token"]?.get(0) ?: ""
                tokenValidity = res.headers["x-token-validity"]?.get(0)?.toLong() ?: Date().time
                //now call session id request
                buildSessionIdRequest(token).responseString { req2, res2, result2 ->
                    result2.fold({ _ ->
                        handler.success(req2, res2, result2.get())
                    }, { err2 ->
                        handler.failure(req2, res2, err2)
                    })
                }
            }, { err ->
                handler.failure(req, res, err)
            })
        }
    }

    private fun getTokenAndExecuteSync(): Triple<Request, Response, Result<String, FuelError>> {
        val triple = buildTokenRequest().responseString()
        if (triple.third is Result.Success) {
            token = triple.second.headers["x-token"]?.get(0) ?: ""
            tokenValidity = triple.second.headers["x-token-validity"]?.get(0)?.toLong() ?: Date().time
            return buildSessionIdRequest(token).responseString()
        }
        return triple
    }

    private fun processToken(handler: Handler<String>) {
        if (tokenValidity < Date().time) {
            getTokenAndExecute(handler = handler)
        } else {
            buildSessionIdRequest(token).responseString { req, res, result ->
                if (res.statusCode == 401) {
                    getTokenAndExecute(handler = handler)
                } else {
                    handler.success(req, res, result.get())
                }
            }
        }
    }

    private fun processTokenSync(): Triple<Request, Response, Result<String, FuelError>> {
        if (tokenValidity < Date().time) {
            return getTokenAndExecuteSync()
        } else {
            val triple = buildSessionIdRequest(token).responseString()
            if (triple.second.statusCode == 401) {
                return getTokenAndExecuteSync()
            } else {
                return triple
            }
        }
    }

    private inline fun <reified T : Any> getSessionIdAndExecute(request: Request, noinline handler: (Request, Response, Result<T, FuelError>) -> Unit, json: Boolean = true) {
        val tokenHandler: Handler<String> = object : Handler<String> {
            override fun failure(req: Request, res: Response, error: FuelError) {
                handler(req, res, Result.error(Exception("failure")).flatMapError {
                    Result.error(FuelError(error.exception))
                })
            }

            override fun success(req: Request, res: Response, value: String) {
                sessionId = res.headers["x-sessionid"]?.get(0) ?: ""
                val cal = Calendar.getInstance()
                cal.add(Calendar.HOUR_OF_DAY, 1)
                sessionIdValidity = cal.time.time
                if (json) {
                    request.header(mapOf("x-sessionid" to sessionId)).responseObject(deserializer = gsonDeserializerOf(), handler = handler)
                } else {
                    request.header(mapOf("x-sessionid" to sessionId)).response(handler = handler as (Request, Response, Result<*, FuelError>) -> Unit)
                }
            }
        }
        processToken(handler = tokenHandler)
    }

    private inline fun <reified T : Any> getSessionIdAndExecute(request: Request, handler: Handler<T>, json: Boolean = true) {
        val tokenHandler: Handler<String> = object : Handler<String> {
            override fun failure(req: Request, res: Response, error: FuelError) {
                handler.failure(req, res, error)
            }

            override fun success(req: Request, res: Response, value: String) {
                sessionId = res.headers["x-sessionid"]?.get(0) ?: ""
                val cal = Calendar.getInstance()
                cal.add(Calendar.HOUR_OF_DAY, 1)
                sessionIdValidity = cal.time.time
                if (json) {
                    request.header(mapOf("x-sessionid" to sessionId)).responseObject(deserializer = gsonDeserializerOf(), handler = handler)
                } else {
                    request.header(mapOf("x-sessionid" to sessionId)).response(handler = handler as Handler<ByteArray>)
                }
            }
        }
        processToken(handler = tokenHandler)
    }

    private inline fun <reified T : Any> processSessionId(request: Request, noinline handler: (Request, Response, Result<T, FuelError>) -> Unit, json: Boolean = true) {
        if (sessionIdValidity < Date().time) {
            getSessionIdAndExecute(request = request, handler = handler, json = json)
        } else {
            if (json) {
                request.header(mapOf("x-sessionid" to sessionId)).responseObject<T>(deserializer = gsonDeserializerOf()) { req, res, result ->
                    if (res.statusCode == 401) {
                        getSessionIdAndExecute(request = request, handler = handler, json = json)
                    } else {
                        handler(req, res, result)
                    }
                }
            } else {
                request.header(mapOf("x-sessionid" to sessionId)).response { req, res, result ->
                    if (res.statusCode == 401) {
                        getSessionIdAndExecute(request = request, handler = handler, json = json)
                    } else {
                        handler(req, res, result as Result<T, FuelError>)
                    }
                }
            }
        }
    }

    private inline fun <reified T : Any> processSessionId(request: Request, handler: Handler<T>, json: Boolean = true) {
        if (sessionIdValidity < Date().time) {
            getSessionIdAndExecute(request = request, handler = handler, json = json)
        } else {
            if (json) {
                request.header(pairs = *arrayOf("x-sessionid" to sessionId)).responseObject<T>(deserializer = gsonDeserializerOf()) { req, res, result ->
                    if (res.statusCode == 401) {
                        getSessionIdAndExecute(request = request, handler = handler, json = json)
                    } else {
                        handler.success(req, res, result.get())
                    }
                }
            } else {
                request.header(pairs = *arrayOf("x-sessionid" to sessionId)).response { req, res, result ->
                    if (res.statusCode == 401) {
                        getSessionIdAndExecute(request = request, handler = handler, json = json)
                    } else {
                        handler.success(req, res, result.get() as T)
                    }
                }
            }
        }
    }

    private inline fun <reified T : Any> processSessionIdSync(request: Request, json: Boolean = true): Triple<Request, Response, Result<T, FuelError>> {
        if (sessionIdValidity < Date().time) {
            return getSessionIdAndExecuteSync(request = request, json = json)
        } else {
            if (json) {
                val triple = request.header(mapOf("x-sessionid" to sessionId)).responseObject<T>(gsonDeserializerOf())
                return if (triple.second.statusCode == 401) {
                    getSessionIdAndExecuteSync(request = request, json = json)
                } else {
                    triple
                }
            } else {
                val triple = request.header(mapOf("x-sessionid" to sessionId)).response()
                if (triple.second.statusCode == 401) {
                    return getSessionIdAndExecuteSync(request = request, json = json)
                } else {
                    return triple as Triple<Request, Response, Result<T, FuelError>>
                }
            }
        }
    }

    private inline fun <reified T : Any> getSessionIdAndExecuteSync(request: Request, json: Boolean = true): Triple<Request, Response, Result<T, FuelError>> {
        val triple = processTokenSync()

        if (triple.third is Result.Success) {
            sessionId = triple.second.headers["x-sessionid"]?.get(0) ?: ""
            val cal = Calendar.getInstance()
            cal.add(Calendar.HOUR_OF_DAY, 1)
            sessionIdValidity = cal.time.time
            return if (json) {
                request.header(mapOf("x-sessionid" to sessionId)).responseObject(gsonDeserializerOf())
            } else {
                request.header(mapOf("x-sessionid" to sessionId)).response() as Triple<Request, Response, Result<T, FuelError>>

            }
        }
        return triple as Triple<Request, Response, Result<T, FuelError>>
    }

    fun getChannels(handler: (Request, Response, Result<List<Channel>, FuelError>) -> Unit) {
        processSessionId(request = buildChannelListRequest(), handler = handler)
    }

    fun getChannels(handler: Handler<List<Channel>>) {
        processSessionId(request = buildChannelListRequest(), handler = handler)
    }

    fun getChannelsSync(): Triple<Request, Response, Result<List<Channel>, FuelError>> {
        return processSessionIdSync(request = buildChannelListRequest())
    }

    fun getApps(handler: (Request, Response, Result<List<Application>, FuelError>) -> Unit) {
        processSessionId(request = buildAppsRequest(), handler = handler)
    }

    fun getApps(handler: Handler<List<Application>>) {
        processSessionId(request = buildAppsRequest(), handler = handler)
    }

    fun getAppsSync(): Triple<Request, Response, Result<List<Application>, FuelError>> {
        return processSessionIdSync(request = buildAppsRequest())
    }

    fun getAppInfo(packageName: String, handler: (Request, Response, Result<List<Application>, FuelError>) -> Unit) {
        processSessionId(request = buildAppInfoRequest(packageName), handler = handler)
    }

    fun getAppInfo(packageName: String, handler: Handler<List<Application>>) {
        processSessionId(request = buildAppInfoRequest(packageName), handler = handler)
    }

    fun getAppInfoSync(packageName: String): Triple<Request, Response, Result<List<Application>, FuelError>> {
        return processSessionIdSync(request = buildAppInfoRequest(packageName))
    }

    fun getAppIcon(packageName: String, handler: (Request, Response, Result<ByteArray, FuelError>) -> Unit) {
        processSessionId(request = buildAppIconRequest(packageName), handler = handler, json = false)
    }

    fun getAppIcon(packageName: String, handler: Handler<ByteArray>) {
        processSessionId(request = buildAppIconRequest(packageName), handler = handler, json = false)
    }

    fun getAppIconSync(packageName: String): Triple<Request, Response, Result<ByteArray, FuelError>> {
        return processSessionIdSync(request = buildAppIconRequest(packageName), json = false)
    }

    fun getCurrentChannel(handler: (Request, Response, Result<Media, FuelError>) -> Unit) {
        processSessionId(request = buildCurrentChannelRequest(), handler = handler)
    }

    fun getCurrentChannel(handler: Handler<Media>) {
        processSessionId(request = buildCurrentChannelRequest(), handler = handler)
    }

    fun getCurrentChannelSync(): Triple<Request, Response, Result<Media, FuelError>> {
        return processSessionIdSync(request = buildCurrentChannelRequest())
    }

    fun getVolume(handler: (Request, Response, Result<Volume, FuelError>) -> Unit) {
        processSessionId(request = buildVolumeRequest(), handler = handler)
    }

    fun getVolume(handler: Handler<Volume>) {
        processSessionId(request = buildVolumeRequest(), handler = handler)
    }

    fun getVolumeSync(): Triple<Request, Response, Result<Volume, FuelError>> {
        return processSessionIdSync(request = buildVolumeRequest())
    }

    fun startApp(packageName: String, handler: (Request, Response, Result<ByteArray, FuelError>) -> Unit) {
        processSessionId(request = buildStartAppRequest(packageName), handler = handler, json = false)
    }

    fun startApp(packageName: String, handler: Handler<ByteArray>) {
        processSessionId(request = buildStartAppRequest(packageName), handler = handler, json = false)
    }

    fun startAppSync(packageName: String): Triple<Request, Response, Result<ByteArray, FuelError>> {
        return processSessionIdSync(request = buildStartAppRequest(packageName), json = false)
    }

    fun displayToast(toast: ToastRequest, handler: (Request, Response, Result<ByteArray, FuelError>) -> Unit) {
        processSessionId(request = buildDisplayToastRequest(toast), handler = handler, json = false)
    }

    fun displayToast(toast: ToastRequest, handler: Handler<ByteArray>) {
        processSessionId(request = buildDisplayToastRequest(toast), handler = handler, json = false)
    }

    fun displayToastSync(toast: ToastRequest): Triple<Request, Response, Result<ByteArray, FuelError>> {
        return processSessionIdSync(request = buildDisplayToastRequest(toast), json = false)
    }

    fun setVolume(volume: Int, handler: (Request, Response, Result<ByteArray, FuelError>) -> Unit) {
        processSessionId(request = buildSetVolumeRequest(volume), handler = handler, json = false)
    }

    fun setVolume(volume: Int, handler: Handler<ByteArray>) {
        processSessionId(request = buildSetVolumeRequest(volume), handler = handler, json = false)
    }

    fun setVolumeSync(volume: Int): Triple<Request, Response, Result<ByteArray, FuelError>> {
        return processSessionIdSync(request = buildSetVolumeRequest(volume), json = false)
    }

    fun registerApp(appName: String, handler: (Request, Response, Result<ByteArray, FuelError>) -> Unit) {
        processSessionId(request = buildRegisterAppRequest(appName), handler = handler, json = false)
    }

    fun registerApp(appName: String, handler: Handler<ByteArray>) {
        processSessionId(request = buildRegisterAppRequest(appName), handler = handler, json = false)
    }

    fun registerAppSync(appName: String): Triple<Request, Response, Result<ByteArray, FuelError>> {
        return processSessionIdSync(request = buildRegisterAppRequest(appName), json = false)
    }

    fun unsubscribe(channelId: String, handler: (Request, Response, Result<ByteArray, FuelError>) -> Unit) {
        return processSessionId(request = buildUnsubscribeNotifRequest(channelId), handler = handler, json = false)
    }

    fun unsubscribe(channelId: String, handler: Handler<ByteArray>) {
        return processSessionId(request = buildUnsubscribeNotifRequest(channelId), handler = handler, json = false)
    }

    fun unsubscribeSync(channelId: String): Triple<Request, Response, Result<ByteArray, FuelError>> {
        return processSessionIdSync(request = buildUnsubscribeNotifRequest(channelId), json = false)
    }

    fun getOpenedChannels(handler: (Request, Response, Result<List<String>, FuelError>) -> Unit) {
        return processSessionId(request = buildGetOpenedChannelRequest(), handler = handler)
    }

    fun getOpenedChannels(handler: Handler<List<String>>) {
        return processSessionId(request = buildGetOpenedChannelRequest(), handler = handler)
    }

    fun getOpenedChannelsSync(): Triple<Request, Response, Result<List<String>, FuelError>> {
        return processSessionIdSync(request = buildGetOpenedChannelRequest())
    }

    fun unsubscribeAllSync(): Triple<Request, Response, Result<List<String>, FuelError>> {
        val (_, _, channelsRes) = getOpenedChannelsSync()
        when (channelsRes) {
            is Result.Failure -> {
                throw channelsRes.getException().exception
            }
            is Result.Success -> {
                for (it in channelsRes.get().distinct()) {
                    unsubscribeSync(channelId = it)
                }
            }
        }
        //for some reason it returns duplicates (!?)
        return getOpenedChannelsSync()
    }

    fun sendNotification(channelId: String, appId: String, message: String, handler: (Request, Response, Result<ByteArray, FuelError>) -> Unit) {
        return processSessionId(request = buildPostNotification(channelId, appId, message), handler = handler, json = false)
    }

    fun sendNotification(channelId: String, appId: String, message: String, handler: Handler<ByteArray>) {
        return processSessionId(request = buildPostNotification(channelId, appId, message), handler = handler, json = false)
    }

    fun sendNotificationSync(channelId: String, appId: String, message: String): Triple<Request, Response, Result<ByteArray, FuelError>> {
        return processSessionIdSync(request = buildPostNotification(channelId, appId, message), json = false)
    }

    fun createCustomRequest(request: Request, handler: (Request, Response, Result<ByteArray, FuelError>) -> Unit) {
        return processSessionId(request = request, handler = handler, json = false)
    }

    fun createCustomRequest(request: Request, handler: Handler<ByteArray>) {
        return processSessionId(request = request, handler = handler, json = false)
    }

    fun createCustomRequestSync(request: Request): Triple<Request, Response, Result<ByteArray, FuelError>> {
        return processSessionIdSync(request = request, json = false)
    }
}