package fr.bmartel.bboxapi.stb.sample

import com.github.kittinunf.result.Result
import de.mannodermaus.rxbonjour.platforms.desktop.DesktopPlatform
import fr.bmartel.bboxapi.stb.BboxApiStb
import fr.bmartel.bboxapi.stb.model.StbServiceEvent

fun main(args: Array<String>) {
    val bboxapi = BboxApiStb(appId = "YourAppId", appSecret = "YourAppSecret")

    bboxapi.startRestDiscovery(findOneAndExit = true, maxDuration = 10000, platform = DesktopPlatform.create()) { eventType, _, _, error ->
        when (eventType) {
            StbServiceEvent.SERVICE_FOUND -> {
                val (_, response, result) = bboxapi.setVolumeSync(volume = 100)
                when (result) {
                    is Result.Failure -> {
                        result.getException().printStackTrace()
                    }
                    is Result.Success -> {
                        println(response.statusCode)
                    }
                }
            }
            StbServiceEvent.DISCOVERY_STOPPED -> println("end of discovery")
            StbServiceEvent.DISCOVERY_ERROR -> error?.printStackTrace()
        }
    }
}