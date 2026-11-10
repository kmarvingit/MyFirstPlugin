package me.example.myFirstPlugin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MagicWandListener implements Listener {

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();

    // 1. Prüfen, ob ein Rechtsklick in die Luft oder auf einen Block gemacht wurde
    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {

      // 2. Das Item aus der Hand des Spielers holen
      ItemStack inHand = player.getInventory().getItemInMainHand();

      // 3. Prüfen, ob es eine Eisenaxt mit Metadaten ist
      if (inHand.getType() == Material.IRON_AXE && inHand.hasItemMeta()) {
        ItemMeta meta = inHand.getItemMeta();

        // 4. Prüfen, ob das Item exakt "Thors Hammer" heißt
        if (meta != null && meta.hasDisplayName() && meta.getDisplayName().equals("§6Thors Hammer")) {

          // 5. Prüfen, ob der Spieler die Permission hat
          if (!player.hasPermission("meinplugin.hammer")) {
            player.sendMessage("§cDazu hast du keine Berechtigung!");
            return;
          }

          // 6. Den anvisierten Block holen (max. 50 Blöcke entfernt) und Blitz beschwören
          Block targetBlock = player.getTargetBlockExact(50);

          if (targetBlock != null) {
            targetBlock.getWorld().strikeLightning(targetBlock.getLocation());
            player.sendMessage("§5Thor lässt seinen Blitz einschlagen!");
          }
        }
      }
    }
  }
}