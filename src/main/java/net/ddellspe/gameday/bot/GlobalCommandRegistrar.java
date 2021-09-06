package net.ddellspe.gameday.bot;

import discord4j.common.JacksonResources;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import discord4j.rest.service.ApplicationService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
public class GlobalCommandRegistrar implements ApplicationRunner {
  private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  private final RestClient restClient;

  public GlobalCommandRegistrar(RestClient restClient) {
    this.restClient = restClient;
  }

  @Override
  public void run(ApplicationArguments args) throws IOException {
    final JacksonResources d4jMapper = JacksonResources.create();

    PathMatchingResourcePatternResolver matcher = new PathMatchingResourcePatternResolver();
    final ApplicationService applicationService = restClient.getApplicationService();
    final long applicationId = restClient.getApplicationId().block();

    Map<String, ApplicationCommandData> discordCommands =
        applicationService
            .getGlobalApplicationCommands(applicationId)
            .collectMap(ApplicationCommandData::name)
            .block();

    Map<String, ApplicationCommandRequest> commands = new HashMap<>();
    for (Resource resource : matcher.getResources("commands/*.json")) {
      ApplicationCommandRequest request =
          d4jMapper
              .getObjectMapper()
              .readValue(resource.getInputStream(), ApplicationCommandRequest.class);

      commands.put(request.name(), request);

      if (!discordCommands.containsKey(request.name())) {
        applicationService.createGlobalApplicationCommand(applicationId, request).block();

        LOGGER.info("Created global command: " + request.name());
      }

      for (ApplicationCommandData discordCommand : discordCommands.values()) {
        long discordCommandId = Long.parseLong(discordCommand.id());

        ApplicationCommandRequest command = commands.get(discordCommand.name());

        if (command == null) {
          applicationService
              .deleteGlobalApplicationCommand(applicationId, discordCommandId)
              .block();

          LOGGER.info("Deleted global command: " + discordCommand.name());
          continue;
        }
        boolean changed =
            !discordCommand.description().equals(command.description())
                || !discordCommand.options().equals(command.options())
                || !discordCommand.defaultPermission().equals(command.defaultPermission());

        if (changed) {
          applicationService
              .modifyGlobalApplicationCommand(applicationId, discordCommandId, command)
              .block();

          LOGGER.info("Updated global command: " + command.name());
        }
      }
    }
  }
}
