package com.pokeskies.skiesskins.config

import com.cobblemon.mod.common.Cobblemon
import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiesskins.utils.FlexibleListAdaptorFactory
import net.minecraft.resources.ResourceLocation

class SkinConfig(
    val enabled: Boolean = true,
    val species: ResourceLocation = ResourceLocation.withDefaultNamespace(""),
    val name: String = "",
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val description: List<String> = emptyList(),
    val aspects: Aspects = Aspects(),
    val scrapping: ScrappingOptions? = null,
) {
    class Aspects(
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val apply: List<String> = emptyList(),
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val required: List<String> = emptyList(),
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val blacklist: List<String> = emptyList(),
        @JsonAdapter(FlexibleListAdaptorFactory::class)
        val remove: List<String> = emptyList(),
    ) {
        override fun toString(): String {
            return "Aspects(apply=$apply, required=$required, blacklist=$blacklist, remove=$remove)"
        }
    }

    override fun toString(): String {
        return "SkinConfig(enabled=$enabled, species=$species, name='$name', description=$description, aspects=$aspects, scrapping=$scrapping)"
    }
}
