package com.pokeskies.skiesskins.commands.subcommands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.skiesskins.SkiesSkins
import com.pokeskies.skiesskins.api.SkiesSkinsAPI
import com.pokeskies.skiesskins.utils.SubCommand
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

class ResetInventoryCommand : SubCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("resetinventory")
            .requires(Permissions.require("skiesskins.command.resetinventory", 4))
            .then(Commands.argument("targets", EntityArgument.players())
                .executes { ctx ->
                    resetInventory(
                        ctx,
                        EntityArgument.getPlayers(ctx, "targets")
                    )
                }
            )
            .build()
    }

    companion object {
        private fun resetInventory(
            ctx: CommandContext<CommandSourceStack>,
            players: Collection<ServerPlayer>
        ): Int {
            if (SkiesSkins.INSTANCE.storage == null) {
                ctx.source.sendMessage(
                    Component.literal("The storage system is not available at the moment. Please try again later.")
                        .withStyle { it.withColor(ChatFormatting.RED) }
                )
                return 1
            }

            if (players.size == 1) {
                val player = players.first()
                val userData = SkiesSkinsAPI.getUserData(player)
                userData.inventory = emptyList()
                SkiesSkinsAPI.saveUserData(player, userData)
                if (SkiesSkins.INSTANCE.inventoryControllers.containsKey(player.uuid)) {
                    SkiesSkins.INSTANCE.inventoryControllers[player.uuid]!!.refresh()
                }
                ctx.source.sendMessage(
                    Component.empty()
                        .append(
                            Component.literal("You reset the skin inventory of ${player.name.string}")
                                .withStyle { it.withColor(ChatFormatting.GREEN) }
                        )
                )
            } else {
                var playerCount = 0
                for (player in players) {
                    val userData = SkiesSkinsAPI.getUserData(player)
                    userData.inventory = emptyList()
                    SkiesSkinsAPI.saveUserData(player, userData)
                    if (SkiesSkins.INSTANCE.inventoryControllers.containsKey(player.uuid)) {
                        SkiesSkins.INSTANCE.inventoryControllers[player.uuid]!!.refresh()
                    }
                    playerCount++
                }
                ctx.source.sendMessage(
                    Component.empty()
                        .append(
                            Component.literal("You reset the skin inventory of $playerCount players")
                                .withStyle { it.withColor(ChatFormatting.GREEN) }
                        )
                )
            }
            return 1
        }
    }
}