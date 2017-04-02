package me.zachbears27;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.connorlinfoot.actionbarapi.ActionBarAPI;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.zachbears27.MySQL;
import net.md_5.bungee.api.ChatColor;

/* 
JumbleHill: A spigot plugin for managing shops in the Jumble Hill district, on MumboCraft Season 3

This Plugin Is Made For MumboCraft & It's Players & Staff.. Any misuse or re-distribution of this plugin will call for said plugin to be removed

Written by A_Brave_Panda & Jamdoggy

 Permission Nodes:
   jumblehill.admin - can change details of any shop regardless of ownership (add, remove, set, delete)
   jumblehill.stats - can view fill list of shops and their trades
*/

// Class: JumbleHill
// Main Plugin Class: Adds the /jumblehill command
public class JumbleHill extends JavaPlugin implements Listener{
	
 Server server = Bukkit.getServer();
 ConsoleCommandSender console = server.getConsoleSender();

 MySQL MySQLC = null;
 Connection c = null;

 // Grab information from the config file
 private String host      = getConfig().getString("Hostname");
 private String port      = getConfig().getString("Port");
 private String db        = getConfig().getString("Database");
 private String username  = getConfig().getString("Username");
 private String password  = getConfig().getString("Password");
 private String table     = getConfig().getString("ShopTable");
 private String itemtable = getConfig().getString("ItemTable");
 private String intro     = ChatColor.GREEN + "" + ChatColor.BOLD + "Jumble Hill " + ChatColor.GRAY + "" + ChatColor.BOLD + ">> ";

 @Override
 public void onDisable() {

 }

 @Override
 public void onEnable() {
	getServer().getPluginManager().registerEvents(this, this);
	registerConfig();
	
	// Open a connection to the MySQL database
	MySQL MySQLC = new MySQL(host, port, db, username, password);
	try {
		c = MySQLC.openConnection();
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
	} catch (SQLException e) {
		e.printStackTrace();
	}
	
	System.out.print("[JumbleHill] Plugin Has Been Loaded!");
	
	
 }
 
 public void registerConfig() {
 	saveDefaultConfig();
 }
 
 private Boolean checkItemName(String name)
 {
   String upperMat = name.toUpperCase();	 
   Material mat = Material.getMaterial(upperMat);

   return ((mat == null) ? false : true);
   
 }
 
 private String getNameFromUUID(String player_uuid)
 {
	 Player this_p;
	 OfflinePlayer this_op;
	 this_p = Bukkit.getPlayer(UUID.fromString(player_uuid));
	 this_op = Bukkit.getOfflinePlayer(UUID.fromString(player_uuid));
	   
	 // Did we find the player...?
	 if (this_p != null) {
		 return this_p.getName();
	 } else {
		 return this_op.getName();
	 }
 }
 
 private UUID getUUIDFromName(String playername)
 {
	 
	 //Player => UUIDS
	 Player this_p;
     this_p = Bukkit.getPlayer(playername);
     UUID playeruuid = this_p.getUniqueId();
  
     	return playeruuid;
 }
 
 
 public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 // Check for a real person
 if (sender instanceof Player) { 
   	Player p = (Player)sender;
   	
    if(cmd.getLabel().equalsIgnoreCase("jumblehill")) {  // Command is /jumblehill
   	  if(args.length == 0 ) {
   		  p.sendMessage(intro + ChatColor.RED + " Incorrect Arguments! Please try: '/jumblehill help'");

	  } else if(args[0].equalsIgnoreCase("help")) {   // Command: /jumblehill help
		  if(args.length == 2) {
		    if (args[1].equals("search")) {
				p.sendMessage(intro + ChatColor.AQUA + "/jumblehill search <item name>");
				p.sendMessage(intro + ChatColor.WHITE + "Searches for a shop selling the specified item.");
				p.sendMessage(intro + ChatColor.WHITE + "Returns a list of shops.");
		    } else if (args[1].equals("stats")) {
			    if (!(p.hasPermission("jumblehill.stats"))) {
					p.sendMessage(ChatColor.RED + "You do not have permission to view shop stats.");
				} else {
					p.sendMessage(intro + ChatColor.AQUA + "/jumblehill stats");
					p.sendMessage(intro + ChatColor.WHITE + "Lists all shops and their trades.");
				}
		    } else if (args[1].equals("set")) {
				p.sendMessage(intro + ChatColor.AQUA + "/jumblehill set");
				p.sendMessage(intro + ChatColor.WHITE + "Create your shop or update your shop co-ordinates.");
				p.sendMessage(intro + ChatColor.WHITE + "If an unknown shop name is enetered, a new shop is created.");
		    } else if (args[1].equals("add")) {
				p.sendMessage(intro + ChatColor.AQUA + "/jumblehill add <item name> <item amount> <item price in diamonds>");
				p.sendMessage(intro + ChatColor.WHITE + "Adds a new searchable trade to your shop.");
				p.sendMessage(intro + ChatColor.WHITE + "Example: /jumblehill add stone 64 1");
		    } else if (args[1].equals("remove")) {
				p.sendMessage(intro + ChatColor.AQUA + "/jumblehill remove <item name>");
				p.sendMessage(intro + ChatColor.WHITE + "Remove a trade listing from your shop");
				p.sendMessage(intro + ChatColor.WHITE + "If more than one trade of a particular item exists, the first will be removed.");
		    } else if (args[1].equals("list")) {
				p.sendMessage(intro + ChatColor.AQUA + "/jumblehill list");
				p.sendMessage(intro + ChatColor.WHITE + "List all trades for a particular shop.");
		    } else if (args[1].equals("delete")) {
				p.sendMessage(intro + ChatColor.AQUA + "/jumblehill delete");
				p.sendMessage(intro + ChatColor.WHITE + "Completely remove a shop, and all of it's trades, from the database.");
		    } else if (args[1].equals("help")) {
				p.sendMessage(intro + ChatColor.AQUA + "/jumblehill help [subcommand]");
				p.sendMessage(intro + ChatColor.WHITE + "Provides help for the /jumblehill command, and all of it's subcommands");
			} else {
				p.sendMessage(intro + ChatColor.AQUA + "/jumblehill search <item name>");
			    if(p.hasPermission("jumblehill.stats")) {
				  p.sendMessage(intro + ChatColor.AQUA + "/jumblehill stats");
				}
				p.sendMessage(intro + ChatColor.AQUA + "/jumblehill set");
				p.sendMessage(intro + ChatColor.AQUA + "/jumblehill add <item name> <item amount> <item price in diamonds>");
				p.sendMessage(intro + ChatColor.AQUA + "/jumblehill remove <item name>");
				p.sendMessage(intro + ChatColor.AQUA + "/jumblehill delete");
				p.sendMessage(intro + ChatColor.AQUA + "/jumblehill list");
				p.sendMessage(intro + ChatColor.AQUA + "/jumblehill help [subcommand]");
			}
		  } else {
				p.sendMessage(intro + ChatColor.AQUA + "/jumblehill set");
				p.sendMessage(intro + ChatColor.AQUA + "/jumblehill add <item name> <item amount> <item price in diamonds>");
				p.sendMessage(intro + ChatColor.AQUA + "/jumblehill remove <item name>");
				p.sendMessage(intro + ChatColor.AQUA + "/jumblehill delete");
				p.sendMessage(intro + ChatColor.AQUA + "/jumblehill list");
				p.sendMessage(intro + ChatColor.AQUA + "/jumblehill help [subcommand]");
		  }

	  } else if(args[0].equalsIgnoreCase("search")) {   // Command: /jumblehill search <item name>
	  
		if (args.length != 2) {
			// Wrong number of args
			p.sendMessage("Please enter an item type to search for.");
			p.sendMessage("See" + ChatColor.AQUA + "/jumblehill help search" + ChatColor.WHITE + " for more info");
		} else if (!checkItemName(args[1])) {
			// Failed item name validation check
			
			if(Bukkit.getPluginManager().getPlugin("ActionBarAPI") != null) {
				p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3.0F, 0.5F);
				ActionBarAPI.sendActionBar(p, "Sorry, " + ChatColor.AQUA + args[1] + ChatColor.WHITE + " is not a valid minecraft item name.", 1000);
			} else {
				p.sendMessage("Sorry, " + ChatColor.AQUA + args[1] + ChatColor.WHITE + " is not a valid minecraft item name.");
			}
		} else {
		  // Checks passed - perform the search
		  String itemselect = "SELECT * FROM " + itemtable + " WHERE itemname = '" + args[1] +"' ORDER BY shop_id;";
		  String shopselect = "";
		  try {
	  
			int count = 0;       // count: A counter for the total number of items found
			int shopcount = 0;   // shopcount: A counter for number of shops found
			int lastID = 0;      // lastID: The ID number of the last shop found

			// Set up MySQL statement ready for use below
			Statement statement = c.createStatement();
			ResultSet res2 = null;

			// Run itemselect query to get list of items, in shop order
			Statement statement2 = c.createStatement();
			ResultSet res = statement2.executeQuery(itemselect);

			if(Bukkit.getPluginManager().getPlugin("ActionBarAPI") != null) {
				ActionBarAPI.sendActionBar(p, ChatColor.BOLD + "Searching for: " + ChatColor.GREEN + ChatColor.BOLD + args[1], 1000);
			} else {
				p.sendMessage(intro + ChatColor.BOLD + "Searching for: " + ChatColor.GREEN + ChatColor.BOLD + args[1]);
			}


			while(res.next()) {
			  count++;

			  // If we have a new Shop ID - show the name of this shop
			  if (lastID != res.getInt("shop_id")) {
				lastID = res.getInt("shop_id"); 
				shopcount++;
     
				// Grab shop details from the database
				shopselect = "SELECT * FROM " + table + " WHERE id = '" + res.getInt("shop_id") +"';";
				res2 = statement.executeQuery(shopselect);
				if(res2.next()) {
				  p.sendMessage(intro + ChatColor.GOLD + "Shop: " + ChatColor.GREEN + ChatColor.BOLD + res2.getString("shopname") + ChatColor.GOLD + " at" + ChatColor.GREEN + ChatColor.BOLD + " [" + res2.getInt("x") + ", " + res2.getInt("y") + ", " + res2.getInt("z") + "]" + ChatColor.GOLD + " owned by " + ChatColor.GREEN + ChatColor.BOLD + getNameFromUUID(res2.getString("owner")));
				} else {
				  // ahould never get here, but just in case there's items in the databse with a deleted shop ID...
				  p.sendMessage("Invalid Shop");
				}
			  } 

			  // Show this line on the item list
			  p.sendMessage(intro + ChatColor.GOLD + "   * " + ChatColor.GREEN + ChatColor.BOLD + res.getInt("itemamount") + " " + res.getString("itemname") + ChatColor.GOLD + " = " + ChatColor.GREEN + ChatColor.BOLD + res.getInt("itemprice") + " diamonds.");
			} 
			
			//if items were found, show how many
			if (count == 0) {
			  p.sendMessage(intro + ChatColor.RED + "Sorry, no shops in Jumble Hill sell that item.");
			} else {
			  p.sendMessage(intro + ChatColor.GREEN + "" + ChatColor.BOLD + count + ChatColor.GOLD + " Trades In Jumble Hill Have Been Found.");
			  p.sendMessage(intro + ChatColor.GREEN + "" + ChatColor.BOLD + shopcount + ChatColor.GOLD + " Shops Are Selling That Item.");
			}
		  }
   	   	  catch (SQLException e) {
   	   		// TODO Auto-generated catch block
   	   		e.printStackTrace();
   	   	  }
		}

   	  } else if(args[0].equalsIgnoreCase("set")) { // Command: /jumblehill set

		// Grab current co-ordinates of the player
	    Location l = p.getLocation();
	    double x = l.getX();
	    double y = l.getY();
	    double z = l.getZ();
   		  
   		new AnvilGUI(this, p, new AnvilGUI.AnvilClickHandler() {
	   	  @Override
	   	  public boolean onClick(AnvilGUI menu, String text){
	  	   	String cleantext = text.replace("'", "").replace("Shop Name", "");
	  		String ShopName = "INSERT INTO "+ table + " (id, shopname, owner, x, y, z) VALUES (NULL, '" + cleantext + "', '" + p.getUniqueId() + "', '" + x + "', '" + y + "', '" + z + "');";
	  		String select = "SELECT * FROM " + table + " WHERE shopname = '" + cleantext +"';";
	  		try {
				// Does this shop name already exist in the database...?
	  			Statement statement = c.createStatement();
	  			ResultSet res = statement.executeQuery(select);

	  			//If shop exists, update location
	  			if(res.next()) {
	  			  //shop exists - if player is the owner of shop, then update the co-ordinates
	  			  if(menu.getPlayer().getName().equalsIgnoreCase(res.getString("owner")) || (menu.getPlayer().hasPermission("jumblehill.admin"))) {
	  				menu.getPlayer().sendMessage(intro + ChatColor.GOLD + "Your shop is called: " + ChatColor.AQUA + cleantext);
			  		menu.getPlayer().sendMessage(intro + ChatColor.GOLD + "Your shop is now located at: " + ChatColor.AQUA + (int)x + ", " + (int)y + ", " + (int)z);
			  		menu.getPlayer().sendMessage(intro + ChatColor.GOLD + "Remember, you can always use this command again to re-update your shop location!");
	  				menu.getPlayer().sendMessage(intro + ChatColor.GREEN + "Shop Location Updated!");
	  				
					String UpdateShopName = "UPDATE " + table + " SET shopname='" + cleantext + "', owner='" + p.getUniqueId() + "', x='" + x + "', y='" + y + "', z='" + z + "' WHERE id='" + res.getInt("id") + "';";
	  				statement.executeUpdate(UpdateShopName);
	  			  } else {
					// Player is not the owner
	  				if(Bukkit.getPluginManager().getPlugin("ActionBarAPI") != null) {
	  					p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3.0F, 0.5F);
	  					ActionBarAPI.sendActionBar(p, intro + ChatColor.RED + "You must own " + cleantext + " to move it!", 1000);
	  				} else {
	  				   menu.getPlayer().sendMessage(intro + ChatColor.RED + "You must own " + cleantext + " to move it!");
	  				}
	  			 
	  			  }
	  			} else {
	  			  // The specified shop name doesn't exist, so add a new one
	  			  menu.getPlayer().sendMessage(intro + ChatColor.GOLD + "Your shop is called: " + ChatColor.AQUA + cleantext);
	  		  	  menu.getPlayer().sendMessage(intro + ChatColor.GOLD + "Your shop is located at: " + ChatColor.AQUA + (int)x + ", " + (int)y + ", " + (int)z);
	  		  	  menu.getPlayer().sendMessage(intro + ChatColor.GOLD + "Remember, you can always use this command again to re-update your shop location!");
	  			  statement.executeUpdate(ShopName);
	  			if(Bukkit.getPluginManager().getPlugin("ActionBarAPI") != null) {
					p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3.0F, 0.5F);
					ActionBarAPI.sendActionBar(p, intro + ChatColor.GREEN + "Shop Location Created!", 1000);
				} else {
					menu.getPlayer().sendMessage(intro + ChatColor.GREEN + "Shop Location Created!");
				}
	  			  
	  			}
	  		} catch (SQLException e2) {	  				
	  			e2.printStackTrace();
	  		}
	   		return true;
	   	  }
	   	}).setInputName("Shop Name").open();
        
	  } else if(args[0].equalsIgnoreCase("add")) { // Command: /jumblehill add <itemname> <number> <price>
		  if(args.length == 4) {
			// Show Anvil GUI to get shop name
	   		new AnvilGUI(this, p, new AnvilGUI.AnvilClickHandler() {
	   		     @Override
	   		     public boolean onClick(AnvilGUI menu, String text){
	  		    	String cleantext = text.replace("'", "").replace("Shop Name", "");
	  		    	String nameselect = "SELECT * FROM " + table + " WHERE shopname = '" + cleantext +"';";	  	
	  		    	
	  		    	try {
                        Statement statement = c.createStatement();
                        ResultSet res = statement.executeQuery(nameselect);
                        if (res.next()) {
                          if(p.getUniqueId().toString().equals(res.getString("owner")) || (p.hasPermission("jumblehill.admin"))) {
							Boolean already_there = false; // Trade was already in database, and has been updated?
							Boolean first_trade = true;    // Shop had no trades, so new one is the first?
							String itemselect = "SELECT * FROM " + itemtable + " WHERE shop_id = '" + res.getInt("id") +"';";
                            Statement statement2 = c.createStatement();
                            ResultSet res2 = statement2.executeQuery(itemselect);

                            // Items found for this shop? - check them to see if we need to update a trade
                            while (res2.next()) {
							  if(res2.getString("itemamount").equals(args[2]) && res2.getString("itemname").equals(args[1])) {
								  if (!checkItemName(args[1])) {
										// Failed item name validation check
										p.sendMessage("Sorry, " + ChatColor.AQUA + args[1] + ChatColor.WHITE + " is not a valid minecraft item name.");
								  } else {
								// This item and amount are already in the database, just update the price
								String itemupdate = "UPDATE " + itemtable + " SET itemprice='" + args[3] + "' WHERE shop_id = '" + res.getInt("id") +"';";
								Statement statement3 = c.createStatement();
								statement3.executeUpdate(itemupdate);
								p.sendMessage(intro + ChatColor.GOLD + "Price Updated: " + ChatColor.GREEN + ChatColor.BOLD + args[2] + " " + args[1] + ChatColor.GOLD + " Now Costs " + ChatColor.GREEN + ChatColor.BOLD + args[3] + ChatColor.GOLD + " Diamond(s).");
								already_there = true;
							  }
							  }
							  first_trade = false; // We have at least 1 trade in the database
							}

							// Did we already find and update the value - if not, make a new one
							if (already_there == false) {
								if (!checkItemName(args[1])) {
									// Failed item name validation check
						  			if(Bukkit.getPluginManager().getPlugin("ActionBarAPI") != null) {
										p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3.0F, 0.5F);
										ActionBarAPI.sendActionBar(p, "Sorry, " + ChatColor.AQUA + args[1] + ChatColor.WHITE + " is not a valid minecraft item name.", 1000);
									} else {
										p.sendMessage("Sorry, " + ChatColor.AQUA + args[1] + ChatColor.WHITE + " is not a valid minecraft item name.");
									}
									
								} else {
								// Add a new price for this shop
								String iteminsert = "INSERT INTO "+ itemtable + " (id, shop_id, itemname, itemprice, itemamount) VALUES (NULL, '" + res.getInt("id") + "', '" + args[1] + "', '" + args[3] + "', '" + args[2] + "');";
								Statement statement4 = c.createStatement();
								statement4.executeUpdate(iteminsert);
								p.sendMessage(intro + ChatColor.GOLD + "Price Added: " + ChatColor.GREEN + ChatColor.BOLD + args[2] + " " + args[1] + ChatColor.GOLD + " Costs "+ ChatColor.GREEN + ChatColor.BOLD + args[3] + ChatColor.GOLD + " Diamond(s).");
								if (first_trade) {
								  p.sendMessage(intro + ChatColor.GREEN + "Congratulations on adding your first price to your shop!");
								}
								}
							}
                          } else {
                             // Not the shop owner or an admin
  				  			if(Bukkit.getPluginManager().getPlugin("ActionBarAPI") != null) {
								p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3.0F, 0.5F);
								ActionBarAPI.sendActionBar(p, intro + ChatColor.RED + "Oi! You don't own this shop, bugger off!" + ChatColor.DARK_RED + " - Jamdoggy @ 2017" , 1000);
							} else {
	                        	  p.sendMessage(intro + ChatColor.RED + "Oi! You don't own this shop, bugger off!" + ChatColor.DARK_RED + " - Jamdoggy @ 2017" );
							}
                          }
                        } else {
                        // Shop not found in database...?
                        	p.sendMessage(intro + ChatColor.RED + "That Shop Doesn't Exist!");
                        }
                    } catch (SQLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
					return true;
	   		      }
	   		}).setInputName("Shop Name").open();
		  } else {
	   		  p.sendMessage(intro + ChatColor.RED + "Incorrect Arguments! Please try: '/jumblehill add {item name} {item amount} {item price in diamonds}'");
		  }
			  	
	  } else if(args[0].equalsIgnoreCase("remove")) {  // Command: /jumblehill remove <item name>
		  
	    if(!(args.length == 1)) {
		  new AnvilGUI(this, p, new AnvilGUI.AnvilClickHandler() {
	   		@Override
	   		public boolean onClick(AnvilGUI menu, String text){
	  		  String cleantext = text.replace("'", "").replace("Shop Name", "");
	  		  String nameselect = "SELECT * FROM " + table + " WHERE shopname = '" + cleantext +"';";	
  		    	
			  try {
				// Grab the shop details from the database, based on entered name
				Statement statement = c.createStatement();
				ResultSet res = statement.executeQuery(nameselect);
				
				if(res.next()) {

				// Does player have permission to change this shop?
				  if(res.getString("owner").equals(p.getUniqueId().toString()) || (p.hasPermission("jumblehill.admin"))) {
					  
					// Check that there are the specified items in the shop
					int shop_id = res.getInt("id");
					String itemselect = "SELECT * FROM " + itemtable + " WHERE itemname = '" + args[1] +"' and shop_id = '" + shop_id + "';";
					Statement statement2 = c.createStatement();
					ResultSet res2 = statement2.executeQuery(itemselect);
					
					if(res2.next()) {
						String itemremove = "DELETE FROM " + itemtable + " WHERE itemname = '" + args[1] +"' and shop_id = '" + shop_id + "' LIMIT 1;";
						Statement statement3 = c.createStatement();
						statement3.executeUpdate(itemremove);
						p.sendMessage(intro + ChatColor.GREEN + "Item sucessfully removed!");
					} else {
					  p.sendMessage(intro + ChatColor.RED + "This item isn't in that shop!");
					}
				  } else {
					p.sendMessage(intro + ChatColor.RED + "Sorry, you don't own this shop!");
				  }
				} else {
				  p.sendMessage(intro + ChatColor.RED + "Error! No shop with that name exists!");
				}
			  } catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			  }
			  return true;
			}
		  }).setInputName("Shop Name").open();
	    } else {
		  p.sendMessage(intro + ChatColor.RED + "Incorrect Arguments! Please try: '/jumblehill remove <item name>'");
	    }
	  }  else if(args[0].equalsIgnoreCase("list")) {  // Command: /jumblehill list
 
        // Use Anvil GUI to grab shop name
		new AnvilGUI(this, p, new AnvilGUI.AnvilClickHandler() {
  		@Override
  		  public boolean onClick(AnvilGUI menu, String text){
			String cleantext = text.replace("'", "").replace("Shop Name", "");
 		    String nameselect = "SELECT * FROM " + table + " WHERE shopname = '" + cleantext +"';";	

			try {
				// Find Shop
				Statement statement = c.createStatement();
				ResultSet res = statement.executeQuery(nameselect);
					
				if(res.next()) {
				  // Grab list of items for this shop
				  int shop_id = res.getInt("id");
				  String itemselect = "SELECT * FROM " + itemtable + " WHERE shop_id = '" + shop_id +"';";
				  Statement statement2 = c.createStatement();
				  ResultSet res2 = statement2.executeQuery(itemselect);
				  
				  // Show shop details
				  p.sendMessage(intro + ChatColor.GREEN + "" + ChatColor.BOLD + res.getString("shopname") + ChatColor.GOLD + " Owned By: " + ChatColor.GREEN + ChatColor.BOLD + getNameFromUUID(res.getString("owner")) + ChatColor.GOLD + " Located At: [" + ChatColor.GREEN + ChatColor.BOLD + res.getString("x") + ", " + res.getString("y") + ", " + res.getString("z") + ChatColor.GOLD + "]");
				  
				  // Show item list
				  while(res2.next()) {
					p.sendMessage(intro + ChatColor.GRAY + "   - " + ChatColor.GREEN + "" + ChatColor.BOLD + "x" + res2.getString("itemamount") + " " + res2.getString("itemname") + ChatColor.GOLD + " For " + ChatColor.GREEN + ChatColor.BOLD + res2.getString("itemprice") + ChatColor.GOLD + " Diamond(s)!");
				  }
				} else {
				  p.sendMessage(ChatColor.RED + "The entered shop name does not exist.");
				}
					
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 		    return true;
	      }
		}).setInputName("Shop Name").open();
 
      }  else if(args[0].equalsIgnoreCase("stats")) {   // Command: /jumblehill stats
	 
	    if(p.hasPermission("jumblehill.stats")) {
	      String shoplist = "SELECT * FROM " + table + ";";  
	      try {
	          Statement statement = c.createStatement();
	          ResultSet res = statement.executeQuery(shoplist);
	   
	          // Go through list of shops    
	          while(res.next()) {
	              int shop_id = res.getInt("id");
	   
	              // Output this Shop's Info         
	              p.sendMessage(ChatColor.GRAY + "[----------------------]");
	              p.sendMessage(intro + ChatColor.GREEN + ChatColor.BOLD + res.getString("shopname") + ChatColor.GOLD + " Owned By: " + ChatColor.GREEN + ChatColor.BOLD + getNameFromUUID(res.getString("owner")) + ChatColor.GOLD + " Located At: [" + ChatColor.GREEN + ChatColor.BOLD + res.getString("x") + ", " + res.getString("y") + ", " + res.getString("z") + ChatColor.GOLD + "]");
	   
	              // Get the list of items in this shop
	              String itemselect = "SELECT * FROM " + itemtable + " WHERE shop_id = '" + shop_id +"';";
	              Statement statement2 = c.createStatement();
	              ResultSet res2 = statement2.executeQuery(itemselect);
	              Boolean noitems = true;

				  // Output list of trades in this shop
	              while(res2.next()) {
	                  noitems = false;
	                  p.sendMessage(intro + ChatColor.GRAY + "   - " + ChatColor.GREEN + ChatColor.BOLD + "x" + res2.getString("itemamount") + " " + res2.getString("itemname") + ChatColor.GOLD + " For " + ChatColor.GREEN + ChatColor.BOLD + res2.getString("itemprice") + ChatColor.GOLD + " Diamond(s)!");
	              }
	              if (noitems) {
	                  p.sendMessage(intro + ChatColor.GRAY + "   -" + ChatColor.RED + " Shop has no items!");
	              }
	          }
	        } catch (SQLException e) {
	          // TODO Auto-generated catch block
	          e.printStackTrace();
	        }
	    } else {
	      p.sendMessage(intro + ChatColor.RED + "Sorry, you do not have permission to list all shops");
	    }

	  }  else if(args[0].equalsIgnoreCase("delete")) {  // Command: /jumblehill delete
 
        // Use Anvil GUI to grab shop name
		new AnvilGUI(this, p, new AnvilGUI.AnvilClickHandler() {
  		@Override
  		  public boolean onClick(AnvilGUI menu, String text){
			String cleantext = text.replace("'", "").replace("Shop Name", "");
 		    String nameselect = "SELECT * FROM " + table + " WHERE shopname = '" + cleantext +"';";	

			try {
				// Find Shop
				Statement statement = c.createStatement();
				ResultSet res = statement.executeQuery(nameselect);
					
				if(res.next()) {
	  			  if(menu.getPlayer().getName().equalsIgnoreCase(res.getString("owner")) || (menu.getPlayer().hasPermission("jumblehill.admin"))) {
					// First remove list of items for this shop
					int shop_id = res.getInt("id");
					String itemremove = "DELETE FROM " + itemtable + " WHERE shop_id = '" + shop_id +"';";
					Statement statement2 = c.createStatement();
					statement2.executeUpdate(itemremove);
				  
					// Now, remove the shop itself
					itemremove = "DELETE FROM " + table + " WHERE id = '" + shop_id +"';";
					statement2.executeUpdate(itemremove);
					p.sendMessage(intro + ChatColor.RED + "Shop deleted.");
				  } else {
					p.sendMessage(intro + ChatColor.RED + "Only the shop owner or a Moderator may delete the shop");
				  }
				} else {
					p.sendMessage(intro + ChatColor.RED + "The specified shop does not exist.");
				}
					
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 		    return true;
	      }
		}).setInputName("Shop Name").open();

	  } else if(args[0].equalsIgnoreCase("perms")) {  // Command: /jumblehill perms

		p.sendMessage(ChatColor.GOLD + "Jumble Hill Permissions:");
		
        // Stuff that everyone has access to
		p.sendMessage(ChatColor.GOLD + "* You can add a new shop (" + ChatColor.AQUA + "/jumblehill set" + ChatColor.GOLD + ")");
		p.sendMessage(ChatColor.GOLD + "* You can perform an item search (" + ChatColor.AQUA + "/jumblehill search" + ChatColor.GOLD + ")");
		
		if (p.hasPermission("jumblehill.admin")) {  // Admin-only
		  p.sendMessage(ChatColor.GOLD + "* You can add items to anyone's shop (" + ChatColor.AQUA + "/jumblehill add" + ChatColor.GOLD + ")");
  		  p.sendMessage(ChatColor.GOLD + "* You can remove items from anyone's shop (" + ChatColor.AQUA + "/jumblehill remove" + ChatColor.GOLD + ")");
		  p.sendMessage(ChatColor.GOLD + "* You can list items in anyone's shop (" + ChatColor.AQUA + "/jumblehill list" + ChatColor.GOLD + ")");
  		  p.sendMessage(ChatColor.GOLD + "* You can delete anyone's shop (" + ChatColor.AQUA + "/jumblehill delete" + ChatColor.GOLD + ")");

		} else {                                  // Non-admin
		  p.sendMessage(ChatColor.GOLD + "* You can add items to your own shop (" + ChatColor.AQUA + "/jumblehill add" + ChatColor.GOLD + ")");
		  p.sendMessage(ChatColor.GOLD + "* You can remove items from your own shop (" + ChatColor.AQUA + "/jumblehill remove" + ChatColor.GOLD + ")");
		  p.sendMessage(ChatColor.GOLD + "* You can list items in your own shop (" + ChatColor.AQUA + "/jumblehill list" + ChatColor.GOLD + ")");
		  p.sendMessage(ChatColor.GOLD + "* You can delete your own shop (" + ChatColor.AQUA + "/jumblehill delete" + ChatColor.GOLD + ")");
		}
		if (p.hasPermission("jumblehill.stats")) {  // Stats-specific permission
		  p.sendMessage(ChatColor.GOLD + "* You can view a list of all shops and prices (" + ChatColor.AQUA + "/jumblehill stats" + ChatColor.GOLD + ")");
		}
	  } else if(args[0].equalsIgnoreCase("getuuid")) {
		  if(args.length == 2) {
			  p.sendMessage("http://" + getUUIDFromName(args[1]) + ".com");
		  } else {
	   		  p.sendMessage(intro + ChatColor.RED + "Incorrect Arguments! Please try: '/jumblehill getuuid {name}'");
		  }
		  
	  }  else {
		p.sendMessage(ChatColor.GOLD + "Command not recognised - type " + ChatColor.AQUA + "/jumblehill help" + ChatColor.GOLD + " for info.");
	  }
	} 
  } else {
	// Not a player - assume command issued from console
	console.sendMessage("[Critical Warning] The JumbleHill command cannot be used from the console.");
  }
  return true;
 } // onCommand
 
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		if (cmd.getName().equalsIgnoreCase("jumblehill") && args.length == 1)
		{
			if (sender instanceof Player)
			{
			   	
          List<String> subList = new ArrayList<>();
                Player p = (Player)sender;
				subList.add(ChatColor.GRAY + "add");
				subList.add(ChatColor.GRAY + "delete");
				subList.add(ChatColor.GRAY + "help");
				subList.add(ChatColor.GRAY + "list");
				subList.add(ChatColor.GRAY + "perms");
				subList.add(ChatColor.GRAY + "remove");
				subList.add(ChatColor.GRAY + "search");
				subList.add(ChatColor.GRAY + "set");
				if (p.hasPermission("jumblehill.stats")) {
				subList.add(ChatColor.GRAY + "stats");
				}
				return subList;
			}
		}
		return null;
	}
	
	
	
} // class JumbleHill