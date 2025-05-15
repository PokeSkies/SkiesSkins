package com.pokeskies.skiesskins.gui

import ca.landonjw.gooeylibs2.api.UIManager
import ca.landonjw.gooeylibs2.api.button.GooeyButton
import ca.landonjw.gooeylibs2.api.page.PageAction
import ca.landonjw.gooeylibs2.api.tasks.Task
import ca.landonjw.gooeylibs2.api.template.Template
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.api.SkiesSkinsAPI
import com.pokeskies.skiesskins.config.ConfigManager
import com.pokeskies.skiesskins.config.gui.PurchaseConfirmGuiConfig
import com.pokeskies.skiesskins.config.shop.ShopConfig
import com.pokeskies.skiesskins.utils.RefreshableGUI
import com.pokeskies.skiesskins.utils.Utils
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemLore

class PurchaseConfirmGui(
    private val player: ServerPlayer,
    val shopId: String,
    val shopConfig: ShopConfig,
    val slotOptions: PurchaseConfirmGuiConfig.InfoButtons.Options,
    val displayStack: () -> ItemStack,
    val callback: (PurchaseConfirmGui) -> Unit
) : RefreshableGUI() {
    private val guiConfig = ConfigManager.PURCHASE_CONFIRM_GUI
    private val template: ChestTemplate = ChestTemplate.Builder(guiConfig.size)
        .build()

    private var forceClosed = false

    init {
        this.subscribe(this, Runnable { refresh() })
        SkiesSkins.INSTANCE.inventoryControllers[player.uuid] = this
        refresh()
    }

    override fun refresh() {
        try {
            val userData = SkiesSkinsAPI.getUserData(player)

            this.template.clear()

            // Load background items
            for ((id, item) in guiConfig.items) {
                val button = GooeyButton.builder()
                    .display(item.createItemStack(player))
                    .onClick { ctx ->
                        for (actionEntry in item.clickActions) {
                            val action = actionEntry.value
                            if (action.matchesClick(ctx.clickType)) {
                                action.executeAction(player)
                            }
                        }
                    }
                    .build();
                for (slot in item.slots) {
                    this.template.set(slot, button)
                }
            }

            // Create the center info item
            slotOptions.slots.forEach { slot ->
                this.template.set(slot, GooeyButton.builder()
                    .display(displayStack.invoke())
                    .build())
            }

            // Confirm button
            guiConfig.buttons.confirm.let { item ->
                val confirmButton = GooeyButton.builder()
                    .display(item.createItemStack(player))
                    .onClick { ctx ->
                        callback.invoke(this)
                    }

                item.name?.let {
                    confirmButton.with(DataComponents.ITEM_NAME, Utils.deserializeText(Utils.parsePlaceholders(player, it)))
                }
                if (item.lore.isNotEmpty()) {
                    confirmButton.with(DataComponents.LORE, ItemLore(guiConfig.buttons.confirm.lore.map { Utils.deserializeText(Utils.parsePlaceholders(player, it)) }))
                }
                item.slots.forEach { slot ->
                    this.template.set(slot, confirmButton.build())
                }
            }

            // Cancel button
            guiConfig.buttons.cancel.let { item ->
                val cancelButton = GooeyButton.builder()
                    .display(item.createItemStack(player))
                    .onClick { ctx ->
                        forceReturn()
                    }

                item.name?.let {
                    cancelButton.with(DataComponents.ITEM_NAME, Utils.deserializeText(Utils.parsePlaceholders(player, it)))
                }
                if (item.lore.isNotEmpty()) {
                    cancelButton.with(DataComponents.LORE, ItemLore(guiConfig.buttons.cancel.lore.map { Utils.deserializeText(Utils.parsePlaceholders(player, it)) }))
                }
                item.slots.forEach { slot ->
                    this.template.set(slot, cancelButton.build())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun forceReturn() {
        forceClosed = true
        UIManager.openUIForcefully(player, ShopGui(player, shopId, shopConfig))
    }

    override fun onClose(action: PageAction) {
        SkiesSkins.INSTANCE.inventoryControllers.remove(player.uuid, this)
        if (!forceClosed) {
            UIManager.openUIForcefully(player, ShopGui(player, shopId, shopConfig))
        }
    }

    override fun getTemplate(): Template {
        return template
    }

    override fun getTitle(): Component {
        return Utils.deserializeText(Utils.parsePlaceholders(player, guiConfig.title))
    }
}
