package com.pokeskies.skiesskins.config.gui

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesskins.api.shop.ShopEntry
import com.pokeskies.skiesskins.config.gui.items.GenericItem
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiesskins.utils.Utils.deserializeText
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore

/*
 * Config options for the GUI that appears when purchasing something from the Shop.
 */
class PurchaseConfirmGuiConfig(
    val title: String = "Confirm Purchase",
    val size: Int = 3,
    val buttons: Buttons = Buttons(),
    val items: Map<String, GenericItem> = emptyMap()
) {
    /*
     * List of buttons that appear in the confirmation GUI.
     */
    class Buttons(
        val info: InfoButtons = InfoButtons(),
        val confirm: GenericItem = GenericItem(),
        val cancel: GenericItem = GenericItem(),
    ) {
        override fun toString(): String {
            return "Buttons(info=$info, confirm=$confirm, cancel=$cancel)"
        }
    }

    /*
     * Options specifically for the information buttons. One set of options for each of the shop entry types.
     */
    class InfoButtons(
        @SerializedName("random")
        val randomInfo: Options = Options(),
        @SerializedName("static")
        val staticInfo: Options = Options(),
        @SerializedName("package")
        val packageInfo: Options = Options(),
    ) {
        /*
         * Config options for the information button.
         */
        class Options(
            val item: Item = Items.BARRIER,
            @JsonAdapter(FlexibleListAdaptorFactory::class)
            val slots: List<Int> = emptyList(),
            val name: String? = null,
            @JsonAdapter(FlexibleListAdaptorFactory::class)
            val lore: List<String> = emptyList()
        ) {
            fun createItemStack(
                player: ServerPlayer,
                entry: ShopEntry
            ): ItemStack {
                val stack = ItemStack(item, 1)

                val dataComponents = DataComponentPatch.builder()

                if (name != null) {
                    dataComponents.set(
                        DataComponents.ITEM_NAME, Component.empty().setStyle(Style.EMPTY.withItalic(false))
                            .append(deserializeText(entry.parse(name, player))))
                }

                if (lore.isNotEmpty()) {
                    val parsedLore = entry.parse(lore, player)
                    dataComponents.set(DataComponents.LORE, ItemLore(parsedLore.stream().map {
                        Component.empty().setStyle(Style.EMPTY.withItalic(false)).append(deserializeText(it)) as Component
                    }.toList()))
                }

                stack.applyComponents(dataComponents.build())

                return entry.modifyStack(stack, player)
            }

            override fun toString(): String {
                return "SkinSlotOptions(item=$item, slots=$slots, name='$name', lore=$lore)"
            }
        }

        override fun toString(): String {
            return "InfoButtons(randomInfo=$randomInfo, staticInfo=$staticInfo, packageInfo=$packageInfo)"
        }
    }

    override fun toString(): String {
        return "PurchaseConfirmGuiConfig(title='$title', size=$size, buttons=$buttons, items=$items)"
    }
}
