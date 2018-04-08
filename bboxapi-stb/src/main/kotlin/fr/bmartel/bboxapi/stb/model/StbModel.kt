package fr.bmartel.bboxapi.stb.model

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result

data class StbService(val ip: String, val port: Int)
enum class StbServiceEvent { SERVICE_FOUND, DISCOVERY_ERROR, DISCOVERY_STOPPED }

data class TokenRequest(val appId: String, val appSecret: String)
data class SessionIdRequest(val token: String)
data class ToastRequest(val message: String, val color: String? = null, val pos_x: Int? = null, val pos_y: Int? = null)
data class VolumeRequest(val volume: Int)
data class RegisterRequest(val appName: String)
data class ResourceItem(val resourceId: String)
data class SubscribeRequest(val appId: String, val resources: List<ResourceItem>)
data class NotificationChannel(val channelId: String?, val subscribeResult: Triple<Request, Response, Result<ByteArray, FuelError>>)
//data class NotificationRequest(val appId: String, val message: String)

data class Channel(val mediaState: String, val mediaTitle: String, val positionId: String)
data class Application(
        val appId: String,
        val appName: String,
        val appState: String,
        val component: String,
        val data: String,
        val leanback: Boolean,
        val logoUrl: String,
        val packageName: String)

data class Media(val mediaService: String, val mediaState: String, val mediaTitle: String, val positionId: String)
data class Volume(val volume: String)


enum class Resource {
    Media, Application, Message,test
}