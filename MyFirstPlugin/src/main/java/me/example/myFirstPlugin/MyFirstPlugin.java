package me.example.myFirstPlugin;


import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class MyFirstPlugin extends JavaPlugin implements Listener {

  @Override
  public void onEnable() {
    getServer().getPluginManager()
        .registerEvents(this, this);

    getLogger().info("Mein Plugin wurde gestartet!");

    getServer().getPluginManager().registerEvents(new MagicWandListener(), this);

    // Verknüpft den Befehl "heal" mit unserer neuen Klasse
    getCommand("heal").setExecutor(new HealCommand());
    getLogger().info("Plugin gestartet!");
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    event.getPlayer().sendPlainMessage(
        "Hallo " + event.getPlayer().getName() + "!"
    );
  }
}
