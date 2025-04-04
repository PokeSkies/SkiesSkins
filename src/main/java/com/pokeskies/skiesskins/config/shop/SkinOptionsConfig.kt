package com.pokeskies.skiesskins.config.shop

class SkinOptionsConfig(
    val random: Map<String, ShopRandomSetConfig> = emptyMap(),
    val static: Map<String, ShopStaticSetConfig> = emptyMap(),
) {
    override fun toString(): String {
        return "SkinOptions(random=$random, static=$static)"
    }
}
