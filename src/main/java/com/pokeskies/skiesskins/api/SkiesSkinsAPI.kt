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
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData

object SkiesSkinsAPI {
    val TAG_SKIN_DATA = "skiesskins_skin_data"
    val TAG_TOKEN_ITEM = "skiesskins_token_item"

    fun getUserData(player: ServerPlayer): UserData {
        return getUserDataOrNull(player) ?: UserData(player.uuid)
    }

    fun getUserDataOrNull(player: ServerPlayer): UserData? {
        return SkiesSkins.INSTANCE.storage?.getUser(player.uuid)
    }

    fun saveUserData(player: ServerPlayer, data: UserData): Boolean {
        return SkiesSkins.INSTANCE.storage?.saveUser(player.uuid, data) ?: false
    }

    fun giveUserSkin(player: ServerPlayer, skin: SkinConfig, amount: Int): Boolean {
        val user = getUserData(player)
        for (i in 1..amount) {
            user.inventory.add(UserSkinData(skin.id))
        }
        return saveUserData(player, user)
    }

    fun openSkinInventory(player: ServerPlayer) {
        InventoryGui(player).open()
    }

    fun getSkin(id: String): SkinConfig? {
        return ConfigManager.SKINS[id]
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

    fun getTokenSkin(item: ItemStack): SkinConfig? {
        item.get(DataComponents.CUSTOM_DATA)?.let { data ->
            if (!data.contains(TAG_TOKEN_ITEM)) return null
            val id = data.copyTag().getString(TAG_TOKEN_ITEM)
            return getSkin(id)
        }

        return null
    }

    fun canTokenize(skin: SkinConfig): Boolean {
        return skin.token != null
    }

    fun tokenizeSkin(skin: SkinConfig, player: ServerPlayer? = null): ItemStack? {
        if (!canTokenize(skin)) return null
        val pokemon = PokemonSpecies.getByIdentifier(skin.species)?.create(1)
        if (pokemon != null) {
            applySkin(pokemon, skin)
        }
        val tokenItem = skin.token!!.display.createItemStack(player, pokemon)
        if (tokenItem.isEmpty) return null

        val data = tokenItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.of(CompoundTag())).copyTag()
        data.putString(TAG_TOKEN_ITEM, skin.id)

        tokenItem.set(DataComponents.CUSTOM_DATA, CustomData.of(data))
        return tokenItem
    }
}
