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
                bboxapi.getAppInfo(packageName = "com.google.android.youtube.tv") { _, _, result ->
                    when (result) {
                        is Result.Failure -> {
                            val ex = result.getException()
                            ex.printStackTrace()
                        }
                        is Result.Success -> {
                            val data = result.get()
                            println(data)
                        }
                    }
                }

                val (_, _, result) = bboxapi.getAppInfoSync(packageName = "com.google.android.youtube.tv")
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