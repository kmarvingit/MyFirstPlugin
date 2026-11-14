package me.example.myFirstPlugin.enums;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public enum CombatLoot {

  // Schwerter
  WOODEN_SWORD(Material.WOODEN_SWORD, LootRarity.COMMON, 1, 1),
  STONE_SWORD(Material.STONE_SWORD, LootRarity.COMMON, 1, 1),
  GOLDEN_SWORD(Material.GOLDEN_SWORD, LootRarity.COMMON, 1, 1),
  IRON_SWORD(Material.IRON_SWORD, LootRarity.COMMON, 1, 1),
  DIAMOND_SWORD(Material.DIAMOND_SWORD, LootRarity.RARE, 1, 1),
  NETHERITE_SWORD(Material.NETHERITE_SWORD, LootRarity.EPIC, 1, 1),

  // Äxte
  WOODEN_AXE(Material.WOODEN_AXE, LootRarity.COMMON, 1, 1),
  STONE_AXE(Material.STONE_AXE, LootRarity.COMMON, 1, 1),
  GOLDEN_AXE(Material.GOLDEN_AXE, LootRarity.COMMON, 1, 1),
  IRON_AXE(Material.IRON_AXE, LootRarity.COMMON, 1, 1),
  DIAMOND_AXE(Material.DIAMOND_AXE, LootRarity.RARE, 1, 1),
  NETHERITE_AXE(Material.NETHERITE_AXE, LootRarity.EPIC, 1, 1),

  // Fernkampf und Spezialwaffen
  BOW(Material.BOW, LootRarity.COMMON, 1, 1),
  CROSSBOW(Material.CROSSBOW, LootRarity.RARE, 1, 1),
  TRIDENT(Material.TRIDENT, LootRarity.EPIC, 1, 1),
  MACE(Material.MACE, LootRarity.EPIC, 1, 1),

  // Lederrüstung
  LEATHER_HELMET(Material.LEATHER_HELMET, LootRarity.COMMON, 1, 1),
  LEATHER_CHESTPLATE(Material.LEATHER_CHESTPLATE, LootRarity.COMMON, 1, 1),
  LEATHER_LEGGINGS(Material.LEATHER_LEGGINGS, LootRarity.COMMON, 1, 1),
  LEATHER_BOOTS(Material.LEATHER_BOOTS, LootRarity.COMMON, 1, 1),

  // Kettenrüstung
  CHAINMAIL_HELMET(Material.CHAINMAIL_HELMET, LootRarity.COMMON, 1, 1),
  CHAINMAIL_CHESTPLATE(Material.CHAINMAIL_CHESTPLATE, LootRarity.COMMON, 1, 1),
  CHAINMAIL_LEGGINGS(Material.CHAINMAIL_LEGGINGS, LootRarity.COMMON, 1, 1),
  CHAINMAIL_BOOTS(Material.CHAINMAIL_BOOTS, LootRarity.COMMON, 1, 1),

  // Goldrüstung
  GOLDEN_HELMET(Material.GOLDEN_HELMET, LootRarity.COMMON, 1, 1),
  GOLDEN_CHESTPLATE(Material.GOLDEN_CHESTPLATE, LootRarity.COMMON, 1, 1),
  GOLDEN_LEGGINGS(Material.GOLDEN_LEGGINGS, LootRarity.COMMON, 1, 1),
  GOLDEN_BOOTS(Material.GOLDEN_BOOTS, LootRarity.COMMON, 1, 1),

  // Eisenrüstung
  IRON_HELMET(Material.IRON_HELMET, LootRarity.COMMON, 1, 1),
  IRON_CHESTPLATE(Material.IRON_CHESTPLATE, LootRarity.COMMON, 1, 1),
  IRON_LEGGINGS(Material.IRON_LEGGINGS, LootRarity.COMMON, 1, 1),
  IRON_BOOTS(Material.IRON_BOOTS, LootRarity.COMMON, 1, 1),

  // Diamantrüstung
  DIAMOND_HELMET(Material.DIAMOND_HELMET, LootRarity.RARE, 1, 1),
  DIAMOND_CHESTPLATE(Material.DIAMOND_CHESTPLATE, LootRarity.RARE, 1, 1),
  DIAMOND_LEGGINGS(Material.DIAMOND_LEGGINGS, LootRarity.RARE, 1, 1),
  DIAMOND_BOOTS(Material.DIAMOND_BOOTS, LootRarity.RARE, 1, 1),

  // Netherite-Rüstung
  NETHERITE_HELMET(Material.NETHERITE_HELMET, LootRarity.EPIC, 1, 1),
  NETHERITE_CHESTPLATE(Material.NETHERITE_CHESTPLATE, LootRarity.EPIC, 1, 1),
  NETHERITE_LEGGINGS(Material.NETHERITE_LEGGINGS, LootRarity.EPIC, 1, 1),
  NETHERITE_BOOTS(Material.NETHERITE_BOOTS, LootRarity.EPIC, 1, 1),

  // Schutz
  SHIELD(Material.SHIELD, LootRarity.COMMON, 1, 1),
  TOTEM(Material.TOTEM_OF_UNDYING, LootRarity.LEGENDARY, 1, 1),
  TURTLE_HELMET(Material.TURTLE_HELMET, LootRarity.RARE, 1, 1),

  // Heilung und Nahrung
  GOLDEN_APPLE(Material.GOLDEN_APPLE, LootRarity.RARE, 1, 4),
  ENCHANTED_GOLDEN_APPLE(Material.ENCHANTED_GOLDEN_APPLE, LootRarity.LEGENDARY, 1, 1),
  COOKED_BEEF(Material.COOKED_BEEF, LootRarity.COMMON, 4, 12),

  // Munition
  ARROW(Material.ARROW, LootRarity.COMMON, 8, 32),
  SPECTRAL_ARROW(Material.SPECTRAL_ARROW, LootRarity.RARE, 4, 16),
  WIND_CHARGE(Material.WIND_CHARGE, LootRarity.RARE, 1, 8),

  // Weitere Kampfgegenstände
  ENDER_PEARL(Material.ENDER_PEARL, LootRarity.RARE, 1, 3),
  SNOWBALL(Material.SNOWBALL, LootRarity.COMMON, 4, 12),
  COBWEB(Material.COBWEB, LootRarity.RARE, 1, 4),
  FLINT_AND_STEEL(Material.FLINT_AND_STEEL, LootRarity.COMMON, 1, 1),
  FISHING_ROD(Material.FISHING_ROD, LootRarity.COMMON, 1, 1),
  LAVA_BUCKET(Material.LAVA_BUCKET, LootRarity.RARE, 1, 1),
  WATER_BUCKET(Material.WATER_BUCKET, LootRarity.COMMON, 1, 1),
  TNT(Material.TNT, LootRarity.RARE, 1, 3);

  private final Material material;
  private final LootRarity rarity;
  private final int minAmount;
  private final int maxAmount;

  CombatLoot(Material material, LootRarity rarity,
             int minAmount, int maxAmount) {
    this.material = material;
    this.rarity = rarity;
    this.minAmount = minAmount;
    this.maxAmount = maxAmount;
  }

  public Material getMaterial() {
    return material;
  }

  public LootRarity getRarity() {
    return rarity;
  }

  public int getMinAmount() {
    return minAmount;
  }

  public int getMaxAmount() {
    return maxAmount;
  }

  public ItemStack createItemStack() {
    int amount = ThreadLocalRandom.current()
        .nextInt(minAmount, maxAmount + 1);

    return new ItemStack(material, amount);
  }
}