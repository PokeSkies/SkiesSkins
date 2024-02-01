package com.pokeskies.skiesskins.config.gui.actions.types

import com.pokeskies.skiesskins.config.gui.actions.Action
import com.pokeskies.skiesskins.config.gui.actions.ActionType
import com.pokeskies.skiesskins.config.gui.actions.ClickType
import com.pokeskies.skiesskins.utils.Utils
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource

class PlaySound(
    type: ActionType = ActionType.PLAYSOUND,
    click: List<ClickType> = listOf(ClickType.ANY),
    private val sound: SoundEvent? = null,
    private val volume: Float = 1.0F,
    private val pitch: Float = 1.0F
) : Action(type, click) {
    override fun executeAction(player: ServerPlayer) {
        Utils.printDebug("Attempting to execute a ${type.identifier} Action: $this")
        if (sound == null) {
            Utils.printError("There was an error while executing a Sound Action for player ${player.name}: Sound was somehow null?")
            return
        }
        player.playNotifySound(sound, SoundSource.MASTER, volume, pitch)
    }

    override fun toString(): String {
        return "PlaySound(type=$type, click=$click, sound=$sound, volume=$volume, pitch=$pitch)"
    }
}