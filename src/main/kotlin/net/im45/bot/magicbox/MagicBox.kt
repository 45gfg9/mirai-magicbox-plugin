package net.im45.bot.magicbox

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregisterAllCommands
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin

object MagicBox : KotlinPlugin(
    JvmPluginDescription(
        "net.im45.bot.magicbox",
        "1.0.0",
        "MagicBox"
    )
) {
    override fun onEnable() {
        super.onEnable()

        MBX.register()
        Control.register()
        Config.reload()
        Data.reload()

        if (!MBX.reload()) {
            Config.enable = false
            logger.error(errLog)
            logger.error("MagicBox disabled.")
        }
    }

    override fun onDisable() {
        super.onDisable()
        unregisterAllCommands(this)
    }
}

