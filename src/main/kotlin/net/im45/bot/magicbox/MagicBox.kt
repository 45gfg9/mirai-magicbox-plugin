package net.im45.bot.magicbox

import com.google.auto.service.AutoService
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin

@AutoService(JvmPlugin::class)
object MagicBox : KotlinPlugin(
        JvmPluginDescription(
                "net.im45.bot.magicbox",
                "1.0-SNAPSHOT",
                "MagicBox"
        )
)
