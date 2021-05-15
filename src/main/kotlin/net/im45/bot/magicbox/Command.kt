package net.im45.bot.magicbox

import io.ktor.util.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.flash
import net.mamoe.mirai.message.data.sendTo
import net.mamoe.mirai.utils.ExternalResource.Companion.sendAsImageTo
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

private const val errMsg = "Error: No such directory"
internal val errLog
    get() = "$errMsg: ${Config.imageDir}"

private fun checkDirectory(path: String = Config.imageDir): Boolean = runCatching {
    Path.of(path).toFile().isDirectory
}.isSuccess

private suspend fun CommandSender.error() {
    MagicBox.logger.error(errLog)
    sendMessage(errMsg)
}

object MBX : SimpleCommand(
    MagicBox, "mbx"
) {
    private val IMAGE_EXT = arrayOf("jpg", "jpeg", "png", "gif")
    private val magic: MutableList<File> = mutableListOf()

    internal fun reload(
        fromPath: String = Config.imageDir,
        recurseSubDirectories: Boolean = Config.recurseSubDirectories
    ): Boolean {
        Config.recurseSubDirectories = recurseSubDirectories
        if (!checkDirectory(fromPath)) return false
        val path = Paths.get(fromPath)

        magic.clear()
        magic += (if (recurseSubDirectories) Files.walk(path) else Files.list(path))
            .filter { it.extension.toLowerCase() in IMAGE_EXT }
            .map(Path::toFile)
            .collect(Collectors.toList())

        return true
    }

    @Handler
    suspend fun UserCommandSender.mbx() {
        if (!Config.enable) return
        if (!checkDirectory()) {
            error()
            return
        }
        if (magic.isEmpty()) {
            sendMessage("Directory ${Config.imageDir} does not contain image.")
            return
        }

        val resource = magic.random().toExternalResource()

        if (subject is Group)
            when (Config.groupTrusts.getValue(subject.id)) {
                Trust.NOT -> return
                Trust.UNKNOWN -> resource.sendAsImageTo(user)
                Trust.MARGINAL -> resource.sendAsImageTo(subject).recallIn(Config.marginallyRecallIn)
                Trust.FULL -> resource.uploadAsImage(subject).flash().sendTo(subject)
                Trust.ULTIMATE -> resource.sendAsImageTo(subject)
            }
        else resource.sendAsImageTo(subject)

        Data.served++
    }

    @Handler
    suspend fun ConsoleCommandSender.mbx() {
        sendMessage("There are ${magic.size} images currently.")
        sendMessage("Image directory: ${Config.imageDir} (Recurse subdirectories: ${Config.recurseSubDirectories})")
    }
}

object Control : CompositeCommand(
    MagicBox, "magicbox"
) {
    @SubCommand
    suspend fun ConsoleCommandSender.dir(path: String, recurseSubDirectories: Boolean = false) {
        if (checkDirectory(path)) {
            Config.imageDir = path
            MBX.reload(recurseSubDirectories = recurseSubDirectories)
        } else error()
    }

    @SubCommand
    suspend fun CommandSender.reload() {
        if (MBX.reload())
            sendMessage("MagicBox 已重载")
        else error()
    }

    @SubCommand
    suspend fun CommandSender.defaultTrust(trust: Trust) {
        Config.defaultTrust = trust
        sendMessage("设置默认信任等级为$trust")
    }

    @SubCommand
    suspend fun UserCommandSender.trust(trust: Trust) {
        if (subject is Group)
            trust(subject as Group, trust)
    }

    @SubCommand
    suspend fun CommandSender.trust(group: Group, trust: Trust) {
        Config.groupTrusts[group.id] = trust
        sendMessage("${trust}信任 ${group.name}")
    }

    @SubCommand
    suspend fun UserCommandSender.distrust() {
        if (subject is Group)
            distrust(subject as Group)
    }

    @SubCommand
    suspend fun CommandSender.distrust(group: Group) {
        Config.groupTrusts.remove(group.id)
        sendMessage("移除 ${group.name} 的信任等级")
    }

    @SubCommand
    suspend fun CommandSender.trusts() {
        sendMessage(buildString {
            append("默认信任等级：${Config.defaultTrust}\n")
            Config.groupTrusts
                .map { e -> "${e.key}\t${e.value}" }
                .run {
                    append(if (isEmpty()) "无信任设置" else joinToString("\n"))
                }
        })
    }

    @SubCommand
    suspend fun CommandSender.enable() {
        if (checkDirectory()) {
            Config.enable = true
            sendMessage("MagicBox 已启用")
        } else error()
    }

    @SubCommand
    suspend fun CommandSender.disable() {
        Config.enable = false
        sendMessage("MagicBox 已禁用")
    }

    @SubCommand
    suspend fun CommandSender.stat() {
        sendMessage("MagicBox ${if (Config.enable) "启" else "禁"}用，目前已发送图片 ${Data.served} 张")
    }
}
