package com.invin.trivia.commands

import com.cobblemon.mod.common.api.text.green
import com.google.gson.GsonBuilder
import com.invin.trivia.Trivia
import com.invin.trivia.config.TriviaSpec
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import me.lucko.fabric.api.permissions.v0.Permissions
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object ReloadCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            LiteralArgumentBuilder.literal<ServerCommandSource>("triviareload")
                .requires(Permissions.require("trivia.reload", 4))
                .executes(ReloadCommand::reload)
        )
    }

    private fun reload(ctx: CommandContext<ServerCommandSource>): Int {
        val player = ctx.source.player ?: return Command.SINGLE_SUCCESS
        val configFile = File("./config/trivia.json")
        val gson = GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()
        val fileReader = FileReader(configFile, Charsets.UTF_8)
        Trivia.config = gson.fromJson(fileReader, TriviaSpec::class.java)
        fileReader.close()
        player.sendMessage(Text.literal("Trivia Config has been reloaded.").green())
        return Command.SINGLE_SUCCESS
    }
}