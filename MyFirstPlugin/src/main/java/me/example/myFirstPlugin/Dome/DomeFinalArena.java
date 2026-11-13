package me.example.myFirstPlugin.Dome;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class DomeFinalArena {


  public static void createCircle(World world, Location center) {

    int centerX = center.getBlockX();
    int centerY = center.getBlockY() + 100;
    int centerZ = center.getBlockZ();

    for (int x = -10; x < 10; x++) {
      for (int z = -10; z < 10; z++) {

        world.getBlockAt(
            centerX + x,
            centerY,
            centerZ + z
        ).setType(Material.BEDROCK);

      }
    }
  }
}
