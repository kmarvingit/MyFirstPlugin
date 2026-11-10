package me.example.myFirstPlugin;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class HammerCommand implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("Nur Spieler können diesen Befehl ausführen!");
      return true;
    }

    Player player = (Player) sender;

    // 1. Das Basis-Item festlegen (z. B. eine Eisenaxt)
    ItemStack hammer = new ItemStack(Material.IRON_AXE);

    // 2. Die Metadaten (Eigenschaften) des Gegenstands abrufen
    ItemMeta meta = hammer.getItemMeta();

    if (meta != null) {
      // 3. Den individuellen Namen setzen (§6 = Goldene Farbe)
      meta.setDisplayName("§6Thors Hammer");

      // 4. Die Beschreibung (Lore) als zeilenweise Liste erstellen
      List<String> lore = new ArrayList<>();
      lore.add("§7Geschmiedet in den Tiefen von Asgard.");
      lore.add("§eRechtsklick: §5Beschwört einen gewaltigen Blitz!");
      meta.setLore(lore);

      // 5. Die angepassten Metadaten zurück auf das Item speichern
      hammer.setItemMeta(meta);
    }

    // 6. Dem Spieler den fertigen Hammer ins Inventar legen
    player.getInventory().addItem(hammer);
    player.sendMessage("§aDu hast §6Thors Hammer §aerhalten!");

    return true;
  }
}