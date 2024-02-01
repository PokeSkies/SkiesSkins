package com.pokeskies.skiesskins.config.gui.actions.types

import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.config.gui.actions.Action
import com.pokeskies.skiesskins.config.gui.actions.ActionType
import com.pokeskies.skiesskins.config.gui.actions.ClickType
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiesskins.utils.Utils
import net.minecraft.server.level.ServerPlayer

class CommandPlayer(
    type: ActionType = ActionType.COMMAND_PLAYER,
    click: List<ClickType> = listOf(ClickType.ANY),
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    private val commands: List<String> = emptyList()
) : Action(type, click) {
    override fun executeAction(player: ServerPlayer) {
        Utils.printDebug("Attempting to execute a ${type.identifier} Action: $this")
        if (SkiesSkins.INSTANCE.server?.commands == null) {
            Utils.printError("There was an error while executing an action for player ${player.name}: Server was somehow null on command execution?")
            return
        }

        for (command in commands) {
            SkiesSkins.INSTANCE.server?.commands?.performPrefixedCommand(
                player.createCommandSourceStack(),
                Utils.parsePlaceholders(player, command)
            )
        }
    }

    override fun toString(): String {
        return "CommandPlayer(type=$type, click=$click, commands=$commands)"
    }
}