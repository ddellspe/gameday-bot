package net.ddellspe.gameday.bot.listeners;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import java.util.Collection;
import net.ddellspe.gameday.bot.commands.AudioManagerTriggerCommand;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AudioManagerTriggerCommandListener {
  private final Collection<AudioManagerTriggerCommand> commands;

  public AudioManagerTriggerCommandListener(ApplicationContext applicationContext) {
    commands = applicationContext.getBeansOfType(AudioManagerTriggerCommand.class).values();
  }

  public Mono<Void> handle(VoiceStateUpdateEvent event) {
    Snowflake guildId = event.getCurrent().getGuildId();
    return Flux.fromIterable(commands)
        .filter(___ -> !event.getCurrent().getUserId().equals(event.getClient().getSelfId()))
        .filter(command -> command.isCorrectEventType(event))
        .filter(
            command ->
                ((event.getCurrent().getChannelId().isPresent()
                        && command
                            .getFilterChannel(event.getCurrent().getGuildId())
                            .equals(event.getCurrent().getChannelId().get()))
                    || event.getOld().isPresent()
                        && event.getOld().get().getChannelId().isPresent()
                        && command
                            .getFilterChannel(event.getCurrent().getGuildId())
                            .equals(event.getOld().get().getChannelId().get())))
        .flatMap(command -> command.handle(event))
        .next();
  }
}
