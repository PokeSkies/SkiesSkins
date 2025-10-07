package com.pokeskies.skiesskins.config.gui.actions.types

import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiesskins.config.gui.actions.Action
import com.pokeskies.skiesskins.config.gui.actions.ActionType
import com.pokeskies.skiesskins.gui.GenericClickType
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiesskins.utils.IRefreshableGui
import com.pokeskies.skiesskins.utils.Utils
import net.minecraft.server.level.ServerPlayer

class MessagePlayer(
    type: ActionType = ActionType.MESSAGE,
    click: List<GenericClickType> = listOf(GenericClickType.ANY),
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    private val message: List<String> = emptyList()
) : Action(type, click) {
    override fun executeAction(player: ServerPlayer, gui: IRefreshableGui) {
        Utils.printDebug("Attempting to execute a ${type.identifier} Action: $this")
        for (line in message) {
            player.sendMessage(Utils.deserializeText(Utils.parsePlaceholders(player, line)))
        }
    }

    override fun toString(): String {
        return "MessagePlayer(type=$type, click=$click, message=$message)"
    }
}