package com.pokeskies.skiesskins.config.gui

import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesskins.config.gui.items.GenericItem
import com.pokeskies.skiesskins.config.gui.items.PokemonItem

/*
 * Config options for the skin remover GUI.
 */
class RemoverGuiConfig(
    val title: String = "Remove Skin",
    val size: Int = 6,
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
        return "RemoverConfig(title='$title', size=$size, partySlots=$partySlots, items=$items)"
    }
}
