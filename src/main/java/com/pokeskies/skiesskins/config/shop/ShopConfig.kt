package com.pokeskies.skiesskins.config.shop

import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiesskins.config.gui.items.GenericItem
import com.pokeskies.skiesskins.config.shop.entries.PackageEntryConfig
import com.pokeskies.skiesskins.config.shop.entries.RandomEntryConfig
import com.pokeskies.skiesskins.config.shop.entries.StaticEntryConfig
import com.pokeskies.skiesskins.economy.EconomyType
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory

/*
 * Shop files located in /shops/{*}.json
 */
class ShopConfig(
    val options: ShopOptions = ShopOptions(),
    val skins: SkinOptionsConfig = SkinOptionsConfig(),
    val packages: Map<String, PackageEntryConfig> = emptyMap(),
    val items: Map<String, GenericItem> = emptyMap(),
) {
    /*
     * Options for specifically this shop
     */
    class ShopOptions(
        val enabled: Boolean = true,
        val economy: EconomyOptions = EconomyOptions(),
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val aliases: List<String> = emptyList(),
        val size: Int = 6,
        val title: String = "Skin Shop",
    ) {
        /*
         * Fallback options for Economy transactions. It can be overridden on each specific skin/package
         */
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

    /*
     * Options for the skins that can appear in this shop
     */
    class SkinOptionsConfig(
        val random: Map<String, RandomEntryConfig> = emptyMap(),
        val static: Map<String, StaticEntryConfig> = emptyMap(),
    ) {
        override fun toString(): String {
            return "SkinOptions(random=$random, static=$static)"
        }
    }

    override fun toString(): String {
        return "ShopConfig(options=$options, skins=$skins, packages=$packages, items=$items)"
    }
}
