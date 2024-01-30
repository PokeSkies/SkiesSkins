package com.pokeskies.skiesskins.gui

import ca.landonjw.gooeylibs2.api.UIManager
import ca.landonjw.gooeylibs2.api.button.GooeyButton
import ca.landonjw.gooeylibs2.api.data.UpdateEmitter
import ca.landonjw.gooeylibs2.api.page.Page
import ca.landonjw.gooeylibs2.api.page.PageAction
import ca.landonjw.gooeylibs2.api.template.Template
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.pokemon.Pokemon
import com.pokeskies.skiesskins.api.SkiesSkinsAPI
import com.pokeskies.skiesskins.config.SkinConfig
import com.pokeskies.skiesskins.data.UserData
import com.pokeskies.skiesskins.utils.Utils
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class ApplyGui(
    private val player: ServerPlayer,
    val skinData: UserData.SkinData,
    val skin: SkinConfig
) : UpdateEmitter<Page?>(), Page {
    private val template: ChestTemplate = ChestTemplate.Builder(3)
        .fill(GooeyButton.builder()
            .display(
                Utils.hideFlags(
                    ItemStack(Items.GRAY_STAINED_GLASS_PANE),
                    ItemStack.TooltipPart.ADDITIONAL
                )
            )
            .title("")
            .build())
        .build()
    private val slots = intArrayOf(10, 11, 12, 14, 15, 16)

    init {
        refresh()
    }

    fun refresh() {
        val party: PlayerPartyStore = Cobblemon.storage.getParty(player.uuid)

        for (i in 0..5) {
            val pokemon: Pokemon? = party.get(i)
            template.set(slots[i], GooeyButton.builder()
                .display(
                    Utils.getOrRunOther(
                        pokemon,
                        { Utils.hideFlags(Utils.nullPokemonToItem(pokemon), ItemStack.TooltipPart.ADDITIONAL) },
                        { ItemStack(CobblemonItems.POKE_BALL) }
                    )
                )
                .title(
                    Utils.getOrOther(
                        pokemon,
                        Utils.deserializeText("<green>Slot " + (i + 1) + " - " + pokemon?.getDisplayName()?.string),
                        Utils.deserializeText("<red>Empty Slot")
                    )
                )
                .onClick { cons ->
                    val pokemon = Cobblemon.storage.getParty(player).get(i)
                    if (pokemon != null) {
                        println("!! ${pokemon.species} ${PokemonSpecies.getByIdentifier(skin.species)}")

                        // Check if correct species
                        val species = PokemonSpecies.getByIdentifier(skin.species)!!
                        if (pokemon.species != species) {
                            player.playNotifySound(SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.15F, 1.0F)
                            player.sendMessage(Component.literal("This skin can only be applied on ${species.name}!")
                                .withStyle { it.withColor(ChatFormatting.RED) })
                            return@onClick
                        }

                        // Check if the Pokemon contains ALL required aspects
                        if (skin.aspects.required.isNotEmpty() &&
                            skin.aspects.required.stream().noneMatch { pokemon.aspects.contains(it) }) {
                            player.playNotifySound(SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.15F, 1.0F)
                            player.sendMessage(Component.literal("This skin requires aspects that are not applied to this Pokemon!")
                                .withStyle { it.withColor(ChatFormatting.RED) })
                        }

                        // Check if the Pokemon contains ANY blacklisted aspects
                        if (skin.aspects.blacklist.isNotEmpty() &&
                            skin.aspects.blacklist.stream().anyMatch { pokemon.aspects.contains(it) }) {
                            player.playNotifySound(SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.15F, 1.0F)
                            player.sendMessage(Component.literal("This Pokemon contains aspects that are blacklisted!")
                                .withStyle { it.withColor(ChatFormatting.RED) })
                        }

                        val user = SkiesSkinsAPI.getUserData(player)
                        val removed = user.inventory.remove(skinData)

                        if (!removed) {
                            player.playNotifySound(SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.15F, 1.0F)
                            player.sendMessage(Component.literal("There was an error while applying this skin!")
                                .withStyle { it.withColor(ChatFormatting.RED) })
                            UIManager.closeUI(player)
                            return@onClick
                        }

                        for (aspect in skin.aspects.apply) {
                            PokemonProperties.parse(aspect).apply(pokemon)
                        }

                        player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.15F, 1.0F)
                        player.sendMessage(Component.literal("Successfully applied the skin!")
                            .withStyle { it.withColor(ChatFormatting.GREEN) })
                        UIManager.closeUI(player)
                    }
                }
                .build()
            )
        }
    }

    override fun getTemplate(): Template {
        return template
    }

    override fun getTitle(): Component {
        return Utils.deserializeText("Applying ${skin.name}")
    }

    override fun onClose(action: PageAction) {

    }
}