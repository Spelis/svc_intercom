package eu.projnull.spelis.svci.commands.handlers;

import eu.projnull.spelis.svci.commands.BroadcastTypeHandler;
import eu.projnull.spelis.svci.voice.BroadcasterState;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

public class StopBroadcastHandler implements BroadcastTypeHandler {

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /intercom stop <world>");
            return true;
        }

        World world = Bukkit.getWorld(args[1]);
        if (world == null) {
            sender.sendMessage("§cWorld not found.");
            return true;
        }

        if (!BroadcasterState.inst().isBroadcastActive(world.getUID())) {
            sender.sendMessage("§cNo active broadcast in this world.");
            return true;
        }

        BroadcasterState.inst().stopBroadcastWithMessage(world.getUID(), "§aThe broadcast has been stopped.");
        return true;
    }

    @Override
    public String getName() {
        return "stop";
    }
}
