package me.example.myFirstPlugin;

import me.example.myFirstPlugin.Dome.DomeCommand;
import me.example.myFirstPlugin.Dome.DomeListener;
import org.bukkit.Bukkit;
import org.bukkit.GameRules;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class MyFirstPlugin extends JavaPlugin implements Listener {

  @Override
  public void onEnable() {

    // 1. Events registrieren
    getServer().getPluginManager().registerEvents(this, this);
    getServer().getPluginManager().registerEvents(
        new MagicWandListener(), this
    );

    // 2. Befehle registrieren
    getCommand("heal").setExecutor(new HealCommand());
    getCommand("gethammer").setExecutor(new HammerCommand());
    getCommand("zonemap").setExecutor(new ZoneMapCommand());

    // 3. PvP-Welt laden
    World pvpWorld = Bukkit.createWorld(
        new WorldCreator("pvp_arena")
    );

    if (pvpWorld != null) {

      pvpWorld.setGameRule(GameRules.PVP, true);

      // 4. DomeCommand erstellen
      DomeCommand domeCommand = new DomeCommand(this, pvpWorld);

      // 5. DomeCommand registrieren
      getCommand("dome").setExecutor(domeCommand);

      // 6. DomeListener mit derselben Instanz registrieren
      DomeListener domeListener = new DomeListener(
          pvpWorld,
          domeCommand
      );

      getServer().getPluginManager().registerEvents(
          domeListener,
          this
      );

      getLogger().info("PvP-Arena wurde geladen!");

    } else {
      getLogger().severe("PvP-Arena konnte nicht geladen werden!");
    }

    getLogger().info("Plugin gestartet!");
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    event.getPlayer().sendPlainMessage(
        "Hallo " + event.getPlayer().getName() + "!"
    );
  }
}