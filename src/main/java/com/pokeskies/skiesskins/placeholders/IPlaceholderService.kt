package com.pokeskies.skiesskins.placeholders

import net.minecraft.server.level.ServerPlayer

interface IPlaceholderService {
    fun parsePlaceholders(player: ServerPlayer, text: String): String
}