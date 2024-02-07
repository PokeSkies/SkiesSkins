package com.pokeskies.skiesskins.config.shop

import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiesskins.economy.EconomyType
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory

class ShopConfig(
    val options: ShopOptions,
    val skins: SkinOptions,
    val packages: PackageOptions,
) {
    class ShopOptions(
        val economy: EconomyOptions,
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val aliases: List<String>,
        val size: Int,
        val title: String,
    ) {
        class EconomyOptions(
            val provider: EconomyType = EconomyType.IMPACTOR,
            val currency: String = "",
        ) {
            override fun toString(): String {
                return "EconomyOptions(provider=$provider, currency='$currency')"
            }
        }

        override fun toString(): String {
            return "ShopOptions(economy=$economy, aliases=$aliases, size=$size, title='$title')"
        }
    }
}