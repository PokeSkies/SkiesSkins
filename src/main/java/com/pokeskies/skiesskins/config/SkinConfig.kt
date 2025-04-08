package com.pokeskies.skiesskins.config

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.economy.EconomyType
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiesskins.utils.Utils.deserializeText
import com.pokeskies.skiesskins.utils.Utils.parsePlaceholders
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

/*
 * The config for a specific skin, pulls from /skins/{*}.json
 */
class SkinConfig(
    val enabled: Boolean = true,
    val species: ResourceLocation = ResourceLocation.withDefaultNamespace(""),
    val name: String = "",
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val description: List<String> = emptyList(),
    val aspects: Aspects = Aspects(),
    val scrapping: ScrappingOptions? = null,
) {

    /*
     * Defines the aspects of a skin that are applied, required, blacklisted, or removed from a Pokemon
     */
    class Aspects(
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val apply: List<String> = emptyList(),
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val required: List<String> = emptyList(),
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val blacklist: List<String> = emptyList(),
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val remove: List<String> = emptyList(),
    ) {
        override fun toString(): String {
            return "Aspects(apply=$apply, required=$required, blacklist=$blacklist, remove=$remove)"
        }
    }

    /*
     * Defines the scrapping options for a skin
     */
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

    fun parse(
        string: String,
        player: ServerPlayer
    ): Component {
        var parsed = string.replace("%name%", name)
            .replace("%species%", PokemonSpecies.getByIdentifier(species)?.name ?: "Invalid Species")

        if (scrapping != null) {
            parsed = parsed.replace("%scrap_value%", scrapping.value.joinToString(" ") { "${it.amount} ${it.getCurrencyFormatted(it.amount > 1)}" })
        } else {
            parsed = parsed.replace("%scrap_value%", "No Value")
        }

        return deserializeText(parsePlaceholders(player, parsed))
    }

    fun parse(list: List<String>, player: ServerPlayer): List<Component> {
        val newList: MutableList<Component> = mutableListOf()
        for (line in list) {
            var initialParse = line.replace("%name%", name)
                .replace("%species%", PokemonSpecies.getByIdentifier(species)?.name ?: "Invalid Species")

            if (scrapping != null) {
                initialParse = initialParse.replace("%scrap_value%", scrapping.value.joinToString(" ") { "${it.amount} ${it.getCurrencyFormatted(it.amount > 1)}" })
            } else {
                initialParse = initialParse.replace("%scrap_value%", "No Value")
            }

            val parsed = parsePlaceholders(player, initialParse)

            if (parsed.contains("%description%", true)) {
                for (dLine in description) {
                    newList.add(deserializeText(parsed.replace("%description%", dLine)))
                }
            } else {
                newList.add(deserializeText(parsed))
            }
        }
        return newList
    }

    override fun toString(): String {
        return "SkinConfig(enabled=$enabled, species=$species, name='$name', description=$description, aspects=$aspects, scrapping=$scrapping)"
    }
}
