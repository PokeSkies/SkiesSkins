package com.pokeskies.skiesskins.config.gui.actions

import ca.landonjw.gooeylibs2.api.button.ButtonClick
import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory
import net.minecraft.server.level.ServerPlayer

abstract class Action(
    val type: ActionType,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val click: List<ClickType> = listOf(ClickType.ANY)
) {
    abstract fun executeAction(player: ServerPlayer)

    fun matchesClick(buttonClick: ButtonClick): Boolean {
        return click.any { it.buttonClicks.contains(buttonClick) }
    }

    override fun toString(): String {
        return "Action(type=$type, click=$click)"
    }
}