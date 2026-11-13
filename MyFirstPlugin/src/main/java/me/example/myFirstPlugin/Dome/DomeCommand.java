package me.example.myFirstPlugin.Dome;


import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DomeCommand implements CommandExecutor {

  private final JavaPlugin plugin;
  private final World world;

  private boolean running = false;
  private BukkitTask finalTask;

  private static final long DURATION = 20L * 60 * 1;

  private final Set<UUID> participants = new HashSet<>();
  private final Set<UUID> eliminated = new HashSet<>();


  public DomeCommand(JavaPlugin plugin, World world) {
    this.plugin = plugin;
    this.world = world;

  }

  public boolean isRunning() {
    return running;
  }


  public boolean isParticipant(Player player) {
    return participants.contains(player.getUniqueId());
  }

  public boolean isEliminated(Player player) {
    return eliminated.contains(player.getUniqueId());
  }

  public void eliminate(Player player) {
    if (isParticipant(player)) {
      eliminated.add(player.getUniqueId());
    }
  }

  public List<Player> getAliveParticipants() {
    List<Player> alive = new ArrayList<>();

    for (UUID id : participants) {
      Player player = Bukkit.getPlayer(id);

      if (player != null
          && player.isOnline()
          && player.getWorld().equals(world)
          //because when player is dead, eliminate is executed
          && !eliminated.contains(id)) {

        alive.add(player);
      }
    }

    return alive;
  }

  @Override
  public boolean onCommand(
      CommandSender sender,
      Command command,
      String label,
      String[] args) {

    if (args.length != 1) {
      sender.sendMessage("§e/dome start | join | leave | stop");
      return true;
    }

    if (args[0].equalsIgnoreCase("start")) {

      if (!sender.hasPermission("myfirstplugin.dome.admin")) {
        sender.sendMessage("§cKeine Berechtigung!");
        return true;
      }

      if (running) {
        sender.sendMessage("§cDie Runde läuft bereits!");
        return true;
      }

      Location center = world.getSpawnLocation();
      WorldBorder border = world.getWorldBorder();

      // Mittelpunkt exakt auf Blockraster ausrichten
      border.setCenter(
          center.getBlockX(),
          center.getBlockZ()
      );

      border.setSize(400.0);
      //no buffer
      border.setDamageBuffer(0.0);
      border.setDamageAmount(1.0);
      border.setWarningDistance(10);

      running = true;

      // WorldBorder schrumpft DURATION lang
      border.changeSize(20.0, DURATION);

      // Nach 10 Minuten Final-Arena erzeugen
      finalTask = Bukkit.getScheduler().runTaskLater(
          plugin,
          () -> {
            if (!running) {
              return;
            }


            DomeFinalArena.createCircle(world, center);


            for (Player player : world.getPlayers()) {
              player.teleport(new Location(world, center.x(), center.y() + 101, center.z()));
              player.sendTitle(
                  "§6FINAL BATTLE!",
                  "§cKämpft bis zum letzten Spieler!",
                  10, 70, 20
              );
            }
          },
          DURATION
      );

      sender.sendMessage("§aDome PvP gestartet!");
      return true;
    }

    if (args[0].equalsIgnoreCase("join")) {

      if (!(sender instanceof Player player)) {
        sender.sendMessage("Nur Spieler können beitreten!");
        return true;
      }

      if (!running) {
        player.sendMessage("§cEs läuft keine PvP-Runde!");
        return true;
      }

      if (isEliminated(player)) {
        player.sendMessage("§cDu bist bereits ausgeschieden!");
        return true;
      }


      if (!participants.add(player.getUniqueId())) {
        player.sendMessage("§eDu bist bereits Teilnehmer!");
        return true;
      }

      player.setGameMode(GameMode.SURVIVAL);

      player.teleport(world.getSpawnLocation());
      player.sendMessage("§aWillkommen in der PvP-Arena!");
      return true;
    }


    if (args[0].equalsIgnoreCase("leave")) {
      if (!sender.hasPermission("myfirstplugin.dome.admin")) {
        sender.sendMessage("§cKeine Berechtigung!");
        return true;
      }

      if (!running) {
        sender.sendMessage("§cEs läuft keine Runde!");
        return true;
      }

      if (sender instanceof Player) {
          Player player = (Player) sender;
          if(world.getPlayers().contains(player) && player.isOnline()){
            List<UUID> list = participants.stream().filter(participantId -> Bukkit.getPlayer(participantId).equals(sender)).toList();
            participants.remove(list.get(0));
            World hub = Bukkit.getWorld("world");
            player.teleport(hub.getSpawnLocation());
          }else{
            sender.sendMessage("Sender ist nicht mehr online/ nicht Teilnehmer");
          }



      } else {
        sender.sendMessage("Sender ist kein Spieler");
      }


    }

    if (args[0].equalsIgnoreCase("stop")) {

      if (!sender.hasPermission("myfirstplugin.dome.admin")) {
        sender.sendMessage("§cKeine Berechtigung!");
        return true;
      }

      if (!running) {
        sender.sendMessage("§cEs läuft keine Runde!");
        return true;
      }


      endRound(null);

      sender.sendMessage("§cPvP-Runde beendet!");
      return true;
    }

    sender.sendMessage("§e/dome start | join | stop");
    return true;
  }

  public void endRound(Player winner) {
    if (!running) return;

    running = false;

    if (finalTask != null) {
      finalTask.cancel();
      finalTask = null;
    }

    world.getWorldBorder().setSize(400.0);

    if (winner != null) {
      Bukkit.broadcastMessage(
          "§6" + winner.getName() + " hat Dome PvP gewonnen!"
      );
    }
  }


  public JavaPlugin getPlugin() {
    return plugin;
  }

  public World getLobby() {
    return Bukkit.getWorld("world");
  }


}