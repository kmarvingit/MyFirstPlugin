package me.example.myFirstPlugin;



import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DomeCommand implements CommandExecutor {

  private final World world;
  private boolean running = false;

  public DomeCommand(World world) {
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

      WorldBorder border = world.getWorldBorder();

      // Mittelpunkt der Arena
      border.setCenter(world.getSpawnLocation());

      // Anfangsgröße: 400 x 400 Blöcke
      border.setSize(400.0);

      // Verkleinerung auf 20 x 20 in 10 Minuten
      border.changeSize(20.0, 20L * 60 * 2);

      // Schaden außerhalb der sicheren Zone
      border.setDamageBuffer(0.0);
      border.setDamageAmount(1.0);

      // Warnung in der Nähe der Grenze
      border.setWarningDistance(10);

      running = true;

      sender.sendMessage("§aDome PvP wurde gestartet!");
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

      // Schrumpfen beenden
      world.getWorldBorder().setSize(400.0);

      running = false;
      sender.sendMessage("§cPvP-Runde beendet!");
      return true;
    }

    sender.sendMessage("§e/dome start | join | stop");
    return true;
  }
}

