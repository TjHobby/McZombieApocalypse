package ru.chess;

import java.io.File;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ZombieApocalypse extends JavaPlugin{

    public boolean zahappening = false;
    public int zkilled = 0;
    public int zgoal;
    public ArrayList<Player> pInvolved = new ArrayList();
    private String worldname;
    public ZAStarter starter;
    public ZAListener listener;
    public ZACommands commands;
    public String version ="1.0";
    
    @Override
    public void onEnable(){
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }
        if (getConfig().getBoolean("configured"))
        {
            commands = new ZACommands(this);
            listener = new ZAListener(this);
            starter = new ZAStarter(this);
            worldname = getConfig().getString("world");
            getServer().getPluginManager().registerEvents(new ZAListener(this), this);
            getServer().getPluginManager().registerEvents(new ZAStarter(this), this);
            
            getServer().getScheduler().scheduleSyncRepeatingTask(this, starter, 0, 100);
            getServer().getScheduler().scheduleSyncRepeatingTask(this, listener, 0, 60);
            getCommand("za").setExecutor(commands);
        }
        else //if the world name isn't configured in config.yml
        {
            Bukkit.getServer().broadcastMessage(ChatColor.RED + "ZA: Please configure Zombie Apocalypse's config.yml");
            getServer().getPluginManager().registerEvents(new AlertConfig(), this);
        }
    }
    
    //fills pInvolved with the online players in the configured world
    public void getPlayersInvolved() {
        pInvolved.clear();
        for (Player p : Bukkit.getServer().getOnlinePlayers()){
            if (p.getWorld().getName().equals(worldname)){
                pInvolved.add(p);
            }
        }
    }
}