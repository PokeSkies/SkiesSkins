package com.pokeskies.skiesskins.commands.subcommands

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.command.argument.PokemonPropertiesArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.api.SkiesSkinsAPI
import com.pokeskies.skiesskins.api.SkinApplyReturn
import com.pokeskies.skiesskins.config.ConfigManager
import com.pokeskies.skiesskins.utils.SubCommand
import com.pokeskies.skiesskins.utils.Utils
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

class GivePokemonCommand : SubCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("givepokemon")
            .requires(Permissions.require("skiesskins.command.givepokemon", 2))
            .then(Commands.argument("targets", EntityArgument.players())
                .then(Commands.argument("skin", StringArgumentType.string())
                    .suggests { _, builder ->
                        SharedSuggestionProvider.suggest(ConfigManager.SKINS.keys.stream(), builder)
                    }
                    .then(Commands.argument("properties", PokemonPropertiesArgumentType.properties())
                        .executes { ctx ->
                            give(
                                ctx,
                                EntityArgument.getPlayers(ctx, "targets"),
                                StringArgumentType.getString(ctx, "skin"),
                                PokemonPropertiesArgumentType.getPokemonProperties(ctx, "properties")
                            )
                        }
                    )
                    .executes { ctx ->
                        give(
                            ctx,
                            EntityArgument.getPlayers(ctx, "targets"),
                            StringArgumentType.getString(ctx, "skin")
                        )
                    }
                )
            )
            .build()
    }

    companion object {
        private fun give(
            ctx: CommandContext<CommandSourceStack>,
            players: Collection<ServerPlayer>,
            skinId: String,
            properties: PokemonProperties? = null
        ): Int {
            if (SkiesSkins.INSTANCE.storage == null) {
                ctx.source.sendMessage(
                    Component.literal("The storage system is not available at the moment. Please try again later.")
                        .withStyle { it.withColor(ChatFormatting.RED) }
                )
                return 1
            }

            val skin = ConfigManager.SKINS[skinId]
            if (skin == null) {
                ctx.source.sendMessage(
                    Component.literal("Cannot find a skin entry from the ID: $skinId")
                        .withStyle { it.withColor(ChatFormatting.RED) }
                )
                return 1
            }

            val pokemon = PokemonSpecies.getByIdentifier(skin.species)?.create(1) ?: run {
                ctx.source.sendMessage(
                    Component.literal("Cannot give skin $skinId because its species ${skin.species} is invalid.")
                        .withStyle { it.withColor(ChatFormatting.RED) }
                )
                return 1
            }

            properties?.apply(pokemon)

            when (SkiesSkinsAPI.applySkin(pokemon, skin)) {
                SkinApplyReturn.INVALID_SPECIES -> { // Should not be possible, but just in case
                    val species = PokemonSpecies.getByIdentifier(skin.species)
                    ctx.source.sendMessage(Component.literal("This skin can only be applied on ${species?.name}!")
                        .withStyle { it.withColor(ChatFormatting.RED) })
                    return 1
                }
                SkinApplyReturn.ALREADY_HAS_SKIN -> {
                    ctx.source.sendMessage(Utils.deserializeText("<red>This Pokemon already has the skin ${skin.name}<reset> <red>applied!"))
                    return 1
                }
                SkinApplyReturn.MISSING_ASPECTS -> {
                    ctx.source.sendMessage(Component.literal("This skin requires aspects that are not applied to this Pokemon!")
                        .withStyle { it.withColor(ChatFormatting.RED) })
                    return 1
                }
                SkinApplyReturn.BLACKLISTED_ASPECTS -> {
                    ctx.source.sendMessage(Component.literal("This Pokemon contains aspects that are blacklisted!")
                        .withStyle { it.withColor(ChatFormatting.RED) })
                    return 1
                }
                SkinApplyReturn.SUCCESS -> {
                    if (players.size == 1) {
                        val player = players.first()
                        Cobblemon.storage.getParty(player).add(pokemon.clone())
                        ctx.source.sendMessage(
                            Component.empty()
                                .append(
                                    Component.literal("You gave a Pokemon with the skin ").withStyle { it.withColor(ChatFormatting.GREEN) }
                                )
                                .append(Utils.deserializeText(skin.name))
                                .append(
                                    Component.literal(" to ${player.name.string}").withStyle { it.withColor(ChatFormatting.GREEN) }
                                )
                        )
                    } else {
                        var given = 0
                        for (player in players) {
                            Cobblemon.storage.getParty(player).add(pokemon.clone())
                            given++
                        }
                        ctx.source.sendMessage(
                            Component.empty()
                                .append(
                                    Component.literal("You gave a Pokemon with the skin ").withStyle { it.withColor(ChatFormatting.GREEN) }
                                )
                                .append(Utils.deserializeText(skin.name))
                                .append(
                                    Component.literal(" to $given players").withStyle { it.withColor(ChatFormatting.GREEN) }
                                )
                        )
                    }
                }
            }
            return 1
        }
    }
}
