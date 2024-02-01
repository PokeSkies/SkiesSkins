package com.pokeskies.skiesskins.config.gui

import com.google.gson.annotations.SerializedName

class RemoverConfig(
    val title: String = "Remove Skin",
    val size: Int = 6,
    @SerializedName("party_slots")
    val partySlots: Map<Int, PartySlotOptions> = emptyMap(),
    val items: Map<String, GenericGuiItem> = emptyMap()
) {
    class PartySlotOptions(
        val filled: PokemonGuiItem = PokemonGuiItem(),
        val empty: PokemonGuiItem = PokemonGuiItem(),
    ) {
        override fun toString(): String {
            return "PartySlotOptions(filled=$filled, empty=$empty)"
        }
    }

    override fun toString(): String {
        return "RemoverConfig(title='$title', size=$size, partySlots=$partySlots, items=$items)"
    }
}