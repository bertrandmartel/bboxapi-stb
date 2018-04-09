package fr.bmartel.bboxapi.stb.sample

import com.github.kittinunf.result.Result
import de.mannodermaus.rxbonjour.platforms.desktop.DesktopPlatform
import fr.bmartel.bboxapi.stb.BboxApiStb
import fr.bmartel.bboxapi.stb.model.*
import java.util.*
import kotlin.concurrent.schedule

fun main(args: Array<String>) {
    val bboxapi = BboxApiStb(appId = "YourAppId", appSecret = "YourAppSecret")
    val resourceList = listOf(Resource.Application, Resource.Media, Resource.Message)

    //look for BboxAPI service & subscribe events
    bboxapi.startRestDiscovery(findOneAndExit = true, maxDuration = 10000, platform = DesktopPlatform.create()) { eventType, service, error ->
        when (eventType) {
            StbServiceEvent.SERVICE_FOUND -> {
                println("service found... opening websocket")

                //unsubscribe all channels
                bboxapi.unsubscribeAllSync()

                val notificationChannel = bboxapi.subscribeNotification(
                        appName = "myApplication",
                        resourceList = listOf(Resource.Application, Resource.Media, Resource.Message),
                        listener = object : BboxApiStb.WebSocketListener {

                            override fun onOpen() {
                                println("websocket opened")
                            }

                            override fun onClose() {
                                println("websocket closed")
                            }

                            override fun onApp(app: AppEvent) {
                                println("application event : $app")
                            }

                            override fun onMedia(media: MediaEvent) {
                                println("channel change event : $media")
                            }

                            override fun onMessage(message: MessageEvent) {
                                println("message event : $message")
                            }

                            override fun onError(error: BboxApiError) {
                                println("error : $error")
                            }

                            override fun onFailure(throwable: Throwable?) {
                                throwable?.printStackTrace()
                            }
                        })

                val (_, _, result) = notificationChannel.subscribeResult
                if (result is Result.Failure) {
                    result.error.printStackTrace()
                } else {
                    println("subscribed with resource $resourceList on channelId ${notificationChannel.channelId} & appId ${notificationChannel.appId}")
                }

                val (_, _, channelIdRes) = bboxapi.getOpenedChannelsSync()
                if (channelIdRes is Result.Failure) {
                    channelIdRes.error.printStackTrace()
                } else {
                    println("channels : ${channelIdRes.get()}")
                }
                val timer = Timer("schedule", true)

                timer.schedule(2000) {
                    val (_, _, result) = bboxapi.sendNotificationSync(
                            channelId = notificationChannel.channelId ?: "",
                            appId = notificationChannel.appId ?: "",
                            message = "some message")
                    when (result) {
                        is Result.Failure -> {
                            result.getException().printStackTrace()
                        }
                        is Result.Success -> {
                            println("message sent")
                        }
                    }

                    bboxapi.sendNotification(channelId = notificationChannel.channelId ?: "",
                            appId = notificationChannel.appId ?: "",
                            message = "some message") { _, response, result ->
                        when (result) {
                            is Result.Failure -> {
                                result.getException().printStackTrace()
                            }
                            is Result.Success -> {
                                println("message sent")
                            }
                        }
                    }
                }
                //bboxapi.closeWebsocket()
            }
            StbServiceEvent.DISCOVERY_STOPPED -> println("end of discovery")
            StbServiceEvent.DISCOVERY_ERROR -> error?.printStackTrace()
        }
    }
}