package net.ddellspe.gameday.bot.audio;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.common.JacksonResources;
import discord4j.common.util.Snowflake;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import net.ddellspe.gameday.bot.model.GuildConfiguration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class GamedayAudioManager {

  public static final AudioPlayerManager PLAYER_MANAGER;

  static {
    PLAYER_MANAGER = new DefaultAudioPlayerManager();
    // This is an optimization strategy that Discord4J can utilize to minimize allocations
    PLAYER_MANAGER.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
    AudioSourceManagers.registerRemoteSources(PLAYER_MANAGER);
    AudioSourceManagers.registerLocalSource(PLAYER_MANAGER);
  }
  private static final Map<Snowflake, GamedayAudioManager> MANAGERS = new ConcurrentHashMap<>();

  public static GamedayAudioManager of(final Snowflake id) {
    return MANAGERS.computeIfAbsent(id, ignored -> new GamedayAudioManager(id));
  }

  private final AudioPlayer player;
  private final GamedayAudioTrackScheduler scheduler;
  private final GamedayAudioProvider provider;
  private final GuildConfiguration configuration;
  private final AtomicBoolean started;

  private GamedayAudioManager(Snowflake guildId) {
    player = PLAYER_MANAGER.createPlayer();
    scheduler = new GamedayAudioTrackScheduler(player);
    provider = new GamedayAudioProvider(player);

    player.addListener(scheduler);
    JacksonResources d4jMapper = JacksonResources.create();
    PathMatchingResourcePatternResolver matcher = new PathMatchingResourcePatternResolver();

    Map<Snowflake, GuildConfiguration> configurationMap = new HashMap<>();
    try {
      Resource config = matcher.getResource("configs/gameday_config.json");
      List<GuildConfiguration> guildConfigurations = d4jMapper.getObjectMapper().readValue(
          config.getInputStream(), new TypeReference<List<GuildConfiguration>>() {});
      for(GuildConfiguration cfg : guildConfigurations) {
        configurationMap.put(cfg.getGuildId(), cfg);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    configuration = configurationMap.get(guildId);
    started = new AtomicBoolean(false);
  }

  public AudioPlayer getPlayer() {
    return player;
  }

  public GamedayAudioTrackScheduler getScheduler() {
    return scheduler;
  }

  public GamedayAudioProvider getProvider() {
    return provider;
  }

  public void start() {
    started.set(true);
  }

  public void stop() {
    scheduler.stop();
    started.set(false);
  }

  public boolean isStarted() {
    return started.get();
  }

  public GuildConfiguration getConfiguration() {
    return configuration;
  }

  public Snowflake getVoiceChannel() {
    return configuration.getVoiceChannelId();
  }

  public Snowflake getChatChannel() {
    return configuration.getChatChanelId();
  }
}
