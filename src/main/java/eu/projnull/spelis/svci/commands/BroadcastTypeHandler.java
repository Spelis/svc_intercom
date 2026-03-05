package eu.projnull.spelis.svci.commands;

import org.bukkit.command.CommandSender;

public interface BroadcastTypeHandler {

    /**
     * Called when the command executes for this type.
     * @param sender The executor
     * @param args Command arguments
     * @return true if handled successfully (for Bukkit), false to show usage
     */
    boolean execute(CommandSender sender, String[] args);

    /**
     * The name of this type (e.g., "file", "live")
     */
    String getName();
}
