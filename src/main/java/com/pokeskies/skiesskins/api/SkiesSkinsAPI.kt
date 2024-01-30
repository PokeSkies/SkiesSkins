package com.pokeskies.skiesskins.api

import ca.landonjw.gooeylibs2.api.UIManager
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.data.UserData
import com.pokeskies.skiesskins.gui.InventoryGui
import net.minecraft.server.level.ServerPlayer

object SkiesSkinsAPI {
    fun getUserData(player: ServerPlayer): UserData {
        val data = SkiesSkins.INSTANCE.storage?.getUser(player.uuid)
        return data ?: UserData(player.uuid)
    }

    fun getUserDataOrNull(player: ServerPlayer): UserData? {
        return SkiesSkins.INSTANCE.storage?.getUser(player.uuid)
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
}