package eu.projnull.spelis.svci.commands.handlers;

import eu.projnull.spelis.svci.Intercom;
import eu.projnull.spelis.svci.commands.BroadcastTypeHandler;
import eu.projnull.spelis.svci.voice.BroadcasterState;
import eu.projnull.spelis.svci.voice.BroadcasterState.Broadcaster;
import eu.projnull.spelis.svci.voice.BroadcasterState.Broadcaster.BroadcastType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LiveBroadcastHandler implements BroadcastTypeHandler {

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length < 4) {
            sender.sendMessage("§cUsage: /intercom live <player> <world> <seconds>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found.");
            return true;
        }

        World world = Bukkit.getWorld(args[2]);
        if (world == null) {
            sender.sendMessage("§cWorld not found.");
            return true;
        }

        int seconds;
        try {
            seconds = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid duration.");
            return true;
        }

        if (seconds <= 0 || seconds > 300) { // optional max
            sender.sendMessage("§cDuration must be between 1 and 300 seconds.");
            return true;
        }

        if (BroadcasterState.inst().isBroadcastActive(world.getUID())) {
            sender.sendMessage("§cA broadcast is already active in this world.");
            return true;
        }

        Broadcaster broadcaster = new Broadcaster(
                target.getUniqueId(),
                world.getUID(),
                BroadcastType.LIVE,
                seconds * 1000L,
                null
        );

        BroadcasterState.inst().startBroadcast(broadcaster);
        sender.sendMessage("§aLive broadcast started for " + target.getName() + " in world " + world.getName());

        // Schedule auto-stop
        Bukkit.getScheduler().runTaskLaterAsynchronously(
                Intercom.getPlugin(Intercom.class),
                () -> BroadcasterState.inst().stopBroadcastWithMessage(world.getUID(), "§aThe broadcast has expired"),
                seconds * 20L
        );

        return true;
    }

    @Override
    public String getName() {
        return "live";
    }
}
