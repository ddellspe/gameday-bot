package net.ddellspe.gameday.bot.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.channel.VoiceChannel;
import net.ddellspe.gameday.bot.audio.GamedayAudioManager;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class StartGamedayCommand implements MessageResponseCommand {
  @Override
  public String getName() {
    return "start";
  }

  @Override
  public Snowflake getFilterChannel(Snowflake guildId) {
    GamedayAudioManager manager = GamedayAudioManager.of(guildId);
    return manager.getChatChannel();
  }

  @Override
  public Mono<Void> handle(MessageCreateEvent event) {
    // This will be guaranteed to be present since we're limiting to Join and Move events
    Snowflake guildId = event.getGuildId().get();
    GamedayAudioManager manager = GamedayAudioManager.of(guildId);
    Snowflake voiceChannelId = manager.getVoiceChannel();

    final Mono<Boolean> nonBotChannelCountIsGreaterThanZero =
        event
            .getClient()
            .getChannelById(voiceChannelId)
            .cast(VoiceChannel.class)
            .flatMapMany(VoiceChannel::getVoiceStates)
            .flatMap(VoiceState::getMember)
            .filter(member -> !member.isBot())
            .count()
            .map(count -> count > 0);
    return event
        .getMessage()
        .getChannel()
        .flatMap(channel -> channel.createMessage("Starting Gameday Manager"))
        .doOnNext(___ -> manager.start())
        .flatMap(channel -> event.getClient().getChannelById(voiceChannelId))
        .cast(VoiceChannel.class)
        .filterWhen(___ -> nonBotChannelCountIsGreaterThanZero)
        .filter(___ -> manager.isStarted())
        .flatMap(
            channel ->
                channel.join(spec -> spec.setSelfDeaf(true).setProvider(manager.getProvider())))
        .then();
  }
}
