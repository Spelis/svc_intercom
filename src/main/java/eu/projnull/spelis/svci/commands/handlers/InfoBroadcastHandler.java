package eu.projnull.spelis.svci.commands.handlers;

import eu.projnull.spelis.svci.commands.BroadcastTypeHandler;
import eu.projnull.spelis.svci.voice.BroadcasterState;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

public class InfoBroadcastHandler implements BroadcastTypeHandler {

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /intercom info <world>");
            return true;
        }

        World world = Bukkit.getWorld(args[1]);
        if (world == null) {
            sender.sendMessage("§cWorld not found.");
            return true;
        }

        BroadcasterState.Broadcaster b = BroadcasterState.inst().getBroadcast(world.getUID());

        if (b == null) {
            sender.sendMessage("§aNo active broadcast in this world.");
            return true;
        }

        String type = b.getType().name();
        String owner = b.getType() == BroadcasterState.Broadcaster.BroadcastType.LIVE ?
                Bukkit.getOfflinePlayer(b.getPlayerId()).getName() : b.getFileName();
        long secondsLeft = (b.getEndTimeMillis() - System.currentTimeMillis()) / 1000;

        sender.sendMessage("§eBroadcast info:");
        sender.sendMessage(" §7Type: §f" + type);
        sender.sendMessage(" §7Owner/File: §f" + owner);
        sender.sendMessage(" §7Time remaining: §f" + secondsLeft + "s");

        return true;
    }

    @Override
    public String getName() {
        return "info";
    }
}
