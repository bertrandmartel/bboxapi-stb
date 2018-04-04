package fr.bmartel.bboxapi.stb.model

data class StbService(val ip: String, val port: Int)
enum class StbServiceEvent { SERVICE_FOUND, DISCOVERY_ERROR, DISCOVERY_STOPPED }

data class TokenRequest(val appId: String, val appSecret: String)
data class SessionIdRequest(val token: String)
data class ToastRequest(val message: String, val color: String? = null, val pos_x: Int? = null, val pos_y: Int? = null)
data class VolumeRequest(val volume: Int)
data class RegisterRequest(val appName: String)

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