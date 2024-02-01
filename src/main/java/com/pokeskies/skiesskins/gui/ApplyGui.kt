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
import com.pokeskies.skiesskins.config.ConfigManager
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
    private val template: ChestTemplate = ChestTemplate.Builder(ConfigManager.APPLY_GUI.size)
        .build()

    private var shouldClose = false

    init {
        refresh()
    }

    fun refresh() {
        for ((id, item) in ConfigManager.APPLY_GUI.items) {
            val button = GooeyButton.builder()
                .display(item.createItemStack(player))
                .build();
            for (slot in item.slots) {
                this.template.set(slot, button)
            }
        }

        val party: PlayerPartyStore = Cobblemon.storage.getParty(player.uuid)
        for (i in 0..5) {
            val slotItem = ConfigManager.APPLY_GUI.partySlots[i + 1] ?: continue
            val pokemon: Pokemon? = party.get(i)
            val button = GooeyButton.builder()
                .display(
                    Utils.getOrRunOther(
                        pokemon,
                        { slotItem.filled.createItemStack(player, pokemon) },
                        { slotItem.empty.createItemStack(player, pokemon) }
                    )
                )
                .onClick { cons ->
                    val pokemon = Cobblemon.storage.getParty(player).get(i)
                    if (pokemon != null) {
                        SkiesSkinsAPI.getPokemonSkin(pokemon)?.let {
                            player.playNotifySound(SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.15F, 1.0F)
                            player.sendMessage(Utils.deserializeText("<red>This Pokemon already has the skin ${it.name}<reset> <red>applied!"))
                            return@onClick
                        }

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

                        if (!removed || !SkiesSkinsAPI.saveUserData(player, user)) {
                            player.playNotifySound(SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.15F, 1.0F)
                            player.sendMessage(Component.literal("There was an error while applying this skin!")
                                .withStyle { it.withColor(ChatFormatting.RED) })
                            shouldClose = true
                            UIManager.closeUI(player)
                            return@onClick
                        }

                        for (aspect in skin.aspects.apply) {
                            PokemonProperties.parse(aspect).apply(pokemon)
                        }

                        pokemon.persistentData.putString(SkiesSkinsAPI.TAG_SKIN_DATA, skinData.id)

                        player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.15F, 1.0F)
                        player.sendMessage(Component.literal("Successfully applied the skin!")
                            .withStyle { it.withColor(ChatFormatting.GREEN) })
                        shouldClose = true
                        UIManager.closeUI(player)
                    }
                }
                .build()

            for (slot in (if (pokemon != null) slotItem.filled else slotItem.empty).slots) {
                template.set(slot, button)
            }
        }
    }

    override fun getTemplate(): Template {
        return template
    }

    override fun getTitle(): Component {
        return Utils.parseSkinString(ConfigManager.APPLY_GUI.title, player, skin)
    }

    override fun onClose(action: PageAction) {
        if (!shouldClose) {
            SkiesSkinsAPI.openSkinInventory(player)
        }
    }
}