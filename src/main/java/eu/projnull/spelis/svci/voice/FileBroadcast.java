package eu.projnull.spelis.svci.voice;

import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.audiochannel.StaticAudioChannel;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import eu.projnull.spelis.svci.misc.OggDecoder;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileBroadcast {

    private final List<AudioPlayer> players = new ArrayList<>();

    /**
     * Starts playing an audio file to all players currently in the world.
     * New players joining mid-broadcast won't hear it (acceptable for now).
     *
     * @param api      the SVC server API instance
     * @param world    the target world
     * @param audioFile the .ogg file to play
     * @param onDone   called when all players' audio finishes
     */
    public void start(VoicechatServerApi api, World world, File audioFile, Runnable onDone) throws Exception {
        short[] pcm = OggDecoder.decode(audioFile); // see below

        for (Player player : world.getPlayers()) {
            VoicechatConnection connection = api.getConnectionOf(player.getUniqueId());
            if (connection == null) continue; // player doesn't have SVC mod

            StaticAudioChannel channel = api.createStaticAudioChannel(
                    UUID.randomUUID(),
                    api.fromServerLevel(/* ServerLevel */ null), // explained below
                    connection
            );
            if (channel == null) continue;

            AudioPlayer ap = api.createAudioPlayer(channel, api.createEncoder(), pcm);
            ap.setOnStopped(onDone);
            ap.startPlaying();
            players.add(ap);
        }
    }

    public void stop() {
        for (AudioPlayer ap : players) {
            ap.stopPlaying();
        }
        players.clear();
    }
}
