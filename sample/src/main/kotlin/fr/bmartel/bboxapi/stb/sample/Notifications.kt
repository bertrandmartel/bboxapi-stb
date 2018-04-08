package fr.bmartel.bboxapi.stb.sample

import com.github.kittinunf.result.Result
import de.mannodermaus.rxbonjour.platforms.desktop.DesktopPlatform
import fr.bmartel.bboxapi.stb.BboxApiStb
import fr.bmartel.bboxapi.stb.model.Resource
import fr.bmartel.bboxapi.stb.model.StbServiceEvent

fun main(args: Array<String>) {
    val bboxapi = BboxApiStb(appId = "YourAppId", appSecret = "YourAppSecret", platform = DesktopPlatform.create())
    val appName = "myApplication"
    val resourceList = listOf(Resource.Application, Resource.Media, Resource.Message, Resource.test)

    val listener = object : BboxApiStb.WebSocketListener {
        override fun onOpen() {
            println("websocket opened")
        }

        override fun onClose() {
            println("websocket closed")
        }

        override fun onMessage(text: String?) {
            println("message received : $text")
        }

        override fun onFailure(throwable: Throwable?) {
            throwable?.printStackTrace()
        }
    }

    //look for BboxAPI service & subscribe events
    bboxapi.startRestDiscovery(findOneAndExit = true, maxDuration = 10000) { eventType, service, error ->
        when (eventType) {
            StbServiceEvent.SERVICE_FOUND -> {
                println("service found... opening websocket")

                bboxapi.unsubscribeAllSync()

                val (channelId, subscribeRes) = bboxapi.subscribeNotification(appName = appName, resourceList = resourceList, listener = listener)
                val (_, _, result) = subscribeRes
                if (result is Result.Failure) {
                    result.error.printStackTrace()
                } else {
                    println("subscribed $appName with resource $resourceList on channelId $channelId")
                }

                val (_, _, channelIdRes) = bboxapi.getOpenedChannelsSync()
                if (channelIdRes is Result.Failure) {
                    channelIdRes.error.printStackTrace()
                } else {
                    println("channels : ${channelIdRes.get()}")
                }


                //bboxapi.closeWebsocket()
            }
            StbServiceEvent.DISCOVERY_STOPPED -> println("end of discovery")
            StbServiceEvent.DISCOVERY_ERROR -> error?.printStackTrace()
        }
    }
}