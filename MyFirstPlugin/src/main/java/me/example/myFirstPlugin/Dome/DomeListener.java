package me.example.myFirstPlugin.Dome;


import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DomeListener implements Listener {

  private final World world;
  private final DomeCommand domeCommand;

  private final Set<UUID> pendingRespawns = new HashSet<>();

  public DomeListener(World world, DomeCommand domeCommand) {
    this.world = world;
    this.domeCommand = domeCommand;
  }

  @EventHandler
  public void removeDeadPlayers(PlayerDeathEvent event) {

    Player deadPlayer = event.getEntity();

    if (!domeCommand.isRunning()) return;
    if (!deadPlayer.getWorld().equals(world)) return;
    if (!domeCommand.isParticipant(deadPlayer)) return;

    //those players that have been died but not respawned yet
    pendingRespawns.add(deadPlayer.getUniqueId());

    // Spieler als ausgeschieden markieren
    domeCommand.eliminate(deadPlayer);

    checkWinner();
  }

  private void checkWinner() {

    if (!domeCommand.isRunning()) return;

    List<Player> alive = domeCommand.getAliveParticipants();

    if (alive.size() == 1) {
      domeCommand.endRound(alive.get(0));
    } else if (alive.isEmpty()) {
      domeCommand.endRound(null);
    }
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {

    Player player = event.getPlayer();

    if (!domeCommand.isRunning()) return;
    if (!domeCommand.isParticipant(player)) return;

    domeCommand.eliminate(player);

    // Nach dem Disconnect verbleibende Spieler prüfen
    Bukkit.getScheduler().runTask(
        domeCommand.getPlugin(),
        this::checkWinner
    );
  }

  @EventHandler
  public void onRespawn(PlayerRespawnEvent event) {

    Player player = event.getPlayer();

    if (!pendingRespawns.remove(player.getUniqueId())) {
      return;
    }

    boolean spectate = domeCommand.isRunning()
        && domeCommand.isEliminated(player);

    if (spectate) {
      event.setRespawnLocation(world.getSpawnLocation());
    } else {
      World lobby = domeCommand.getLobby();

      if (lobby != null) {
        event.setRespawnLocation(lobby.getSpawnLocation());
      }
    }

    // GameMode erst nach dem Respawn ändern
    Bukkit.getScheduler().runTask(
        domeCommand.getPlugin(),
        () -> {
          if (domeCommand.isRunning()
              && domeCommand.isEliminated(player)) {

            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage("§cDu bist ausgeschieden!");

          } else {

            World lobby = domeCommand.getLobby();

            if (lobby != null) {
              player.teleport(lobby.getSpawnLocation());
            }

            player.setGameMode(GameMode.SURVIVAL);
          }
        }
    );
  }
}
