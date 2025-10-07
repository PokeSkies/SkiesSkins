package com.pokeskies.skiesskins.config.gui.actions.types

import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.config.gui.actions.Action
import com.pokeskies.skiesskins.config.gui.actions.ActionType
import com.pokeskies.skiesskins.gui.GenericClickType
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiesskins.utils.IRefreshableGui
import com.pokeskies.skiesskins.utils.Utils
import net.minecraft.server.level.ServerPlayer

class MessageBroadcast(
    type: ActionType = ActionType.BROADCAST,
    click: List<GenericClickType> = listOf(GenericClickType.ANY),
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    private val message: List<String> = emptyList()
) : Action(type, click) {
    override fun executeAction(player: ServerPlayer, gui: IRefreshableGui) {
        Utils.printDebug("Attempting to execute a ${type.identifier} Action: $this")
        if (SkiesSkins.INSTANCE.adventure == null) {
            Utils.printError("There was an error while executing an action for player ${player.name}: Adventure was somehow null on message broadcast?")
            return
        }

        for (line in message) {
            SkiesSkins.INSTANCE.adventure!!.all().sendMessage(Utils.deserializeText(Utils.parsePlaceholders(player, line)))
        }
    }

    override fun toString(): String {
        return "MessageBroadcast(type=$type, click=$click, message=$message)"
    }
}