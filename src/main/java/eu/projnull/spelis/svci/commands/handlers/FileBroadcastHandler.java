package eu.projnull.spelis.svci.commands.handlers;

import de.maxhenkel.voicechat.api.ServerLevel;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.audiochannel.StaticAudioChannel;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import eu.projnull.spelis.svci.Intercom;
import eu.projnull.spelis.svci.commands.BroadcastTypeHandler;
import eu.projnull.spelis.svci.voice.BroadcasterState;
import eu.projnull.spelis.svci.voice.BroadcasterState.Broadcaster;
import eu.projnull.spelis.svci.voice.BroadcasterState.Broadcaster.BroadcastType;
import eu.projnull.spelis.svci.misc.OggDecoder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.craftbukkit.CraftWorld;

import java.io.File;
import java.util.UUID;

public class FileBroadcastHandler implements BroadcastTypeHandler {

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /intercom file <filename> <world>");
            return true;
        }

        String fileName = args[1];
        World world = Bukkit.getWorld(args[2]);
        if (world == null) {
            sender.sendMessage("§cWorld not found.");
            return true;
        }

        if (BroadcasterState.inst().isBroadcastActive(world.getUID())) {
            sender.sendMessage("§cA broadcast is already active in this world.");
            return true;
        }

        File soundFile = new File(Intercom.getPlugin(Intercom.class).getDataFolder(), "sounds/" + fileName);
        if (!soundFile.exists()) {
            Intercom.LOGGER.warn("File not found: {}", soundFile.getAbsoluteFile());
            sender.sendMessage("§cFile not found: sounds/" + fileName);
            return true;
        }

        sender.sendMessage("§7Loading audio file...");

        Bukkit.getScheduler().runTaskAsynchronously(Intercom.getPlugin(Intercom.class), () -> {
            short[] pcm;
            try {
                pcm = OggDecoder.decode(soundFile);
            } catch (Exception e) {
                sender.sendMessage("§cFailed to decode audio file: " + e.getMessage());
                return;
            }

            long durationMillis = pcm.length / 48L;

            Bukkit.getScheduler().runTask(Intercom.getPlugin(Intercom.class), () -> {
                if (BroadcasterState.inst().isBroadcastActive(world.getUID())) {
                    sender.sendMessage("§cA broadcast started in this world while the file was loading.");
                    return;
                }

                VoicechatServerApi api = Intercom.getPlugin(Intercom.class).getVoicechatPlugin().getVoicechatServerApi();
                if (api == null) {
                    sender.sendMessage("§cVoice chat API is not available.");
                    return;
                }

                de.maxhenkel.voicechat.api.ServerLevel level = api.fromServerLevel(world);

                Broadcaster broadcaster = new Broadcaster(
                        null,
                        world.getUID(),
                        BroadcastType.FILE,
                        durationMillis,
                        fileName
                );
                BroadcasterState.inst().startBroadcast(broadcaster);

                for (Player p : world.getPlayers()) {
                    VoicechatConnection connection = api.getConnectionOf(p.getUniqueId());
                    if (connection == null) continue;

                    StaticAudioChannel channel = api.createStaticAudioChannel(UUID.randomUUID(), level, connection);
                    if (channel == null) continue;

                    AudioPlayer audioPlayer = api.createAudioPlayer(channel, api.createEncoder(), pcm);
                    audioPlayer.startPlaying();
                }

                sender.sendMessage("§aFile broadcast started: §f" + fileName + " §ain §f" + world.getName());

                Bukkit.getScheduler().runTaskLaterAsynchronously(
                        Intercom.getPlugin(Intercom.class),
                        () -> BroadcasterState.inst().stopBroadcastWithMessage(world.getUID(), "§aThe broadcast has ended."),
                        durationMillis / 50L
                );
            });
        });

        return true;
    }

    @Override
    public String getName() {
        return "file";
    }
}
