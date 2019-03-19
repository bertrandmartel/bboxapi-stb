package fr.bmartel.bboxapi.stb.sample

import com.github.kittinunf.result.Result
import de.mannodermaus.rxbonjour.platforms.desktop.DesktopPlatform
import fr.bmartel.bboxapi.stb.BboxApiStb
import fr.bmartel.bboxapi.stb.model.StbServiceEvent
import fr.bmartel.bboxapi.stb.model.ToastRequest

fun main(args: Array<String>) {
    val bboxapi = BboxApiStb(appId = "YourAppId", appSecret = "YourAppSecret")

    bboxapi.startRestDiscovery(findOneAndExit = true, maxDuration = 10000, platform = DesktopPlatform.create()) { eventType, service, changed, error ->
        when (eventType) {
            StbServiceEvent.SERVICE_FOUND -> {
                println("service found : ${service?.ip}:${service?.port}")
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
            }
            StbServiceEvent.DISCOVERY_STOPPED -> println("end of discovery")
            StbServiceEvent.DISCOVERY_ERROR -> error?.printStackTrace()
        }
    }
}