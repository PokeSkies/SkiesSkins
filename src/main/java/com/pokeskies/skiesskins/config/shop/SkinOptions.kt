package com.pokeskies.skiesskins.config.shop

class SkinOptions(
    val random: Map<String, ShopRandomSet> = emptyMap(),
    val static: Map<String, ShopStaticSet> = emptyMap(),
) {
    override fun toString(): String {
        return "SkinOptions(random=$random, static=$static)"
    }
}