package com.pokeskies.skiesskins.commands.subcommands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.api.SkiesSkinsAPI
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

class GiveTokenCommand : SubCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("givetoken")
            .requires(Permissions.require("skiesskins.command.givetoken", 2))
            .then(Commands.argument("targets", EntityArgument.players())
                .then(Commands.argument("skin", StringArgumentType.string())
                    .suggests { _, builder ->
                        SharedSuggestionProvider.suggest(ConfigManager.SKINS.keys.stream(), builder)
                    }
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                        .executes { ctx ->
                            give(
                                ctx,
                                EntityArgument.getPlayers(ctx, "targets"),
                                StringArgumentType.getString(ctx, "skin"),
                                IntegerArgumentType.getInteger(ctx, "amount")
                            )
                        }
                    )
                    .executes { ctx ->
                        give(
                            ctx,
                            EntityArgument.getPlayers(ctx, "targets"),
                            StringArgumentType.getString(ctx, "skin"),
                            1
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
            amount: Int
        ): Int {
            if (SkiesSkins.INSTANCE.storage == null) {
                ctx.source.sendMessage(
                    Component.literal("The storage system is not available at the moment. Please try again later.")
                        .withStyle { it.withColor(ChatFormatting.RED) }
                )
                return 1
            }

            val skin = SkiesSkinsAPI.getSkin(skinId)
            if (skin == null) {
                ctx.source.sendMessage(
                    Component.literal("Cannot find a skin entry from the ID: $skinId")
                        .withStyle { it.withColor(ChatFormatting.RED) }
                )
                return 1
            }

            if (!SkiesSkinsAPI.canTokenize(skin)) {
                ctx.source.sendMessage(
                    Component.literal("The skin ")
                        .append(Utils.deserializeText(skin.name))
                        .append(Component.literal(" cannot be tokenized."))
                        .withStyle { it.withColor(ChatFormatting.RED) }
                )
                return 1
            }

            val token = SkiesSkinsAPI.tokenizeSkin(skin) ?: run {
                ctx.source.sendMessage(
                    Component.literal("Failed to create a token for skin ")
                        .append(Utils.deserializeText(skin.name))
                        .append(Component.literal(". Ensure the token settings are configured correctly.").withStyle { it.withColor(ChatFormatting.RED) })
                )
                return 1
            }

            if (players.size == 1) {
                val player = players.first()
                player.inventory.placeItemBackInInventory(token.copyWithCount(amount))
                ctx.source.sendMessage(
                    Component.empty()
                        .append(
                            Component.literal("You gave $amount ").withStyle { it.withColor(ChatFormatting.GREEN) }
                        )
                        .append(Utils.deserializeText(skin.name))
                        .append(
                            Component.literal(" token(s) to ${player.name.string}").withStyle { it.withColor(ChatFormatting.GREEN) }
                        )
                )
            } else {
                var given = 0
                for (player in players) {
                    player.inventory.placeItemBackInInventory(token.copyWithCount(amount))
                    given++
                }
                ctx.source.sendMessage(
                    Component.empty()
                        .append(
                            Component.literal("You gave $amount ").withStyle { it.withColor(ChatFormatting.GREEN) }
                        )
                        .append(Utils.deserializeText(skin.name))
                        .append(
                            Component.literal(" token(s) to $given players").withStyle { it.withColor(ChatFormatting.GREEN) }
                        )
                )
            }
            return 1
        }
    }
}
