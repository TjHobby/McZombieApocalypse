package ru.chess;

import java.util.ArrayList;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;


public class ZAListener implements Listener, Runnable{
    private ZombieApocalypse main;
    private double killPercent;
    private boolean morning = false;
    private String worldname = "";
    private int numRewardItems;
    private ArrayList<Material> rewardItems = new ArrayList();
    private ArrayList<Integer> rewardChances = new ArrayList();
    private int totalChances = 0;
    private ArrayList<Integer> rewardAmounts = new ArrayList();
    private ArrayList<String> rewardMessages = new ArrayList();
    private Random rng = new Random();
    
    public ZAListener(ZombieApocalypse za){
        main = za;
        worldname = main.getConfig().getString("world");
        killPercent = main.getConfig().getDouble("winpercent");
        
        //custom item configuration
        numRewardItems = main.getConfig().getInt("numitems");
        for (int i = 1; i <= numRewardItems; i++){
            rewardItems.add(Material.getMaterial(main.getConfig().getString("item" + i)));
            int chance = main.getConfig().getInt("chance" + i);
            rewardChances.add(chance + totalChances);
            totalChances += chance;
            rewardAmounts.add(main.getConfig().getInt("amount" + i));
            rewardMessages.add(main.getConfig().getString("message" + i));
        }
    }
    
    @EventHandler
    public void ZombieKilled(EntityDeathEvent e){ //if a mob dies
        if(e.getEntity().toString().equals("CraftZombie") && e.getEntity().getWorld().getName().equals(worldname))
        {

            main.zkilled++; //increment zombie kill counter
            if(main.zahappening && !morning) //if the zombie apocalypse is happening and it's not morning yet
            {
                e.getDrops().clear();
                e.getDrops().add(new ItemStack(Material.DIAMOND,8));
                if(main.zkilled == main.zgoal*0.5) {
                    for (Player p: main.pInvolved){
                        p.sendMessage(ChatColor.GREEN + "ZA: You are halfway there! Slaughter the undead mercilessly!");
                    }
                }
                if(main.zkilled >= main.zgoal) //if the players have successfully defended themselves from the zombies, thus main.zkilled == main.zgoal
                {
                    main.zahappening = false; //stop the zombie apocalypse so it won't say they failed after time expires
                    giveRewards();
                }

            }
        }
    }
    
    private void giveRewards() {
        int reward;
        for (Player p: main.pInvolved)
        {
            p.sendMessage(ChatColor.GREEN + "ZA: Congratulations! You have defended yourself from the undead!");
            reward = rng.nextInt(totalChances) + 1;
            
            int i = 0;
            boolean found = false;
            while (i < numRewardItems && !found) {
                if (reward <= rewardChances.get(i))
                {
                    found = true;
                    p.sendMessage(ChatColor.GOLD + "ZA: " + rewardMessages.get(i));
                    ItemStack itm = new ItemStack(rewardItems.get(i));
                    itm.setAmount(rewardAmounts.get(i));
                    PlayerInventory pi = p.getInventory();
                    pi.addItem(itm);
                }
                i++;
            }
        }
    }
    
    /***************************************************************************
     * START APOCALYPSE
     **************************************************************************/
    
    public void startApocalypseListener(int numZombies){
        main.zkilled = 0; //set the amount of zombies killed to 0
        
        main.zgoal = (int) (main.pInvolved.size() * (killPercent * numZombies));
        for (Player p: main.pInvolved){
            p.sendMessage(ChatColor.GREEN + "ZA: Kill " + main.zgoal + " zombies before dawn!");
        }
        main.zahappening = true;
    }

    @Override
    public void run() {
        if (worldname.equals("")){
            worldname = main.getConfig().getString("world");
        }
        long time = Bukkit.getServer().getWorld(worldname).getTime();
        if ((time >= 23000 && time <= 23060) || (time >= 25 && time <= 85))
        {
            if (main.zahappening)
            {
                main.zahappening = false;
                for (Player p: Bukkit.getOnlinePlayers()){
                    if (p.getWorld().toString().equals("CraftWorld{name="+worldname+"}")){
                        p.sendMessage(ChatColor.RED + "ZA: You have failed! The zombies have overwhelmed you.");
                    }
                }
            }
        }
    }
    
    /***************************************************************************
     * OTHER EVENT HANDLERS, NOT DEALING WITH APOCALYPSE
     **************************************************************************/
    
    @EventHandler //called when a player joins the server
    public void PlayerWelcome(PlayerJoinEvent e){
        e.getPlayer().sendMessage(ChatColor.GREEN + "ZA: This server runs the Zombie Apocalypse plugin by The_Chess");
        e.getPlayer().sendMessage(ChatColor.GREEN + "ZA: Type /za help to see information about ZA.");
    }
    
    @EventHandler //called when a player tries to enter bed, forces them not to sleep
    public void kickOutOfBed(PlayerBedEnterEvent e){
        if(main.zahappening){
            e.getPlayer().sendMessage(ChatColor.RED + "ZA: You're too scared to sleep during the zombie apocalypse.");
            e.setCancelled(true);
        }
    }
}