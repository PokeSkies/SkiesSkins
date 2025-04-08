package com.pokeskies.skiesskins.api

import ca.landonjw.gooeylibs2.api.UIManager
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
        UIManager.openUIForcefully(player, InventoryGui(player))
    }

    fun getPokemonSkin(pokemon: Pokemon?): Pair<String, SkinConfig>? {
        if (pokemon == null) return null
        val skinId = pokemon.persistentData.getString(TAG_SKIN_DATA)
        ConfigManager.SKINS[skinId]?.let { return Pair(skinId, it) }

        if (ConfigManager.CONFIG.findEquivalent) {
            for ((id, config) in ConfigManager.SKINS) {
                val species = PokemonSpecies.getByIdentifier(config.species) ?: continue
                if (species != pokemon.species) continue

                // gather a list of aspects that must be present
                val requiredAspects: MutableList<String> = mutableListOf()
                for (aspect in config.aspects.apply) {
                    requiredAspects.addAll(PokemonProperties.parse(aspect).aspects)
                }
                for (aspect in config.aspects.required) {
                    requiredAspects.addAll(PokemonProperties.parse(aspect).aspects)
                }

                // gather a list of aspects that cannot be present
                val blacklistedAspects: MutableList<String> = mutableListOf()
                for (aspect in config.aspects.blacklist) {
                    blacklistedAspects.removeAll(PokemonProperties.parse(aspect).aspects)
                }

                if (pokemon.aspects.containsAll(requiredAspects) && pokemon.aspects.none { blacklistedAspects.contains(it) }) {
                    return Pair(id, config)
                }
            }
        }

        return null
    }
}
