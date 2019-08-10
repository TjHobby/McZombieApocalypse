package ru.chess;
/*Jake Harrington
 * This class will keep watching the time until it becomes 9:00 P.M. on the server
 * named "world". Once it does it will generate a random number between 0 and 4,
 * if the number is 0 a zombie apocalypse will start.
 */
import java.util.*;

import net.minecraft.server.v1_14_R1.Schedule;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.entity.*;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class ZAStarter implements Listener, Runnable{
    private Random rng = new Random();
    private ZombieApocalypse main;
    private String worldname = "";
    private int numConfiguredZombies;
    private boolean warningmsg;
    private boolean safemsg;
    private boolean serverMessages;
    private int hardlimit;
    private double apocalypseChance;
    private int delay;
    
    public ZAStarter(ZombieApocalypse za) {
        main = za;
        worldname = main.getConfig().getString("world");
        warningmsg = main.getConfig().getBoolean("warningmsg");
        safemsg = main.getConfig().getBoolean("safemsg");
        hardlimit = main.getConfig().getInt("hardlimit");
        numConfiguredZombies = main.getConfig().getInt("numzombies");
        serverMessages = main.getConfig().getBoolean("logServerMessages");
        apocalypseChance = main.getConfig().getDouble("chance");
        delay = main.getConfig().getInt("delay");
    }
    
    long time;
    int num;
    @Override
    public void run() {
        time = Bukkit.getServer().getWorld(worldname.toString()).getTime();
        num = rng.nextInt(100);
        
        //8:00 warning message
        if(time >= 12000 && time <= 12100 && warningmsg){
            main.getPlayersInvolved();
            for (Player p : main.pInvolved){ //check how many people are in the configured world, it wont' make a zombie apocalypse if nobody is on the world
                p.sendMessage(ChatColor.GREEN + "ZA: There are sounds coming from the ground, the dead might soon awake!");
            }
            if (serverMessages)
                System.out.println("ZA: There are sounds coming from the ground, the dead might soon awake!");
        }
        
        //9:00 possible apocalypse
        if(time >= 13000 && time <= 13100) //9 PM, time to possibly start apocalypse
        {
            if (num < apocalypseChance)
                startApocalypse(false, 0);
            else {
                main.getPlayersInvolved();
                if(safemsg){
                    for (Player p : main.pInvolved)
                        p.sendMessage(ChatColor.GREEN + "ZA: You are safe for another night, the undead still lie in their graves.");
                }
            }
        }
    }
    
    /***************************************************************************
     * Default Manual Start
     **************************************************************************/
    public void startApocalypseByCommand(int zombies)
    {
        startApocalypse(true, zombies); //start the apocalypse
    }
    
    
    //specifiedZombies != 0 when it's a specified manual start, we will make sure this number isn't too high
    public void startApocalypse(boolean isManualStart, int specifiedZombies) {

        if(isManualStart)
            Bukkit.getServer().getWorld(worldname).setTime(13150); //set the time of the world to just after it would naturally roll for an apoc

        main.getPlayersInvolved();

        int zombiesPerPlayer = getZombiesPerPlayer(specifiedZombies);

        main.listener.startApocalypseListener(zombiesPerPlayer);

        if(!main.pInvolved.isEmpty()) {
            for (Player p: main.pInvolved) {
                if (isManualStart) {
                    p.sendMessage(ChatColor.GREEN + "ZA: Manual Start");
                    p.sendMessage(ChatColor.GREEN + "ZA: Starting apocalypse with " + zombiesPerPlayer * main.pInvolved.size() + " zombies.");
                }
                p.sendMessage(ChatColor.BOLD + "ZA: The horde of the undead have been let loose! Survive!");
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,99999,0));
            }
            Bukkit.getServer().getWorld(worldname).setMonsterSpawnLimit(2500);
            Bukkit.getWorld(worldname).setPVP(false);
            new BukkitRunnable()
            {
                @Override
                public void run()
                {spawn(zombiesPerPlayer);}
            }.runTaskTimer(main, 0L, delay);

        }
        else if (serverMessages) {
            System.out.println("ZA: Nobody is around, the disappointed zombies return to their graves.");
        }
    }
    private synchronized void spawn(int zombiesPerPlayer){
        int x, z; //going to be randomly generated for placing zombies
        //without the xOrz the zombies will spawn in four distinct quadrants because both x and z are being offset.
        boolean xOrz; //boolean that decides whether x or z will be offset by 10 to distance the mob from the player
        boolean xneg, zneg; //random if it will be +x/-x or +y/-y from player's location
        //without xneg/zneg zombies will only spawn within a 90 degree area from the player, this allows zombies to spawn all around
        for(Player p: main.pInvolved)
        {
            for (int j = 0; j < zombiesPerPlayer; j++){
                x = rng.nextInt(10); //x coord of zombie, make sure it's at least 10 blocks away from player
                z = rng.nextInt(10); //z coord of zombie
                xOrz =rng.nextBoolean();
                xneg = rng.nextBoolean();
                zneg = rng.nextBoolean();
                if(xOrz)
                    x+=10; //if true, add 10 to x, do this before xneg and zneg, because if they are negative this would offset them closer to the player
                else
                    z+=10; //if false, add 10 to z to offset it from the player
                if (xneg) //spawn -x from player
                    x *= -1;
                if (zneg) //spawn -z from player
                    z *= -1;
                Location loc = p.getLocation();
                loc.setX(loc.getX() + x); //set x of this zombie
                loc.setZ(loc.getZ() + z); //set z of this zombie
                loc.setY(Bukkit.getWorld(worldname).getHighestBlockYAt(loc)); //set y to to the highest block's coordinates for the generated x and y
                if(loc.getBlock().getLightLevel()<10){
                EntityType zombie = EntityType.ZOMBIE;
                LivingEntity spawnedmob = (LivingEntity) Bukkit.getWorld(worldname).spawnEntity(loc, zombie) ;
                spawnedmob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,999000,2));
                spawnedmob.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 999000, 2));
                spawnedmob.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 999000, 1));
                spawnedmob.getEquipment().setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
                spawnedmob.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
}
            }
        }
    }
    private int getZombiesPerPlayer(int specifiedZombies) {
        int zombiesPerPlayer = (specifiedZombies == 0) ? numConfiguredZombies : specifiedZombies;
        int i = main.pInvolved.size();
        
        if (hardlimit > 0) {
            if (zombiesPerPlayer * i > hardlimit && i > 0){
                zombiesPerPlayer = hardlimit / i;
            }
        }
        
        return zombiesPerPlayer;
    }
}
