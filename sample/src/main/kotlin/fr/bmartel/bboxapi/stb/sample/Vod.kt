package fr.bmartel.bboxapi.stb.sample

import com.github.kittinunf.result.Result
import fr.bmartel.bboxapi.stb.BboxApiStb

fun main(args: Array<String>) {
    val bboxapi = BboxApiStb(appId = "YourAppId", appSecret = "YourAppSecret")

    val (_,_,result) = bboxapi.getVodSync(page = 1, limit = 2)

    when (result) {
        is Result.Failure -> {
            result.getException().printStackTrace()
        }
        is Result.Success -> {
            println(result.get())
        }
    }
}