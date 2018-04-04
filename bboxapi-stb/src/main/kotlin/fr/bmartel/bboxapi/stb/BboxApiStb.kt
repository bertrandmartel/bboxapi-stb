package fr.bmartel.bboxapi.stb

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.gson.gsonDeserializerOf
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMapError
import com.google.gson.Gson
import de.mannodermaus.rxbonjour.Platform
import de.mannodermaus.rxbonjour.RxBonjour
import de.mannodermaus.rxbonjour.drivers.jmdns.JmDNSDriver
import fr.bmartel.bboxapi.stb.model.*
import io.reactivex.disposables.Disposable
import java.util.*
import kotlin.concurrent.schedule

const val BBOXAPI_SERVICE_TYPE = "_http._tcp"
const val BBOXAPI_SERVICE_NAME = "Bboxapi"

class BboxApiStb(val appId: String, val appSecret: String, val platform: Platform) {

    var hasSessionId = false

    var cloudHost: String = "http://api.bbox.fr"

    var tokenValidity: Long = Date().time
    var token: String = ""

    var sessionIdValidity: Long = Date().time
    var sessionId: String = ""

    var serviceDiscovery: Disposable? = null

    var discoveryTimer: Timer? = null

    private fun buildTokenRequest(): Request {
        return Fuel.post("$cloudHost/v1.3/security/token")
                .header(mapOf("Content-Type" to "application/json"))
                .body(Gson().toJson(TokenRequest(appId = appId, appSecret = appSecret)))
    }

    private fun buildSessionIdRequest(): Request {
        return Fuel.post("/security/sessionId")
                .header(mapOf("Content-Type" to "application/json"))
                .body(Gson().toJson(SessionIdRequest(token = token)))
    }

    private fun buildChannelListRequest(): Request {
        return Fuel.get("/media/tvchannellist")
    }

    private fun buildAppsRequest(): Request {
        return Fuel.get("/applications")
    }

    private fun buildAppInfoRequest(packageName: String): Request {
        return Fuel.get("/applications/$packageName")
    }

    private fun buildAppIconRequest(packageName: String): Request {
        return Fuel.get("/applications/$packageName/image")
    }

    private fun buildCurrentChannelRequest(): Request {
        return Fuel.get("/media")
    }

    private fun buildVolumeRequest(): Request {
        return Fuel.get("/userinterface/volume")
    }

    private fun buildStartAppRequest(packageName: String): Request {
        return Fuel.post("/applications/$packageName")
    }

    private fun buildDisplayToastRequest(toast: ToastRequest): Request {
        return Fuel.post("/userinterface/toast")
                .header(mapOf("Content-Type" to "application/json"))
                .body(Gson().toJson(toast))
    }

    private fun buildSetVolumeRequest(volume: Int): Request {
        return Fuel.post("/userinterface/volume")
                .header(mapOf("Content-Type" to "application/json"))
                .body(Gson().toJson(VolumeRequest(volume)))
    }

    private fun buildRegisterAppRequest(appName: String): Request {
        return Fuel.post("/applications/register")
                .header(mapOf("Content-Type" to "application/json"))
                .body(Gson().toJson(RegisterRequest(appName)))
    }

    /**
     * discover Bbox API services.
     */
    fun startDiscovery(findOneAndExit: Boolean = false, maxDuration: Int = 0, handler: (StbServiceEvent, StbService?, Throwable?) -> Unit) {
        val rxBonjour = RxBonjour.Builder()
                .platform(platform)
                .driver(JmDNSDriver.create())
                .create()

        val obs = rxBonjour.newDiscovery(type = BBOXAPI_SERVICE_TYPE)
        serviceDiscovery = obs.subscribe(
                { event ->
                    if (event.service.name == BBOXAPI_SERVICE_NAME) {
                        if (findOneAndExit) {
                            stopDiscovery()
                        }
                        setBoxHost("http://${event.service.host.hostAddress}:${event.service.port}")
                        handler(StbServiceEvent.SERVICE_FOUND, StbService(event.service.host.hostAddress, event.service.port), null)
                    }
                },
                { error ->
                    handler(StbServiceEvent.DISCOVERY_ERROR, null, error)
                })
        if (maxDuration > 0) {
            discoveryTimer = Timer()
            discoveryTimer?.schedule(delay = maxDuration.toLong()) {
                stopDiscovery()
                handler(StbServiceEvent.DISCOVERY_STOPPED, null, null)
                discoveryTimer?.cancel()
            }
        }
    }

    fun stopDiscovery() {
        if (serviceDiscovery?.isDisposed == false) {
            serviceDiscovery?.dispose()
            discoveryTimer?.cancel()
        }
    }

    fun setBoxHost(host: String) {
        FuelManager.instance.basePath = "$host/api.bbox.lan/v0"
    }

    private fun getTokenAndExecute(request: Request, handler: Handler<String>) {
        buildTokenRequest().responseString { req, res, result ->
            result.fold({ d ->
                token = res.headers["x-token"]?.get(0) ?: ""
                tokenValidity = res.headers["x-token-validity"]?.get(0)?.toLong() ?: Date().time
                //now call session id request
                request.body(Gson().toJson(SessionIdRequest(token = token))).responseString { req2, res2, result2 ->
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

    private fun getTokenAndExecuteSync(request: Request): Triple<Request, Response, Result<String, FuelError>> {
        val triple = buildTokenRequest().responseString()
        if (triple.third is Result.Success) {
            token = triple.second.headers["x-token"]?.get(0) ?: ""
            tokenValidity = triple.second.headers["x-token-validity"]?.get(0)?.toLong() ?: Date().time
            return request.body(Gson().toJson(SessionIdRequest(token = token))).responseString()
        }
        return triple
    }

    private fun processToken(request: Request, handler: Handler<String>) {
        if (tokenValidity < Date().time) {
            getTokenAndExecute(request = request, handler = handler)
        } else {
            request.body(Gson().toJson(SessionIdRequest(token = token))).responseString { req, res, result ->
                if (res.statusCode == 401) {
                    getTokenAndExecute(request = request, handler = handler)
                } else {
                    handler.success(req, res, result.get())
                }
            }
        }
    }

    private fun processTokenSync(request: Request): Triple<Request, Response, Result<String, FuelError>> {
        if (tokenValidity < Date().time) {
            return getTokenAndExecuteSync(request = request)
        } else {
            val triple = request.body(Gson().toJson(SessionIdRequest(token = token))).responseString()
            if (triple.second.statusCode == 401) {
                return getTokenAndExecuteSync(request = request)
            } else {
                return triple
            }
        }
    }

    private inline fun <reified T : Any> getSessionIdAndExecute(request: Request, noinline handler: (Request, Response, Result<T, FuelError>) -> Unit, json: Boolean = true) {
        val tokenRequest = buildSessionIdRequest()

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
                    request.header(pairs = *arrayOf("x-sessionid" to sessionId)).responseObject(deserializer = gsonDeserializerOf(), handler = handler)
                } else {
                    request.header(pairs = *arrayOf("x-sessionid" to sessionId)).response(handler = handler as (Request, Response, Result<*, FuelError>) -> Unit)
                }
            }
        }
        processToken(request = tokenRequest, handler = tokenHandler)
    }

    private inline fun <reified T : Any> getSessionIdAndExecute(request: Request, handler: Handler<T>, json: Boolean = true) {
        val tokenRequest = buildSessionIdRequest()

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
                    request.header(pairs = *arrayOf("x-sessionid" to sessionId)).responseObject(deserializer = gsonDeserializerOf(), handler = handler)
                } else {
                    request.header(pairs = *arrayOf("x-sessionid" to sessionId)).response(handler = handler as Handler<ByteArray>)
                }
            }
        }
        processToken(request = tokenRequest, handler = tokenHandler)
    }

    private inline fun <reified T : Any> processSessionId(request: Request, noinline handler: (Request, Response, Result<T, FuelError>) -> Unit, json: Boolean = true) {
        if (sessionIdValidity < Date().time) {
            getSessionIdAndExecute(request = request, handler = handler, json = json)
        } else {
            if (json) {
                request.header(pairs = *arrayOf("x-sessionid" to sessionId)).responseObject<T>(deserializer = gsonDeserializerOf()) { req, res, result ->
                    if (res.statusCode == 401) {
                        getSessionIdAndExecute(request = request, handler = handler, json = json)
                    } else {
                        handler(req, res, result)
                    }
                }
            } else {
                request.header(pairs = *arrayOf("x-sessionid" to sessionId)).response { req, res, result ->
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
                val triple = request.header(pairs = *arrayOf("x-sessionid" to sessionId)).responseObject<T>(gsonDeserializerOf())
                return if (triple.second.statusCode == 401) {
                    getSessionIdAndExecuteSync(request = request, json = json)
                } else {
                    triple
                }
            } else {
                val triple = request.header(pairs = *arrayOf("x-sessionid" to sessionId)).response()
                if (triple.second.statusCode == 401) {
                    return getSessionIdAndExecuteSync(request = request, json = json)
                } else {
                    return triple as Triple<Request, Response, Result<T, FuelError>>
                }
            }
        }
    }

    private inline fun <reified T : Any> getSessionIdAndExecuteSync(request: Request, json: Boolean = true): Triple<Request, Response, Result<T, FuelError>> {
        val triple = processTokenSync(request = buildSessionIdRequest())

        if (triple.third is Result.Success) {
            sessionId = triple.second.headers["x-sessionid"]?.get(0) ?: ""
            val cal = Calendar.getInstance()
            cal.add(Calendar.HOUR_OF_DAY, 1)
            sessionIdValidity = cal.time.time
            return if (json) {
                request.header(pairs = *arrayOf("x-sessionid" to sessionId)).responseObject(gsonDeserializerOf())
            } else {
                request.header(pairs = *arrayOf("x-sessionid" to sessionId)).response() as Triple<Request, Response, Result<T, FuelError>>
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

    fun startApp(packageName: String, handler: (Request, Response, Result<String, FuelError>) -> Unit) {
        processSessionId(request = buildStartAppRequest(packageName), handler = handler, json = false)
    }

    fun startApp(packageName: String, handler: Handler<String>) {
        processSessionId(request = buildStartAppRequest(packageName), handler = handler, json = false)
    }

    fun startAppSync(packageName: String): Triple<Request, Response, Result<String, FuelError>> {
        return processSessionIdSync(request = buildStartAppRequest(packageName), json = false)
    }

    fun displayToast(toast: ToastRequest, handler: (Request, Response, Result<String, FuelError>) -> Unit) {
        processSessionId(request = buildDisplayToastRequest(toast), handler = handler, json = false)
    }

    fun displayToast(toast: ToastRequest, handler: Handler<String>) {
        processSessionId(request = buildDisplayToastRequest(toast), handler = handler, json = false)
    }

    fun displayToastSync(toast: ToastRequest): Triple<Request, Response, Result<String, FuelError>> {
        return processSessionIdSync(request = buildDisplayToastRequest(toast), json = false)
    }

    fun setVolume(volume: Int, handler: (Request, Response, Result<String, FuelError>) -> Unit) {
        processSessionId(request = buildSetVolumeRequest(volume), handler = handler, json = false)
    }

    fun setVolume(volume: Int, handler: Handler<String>) {
        processSessionId(request = buildSetVolumeRequest(volume), handler = handler, json = false)
    }

    fun setVolumeSync(volume: Int): Triple<Request, Response, Result<String, FuelError>> {
        return processSessionIdSync(request = buildSetVolumeRequest(volume), json = false)
    }

    fun registerApp(appName: String, handler: (Request, Response, Result<String, FuelError>) -> Unit) {
        processSessionId(request = buildRegisterAppRequest(appName), handler = handler, json = false)
    }

    fun registerApp(appName: String, handler: Handler<String>) {
        processSessionId(request = buildRegisterAppRequest(appName), handler = handler, json = false)
    }

    fun registerAppSync(appName: String): Triple<Request, Response, Result<String, FuelError>> {
        return processSessionIdSync(request = buildRegisterAppRequest(appName), json = false)
    }
}