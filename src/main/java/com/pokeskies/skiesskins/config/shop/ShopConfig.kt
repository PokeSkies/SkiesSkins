package com.pokeskies.skiesskins.config.shop

import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiesskins.config.gui.GenericGuiItem
import com.pokeskies.skiesskins.economy.EconomyType
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory

class ShopConfig(
    val options: ShopOptions = ShopOptions(),
    val skins: SkinOptions = SkinOptions(),
    val packages: Map<String, ShopPackage> = emptyMap(),
    val items: Map<String, GenericGuiItem> = emptyMap(),
) {
    class ShopOptions(
        val enabled: Boolean = true,
        val economy: EconomyOptions = EconomyOptions(),
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val aliases: List<String> = emptyList(),
        val size: Int = 6,
        val title: String = "Skin Shop",
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
            return "ShopOptions(enabled=$enabled, economy=$economy, aliases=$aliases, size=$size, title='$title')"
        }
    }

    override fun toString(): String {
        return "ShopConfig(options=$options, skins=$skins, packages=$packages, items=$items)"
    }
}