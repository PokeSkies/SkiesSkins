package com.pokeskies.skiesskins.config.gui.actions

import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiesskins.gui.GenericClickType
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiesskins.utils.IRefreshableGui
import eu.pb4.sgui.api.ClickType
import net.minecraft.server.level.ServerPlayer

abstract class Action(
    val type: ActionType,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val click: List<GenericClickType> = listOf(GenericClickType.ANY)
) {
    abstract fun executeAction(player: ServerPlayer, gui: IRefreshableGui)

    fun matchesClick(buttonClick: ClickType): Boolean {
        return click.any { it.buttonClicks.contains(buttonClick) }
    }

    override fun toString(): String {
        return "Action(type=$type, click=$click)"
    }
}