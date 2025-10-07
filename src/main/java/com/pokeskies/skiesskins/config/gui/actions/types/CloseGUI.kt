package com.pokeskies.skiesskins.config.gui.actions.types

import com.pokeskies.skiesskins.config.gui.actions.Action
import com.pokeskies.skiesskins.config.gui.actions.ActionType
import com.pokeskies.skiesskins.gui.GenericClickType
import com.pokeskies.skiesskins.utils.IRefreshableGui
import com.pokeskies.skiesskins.utils.Utils
import net.minecraft.server.level.ServerPlayer

class CloseGUI(
    type: ActionType = ActionType.CLOSE_GUI,
    click: List<GenericClickType> = listOf(GenericClickType.ANY)
) : Action(type, click) {
    override fun executeAction(player: ServerPlayer, gui: IRefreshableGui) {
        Utils.printDebug("Attempting to execute a ${type.identifier} Action: $this")
        gui.close()
    }
    override fun toString(): String {
        return "CloseGUI(type=$type, click=$click)"
    }
}