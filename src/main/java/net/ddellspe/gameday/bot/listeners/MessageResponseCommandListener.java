package net.ddellspe.gameday.bot.listeners;

import discord4j.core.event.domain.message.MessageCreateEvent;
import java.util.Collection;
import net.ddellspe.gameday.bot.commands.MessageResponseCommand;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MessageResponseCommandListener {
  private final Collection<MessageResponseCommand> commands;

  public MessageResponseCommandListener(ApplicationContext applicationContext) {
    commands = applicationContext.getBeansOfType(MessageResponseCommand.class).values();
  }

  public Mono<Void> handle(MessageCreateEvent event) {
    return Flux.fromIterable(commands)
        .filter(
            command ->
                ((command.getFilterChannel(event.getGuildId().get()) != null
                        && event
                            .getMessage()
                            .getChannelId()
                            .equals(command.getFilterChannel(event.getGuildId().get())))
                    || command.getFilterChannel(event.getGuildId().get()) == null))
        .filter(command -> ("!" + command.getName()).equals(event.getMessage().getContent()))
        .next()
        .flatMap(command -> command.handle(event));
  }
}
