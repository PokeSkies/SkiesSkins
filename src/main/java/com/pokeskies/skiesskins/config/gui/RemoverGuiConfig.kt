package com.pokeskies.skiesskins.config.gui

import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesskins.config.gui.items.GenericItem
import com.pokeskies.skiesskins.config.gui.items.PokemonItem
import com.pokeskies.skiesskins.gui.InventoryType

/*
 * Config options for the skin remover GUI.
 */
class RemoverGuiConfig(
    val title: String = "Remove Skin",
    val type: InventoryType = InventoryType.GENERIC_9x3,
    @SerializedName("party_slots")
    val partySlots: Map<Int, PartySlotOptions> = emptyMap(),
    val items: Map<String, GenericItem> = emptyMap()
) {
    /*
     * GUI options that represents a slot in your party.
     */
    class PartySlotOptions(
        val filled: PokemonItem = PokemonItem(),
        val empty: PokemonItem = PokemonItem(),
    ) {
        override fun toString(): String {
            return "PartySlotOptions(filled=$filled, empty=$empty)"
        }
    }

    override fun toString(): String {
        return "RemoverConfig(title='$title', type=$type, partySlots=$partySlots, items=$items)"
    }
}
