package ru.chess;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author deathbysniper
 */
public class ZACommands implements CommandExecutor{
    private ZombieApocalypse main;
    
    public ZACommands(ZombieApocalypse za) {
        main = za;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(commandLabel.equalsIgnoreCase("za") && (args.length == 0 || args[0].equals("commands")))
        {
            sender.sendMessage(ChatColor.GREEN + 
                "----Zombie Apocalypse Commands----\n" + 
                "/za help - displays information about the plugin\n" +
                "/za version - display the version of Z.A.\n" +
                "/za kills - display kills and goal for the current apocalypse\n" +
                "OP only commands:\n" +
                "/za start - starts a zombie apocalypse\n" +
                "/za start <int> - starts a zombie apocalypse with the specified number of zombies per player\n" +
                "/za getitem - gives the name of the item in your hand.");
            return true;
        }
        
        else if(commandLabel.equalsIgnoreCase("za") && args[0].equals("help"))
        {
            sender.sendMessage(ChatColor.GREEN + "----Zombie Apocalypse Information----\n" + 
                               "Every night at 9:00 P.M. there is a 20% chance to start a zombie apocalypse. There is a warning at 8:00 P.M. to remind you to be ready. If a zombie apocalypse happens zombies will spawn around each player. If 25 zombies are killed for each player online everyone will be rewarded. There is a 75% chance to get one diamond, 23% chance to get two diamonds, and a 2% chance to get one of two very good items. Type \"/za commands\" to see a list of all commands.");
            return true;
        }
        
        
        else if(commandLabel.equalsIgnoreCase("za") && args[0].equals("start") && sender.isOp()) //manual start
        {
            if (args.length == 1){ //if they only said /za start, start with default zombies
                main.starter.startApocalypseByCommand(0);
                return true;
            }
            if (args.length == 2){ //if they have 2 args, check if what's after start is an integer
                if (isInteger(args[1])){ //if it's an integer
                    int i = Integer.parseInt(args[1]);
                    if (i > 0)
                    {
                        main.starter.startApocalypseByCommand(i);
                    }
                }
                else{ //invalid entry
                    sender.sendMessage(ChatColor.RED + "ZA: Invalid start, the command must be /za start or /za start <integer>.");
                }
                return true;
            }
                    
        }
        
        
        else if(commandLabel.equalsIgnoreCase("za") && args[0].equals("start") && !sender.isOp()) //don't start if the user isnt OP, yell at them
        {
            sender.sendMessage(ChatColor.RED + "ZA: You need to be an OP to start the apocalypse!");
            return true;
        }
        
        
        else if(commandLabel.equalsIgnoreCase("za") && args[0].equals("version"))
        {
            sender.sendMessage(ChatColor.GOLD + "Zombie Apocalypse " + main.version + "by sheodox");
            return true;
        }
        
        else if(commandLabel.equalsIgnoreCase("za") && args[0].equals("kills"))
        {
            if(main.zahappening) //if during the apocalypse
            {
                sender.sendMessage(ChatColor.GREEN + "ZA: " + ChatColor.DARK_GREEN + main.zkilled + "/" + main.zgoal + ChatColor.GREEN + " zombies killed.");
            }
            else { //if an apocalypse isn't going on
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "ZA: The undead are still sleeping, try again during the apocalypse.");
            }
            return true;
        }
        else if(commandLabel.equalsIgnoreCase("za") && args[0].equals("getitem")){ //toggle returning the return of the items names
            if(sender.isOp())
            {
                Player p = (Player) sender;
                sender.sendMessage(ChatColor.GREEN + "" + p.getItemInHand().getType().toString());
            }
            else{
                sender.sendMessage(ChatColor.RED + "You must be an OP to use this command!");
            }
            return true;
        }
        return false;
    }
    
    /**************************************************************************/
    //used for checking if an argument is an integer
    public static boolean isInteger(String s) {
        try { //try to parse the string to see if it's an integer
            Integer.parseInt(s); 
        } catch(NumberFormatException e) { 
            return false; //it isn't an integer, return false
        }
        return true;
    }
}