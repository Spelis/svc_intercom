package eu.projnull.spelis.svci.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class IntercomCommand implements CommandExecutor {

    private final Map<String, BroadcastTypeHandler> handlers = new HashMap<>();

    public IntercomCommand() {
        registerHandler(new eu.projnull.spelis.svci.commands.handlers.LiveBroadcastHandler());
        registerHandler(new eu.projnull.spelis.svci.commands.handlers.StopBroadcastHandler());
        registerHandler(new eu.projnull.spelis.svci.commands.handlers.InfoBroadcastHandler());
        registerHandler(new eu.projnull.spelis.svci.commands.handlers.FileBroadcastHandler());
    }

    public void registerHandler(BroadcastTypeHandler handler) {
        handlers.put(handler.getName().toLowerCase(), handler);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("svcintercom.broadcast")) {
            sender.sendMessage("§cYou don't have permission to use the intercom.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /intercom <type> ...");
            return true;
        }

        String type = args[0].toLowerCase();
        BroadcastTypeHandler handler = handlers.get(type);

        if (handler == null) {
            sender.sendMessage("§cUnknown broadcast type: " + type);
            return true;
        }

        return handler.execute(sender, args);
    }
}
