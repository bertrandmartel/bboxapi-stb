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
data class NotificationChannel(val appId: String?, val channelId: String?, val subscribeResult: Triple<Request, Response, Result<ByteArray, FuelError>>)
data class NotificationRequest(val appId: String, val message: String)

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

data class MediaEvent(val mediaService: String, val mediaState: String, val mediaTitle: String, val positionId: Int, val epgChannelNumber: Int)
data class AppEvent(val packageName: String, val state: String)
data class MessageEvent(val message: String, val source: String)

data class BboxApiError(val error: String)

enum class Resource {
    Media, Application, Message
}

data class Vod(
        val _id: String,
        val productId: String,
        val externalId: String,
        val title: String,
        val series: String,
        val year: Int,
        val runtime: Int,
        val contentType: String,
        val outBundle: Int,
        val bundles: String,
        val provider: String,
        val actors: String,
        val directors: String,
        val coverUrlPortrait: String,
        val coverUrlLandscape: String,
        val startDate: String,
        val endDate: String,
        val episodeNumber: Int,
        val seasonNumber: Int,
        val definition: Int,
        val languages: String,
        val captionLanguages: String,
        val captionLanguagesImpairedHearing: String,
        val captionLanguageInVideo: String,
        val parentalGuidance: Int,
        val audioCodingName: String,
        val genres: String,
        val publicRating: Float,
        val pressRating: Float,
        val summaryShort: String,
        val summaryLong: String,
        val countryOfOrigin: String,
        val externalTrailerUrl: String,
        val externalTrailerId: String,
        val hasTrailer: Int,
        val secondaryTitle: String,
        val mostPopular: Int,
        val highlight: Int
)

enum class VodMode(val mode: String) {
    SIMPLE("simple")
}

enum class EpgMode(val mode: String) {
    SIMPLE("simple")
}

enum class ParentalGuidance(val guidance: String) {
    MINUS_10("1"),
    MINUS_12("1,2"),
    MINUS_16("1,2,3"),
    MINUS_18("1,2,3,4,5")
}

data class EpgProgram(
        val eventId: String,
        val externalId: String,
        val epgChannelNumber: Int,
        val positionId: Int,
        val title: String,
        val genre: List<String>,
        val summary: String,
        val character: String,
        val startTime: String,
        val endTime: String,
        val duration: String,
        val country: String,
        val date: Int,
        val publicRank: String,
        val parentalGuidance: String,
        val thumb: String
)