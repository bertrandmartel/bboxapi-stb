package fr.bmartel.bboxapi.stb

import com.google.gson.Gson
import fr.bmartel.bboxapi.stb.model.*
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import java.util.*

const val TOKEN = "123456789"
const val SESSION_ID = "1234567890123456789"
const val APP_ID = "100-100"
const val APP_SECRET = "1234567890123456789"

const val APP_TEST = "fr.bmartel.android.app"

class MockDispatcher : Dispatcher() {

    override fun dispatch(request: RecordedRequest): MockResponse {
        return when {
            request.method == "POST" && request.path == "/v1.3/security/token" -> sendToken(request = request)
            request.method == "POST" && request.path == "/api.bbox.lan/v0/security/sessionId" -> sendSessionId(request = request)
            request.method == "POST" && request.path == "/api.bbox.lan/v0/applications/$APP_TEST" -> sendOk(request = request)
            request.method == "POST" && request.path == "/api.bbox.lan/v0/userinterface/toast" -> sendToast(request = request)
            request.method == "POST" && request.path == "/api.bbox.lan/v0/userinterface/volume" -> sendVolume(request = request)
            request.method == "POST" && request.path == "/api.bbox.lan/v0/applications/register" -> sendRegisterResp(request = request)
            request.method == "GET" && request.path == "/api.bbox.lan/v0/media/tvchannellist" -> sendResponse(request = request, fileName = "channels.json")
            request.method == "GET" && request.path == "/api.bbox.lan/v0/applications" -> sendResponse(request = request, fileName = "apps.json")
            request.method == "GET" && request.path == "/api.bbox.lan/v0/applications/$APP_TEST" -> sendResponse(request = request, fileName = "app_info.json")
            request.method == "GET" && request.path == "/api.bbox.lan/v0/applications/$APP_TEST/image" -> sendResponse(request = request, fileName = "ic_launcher.png", text = false)
            request.method == "GET" && request.path == "/api.bbox.lan/v0/media" -> sendResponse(request = request, fileName = "current_channel.json")
            request.method == "GET" && request.path == "/api.bbox.lan/v0/userinterface/volume" -> sendResponse(request = request, fileName = "volume.json")
            else -> MockResponse().setResponseCode(404)
        }
    }

    private fun sendSessionId(request: RecordedRequest): MockResponse {
        val session = Gson().fromJson(request.body.readUtf8(), SessionIdRequest::class.java)
        if (session.token == TOKEN) {
            return MockResponse()
                    .setHeader("x-sessionid", SESSION_ID)
                    .setResponseCode(200)
        }
        return MockResponse().setResponseCode(401)
    }

    private fun sendToken(request: RecordedRequest): MockResponse {
        val tokenRequest = Gson().fromJson(request.body.readUtf8(), TokenRequest::class.java)
        if (tokenRequest.appId == APP_ID && tokenRequest.appSecret == APP_SECRET) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.HOUR_OF_DAY, 1)
            return MockResponse()
                    .setHeader("x-token", TOKEN)
                    .setHeader("x-token-validity", cal.time.time)
                    .setResponseCode(200)
        }
        return MockResponse().setResponseCode(401)
    }

    private fun sendResponse(request: RecordedRequest, fileName: String, text: Boolean = true): MockResponse {
        if (request.getHeader("x-sessionid") == SESSION_ID) {
            return if (text)
                MockResponse().setResponseCode(200).setBody(TestUtils.getResTextFile(fileName = fileName))
            else {
                val buffer = Buffer()
                buffer.write(TestUtils.getResBinaryFile(fileName = fileName))
                MockResponse().setResponseCode(200).setBody(buffer)
            }
        }
        return MockResponse().setResponseCode(401)
    }

    private fun sendOk(request: RecordedRequest): MockResponse {
        if (request.getHeader("x-sessionid") == SESSION_ID) {
            return MockResponse().setResponseCode(200)
        }
        return MockResponse().setResponseCode(401)
    }

    private fun sendToast(request: RecordedRequest): MockResponse {
        if (request.getHeader("x-sessionid") == SESSION_ID &&
                request.getHeader("Content-Type") == "application/json") {
            val toast = Gson().fromJson(request.body.readUtf8(), ToastRequest::class.java)
            if (toast.message != "") {
                return MockResponse().setResponseCode(200)
            }
        }
        return MockResponse().setResponseCode(401)
    }

    private fun sendVolume(request: RecordedRequest): MockResponse {
        if (request.getHeader("x-sessionid") == SESSION_ID &&
                request.getHeader("Content-Type") == "application/json") {
            val volume = Gson().fromJson(request.body.readUtf8(), VolumeRequest::class.java)
            if (volume.volume > 0) {
                return MockResponse().setResponseCode(200)
            }
        }
        return MockResponse().setResponseCode(401)
    }

    private fun sendRegisterResp(request: RecordedRequest): MockResponse {
        if (request.getHeader("x-sessionid") == SESSION_ID &&
                request.getHeader("Content-Type") == "application/json") {
            val register = Gson().fromJson(request.body.readUtf8(), RegisterRequest::class.java)
            if (register.appName != "") {
                return MockResponse().setResponseCode(200)
            }
        }
        return MockResponse().setResponseCode(401)
    }
}