package ru.chess;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class AlertConfig implements Listener{
    
    @EventHandler
    public void onLoginAlert(PlayerJoinEvent e){
        Bukkit.getServer().broadcastMessage(ChatColor.RED + "ZA: Please configure the ZombieApocalypse plugin.");
    }
}
