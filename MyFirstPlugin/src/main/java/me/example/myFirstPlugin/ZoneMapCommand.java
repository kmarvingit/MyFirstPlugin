package me.example.myFirstPlugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class ZoneMapCommand implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("Nur Spieler können diesen Befehl ausführen!");
      return true;
    }

    Player player = (Player) sender;

    // 1. Ein gefülltes Karten-Item erstellen
    ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
    MapMeta meta = (MapMeta) mapItem.getItemMeta();

    if (meta != null) {
      // 2. Eine frische, freie MapView vom Server anfordern
      MapView mapView = Bukkit.createMap(player.getWorld());

      // 3. WICHTIG: Alle Standard-Renderer entfernen (sonst zeichnet Minecraft die normale Umgebung darüber)
      for (MapRenderer renderer : mapView.getRenderers()) {
        mapView.removeRenderer(renderer);
      }

      // 4. Unseren eigenen Renderer hinzufügen
      mapView.addRenderer(new ZoneMapRenderer());

      // 5. Die konfigurierte MapView an das Item binden
      meta.setMapView(mapView);
      meta.setDisplayName("§cZonen-Radar");
      mapItem.setItemMeta(meta);
    }

    // 6. Dem Spieler die Karte ins Inventar legen
    player.getInventory().addItem(mapItem);
    player.sendMessage("§aDu hast den §cZonen-Radar §aerhalten!");

    return true;
  }
}