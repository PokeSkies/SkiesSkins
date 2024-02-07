package com.pokeskies.skiesskins.commands.subcommands

import ca.landonjw.gooeylibs2.api.UIManager
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.config.ConfigManager
import com.pokeskies.skiesskins.gui.ShopGui
import com.pokeskies.skiesskins.utils.SubCommand
import me.lucko.fabric.api.permissions.v0.Permissions
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

class ShopCommand : SubCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("shop")
            .requires(Permissions.require("skiesskins.command.shop", 4))
            .then(Commands.argument("shop", StringArgumentType.string())
                .suggests { _, builder ->
                    SharedSuggestionProvider.suggest(ConfigManager.SHOPS.keys.stream(), builder)
                }
                .then(Commands.argument("player", EntityArgument.players())
                    .executes { ctx ->
                        val shopId = StringArgumentType.getString(ctx, "shop")
                        val players = EntityArgument.getPlayers(ctx, "player")
                        openShop(ctx, shopId, players)
                    }
                )
                .requires { obj: CommandSourceStack -> obj.isPlayer }
                .executes { ctx ->
                    val shopId = StringArgumentType.getString(ctx, "shop")
                    openShop(ctx, shopId, listOf(ctx.source.playerOrException))
                }
            )
            .build()
    }

    companion object {
        private fun openShop(
            ctx: CommandContext<CommandSourceStack>,
            shopId: String,
            players: Collection<ServerPlayer>,
        ): Int {
            if (SkiesSkins.INSTANCE.storage == null) {
                ctx.source.sendMessage(
                    Component.literal("The storage system is not available at the moment. Please try again later.")
                        .withStyle { it.withColor(ChatFormatting.RED) }
                )
                return 1
            }

            val shopConfig = ConfigManager.SHOPS[shopId]
            if (shopConfig == null) {
                ctx.source.sendMessage(
                    Component.literal("Cannot find a shop entry from the ID: $shopId")
                        .withStyle { it.withColor(ChatFormatting.RED)}
                )
                return 1
            }

            for (player in players) {
                UIManager.openUIForcefully(player, ShopGui(player, shopId, shopConfig))
            }
            return 1
        }
    }
}