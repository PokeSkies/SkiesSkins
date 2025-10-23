package com.pokeskies.skiesskins.api

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.pokemon.Pokemon
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.config.ConfigManager
import com.pokeskies.skiesskins.config.SkinConfig
import com.pokeskies.skiesskins.data.UserData
import com.pokeskies.skiesskins.data.UserSkinData
import com.pokeskies.skiesskins.gui.InventoryGui
import net.minecraft.server.level.ServerPlayer

object SkiesSkinsAPI {
    val TAG_SKIN_DATA = "skiesskins_skin_data"

    fun getUserData(player: ServerPlayer): UserData {
        val data = SkiesSkins.INSTANCE.storage?.getUser(player.uuid)
        return data ?: UserData(player.uuid)
    }

    fun getUserDataOrNull(player: ServerPlayer): UserData? {
        return SkiesSkins.INSTANCE.storage?.getUser(player.uuid)
    }

    fun saveUserData(player: ServerPlayer, data: UserData): Boolean {
        return SkiesSkins.INSTANCE.storage?.saveUser(player.uuid, data) ?: false
    }

    fun giveUserSkin(player: ServerPlayer, skinId: String, amount: Int) {
        val user = getUserData(player)
        for (i in 1..amount) {
            user.inventory.add(UserSkinData(skinId))
        }
        SkiesSkins.INSTANCE.storage?.saveUser(player.uuid, user)
    }

    fun openSkinInventory(player: ServerPlayer) {
        InventoryGui(player).open()
    }

    fun getPokemonSkin(pokemon: Pokemon?): SkinConfig? {
        if (pokemon == null) return null
        val skinId = pokemon.persistentData.getString(TAG_SKIN_DATA)
        ConfigManager.SKINS[skinId]?.let { skin -> return skin }

        if (ConfigManager.CONFIG.findEquivalent) {
            for (skin in ConfigManager.SKINS.values) {
                val species = PokemonSpecies.getByIdentifier(skin.species) ?: continue
                if (species != pokemon.species) continue

                // gather a list of aspects that must be present
                val requiredAspects: MutableList<String> = mutableListOf()
                for (aspect in skin.aspects.apply) {
                    requiredAspects.addAll(PokemonProperties.parse(aspect).aspects)
                }
                for (aspect in skin.aspects.required) {
                    requiredAspects.addAll(PokemonProperties.parse(aspect).aspects)
                }

                // gather a list of aspects that cannot be present
                val blacklistedAspects: MutableList<String> = mutableListOf()
                for (aspect in skin.aspects.blacklist) {
                    blacklistedAspects.removeAll(PokemonProperties.parse(aspect).aspects)
                }

                if (pokemon.aspects.containsAll(requiredAspects) && pokemon.aspects.none { blacklistedAspects.contains(it) }) {
                    return skin
                }
            }
        }

        return null
    }

    fun applySkin(pokemon: Pokemon, skin: SkinConfig): SkinApplyReturn {
        getPokemonSkin(pokemon)?.let {
            return SkinApplyReturn.ALREADY_HAS_SKIN
        }

        // Check if correct species
        val species = PokemonSpecies.getByIdentifier(skin.species)!!
        if (pokemon.species != species) {
            return SkinApplyReturn.INVALID_SPECIES
        }

        // Check if the Pokemon contains ALL required aspects
        if (skin.aspects.required.isNotEmpty() &&
            skin.aspects.required.stream().noneMatch { pokemon.aspects.contains(it) }) {
            return SkinApplyReturn.MISSING_ASPECTS
        }

        // Check if the Pokemon contains ANY blacklisted aspects
        if (skin.aspects.blacklist.isNotEmpty() &&
            skin.aspects.blacklist.stream().anyMatch { pokemon.aspects.contains(it) }) {
            return SkinApplyReturn.BLACKLISTED_ASPECTS
        }

        for (aspect in skin.aspects.apply) {
            PokemonProperties.parse(aspect).apply(pokemon)
        }
        pokemon.persistentData.putString(TAG_SKIN_DATA, skin.id)
        if (skin.untradable ?: ConfigManager.CONFIG.untradable) {
            pokemon.tradeable = false
        }

        return SkinApplyReturn.SUCCESS
    }

    fun removeSkin(pokemon: Pokemon): Pair<SkinRemoveReturn, SkinConfig?> {
        val skin = getPokemonSkin(pokemon) ?: run {
            return SkinRemoveReturn.SKIN_NOT_APPLIED to null
        }

        for (aspect in skin.aspects.remove) {
            PokemonProperties.parse(aspect).apply(pokemon)
        }
        pokemon.persistentData.remove(TAG_SKIN_DATA)

        // Set the Pokemon to trade-able if the skin was originally marked as untradable
        if (skin.untradable ?: ConfigManager.CONFIG.untradable) {
            pokemon.tradeable = true
        }

        return SkinRemoveReturn.SUCCESS to skin
    }
}
