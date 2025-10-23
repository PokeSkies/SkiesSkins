package com.pokeskies.skiesskins.gui

import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.config.ConfigManager
import com.pokeskies.skiesskins.config.gui.PurchaseConfirmGuiConfig
import com.pokeskies.skiesskins.config.shop.ShopConfig
import com.pokeskies.skiesskins.utils.IRefreshableGui
import com.pokeskies.skiesskins.utils.Utils
import com.pokeskies.skiesskins.utils.clear
import com.pokeskies.skiesskins.utils.setSlots
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

class PurchaseConfirmGui(
    player: ServerPlayer,
    val shopId: String,
    val shopConfig: ShopConfig,
    val slotOptions: PurchaseConfirmGuiConfig.InfoButtons.Options,
    val displayStack: () -> ItemStack,
    val callback: (PurchaseConfirmGui) -> Unit
) : IRefreshableGui(
    ConfigManager.PURCHASE_CONFIRM_GUI.type.type, player, false
) {
    private val guiConfig = ConfigManager.PURCHASE_CONFIRM_GUI

    private var forceClosed = false

    init {
        SkiesSkins.INSTANCE.inventoryInstances[player.uuid] = this
        refresh()
    }

    override fun refresh() {
        try {
            clear()

            // Load background items
            for ((_, item) in guiConfig.items) {
                setSlots(item.slots, GuiElementBuilder.from(item.createItemStack(player))
                    .setCallback { type ->
                        for (actionEntry in item.clickActions) {
                            val action = actionEntry.value
                            if (action.matchesClick(type)) {
                                action.executeAction(player, this)
                            }
                        }
                    }
                )
            }

            // Create the center info item
            setSlots(slotOptions.slots, GuiElementBuilder.from(displayStack.invoke()))

            // Confirm button
            guiConfig.buttons.confirm.let { item ->
                val confirmButton = GuiElementBuilder
                    .from(item.createItemStack(player))
                    .setCallback { type ->
                        callback.invoke(this)
                    }

                item.name?.let {
                    confirmButton.setName(Utils.deserializeText(Utils.parsePlaceholders(player, it)))
                }
                if (item.lore.isNotEmpty()) {
                    confirmButton.setLore(guiConfig.buttons.confirm.lore.map { Utils.deserializeText(Utils.parsePlaceholders(player, it)) })
                }
                setSlots(item.slots, confirmButton)
            }

            // Cancel button
            guiConfig.buttons.cancel.let { item ->
                val cancelButton = GuiElementBuilder
                    .from(item.createItemStack(player))
                    .setCallback { type ->
                        forceReturn()
                    }

                item.name?.let {
                    cancelButton.setName(Utils.deserializeText(Utils.parsePlaceholders(player, it)))
                }
                if (item.lore.isNotEmpty()) {
                    cancelButton.setLore(guiConfig.buttons.cancel.lore.map { Utils.deserializeText(Utils.parsePlaceholders(player, it)) })
                }
                setSlots(item.slots, cancelButton)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun forceReturn() {
        forceClosed = true
        ShopGui(player, shopId, shopConfig).open()
    }

    override fun onClose() {
        SkiesSkins.INSTANCE.inventoryInstances.remove(player.uuid, this)
        if (!forceClosed) {
            forceReturn()
        }
    }

    override fun getTitle(): Component {
        return Utils.deserializeText(Utils.parsePlaceholders(player, guiConfig.title))
    }
}
