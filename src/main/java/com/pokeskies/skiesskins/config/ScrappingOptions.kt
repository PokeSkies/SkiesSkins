package com.pokeskies.skiesskins.config

import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.config.shop.ShopConfig
import com.pokeskies.skiesskins.economy.EconomyType
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory
import net.minecraft.server.level.ServerPlayer

class ScrappingOptions(
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val value: List<ScrapValue> = emptyList()
) {

    class ScrapValue(
        val provider: EconomyType,
        val currency: String,
        val amount: Int
    ) {
        fun getCurrencyFormatted(singular: Boolean = false): String {
            val service = SkiesSkins.INSTANCE.economyManager.getService(provider) ?: return currency
            return service.getCurrencyFormatted(currency, singular)
        }

        fun deposit(player: ServerPlayer): Boolean {
            val service = SkiesSkins.INSTANCE.economyManager.getService(provider) ?: return false
            return service.deposit(player, amount.toDouble(), currency)
        }

        override fun toString(): String {
            return "ScrapValue(provider=$provider, currency='$currency', amount=$amount)"
        }
    }

    override fun toString(): String {
        return "ScrappingOptions(value=$value)"
    }
}