package net.tabletopmc.test;

import org.bukkit.plugin.java.JavaPlugin;

public class TestPlugin extends JavaPlugin {
  @Override
  public void onEnable() {
    getSLF4JLogger().info("Hey!");
  }
}
