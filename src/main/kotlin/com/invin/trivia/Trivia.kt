package com.invin.trivia

import com.cobblemon.mod.common.api.text.*
import com.google.gson.GsonBuilder
import com.invin.trivia.commands.ReloadCommand
import com.invin.trivia.config.TriviaSpec
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.time.Instant

class Trivia : ModInitializer {

    companion object {
        lateinit var config: TriviaSpec
        var randTime = Math.random() * 2 + 3
        var triviaTime = Instant.now().plusSeconds((randTime * 60).toLong())
        var randQuestion = ""
        var answered = false
    }
    override fun onInitialize() {
        loadConfig()
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register { _, _ ->
            loadConfig()
        }
        CommandRegistrationCallback.EVENT.register{ dispatcher, _, _ ->
            ReloadCommand.register(dispatcher)
        }

        ServerTickEvents.START_SERVER_TICK.register {
            if (Instant.now().isAfter(triviaTime)) {
                answered = false
                randQuestion = config.questionAnswer.keys.random()
                val message = Text.literal("[").append(Text.literal("Trivia").gold()).append(Text.literal("] ")).aqua()
                message.append(Text.literal(randQuestion).green())
                broadcast(it, message)
                randTime = Math.random() * 2 + 3
                triviaTime = Instant.now().plusSeconds((randTime * 60).toLong())
            }
        }

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register { message, player, _ ->
            if (message.content.string.lowercase() == (config.questionAnswer[randQuestion]?.lowercase() ?: "") && !answered) {
                val sendMessage = Text.literal(player.name.string).aqua().bold()
                sendMessage.append(Text.literal(" got the answer!").lightPurple())
                broadcast(player.server, sendMessage)
                player.server.commandManager.dispatcher.execute("adminpay ${player.name.string} pokedollar 500", player.server.commandSource)
                answered = true

                return@register false
            }
            return@register true
        }
    }
    private fun broadcast(server: MinecraftServer, message: Text) {
        server.playerManager.playerList.forEach{player ->
            player.sendMessage(message)
        }
    }
    private fun loadConfig() {
        val configDir = File("./config/")
        if (!configDir.exists()) {
            configDir.mkdirs()
        }

        val configFile = File(configDir, "trivia.json")
        val gson = GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()

        if (!configFile.exists()) {
            config = TriviaSpec()
            val fileWriter = FileWriter(configFile, Charsets.UTF_8)

            gson.toJson(config, fileWriter)

            fileWriter.flush()
            fileWriter.close()
        } else {
            val fileReader = FileReader(configFile, Charsets.UTF_8)
            config = gson.fromJson(fileReader, TriviaSpec::class.java)
            fileReader.close()
        }
    }
}