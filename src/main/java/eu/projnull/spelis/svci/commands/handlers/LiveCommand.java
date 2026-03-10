package eu.projnull.spelis.svci.commands.handlers;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import eu.projnull.spelis.svci.Intercom;
import eu.projnull.spelis.svci.commands.Handler;
import eu.projnull.spelis.svci.commands.Helpers;
import eu.projnull.spelis.svci.voice.BroadcasterState;
import eu.projnull.spelis.svci.voice.SpeakerManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LiveCommand implements Handler {

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> buildCommand() {
        return Commands.literal("live")
                .requires(cs -> cs.getSender().hasPermission("svcintercom.broadcast.start"))
                .then(Commands.argument("player", ArgumentTypes.player())
                        .then(Commands.argument("world", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    Helpers.getAllWorldsSuggestion(builder);
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("duration", IntegerArgumentType.integer(1, 300))
                                        // With optional mode parameter
                                        .then(Commands.argument("mode", StringArgumentType.word())
                                                .suggests((ctx, builder) -> {
                                                    builder.suggest("global");
                                                    builder.suggest("speaker");
                                                    return builder.buildFuture();
                                                })
                                                .executes(ctx -> executeLiveBroadcast(ctx, true))
                                        )
                                        // Without mode parameter (defaults to speaker if speakers exist, otherwise global)
                                        .executes(ctx -> executeLiveBroadcast(ctx, false))
                                )
                        )
                );
    }

    private int executeLiveBroadcast(CommandContext<CommandSourceStack> ctx, boolean hasMode) {
        CommandSourceStack source = ctx.getSource();
        CommandSender sender = source.getSender();
        
        Player player;
        try {
            player = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
        } catch (Exception e) {
            sender.sendMessage("§cCould not resolve player");
            return 0;
        }
        
        World world = Bukkit.getWorld(StringArgumentType.getString(ctx, "world"));
        int seconds = ctx.getArgument("duration", int.class);

        if (world == null) {
            sender.sendMessage("§cWorld not found.");
            return 0;
        }
        if (seconds <= 0 || seconds > 300) {
            sender.sendMessage("§cInvalid duration (must be 1-300 seconds)");
            return 0;
        }

        if (BroadcasterState.inst().isBroadcastActive(world.getUID())) {
            sender.sendMessage("§cA broadcast is already active in this world.");
            return 0;
        }

        // Determine broadcast mode
        BroadcasterState.Broadcaster.BroadcastMode mode;
        if (hasMode) {
            String modeStr = StringArgumentType.getString(ctx, "mode").toLowerCase();
            if (modeStr.equals("global")) {
                mode = BroadcasterState.Broadcaster.BroadcastMode.GLOBAL;
            } else if (modeStr.equals("speaker")) {
                mode = BroadcasterState.Broadcaster.BroadcastMode.SPEAKER;
            } else {
                sender.sendMessage("§cInvalid mode. Use 'global' or 'speaker'");
                return 0;
            }
        } else {
            // Auto-detect: use speaker mode if speakers exist, otherwise global
            boolean hasSpeakers = !SpeakerManager.inst().getSpeakers(world.getUID()).isEmpty();
            mode = hasSpeakers ? BroadcasterState.Broadcaster.BroadcastMode.SPEAKER : BroadcasterState.Broadcaster.BroadcastMode.GLOBAL;
        }

        // Warn if speaker mode selected but no speakers exist
        if (mode == BroadcasterState.Broadcaster.BroadcastMode.SPEAKER) {
            if (SpeakerManager.inst().getSpeakers(world.getUID()).isEmpty()) {
                sender.sendMessage("§eWarning: Speaker mode selected but no speakers exist in this world. No one will hear the broadcast!");
            }
        }

        BroadcasterState.Broadcaster broadcaster = new BroadcasterState.Broadcaster(
                player.getUniqueId(),
                world.getUID(),
                BroadcasterState.Broadcaster.BroadcastType.LIVE,
                mode,
                seconds * 1000L,
                null
        );

        BroadcasterState.inst().startBroadcast(broadcaster);
        
        String modeText = mode == BroadcasterState.Broadcaster.BroadcastMode.GLOBAL ? "§7(global)" : "§7(speaker)";
        sender.sendMessage("§aLive broadcast started for §f" + player.getName() + " §ain world §f" + world.getName() + " " + modeText);

        // Schedule auto-stop
        Bukkit.getScheduler().runTaskLaterAsynchronously(
                Intercom.getPlugin(Intercom.class),
                () -> BroadcasterState.inst().stopBroadcastWithMessage(world.getUID(), "§aThe broadcast has expired"),
                seconds * 20L
        );

        return 1;
    }
}
