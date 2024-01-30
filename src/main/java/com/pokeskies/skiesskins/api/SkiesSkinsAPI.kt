package com.pokeskies.skiesskins.api

import ca.landonjw.gooeylibs2.api.UIManager
import com.cobblemon.mod.common.pokemon.Pokemon
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.config.ConfigManager
import com.pokeskies.skiesskins.config.SkinConfig
import com.pokeskies.skiesskins.data.UserData
import com.pokeskies.skiesskins.data.UserData.SkinData
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
            user.inventory.add(UserData.SkinData(skinId))
        }
        SkiesSkins.INSTANCE.storage?.saveUser(player.uuid, user)
    }

    fun openSkinInventory(player: ServerPlayer) {
        UIManager.openUIForcefully(player, InventoryGui(player))
    }

    fun getPokemonSkin(pokemon: Pokemon): SkinConfig? {
        val data = pokemon.persistentData.getString(TAG_SKIN_DATA)

        if (data.isNullOrEmpty()) {
            return null
        }

        return ConfigManager.SKINS[data]
    }
}