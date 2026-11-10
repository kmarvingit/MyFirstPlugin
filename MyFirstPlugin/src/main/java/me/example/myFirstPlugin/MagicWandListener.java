package me.example.myFirstPlugin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class MagicWandListener implements Listener {

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();

    // Prüfen, ob der Spieler einen Rechtsklick in die Luft oder auf einen Block gemacht hat
    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {

      // Prüfen, ob der Spieler einen Stock (STICK) in der Hand hält
      if (player.getInventory().getItemInMainHand().getType() == Material.STICK) {

        // Prüfen, ob er die Permission dazu hat
        if (!player.hasPermission("meinplugin.wand")) {
          player.sendMessage("§cDazu hast du keine Berechtigung!");
          return;
        }

        // Den Block holen, den der Spieler anvisiert (maximal 50 Blöcke entfernt)
        Block targetBlock = player.getTargetBlockExact(50);

        if (targetBlock != null) {
          // Einen Blitz an dieser Position einschlagen lassen
          targetBlock.getWorld().strikeLightning(targetBlock.getLocation());
          player.sendMessage("§5Ein magischer Blitz wurde beschworen!");
        }
      }
    }
  }
}