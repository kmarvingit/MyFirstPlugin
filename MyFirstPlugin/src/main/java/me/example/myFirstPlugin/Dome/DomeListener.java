
package me.example.myFirstPlugin.Dome;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DomeListener implements Listener {

  private final DomeCommand domeCommand;

  // Spieler, die gestorben sind,
  // aber noch nicht respawnt wurden.
  private final Set<UUID> pendingRespawns = new HashSet<>();

  public DomeListener(DomeCommand domeCommand) {
    this.domeCommand = domeCommand;
  }

  @EventHandler
  public void removeDeadPlayers(PlayerDeathEvent event) {

    Player deadPlayer = event.getEntity();

    // Aktuelle Arena holen
    World arena = domeCommand.getArenaWorld();

    if (!domeCommand.isRunning()) return;
    if (arena == null) return;
    if (!deadPlayer.getWorld().equals(arena)) return;
    if (!domeCommand.isParticipant(deadPlayer)) return;
    if (domeCommand.isEliminated(deadPlayer)) return;

    // Respawn des Spielers später behandeln
    pendingRespawns.add(deadPlayer.getUniqueId());

    // Spieler ausscheiden lassen
    domeCommand.eliminate(deadPlayer);

    // Gewinner im nächsten Tick prüfen
    Bukkit.getScheduler().runTask(
        domeCommand.getPlugin(),
        this::checkWinner
    );
  }

  private void checkWinner() {

    if (!domeCommand.isRunning()) return;

    List<Player> alive = domeCommand.getAliveParticipants();

    if (alive.size() == 1) {

      Player winner = alive.get(0);
      domeCommand.endRound(winner);

    } else if (alive.isEmpty()) {

      domeCommand.endRound(null);
    }
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {

    Player player = event.getPlayer();

    if (!domeCommand.isRunning()) return;
    if (!domeCommand.isParticipant(player)) return;

    // Spieler beim Verlassen eliminieren
    domeCommand.eliminate(player);

    // Verbleibende Spieler im nächsten Tick prüfen
    Bukkit.getScheduler().runTask(
        domeCommand.getPlugin(),
        this::checkWinner
    );
  }

  @EventHandler
  public void onRespawn(PlayerRespawnEvent event) {
    if (domeCommand.isRunning()) {
      Player player = event.getPlayer();
      UUID playerId = player.getUniqueId();

      // Nur Respawns behandeln, die durch
      // einen Tod in Dome PvP entstanden sind.
      if (!pendingRespawns.remove(playerId)) {
        return;
      }

      World arena = domeCommand.getArenaWorld();




        // Runde läuft noch: In Arena respawnen
        event.setRespawnLocation(
            arena.getSpawnLocation()
        );

      // GameMode nach dem Respawn ändern; nach Respawn Event
      Bukkit.getScheduler().runTask(
          domeCommand.getPlugin(),
          () -> {

            if (!player.isOnline()) return;

            // Prüfen, ob es immer noch dieselbe Runde ist; bzw es überhaupt noch läuft
            boolean stillSpectating = domeCommand.isRunning()
                && domeCommand.getArenaWorld() == arena;

            if (stillSpectating) {

              player.setGameMode(GameMode.SPECTATOR);
              player.sendMessage(
                  "§cDu bist ausgeschieden!"
              );

            } else {

              World currentLobby = domeCommand.getLobby();

              if (currentLobby != null
                  && !player.getWorld().equals(currentLobby)) {

                player.teleport(
                    currentLobby.getSpawnLocation()
                );
              }

              player.setGameMode(GameMode.SURVIVAL);
            }
          }
      );
    } else {
      World arena = domeCommand.getArenaWorld();

      if (arena == null) return;
      if (domeCommand.isRunning()) return;
      if (!event.getPlayer().getWorld().equals(arena)) return;

      // Spieler respawnt direkt in der Lobby
      event.setRespawnLocation(
          domeCommand.getLobby().getSpawnLocation()
      );

      // Nach dem Respawn die Arena entladen
      Bukkit.getScheduler().runTask(
          domeCommand.getPlugin(),
          domeCommand::tryUnloadArena
      );
    }

  }


}
