package net.ddellspe.gameday.bot;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Channel;
import discord4j.rest.RestClient;
import java.util.List;
import net.ddellspe.gameday.bot.listeners.VoiceStateTriggerListener;
import net.ddellspe.gameday.bot.listeners.MessageResponseCommandListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class GamedayBot {
  private static final Logger LOGGER = LoggerFactory.getLogger(GamedayBot.class);

  public static void main(String[] args) {
    ApplicationContext springContext =
        new SpringApplicationBuilder(GamedayBot.class).build().run(args);

    DiscordClientBuilder.create(System.getenv("BOT_TOKEN"))
        .build()
        .withGateway(
            gatewayClient -> {
              List<Guild> guilds = gatewayClient.getGuilds().collectList().block();
              for (Guild guild : guilds) {
                System.out.println(guild);
                for (Channel channel : guild.getChannels().collectList().block()) {
                  System.out.println(channel);
                }
              }

              MessageResponseCommandListener messageResponseCommandListener =
                  new MessageResponseCommandListener(springContext);

              Mono<Void> onMessageResponseCommand =
                  gatewayClient
                      .on(MessageCreateEvent.class, messageResponseCommandListener::handle)
                      .then();

              VoiceStateTriggerListener audioManagerTriggerCommandListener =
                  new VoiceStateTriggerListener(springContext);

              Mono<Void> onVoiceStateChanged =
                  gatewayClient
                      .on(VoiceStateUpdateEvent.class, audioManagerTriggerCommandListener::handle)
                      .then();

              return Mono.when(onMessageResponseCommand, onVoiceStateChanged);
            })
        .block();
  }

  @Bean
  public RestClient discordRestClient() {
    return RestClient.create(System.getenv("BOT_TOKEN"));
  }
}
