package eu.projnull.spelis.svci;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import eu.projnull.spelis.svci.commands.IntercomCommand;
import eu.projnull.spelis.svci.misc.BroadcastHudTask;
import eu.projnull.spelis.svci.voice.VoicePlugin;
import org.bukkit.plugin.java.JavaPlugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public final class Intercom extends JavaPlugin {

    public static final String PLUGIN_ID = "SVCIntercom";
    public static final Logger LOGGER = LogManager.getLogger(PLUGIN_ID);

    private final BroadcastHudTask hudTask = new BroadcastHudTask();

    @Nullable
    private VoicePlugin voicechatPlugin;

    @Override
    public void onEnable() {
        BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service != null) {
            voicechatPlugin = new VoicePlugin();
            service.registerPlugin(voicechatPlugin);
            LOGGER.info("Successfully registered intercom plugin");
        } else {
            LOGGER.info("Failed to register intercom plugin");
        }

        getCommand("intercom").setExecutor(new IntercomCommand());

        hudTask.start(this);
    }

    @Override
    public void onDisable() {
        if (voicechatPlugin != null) {
            getServer().getServicesManager().unregister(voicechatPlugin);
            LOGGER.info("Successfully unregistered intercom plugin");
        }
        hudTask.stop();
    }

    @Nullable
    public VoicePlugin getVoicechatPlugin() {
        return voicechatPlugin;
    }
}
