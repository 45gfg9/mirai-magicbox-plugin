package net.im45.bot.magicbox

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object Config : AutoSavePluginConfig("mbxcfg") {
    var enable: Boolean by value(true)
    var imageDir: String by value(".")
    var recurseSubDirectories: Boolean by value(false)
    var defaultTrust: Trust by value(Trust.NOT)
    val marginallyRecallIn: Long by value(5000L)
    val groupTrusts by value(mutableMapOf<Long, Trust>().withDefault { defaultTrust })
}

object Data : AutoSavePluginData("mbxdat") {
    var served: Long by value(0L)
}
