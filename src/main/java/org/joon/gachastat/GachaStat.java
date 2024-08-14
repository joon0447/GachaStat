package org.joon.gachastat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.joon.gachastat.Listener.StatListener;
import org.joon.gachastat.Manager.StatManager;

public final class GachaStat extends JavaPlugin {

    private StatManager statManager;

    @Override
    public void onEnable() {
        // Plugin startup logic

        registerManagers();
        registerListeners();
    }

    @Override
    public void onDisable() {

    }

    private void registerManagers(){
        statManager = new StatManager();
    }

    private void registerListeners(){
        Bukkit.getLogger().info("리스너를 등록했습니다.");
        Bukkit.getPluginManager().registerEvents(new StatListener(statManager), this);
    }

    public static GachaStat getInstance(){
        return JavaPlugin.getPlugin(GachaStat.class);
    }

}
