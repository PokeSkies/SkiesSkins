package com.pokeskies.skiesskins.config

import net.minecraft.resources.ResourceLocation

class SkinConfig(
    val enabled: Boolean = true,
    val name: String = "",
    val species: ResourceLocation = ResourceLocation(""),
    val aspects: Aspects = Aspects(),
) {
    class Aspects(
        val apply: List<String> = emptyList(),
        val required: List<String> = emptyList(),
        val blacklist: List<String> = emptyList()
    ) {
        override fun toString(): String {
            return "Aspects(apply=$apply, required=$required, blacklist=$blacklist)"
        }
    }

    override fun toString(): String {
        return "SkinEntry(enabled=$enabled, name='$name', species=$species, aspects=$aspects)"
    }
}