package fr.bmartel.bboxapi.stb

import fr.bmartel.bboxapi.stb.model.AppEvent
import fr.bmartel.bboxapi.stb.model.BboxApiError
import fr.bmartel.bboxapi.stb.model.MediaEvent
import fr.bmartel.bboxapi.stb.model.MessageEvent

interface IWebsocketListener {
    fun onOpen()
    fun onClose()
    fun onError(error: BboxApiError)
    fun onMedia(media: MediaEvent)
    fun onApp(app: AppEvent)
    fun onMessage(message: MessageEvent)
    fun onFailure(throwable: Throwable?)
}