package fr.bmartel.bboxapi.stb.sample

import com.github.kittinunf.result.Result
import de.mannodermaus.rxbonjour.platforms.desktop.DesktopPlatform
import fr.bmartel.bboxapi.stb.BboxApiStb
import fr.bmartel.bboxapi.stb.model.StbServiceEvent

fun main(args: Array<String>) {
    val bboxapi = BboxApiStb(appId = "YourAppId", appSecret = "YourAppSecret", platform = DesktopPlatform.create())

    bboxapi.startRestDiscovery(findOneAndExit = true, maxDuration = 10000) { eventType, service, error ->
        when (eventType) {
            StbServiceEvent.SERVICE_FOUND -> {
                bboxapi.getAppIcon(packageName = "com.google.android.youtube.tv") { request, response, result ->
                    when (result) {
                        is Result.Failure -> {
                            val ex = result.getException()
                            ex.printStackTrace()
                            println(request)
                            println(response)
                        }
                        is Result.Success -> {
                            val data = result.get()
                            println(data.size)
                        }
                    }
                }
            }
            StbServiceEvent.DISCOVERY_STOPPED -> println("end of discovery")
            StbServiceEvent.DISCOVERY_ERROR -> error?.printStackTrace()
        }
    }
}