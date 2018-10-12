package fr.bmartel.bboxapi.androidsample

data class StbServiceItem(
        val ip: String,
        val port: Int,
        val imgRes: Int,
        var principal: Boolean = false
)