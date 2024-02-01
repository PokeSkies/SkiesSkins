package com.pokeskies.skiesskins.config.gui.actions.types

import ca.landonjw.gooeylibs2.api.UIManager
import com.pokeskies.skiesskins.config.gui.actions.Action
import com.pokeskies.skiesskins.config.gui.actions.ActionType
import com.pokeskies.skiesskins.config.gui.actions.ClickType
import com.pokeskies.skiesskins.utils.Utils
import net.minecraft.server.level.ServerPlayer

class CloseGUI(
    type: ActionType = ActionType.CLOSE_GUI,
    click: List<ClickType> = listOf(ClickType.ANY)
) : Action(type, click) {
    override fun executeAction(player: ServerPlayer) {
        Utils.printDebug("Attempting to execute a ${type.identifier} Action: $this")
        UIManager.closeUI(player)
    }
    override fun toString(): String {
        return "CloseGUI(type=$type, click=$click)"
    }
}