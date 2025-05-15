package com.pokeskies.skiesskins.config.shop

import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.economy.EconomyType
import net.minecraft.server.level.ServerPlayer

/*
 * Specifying a cost for a skin/package.
 */
class ShopCostConfig(
    val provider: EconomyType?,
    val currency: String?,
    val amount: Int
) {
    fun getCurrencyOrDefault(shopConfig: ShopConfig): String {
        return currency ?: shopConfig.options.economy.currency
    }

    fun getProviderOrDefault(shopConfig: ShopConfig): EconomyType {
        return provider ?: shopConfig.options.economy.provider
    }

    fun getCurrencyFormatted(shopConfig: ShopConfig, singular: Boolean = false): String {
        val currency = getCurrencyOrDefault(shopConfig)
        val service = SkiesSkins.INSTANCE.economyManager.getService(getProviderOrDefault(shopConfig)) ?: return currency
        return service.getCurrencyFormatted(currency, singular)
    }

    fun withdraw(player: ServerPlayer, shopConfig: ShopConfig): Boolean {
        val service = SkiesSkins.INSTANCE.economyManager.getService(getProviderOrDefault(shopConfig)) ?: return false
        return service.withdraw(player, amount.toDouble(), getCurrencyOrDefault(shopConfig))
    }
}
