package net.ddellspe.gameday.bot.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.VoiceConnection;
import java.util.Map;
import net.ddellspe.gameday.bot.audio.GamedayAudioManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AutoLeaveCommand implements AudioManagerTriggerCommand {

  @Override
  public boolean isCorrectEventType(VoiceStateUpdateEvent event) {
    return event.isMoveEvent() || event.isLeaveEvent();
  }

  @Override
  public Snowflake getFilterChannel(Snowflake guildId) {
    GamedayAudioManager manager = GamedayAudioManager.of(guildId);
    return manager.getVoiceChannels().get(guildId);
  }

  @Override
  public Mono<Void> handle(VoiceStateUpdateEvent event) {
    // This will be guaranteed to be present since we're limiting to Join and Move events
    Snowflake guildId = event.getCurrent().getGuildId();
    GamedayAudioManager manager = GamedayAudioManager.of(guildId);
    Snowflake voiceChannelId = manager.getVoiceChannels().get(guildId);

    final Mono<Boolean> nonBotChannelCountIsZero =
        event
            .getClient()
            .getChannelById(voiceChannelId)
            .cast(VoiceChannel.class)
            .flatMapMany(VoiceChannel::getVoiceStates)
            .flatMap(VoiceState::getMember)
            .filter(member -> !member.isBot())
            .count()
            .map(count -> count == 0);
    return event
        .getClient()
        .getVoiceConnectionRegistry()
        .getVoiceConnection(guildId)
        .filterWhen(___ -> nonBotChannelCountIsZero)
        .flatMap(VoiceConnection::disconnect);
  }
}
