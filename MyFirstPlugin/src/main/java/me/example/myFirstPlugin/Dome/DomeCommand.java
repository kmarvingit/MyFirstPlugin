package me.example.myFirstPlugin.Dome;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class DomeCommand implements CommandExecutor {

  private final JavaPlugin plugin;
  private final World world;

  private boolean running = false;
  private BukkitTask finalTask;

  private static final long DURATION = 20L * 60 * 1;

  public DomeCommand(JavaPlugin plugin, World world) {
    this.plugin = plugin;
    this.world = world;
  }

  @Override
  public boolean onCommand(
      CommandSender sender,
      Command command,
      String label,
      String[] args) {

    if (args.length != 1) {
      sender.sendMessage("§e/dome start | join | stop");
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
      border.setDamageBuffer(0.0);
      border.setDamageAmount(1.0);
      border.setWarningDistance(10);

      running = true;

      // WorldBorder schrumpft 10 Minuten lang
      border.changeSize(20.0, DURATION);

      // Nach 10 Minuten Final-Arena erzeugen
      finalTask = Bukkit.getScheduler().runTaskLater(
          plugin,
          () -> {
            if (!running) {
              return;
            }


            FinalArena.createCircle(world, center);



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

      player.teleport(world.getSpawnLocation());
      player.sendMessage("§aWillkommen in der PvP-Arena!");
      return true;
    }

    if (args[0].equalsIgnoreCase("stop")) {

      if (!sender.hasPermission("myfirstplugin.dome.admin")) {
        sender.sendMessage("§cKeine Berechtigung!");
        return true;
      }

      running = false;

      // Geplantes Finale abbrechen
      if (finalTask != null) {
        finalTask.cancel();
        finalTask = null;
      }

      world.getWorldBorder().setSize(400.0);

      sender.sendMessage("§cPvP-Runde beendet!");
      return true;
    }

    sender.sendMessage("§e/dome start | join | stop");
    return true;
  }
}