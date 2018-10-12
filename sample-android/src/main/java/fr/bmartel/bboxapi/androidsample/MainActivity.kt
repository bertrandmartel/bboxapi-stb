package fr.bmartel.bboxapi.androidsample

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import com.github.kittinunf.fuel.core.HttpException
import com.github.kittinunf.result.Result
import de.mannodermaus.rxbonjour.platforms.android.AndroidPlatform
import fr.bmartel.bboxapi.stb.BboxApiStb
import fr.bmartel.bboxapi.stb.IWebsocketListener
import fr.bmartel.bboxapi.stb.model.*
import java.net.ConnectException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.schedule

class MainActivity : Activity() {

    private lateinit var bboxapi: BboxApiStb

    private var restServiceAdapter: StbServiceAdapter? = null
    private var wsServiceAdapter: StbServiceAdapter? = null

    private lateinit var mHandler: Handler

    private lateinit var mExecutor: ExecutorService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        bboxapi = BboxApiStb(appId = getString(R.string.app_id), appSecret = getString(R.string.app_secret))
        mHandler = Handler()
        mExecutor = Executors.newSingleThreadExecutor()
        val discoveryRestBtn = findViewById<Button>(R.id.rest_discovery_btn)
        val discoveryWsBtn = findViewById<Button>(R.id.ws_discovery_btn)
        val subscribeBtn = findViewById<Button>(R.id.ws_subscribe)
        restServiceAdapter = StbServiceAdapter(context = this, list = getStbServiceList(ws = true))
        wsServiceAdapter = StbServiceAdapter(context = this, list = getStbServiceList(ws = false))
        val stbRestList = findViewById<ListView>(R.id.stb_rest_service_list)
        val stbWsList = findViewById<ListView>(R.id.stb_ws_service_list)
        stbRestList.adapter = restServiceAdapter
        stbWsList.adapter = wsServiceAdapter
        stbRestList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            if (restServiceAdapter?.list?.get(position)?.principal == false) {
                val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            //listener.onSelectService(serviceAdapter?.list?.get(position)!!)
                            dialog.dismiss()
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                            dialog.dismiss()
                        }
                    }
                }
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Utiliser ce service?").setPositiveButton("Oui", dialogClickListener)
                        .setNegativeButton("Non", dialogClickListener).show()
            }
        }
        discoveryRestBtn.setOnClickListener({
            val res = bboxapi.startRestDiscovery(findOneAndExit = false, maxDuration = 10000, platform = AndroidPlatform.create(this)) { eventType, service, _, error ->
                when (eventType) {
                    StbServiceEvent.SERVICE_FOUND -> {
                        println("rest service found : ${service?.ip}:${service?.port}")
                        mHandler.post({
                            restServiceAdapter?.list = getStbServiceList(ws = false)
                            restServiceAdapter?.notifyDataSetChanged()
                        })
                    }
                    StbServiceEvent.DISCOVERY_STOPPED -> println("end of discovery")
                    StbServiceEvent.DISCOVERY_ERROR -> error?.printStackTrace()
                }
            }
            if (!res) {
                mHandler.post({
                    Toast.makeText(this, "rest discovery already started", Toast.LENGTH_SHORT).show()
                })
            }
        })
        discoveryWsBtn.setOnClickListener({
            val res = bboxapi.startWebsocketDiscovery(findOneAndExit = false, maxDuration = 10000, platform = AndroidPlatform.create(this)) { eventType, service, _, error ->
                when (eventType) {
                    StbServiceEvent.SERVICE_FOUND -> {
                        println("ws service found : ${service?.ip}:${service?.port}")
                        mHandler.post({
                            wsServiceAdapter?.list = getStbServiceList(ws = true)
                            wsServiceAdapter?.notifyDataSetChanged()
                        })
                    }
                    StbServiceEvent.DISCOVERY_STOPPED -> println("end of discovery")
                    StbServiceEvent.DISCOVERY_ERROR -> error?.printStackTrace()
                }
            }
            if (!res) {
                mHandler.post({
                    Toast.makeText(this, "ws discovery already started", Toast.LENGTH_SHORT).show()
                })
            }
        })
        subscribeBtn.setOnClickListener({
            subscribe()
        })
    }

    private fun getStbServiceList(ws: Boolean): List<StbServiceItem> {
        val serviceList = mutableListOf<StbServiceItem>()
        val services: MutableList<StbService>
        val selectService: StbService?
        if (ws) {
            services = bboxapi.wsServiceList
            selectService = bboxapi.wsService
        } else {
            services = bboxapi.restServiceList
            selectService = bboxapi.restService
        }
        services.mapTo(serviceList) {
            if (selectService != null && selectService.ip === it.ip) {
                StbServiceItem(ip = it.ip, port = it.port, principal = true, imgRes = R.drawable.ic_action_cloud)
            } else {
                StbServiceItem(ip = it.ip, port = it.port, principal = false, imgRes = R.drawable.ic_action_cloud)
            }
        }
        return serviceList
    }

    private fun subscribe() {
        mExecutor.execute({
            try {
                //unsubscribe all channels
                bboxapi.unsubscribeAllSync()

                val notificationChannel = bboxapi.subscribeNotification(
                        appName = "myApplication",
                        resourceList = listOf(Resource.Application, Resource.Media, Resource.Message),
                        listener = object : IWebsocketListener {

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

                if (notificationChannel == null) {
                    mHandler.post({
                        Toast.makeText(this, "notification channel was not created", Toast.LENGTH_SHORT).show()
                    })
                } else {
                    val (_, _, result) = notificationChannel.subscribeResult
                    if (result is Result.Failure) {
                        result.error.printStackTrace()
                    } else {
                        println("subscribed with resource on channelId ${notificationChannel.channelId} & appId ${notificationChannel.appId}")
                    }

                    val (_, _, channelIdRes) = bboxapi.getOpenedChannelsSync()
                    if (channelIdRes is Result.Failure) {
                        channelIdRes.error.printStackTrace()
                    } else {
                        println("channels : ${channelIdRes.get()}")
                    }
                    val timer = Timer("schedule", true)

                    timer.schedule(2000) {
                        bboxapi.sendNotification(channelId = notificationChannel.channelId ?: "",
                                appId = notificationChannel.appId ?: "",
                                message = "some message") { _, _, result ->
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
                }
            } catch (e: HttpException) {
                mHandler.post({
                    Toast.makeText(this, "http exception : ${e.message}", Toast.LENGTH_SHORT).show()
                })
            } catch (e: ConnectException){
                mHandler.post({
                    Toast.makeText(this, "can't connect to server", Toast.LENGTH_SHORT).show()
                })
            }
        })
    }
}