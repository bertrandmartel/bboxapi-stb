package fr.bmartel.bboxapi.stb.sample

import com.github.kittinunf.fuel.core.HttpException
import com.github.kittinunf.result.Result
import de.mannodermaus.rxbonjour.platforms.desktop.DesktopPlatform
import fr.bmartel.bboxapi.stb.BboxApiStb
import fr.bmartel.bboxapi.stb.model.StbServiceEvent

fun main(args: Array<String>) {
    val bboxapi = BboxApiStb(appId = "YourAppId", appSecret = "YourAppSecret")

    bboxapi.startRestDiscovery(findOneAndExit = true, maxDuration = 10000, platform = DesktopPlatform.create()) { eventType, service, changed, error ->
        when (eventType) {
            StbServiceEvent.SERVICE_FOUND -> {
                bboxapi.getApps { _, response, result ->
                    when (result) {
                        is Result.Failure -> {
                            val ex = result.getException()
                            when {
                                ex.exception is HttpException -> println("http error : ${response.statusCode}")
                                else -> ex.printStackTrace()
                            }
                        }
                        is Result.Success -> {
                            val data = result.get()
                            println(data)
                        }
                    }
                }

                val (_, _, result) = bboxapi.getAppsSync()
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