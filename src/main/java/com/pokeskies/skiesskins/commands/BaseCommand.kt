package com.pokeskies.skiesskins.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.skiesskins.api.SkiesSkinsAPI
import com.pokeskies.skiesskins.commands.subcommands.*
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

class BaseCommand {
    private val aliases = listOf("skiesskins", "skins")

    /**
     * /skins - help command or open inventory?
     * /skins inventory [player] - open your skin inventory or open for another player
     * /skins shop <shop-id> [player] - open a shop or open for another player
     * /skins giveskin <skin-id> <amount> [player] - give an amount of a certain skin
     *  - /skins giveskin random <amount> [player] - give a random skin the amount of times (random for each amount)
     */
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val rootCommands: List<LiteralCommandNode<CommandSourceStack>> = aliases.map {
            Commands.literal(it)
                .requires(Permissions.require("skiesskins.command.base", 4))
                .requires { obj: CommandSourceStack -> obj.isPlayer }
                .executes(BaseCommand::execute)
                .build()
        }

        val subCommands: List<LiteralCommandNode<CommandSourceStack>> = listOf(
            DebugCommand().build(),
            ReloadCommand().build(),
            ShopCommand().build(),
            GiveSkinCommand().build(),
            RemoverCommand().build(),
        )

        rootCommands.forEach { root ->
            subCommands.forEach { sub -> root.addChild(sub) }
            dispatcher.root.addChild(root)
        }
    }

    companion object {
        private fun execute(ctx: CommandContext<CommandSourceStack>): Int {
            val player = ctx.source.player
            if (player != null) SkiesSkinsAPI.openSkinInventory(player)
            return 1
        }
    }
}