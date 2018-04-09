package fr.bmartel.bboxapi.stb.sample

import com.github.kittinunf.result.Result
import de.mannodermaus.rxbonjour.platforms.desktop.DesktopPlatform
import fr.bmartel.bboxapi.stb.BboxApiStb
import fr.bmartel.bboxapi.stb.model.StbServiceEvent

fun main(args: Array<String>) {
    val bboxapi = BboxApiStb(appId = "YourAppId", appSecret = "YourAppSecret")

    bboxapi.startRestDiscovery(findOneAndExit = true, maxDuration = 10000, platform = DesktopPlatform.create()) { eventType, service, error ->
        when (eventType) {
            StbServiceEvent.SERVICE_FOUND -> {
                bboxapi.getCurrentChannel { _, _, result ->
                    when (result) {
                        is Result.Failure -> {
                            result.getException().printStackTrace()
                        }
                        is Result.Success -> {
                            println(result.get())
                        }
                    }
                }

                val (_, _, result) = bboxapi.getCurrentChannelSync()
                when (result) {
                    is Result.Failure -> {
                        result.getException().printStackTrace()
                    }
                    is Result.Success -> {
                        println(result.get())
                    }
                }
            }
            StbServiceEvent.DISCOVERY_STOPPED -> println("end of discovery")
            StbServiceEvent.DISCOVERY_ERROR -> error?.printStackTrace()
        }
    }
}