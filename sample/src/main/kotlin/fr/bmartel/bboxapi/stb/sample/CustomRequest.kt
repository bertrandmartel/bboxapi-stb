package fr.bmartel.bboxapi.stb.sample

import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.result.Result
import de.mannodermaus.rxbonjour.platforms.desktop.DesktopPlatform
import fr.bmartel.bboxapi.stb.BboxApiStb
import fr.bmartel.bboxapi.stb.model.StbServiceEvent

fun main(args: Array<String>) {
    val bboxapi = BboxApiStb(appId = "YourAppId", appSecret = "YourAppSecret")

    bboxapi.startRestDiscovery(findOneAndExit = true, maxDuration = 10000, platform = DesktopPlatform.create()) { eventType, service, error ->
        when (eventType) {
            StbServiceEvent.SERVICE_FOUND -> {
                println("service found : ${service?.ip}:${service?.port}")

                bboxapi.createCustomRequest(bboxapi.manager.request(method = Method.GET, path = "/applications")) { _, _, result ->
                    when (result) {
                        is Result.Failure -> {
                            result.getException().printStackTrace()
                        }
                        is Result.Success -> {
                            println(String(result.get()))
                        }
                    }
                }

                val (_, _, result) = bboxapi.createCustomRequestSync(bboxapi.manager.request(method = Method.GET, path = "/applications"))
                when (result) {
                    is Result.Failure -> {
                        result.getException().printStackTrace()
                    }
                    is Result.Success -> {
                        println(String(result.get()))
                    }
                }
            }
            StbServiceEvent.DISCOVERY_STOPPED -> println("end of discovery")
            StbServiceEvent.DISCOVERY_ERROR -> error?.printStackTrace()
        }
    }
}