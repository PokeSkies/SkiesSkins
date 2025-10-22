package com.pokeskies.skiesskins.utils

import com.pokeskies.skiesskins.SkiesSkins
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ResolvableProfile
import kotlin.math.max
import kotlin.math.min

object MenuUtils {
    fun getSlot(y: Int, x: Int): Int {
        return y * 9 + x
    }

    fun getPlayerHead(player: ServerPlayer): ItemStack {
        val itemStack = ItemStack(Items.PLAYER_HEAD, 1)
        val gameProfile = SkiesSkins.INSTANCE.server.profileCache?.get(player.uuid)
        if (gameProfile != null && gameProfile.isPresent) {
            itemStack.applyComponents(DataComponentPatch.builder()
                .set(DataComponents.PROFILE, ResolvableProfile(gameProfile.get()))
                .build())
        }
        return itemStack
    }
}

fun SimpleGui.setSlot(row: Int, column: Int, element: GuiElementBuilderInterface<*>?) {
    this.setSlot(row * width + column, element)
}

fun SimpleGui.setSlots(slots: List<Int>?, element: GuiElementBuilderInterface<*>?) {
    if (slots == null) return
    slots.forEach { this.setSlot(it, element) }
}

fun SimpleGui.rectangle(startRow: Int, startCol: Int, length: Int, width: Int, button: GuiElementBuilderInterface<*>?) {
    val startRow = max(0.0, startRow.toDouble()).toInt()
    val startCol = max(0.0, startCol.toDouble()).toInt()
    val endRow = height.coerceAtMost(startRow + length)
    val endCol = min(this.width.toDouble(), (startCol + width).toDouble()).toInt()

    for (row in startRow until endRow) {
        for (col in startCol until endCol) {
            setSlot(row, col, button)
        }
    }
}

fun SimpleGui.rectangleFromList(
    startRow: Int,
    startCol: Int,
    length: Int,
    width: Int,
    buttons: List<GuiElementBuilderInterface<*>?>?,
    empty: GuiElementBuilderInterface<*>? = null
) {
    val startRow = max(0.0, startRow.toDouble()).toInt()
    val startCol = max(0.0, startCol.toDouble()).toInt()
    val iterator: Iterator<GuiElementBuilderInterface<*>?> = buttons?.iterator() ?: return
    val endRow = height.coerceAtMost(startRow + length)
    val endCol = min(this.width.toDouble(), (startCol + width).toDouble()).toInt()

    for (row in startRow until endRow) {
        for (col in startCol until endCol) {
            if (iterator.hasNext()) {
                val button = iterator.next() ?: continue
                setSlot(row, col, button)
            } else if (empty != null) {
                setSlot(row, col, empty)
            }
        }
    }
}

fun SimpleGui.border(startRow: Int, startCol: Int, length: Int, width: Int, button: GuiElementBuilderInterface<*>) {
    val startRow = max(0.0, startRow.toDouble()).toInt()
    val startCol = max(0.0, startCol.toDouble()).toInt()
    val endRow = height.coerceAtMost(startRow + length)
    val endCol = min(this.width.toDouble(), (startCol + width).toDouble()).toInt()

    for (row in startRow until endRow) {
        setSlot(row, startCol, button)
        setSlot(row, endCol - 1, button)
    }
    for (col in startCol until endCol) {
        setSlot(startRow, col, button)
        setSlot(endRow - 1, col, button)
    }
}

fun SimpleGui.fill(button: GuiElementBuilderInterface<*>) {
    for (i in 0 until size) {
        if (getSlot(i) == null) {
            setSlot(i, button)
        }
    }
}

fun SimpleGui.clear(includePlayer: Boolean = false) {
    for (i in 0 until if (includePlayer) size else virtualSize) {
        clearSlot(i)
    }
}

fun GuiElementBuilder.appendLore(list: List<Component>): GuiElementBuilder {
    list.forEach { this.addLoreLine(it) }
    return this
}