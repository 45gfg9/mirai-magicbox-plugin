package net.im45.bot.magicbox

enum class Trust(private val readableString: String) {
    UNKNOWN("未知"),
    NOT("永不"),
    MARGINAL("勉强"),
    FULL("完全"),
    ULTIMATE("绝对");

    override fun toString() = readableString
}
