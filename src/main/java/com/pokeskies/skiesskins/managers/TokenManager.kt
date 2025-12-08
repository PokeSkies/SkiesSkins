package com.pokeskies.skiesskins.managers

import com.pokeskies.skiesskins.api.SkiesSkinsAPI
import com.pokeskies.skiesskins.utils.Utils
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.ItemStack

object TokenManager {
    fun init() {
        UseItemCallback.EVENT.register { player, level, hand ->
            val item = player.getItemInHand(hand)
            if (player !is ServerPlayer) return@register InteractionResultHolder.pass(item)

            return@register InteractionResultHolder(handleInteract(item, player), item)
        }
        UseBlockCallback.EVENT.register { player, level, hand, hitResult ->
            val item = player.getItemInHand(hand)
            if (player !is ServerPlayer) return@register InteractionResult.PASS

            return@register handleInteract(item, player)
        }
    }

    private fun handleInteract(item: ItemStack, player: ServerPlayer): InteractionResult {
        if (item.isEmpty) return InteractionResult.PASS

        val skin = SkiesSkinsAPI.getTokenSkin(item) ?: return InteractionResult.PASS

        if (!SkiesSkinsAPI.canTokenize(skin)) {
            player.playNotifySound(SoundEvents.FIRE_EXTINGUISH, player.soundSource, 0.5f, 1.0f)
            player.sendMessage(Component.text("This skin cannot be applied via token.", NamedTextColor.RED))
            return InteractionResult.FAIL
        }

        if (!SkiesSkinsAPI.giveUserSkin(player, skin, 1)) {
            player.playNotifySound(SoundEvents.FIRE_EXTINGUISH, player.soundSource, 0.5f, 1.0f)
            player.sendMessage(Component.text("Failed to claim skin. Please contact an admin!", NamedTextColor.RED))
            return InteractionResult.FAIL
        }

        item.shrink(1)

        player.playNotifySound(SoundEvents.PLAYER_LEVELUP, player.soundSource, 0.5f, 1.0f)
        player.sendMessage(Component.text("Successfully claimed token for skin ", NamedTextColor.GREEN)
            .append(Utils.deserializeText(skin.name))
        )

        return InteractionResult.PASS
    }
}