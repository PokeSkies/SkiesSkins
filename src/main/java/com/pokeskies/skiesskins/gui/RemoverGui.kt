package com.pokeskies.skiesskins.gui

import ca.landonjw.gooeylibs2.api.UIManager
import ca.landonjw.gooeylibs2.api.button.GooeyButton
import ca.landonjw.gooeylibs2.api.data.UpdateEmitter
import ca.landonjw.gooeylibs2.api.page.Page
import ca.landonjw.gooeylibs2.api.template.Template
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.pokemon.Pokemon
import com.pokeskies.skiesskins.api.SkiesSkinsAPI
import com.pokeskies.skiesskins.config.ConfigManager
import com.pokeskies.skiesskins.data.UserData
import com.pokeskies.skiesskins.utils.Utils
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource

class RemoverGui(
    private val player: ServerPlayer
) : UpdateEmitter<Page?>(), Page {
    private val template: ChestTemplate = ChestTemplate.Builder(ConfigManager.REMOVER_GUI.size)
        .build()

    init {
        refresh()
    }

    fun refresh() {
        for ((id, item) in ConfigManager.REMOVER_GUI.items) {
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

        val party: PlayerPartyStore = Cobblemon.storage.getParty(player.uuid)
        for (i in 0..5) {
            val slotItem = ConfigManager.REMOVER_GUI.partySlots[i + 1] ?: continue
            val partyPokemon: Pokemon? = party.get(i)
            val button = GooeyButton.builder()
                .display(
                    Utils.getOrRunOther(
                        partyPokemon,
                        { slotItem.filled.createItemStack(player, partyPokemon) },
                        { slotItem.empty.createItemStack(player, partyPokemon) }
                    )
                )
                .onClick { cons ->
                    val pokemon = Cobblemon.storage.getParty(player).get(i)
                    if (pokemon != null) {
                        val skinEntry = SkiesSkinsAPI.getPokemonSkin(pokemon)
                        if (skinEntry == null) {
                            player.playNotifySound(SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.15F, 1.0F)
                            player.sendMessage(Utils.deserializeText("<red>This Pokemon does not have any skin applied!"))
                            return@onClick
                        }

                        val user = SkiesSkinsAPI.getUserData(player)
                        val result = user.inventory.add(UserData.SkinData(skinEntry.first))

                        if (!result || !SkiesSkinsAPI.saveUserData(player, user)) {
                            player.playNotifySound(SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.15F, 1.0F)
                            player.sendMessage(Component.literal("There was an error while removing this skin!")
                                .withStyle { it.withColor(ChatFormatting.RED) })
                            UIManager.closeUI(player)
                            return@onClick
                        }

                        for (aspect in skinEntry.second.aspects.remove) {
                            PokemonProperties.parse(aspect).apply(pokemon)
                        }
                        pokemon.persistentData.remove(SkiesSkinsAPI.TAG_SKIN_DATA)
                        if (ConfigManager.CONFIG.untradable) pokemon.tradeable = true

                        player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.15F, 1.0F)
                        player.sendMessage(Component.literal("Successfully removed the skin!")
                            .withStyle { it.withColor(ChatFormatting.GREEN) })
                        UIManager.closeUI(player)
                    }
                }
                .build()

            for (slot in (if (partyPokemon != null) slotItem.filled else slotItem.empty).slots) {
                template.set(slot, button)
            }
        }
    }

    override fun getTemplate(): Template {
        return template
    }

    override fun getTitle(): Component {
        return Utils.deserializeText(Utils.parsePlaceholders(player, ConfigManager.REMOVER_GUI.title))
    }
}