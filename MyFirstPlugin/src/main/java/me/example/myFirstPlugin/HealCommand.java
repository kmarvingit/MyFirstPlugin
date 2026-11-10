package me.example.myFirstPlugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HealCommand implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

    // 1. Die Permission prüfen (Hat der Spieler das Recht dazu?)
    if (!sender.hasPermission("meinplugin.heal")) {
      sender.sendMessage("§cDazu fehl dir die Berechtigung!");
      return true;
    }

    // 2. Fall A: Keine Argumente eingegeben (/heal) -> Sich selbst heilen
    if (args.length == 0) {
      if (sender instanceof Player) {
        Player p = (Player) sender;
        p.setHealth(20.0);
        p.setFoodLevel(20);
        p.sendMessage("§aDu hast dich selbst geheilt!");
      } else {
        sender.sendMessage("Die Konsole muss einen Spieler angeben: /heal <Spieler>");
      }
      return true;
    }

    // 3. Fall B: Ein Argument eingegeben (/heal <Spieler>) -> Anderen heilen
    if (args.length == 1) {
      Player target = Bukkit.getPlayer(args[0]);

      if (target == null) {
        sender.sendMessage("§cDer Spieler " + args[0] + " ist nicht online!");
        return true;
      }

      target.setHealth(20.0);
      target.setFoodLevel(20);

      target.sendMessage("§aDu wurdest von " + sender.getName() + " geheilt!");
      sender.sendMessage("§aDu hast " + target.getName() + " geheilt!");
      return true;
    }

    sender.sendMessage("§cVerwendung: /heal [Spieler]");
    return true;
  }
}
