package dev.chasem.cobblemonextras.commands

import com.cobblemon.mod.common.Cobblemon.storage
import com.cobblemon.mod.common.battles.BattleBuilder.pvp1v1
import com.cobblemon.mod.common.battles.BattleFormat.Companion.GEN_9_SINGLES
import com.cobblemon.mod.common.battles.BattleRegistry.BattleChallenge
import com.cobblemon.mod.common.battles.BattleRegistry.pvpChallenges
import com.cobblemon.mod.common.util.lang
import com.cobblemon.mod.common.util.permission
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import dev.chasem.cobblemonextras.CobblemonExtras
import dev.chasem.cobblemonextras.permissions.CobblemonExtrasPermissions
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.*
import net.minecraft.util.Formatting

class Battle {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("battle")
                .permission(CobblemonExtrasPermissions.COMPESEE_OTHER_PERMISSION)
                .then(
                    CommandManager.argument("player", EntityArgumentType.player())
                        .executes { ctx: CommandContext<ServerCommandSource> -> execute(ctx) })
        )
    }

    private fun execute(ctx: CommandContext<ServerCommandSource>): Int {
        if (ctx.source.player != null) {
            val player = ctx.source.player
            val battlePartner: ServerPlayerEntity = try {
                EntityArgumentType.getPlayer(ctx, "player")
            } catch (e: CommandSyntaxException) {
                ctx.source.sendError(Text.of("Error finding player."))
                return 1
            }
            if (battlePartner.uuid == player!!.uuid) {
                ctx.source.sendError(Text.of("Life's tough enough, don't battle yourself."))
                return 1
            }

            // Check in on battle requests, if the other player has challenged me, this starts the battle
            val existingChallenge: BattleChallenge? = pvpChallenges[battlePartner.uuid]
            if (existingChallenge != null && !existingChallenge.isExpired()) {
                pvp1v1(
                    player, battlePartner, GEN_9_SINGLES,
                    cloneParties = false, healFirst = false
                ) { serverPlayerEntity: ServerPlayerEntity? ->
                    storage.getParty(
                        serverPlayerEntity!!
                    )
                }
                pvpChallenges.remove(battlePartner.uuid)
            } else {
                val challenge = BattleChallenge(battlePartner.uuid, 30)
                pvpChallenges[player.uuid] = challenge

                // TODO EXPIRE AFTER 30 seconds.
                // BattleRegistry.pvpChallenges.remove(player.uuid, challenge)
                val accept = Texts.join(
                    Text.literal("[ACCEPT]")
                        .getWithStyle(
                            Style.EMPTY.withBold(true).withColor(Formatting.GREEN)
                                .withClickEvent(
                                    ClickEvent(
                                        ClickEvent.Action.RUN_COMMAND,
                                        "/battle " + player.entityName
                                    )
                                )
                                .withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Accept Battle")))
                        ), Text.of("")
                )
                battlePartner.sendMessage(
                    player.displayName.copy().formatted(Formatting.YELLOW).append(
                        Text.literal(" has challenged you to a battle.").formatted(
                            Formatting.WHITE
                        )
                    )
                )
                battlePartner.sendMessage(
                    Text.literal("Click ").formatted(Formatting.WHITE).append(accept).append(
                        Text.literal(" to start a battle.").formatted(
                            Formatting.WHITE
                        )
                    )
                )
                player.sendMessage(
                    lang(
                        "challenge.sender",
                        battlePartner.displayName.copy().formatted(Formatting.YELLOW)
                    )
                )
            }
        } else {
            ctx.source.sendError(Text.of("Sorry, this is only for players."))
        }
        return 1
    }
}