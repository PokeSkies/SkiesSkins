package com.pokeskies.skiesskins.commands.subcommands

import ca.landonjw.gooeylibs2.api.UIManager
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.skiesskins.gui.ShopGui
import com.pokeskies.skiesskins.utils.SubCommand
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

class ShopCommand : SubCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("shop")
            .requires(Permissions.require("skiesskins.command.shop", 4))
            .executes(ShopCommand::shop)
            .build()
    }

    companion object {
        private fun shop(ctx: CommandContext<CommandSourceStack>): Int {
            val player = ctx.source.player
            if (player != null) UIManager.openUIForcefully(player, ShopGui(player))
            return 1
        }
    }
}