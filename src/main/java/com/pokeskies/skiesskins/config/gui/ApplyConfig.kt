package com.pokeskies.skiesskins.config.gui

import com.google.gson.annotations.SerializedName

class ApplyConfig(
    val title: String = "Apply Skin %skin_name%",
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
        return "ApplyConfig(title='$title', size=$size, partySlots=$partySlots, items=$items)"
    }
}