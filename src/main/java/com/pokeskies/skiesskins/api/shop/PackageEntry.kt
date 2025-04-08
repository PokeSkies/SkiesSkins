package com.pokeskies.skiesskins.api.shop

import com.pokeskies.skiesskins.config.gui.items.ShopItem.Companion.amountRegex
import com.pokeskies.skiesskins.config.gui.items.ShopItem.Companion.currencyRegex
import com.pokeskies.skiesskins.config.shop.ShopConfig
import com.pokeskies.skiesskins.config.shop.ShopCostConfig
import com.pokeskies.skiesskins.config.shop.entries.PackageEntryConfig
import com.pokeskies.skiesskins.utils.Utils
import net.minecraft.server.level.ServerPlayer

class PackageEntry(
    val shopId: String,
    val shopConfig: ShopConfig,
    val packageConfig: PackageEntryConfig,
    val cost: List<ShopCostConfig>,
    val limit: Int,
    val purchases: Int
) : ShopEntry {
    override fun parse(entry: String, player: ServerPlayer): String {
        var parsed = entry.replace("%cost%", cost.joinToString(" ") { "${it.amount} ${it.getCurrencyFormatted(shopConfig, (it.amount > 1))}" })
            .replace("%limit%", if (limit > 0) limit.toString() else "∞")
            .replace("%purchases%", purchases.toString())
            .replace("%name%", packageConfig.name)


        amountRegex.findAll(parsed).toList().forEach { matchResult ->
            val idx = matchResult.groupValues[1].toInt()
            parsed = parsed.replace(
                "%cost_amount:$idx%",
                if (cost.size > idx)
                    cost[idx].amount.toString()
                else ""
            )
        }

        currencyRegex.findAll(parsed).toList().forEach { matchResult ->
            val idx = matchResult.groupValues[1].toInt()
            parsed = parsed.replace(
                "%cost_currency:$idx%",
                if (cost.size > idx)
                    cost[idx].let { it.getCurrencyFormatted(shopConfig, (it.amount > 1)) }
                else ""
            )
        }

        return parsed
    }

    override fun parse(entry: List<String>, player: ServerPlayer): List<String> {
        val newList: MutableList<String> = mutableListOf()
        for (line in entry) {
            newList.add(Utils.parsePlaceholders(player, parse(line, player)))
        }
        return newList
    }
}
