package net.im45.bot.magicbox

import com.google.auto.service.AutoService
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregisterAllCommands
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.utils.sendTo
import net.mamoe.mirai.utils.toExternalImage
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

private val IMAGE_EXT = listOf("jpg", "jpeg", "png", "gif")

@AutoService(JvmPlugin::class)
object MagicBox : KotlinPlugin(
        JvmPluginDescription(
                "net.im45.bot.magicbox",
                "1.0-SNAPSHOT",
                "MagicBox"
        )
) {
    override fun onEnable() {
        super.onEnable()

        MagicBoxGet.register()
        MagicBoxCtrl.register()
        MagicBoxConfig.reload()
        MagicBoxData.reload()

        MagicBoxGet.reload()
    }

    override fun onDisable() {
        super.onDisable()
        unregisterAllCommands(this)
    }
}

object MagicBoxData : AutoSavePluginData("mbxdat") {
    var enabled: Boolean by value(true)
    var served: Long by value(0L)
}

object MagicBoxConfig : AutoSavePluginConfig("mbxcfg") {
    var imageDir: String by value(".")
    var recurseSubDirectories: Boolean by value(false)
    val trustedGroups: MutableSet<Long> by value(mutableSetOf())
}

object MagicBoxGet : SimpleCommand(
        MagicBox, "mbx"
) {
    private lateinit var magic: List<File>

    fun reload(fromPath: Path = Path.of(MagicBoxConfig.imageDir), recurseSubDirectories: Boolean = false) {
        MagicBoxConfig.recurseSubDirectories = recurseSubDirectories;
        magic = (if (MagicBoxConfig.recurseSubDirectories) Files.walk(fromPath) else Files.list(fromPath))
                .map(Path::toFile)
                .filter { it.extension.toLowerCase() in IMAGE_EXT }
                .collect(Collectors.toList())
    }

    @Handler
    suspend fun UserCommandSender.mbx() {
        if (magic.isEmpty()) {
            sendMessage("Directory ${MagicBoxConfig.imageDir} does not contain image.")
            return
        }
        magic.random()
                .toExternalImage(true)
                .sendTo(if (subject is Group && subject.id !in MagicBoxConfig.trustedGroups) user else subject)
        MagicBoxData.served++
    }

    @Handler
    suspend fun ConsoleCommandSender.mbx() {
        sendMessage(magic.toString())
    }
}

object MagicBoxCtrl : CompositeCommand(
        MagicBox, "magicbox"
) {
    @SubCommand
    suspend fun ConsoleCommandSender.dir(path: String) {
        println(path)
        runCatching {
            Path.of(path).toFile().isDirectory
        }.onFailure {
            sendMessage("Given String is not a valid path.")
        }.onSuccess {
            if (it) {
                MagicBoxConfig.imageDir = path
                sendMessage("Path set.")
                MagicBoxGet.reload()
            } else {
                sendMessage("Given path is not a directory.")
            }
        }
    }

    @SubCommand
    suspend fun CommandSender.reload() {
        MagicBoxGet.reload()
        sendMessage("MagicBox reloaded")
    }

    @SubCommand
    suspend fun UserCommandSender.trust() {
        if (subject is Group)
            trust(subject as Group)
    }

    @SubCommand
    suspend fun UserCommandSender.trust(group: Group) {
        MagicBoxConfig.trustedGroups.add(group.id)
        sendMessage("Trusted group ${group.name}")
    }

    @SubCommand
    suspend fun UserCommandSender.distrust() {
        if (subject is Group)
            distrust(subject as Group)
    }

    @SubCommand
    suspend fun UserCommandSender.distrust(group: Group) {
        MagicBoxConfig.trustedGroups.remove(group.id)
        sendMessage("Distrusted group ${group.name}")
    }

    @SubCommand
    suspend fun CommandSender.trusted() {
        MagicBoxConfig.trustedGroups.run {
            sendMessage(if (isEmpty()) "No trusted groups" else joinToString(", "))
        }
    }

    @SubCommand
    suspend fun CommandSender.enable() {
        MagicBoxData.enabled = true
        sendMessage("MagicBox enabled")
    }

    @SubCommand
    suspend fun CommandSender.disable() {
        MagicBoxData.enabled = false
        sendMessage("MagicBox disabled")
    }

    @SubCommand
    suspend fun CommandSender.stat() {
        sendMessage("MagicBox is ${if (MagicBoxData.enabled) "enabled" else "disabled"}, currently served ${MagicBoxData.served} pictures")
    }
}
