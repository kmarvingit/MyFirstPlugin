package me.example.myFirstPlugin;

import org.bukkit.Bukkit;
import org.bukkit.GameRules;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class MyFirstPlugin extends JavaPlugin implements Listener {

  @Override
  public void onEnable() {
    // 1. Events registrieren
    getServer().getPluginManager().registerEvents(this, this);
    getServer().getPluginManager().registerEvents(new MagicWandListener(), this);

    // 2. Befehle registrieren
    getCommand("heal").setExecutor(new HealCommand());
    getCommand("gethammer").setExecutor(new HammerCommand());
    getCommand("zonemap").setExecutor(new ZoneMapCommand());

    // 3. Welt laden & Welt-abhängige Befehle registrieren
    World pvpWorld = Bukkit.createWorld(new WorldCreator("pvp_arena"));

    if (pvpWorld != null) {
      pvpWorld.setGameRule(GameRules.PVP, true);
      getCommand("dome").setExecutor(new DomeCommand(pvpWorld));
      getLogger().info("PvP-Arena wurde geladen!");
    }

    getLogger().info("Plugin gestartet!");
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    event.getPlayer().sendPlainMessage("Hallo " + event.getPlayer().getName() + "!");
  }
}