package me.kamdenb.tntwars;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.sk89q.worldedit.world.World;

public class Main extends JavaPlugin implements Listener {
	FileConfiguration config = getConfig();
	
	private ArrayList<Player> inGame = new ArrayList<Player>();
	private ArrayList<Player> red = new ArrayList<Player>();
	private boolean redFull = false;
	private ArrayList<Player> blue = new ArrayList<Player>();
	private boolean blueFull = false;
	private ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
	private ArrayList<Player> adminBypass = new ArrayList<Player>();
	private boolean gameStarted = false;
	
	public static Main plugin;
	
	public void onEnable() {
		config.options().copyDefaults(true);
		saveConfig();
		PluginManager pm = Bukkit.getServer().getPluginManager();
		pm.registerEvents(this, this);
		ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
	
		plugin = this;
		
		console.sendMessage("TNT Wars Enabled!");
	}
	
	public void onDisable() {
		plugin = null;
	}
	
	public void joinBlue(Player p) {
		this.inGame.clear();
		this.red.clear();
		this.blue.clear();
		if(blueFull) {
			red.add(p);
		} else {			
			blue.add(p);
		}
		if(blue.size() == Integer.parseInt(config.getString("max-players"))) blueFull = true;
		inGame.add(p);
	}
	
	public void joinRed(Player p) {
		if(redFull) {
			blue.add(p);
		} else {			
			red.add(p);
		}
		if(red.size() == Integer.parseInt(config.getString("max-players"))) redFull = true;
		inGame.add(p);
	}
	
	public ArrayList<Integer> getPlayerLoc(Player player) {
		ArrayList<Integer> coords = new ArrayList<Integer>();
		coords.add(player.getLocation().getBlockX());
		coords.add(player.getLocation().getBlockZ());
		coords.add(player.getLocation().getBlockY());
		return coords;
	}
	
	public void tpPlayerToTeamSpawn(Player player) {
		String spawn = this.red.contains(player) ? "red-spawn" : "blue-spawn";
		Location pSpawn = new Location(
			player.getWorld(), 
			Integer.parseInt(config.getStringList(spawn).get(0)), 
			Integer.parseInt(config.getStringList(spawn).get(2)), 
			Integer.parseInt(config.getStringList(spawn).get(1))
		);
		player.teleport(pSpawn);
	}
	
	public void tpPlayerToLobby(Player player) {
		Location pSpawn = new Location(
			player.getWorld(), 
			Integer.parseInt(config.getStringList("main-spawn").get(0)), 
			Integer.parseInt(config.getStringList("main-spawn").get(2)), 
			Integer.parseInt(config.getStringList("main-spawn").get(1))
		);
		player.teleport(pSpawn);
	}
	
	public void removePlayerFromGame(Player player) {
		for(Player p : this.inGame) {
			p.sendMessage(p.getDisplayName() + " has left the arena");
		}
		inGame.remove(player);
		if(red.contains(player))
			red.remove(player);
		else 
			blue.remove(player);
		player.setHealth(20);
		this.tpPlayerToLobby(player);
	}
	
	int startTime;
	public void startGame(ArrayList<Player> inGame) {
		CountdownTimer timer = new CountdownTimer(plugin,
		        10,
		        () -> {
		        },
		        () -> {
		            for(Player p : inGame) {
		            	p.sendMessage("§aGame started!");
		            	this.tpPlayerToTeamSpawn(p);
		            	this.gameStarted = true;
		            }
		        },
		        (t) -> {
		        	for(Player p : inGame) {
		        		p.sendMessage("§aGame starting in " + t.getSecondsLeft());
		        	}
		        }

		);
		timer.scheduleTimer();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {
		Player player = (Player) sender;
		
		if(command.equalsIgnoreCase("maxplayers")) {
			player.sendMessage(config.getString("max-players"));
		}
		
		if(command.equalsIgnoreCase("ingame")) {
			player.sendMessage(this.inGame.toString());
		}
		
		if(command.equalsIgnoreCase("teams")) {
			player.sendMessage(this.red.toString());
			player.sendMessage(this.blue.toString());
		}
		
		if(command.equalsIgnoreCase("tntwars")) {
			if(inGame.contains(player)) {
				player.sendMessage("you are already in queue");
				return true;
			}
			int rnd = (int)(Math.random()*2);
			this.console.sendMessage(Integer.toString(rnd));
			switch(rnd) {
			case 0:
				joinRed(player);
				break;
			case 1:
				joinBlue(player);
				break;
			default:
				player.sendMessage(Integer.toString(rnd));
				this.console.sendMessage(Integer.toString(rnd));
			}
			String msg = String.format("§%hYou have joined the %s team", (rnd == 0) ? config.getInt("red-color") : config.getInt("blue-color"), (rnd == 0) ? "red" : "blue");
			player.sendMessage(msg);
			return true;
		}
		
		if(command.equalsIgnoreCase("twset")) {
			switch(args[0]) {
			case "c1":
				config.set("corner-1", this.getPlayerLoc(player));
				saveConfig();
				break;
			case "c2":
				config.set("corner-2", this.getPlayerLoc(player));
				saveConfig();
				break;
			}
		}
		
		if(command.equalsIgnoreCase("teamspawn")) {
			this.tpPlayerToTeamSpawn(player);
		}
		
		if(command.equalsIgnoreCase("twspawn")) {
			switch(args[0]) {
			case "red":
				player.sendMessage("§aSet red spawn");
				config.set("red-spawn", this.getPlayerLoc(player));
				saveConfig();
				break;
			case "blue":
				player.sendMessage("§aSet blue spawn");
				config.set("blue-spawn", this.getPlayerLoc(player));
				saveConfig();
				break;
			case "main":
				player.sendMessage("§aSet main spawn");
				config.set("main-spawn", this.getPlayerLoc(player));
				saveConfig();
				break;
			}
		}
		
		if(command.equalsIgnoreCase("start")) {
			this.startGame(this.inGame);
		}
		
		if(command.equalsIgnoreCase("twadmin")) {			
			if(adminBypass.contains(player))
				adminBypass.remove(player);
			else 
				adminBypass.add(player);
			player.sendMessage("Admin mode toggled");
		}
		
		if(command.equalsIgnoreCase("lobby")) {
			this.tpPlayerToLobby(player);
		}
		
		if(command.equalsIgnoreCase("load")) {
			World world = 
			MapGen mapGen = new MapGen();
			double x, y = (double) 0;
			double z = (double) 60;
			mapGen.loadSchematic(, x, y, z);
		}
		
		return true;
	}
	
	List<String> coords1 = config.getStringList("corner-1");
	String x1 = coords1.get(0);
	String z1 = coords1.get(1);
	String y1 = coords1.get(2);
	List<String> coords2 = config.getStringList("corner-2");
	String x2 = coords2.get(0);
	String z2 = coords2.get(1);
	String y2 = coords2.get(2);
	
	Integer xmax = (Integer.parseInt(x1) > Integer.parseInt(x2)) ? Integer.parseInt(x1) : Integer.parseInt(x2);
	Integer xmin = (Integer.parseInt(x1) < Integer.parseInt(x2)) ? Integer.parseInt(x1) : Integer.parseInt(x2);
	Integer zmax = (Integer.parseInt(z1) > Integer.parseInt(z2)) ? Integer.parseInt(z1) : Integer.parseInt(z2);
	Integer zmin = (Integer.parseInt(z1) < Integer.parseInt(z2)) ? Integer.parseInt(z1) : Integer.parseInt(z2);
	Integer ylimit = (Integer.parseInt(y1) < Integer.parseInt(y2)) ? Integer.parseInt(y1) : Integer.parseInt(y2);
	
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if(gameStarted == true) {
			if(inGame.contains(e.getPlayer()) && !adminBypass.contains(e.getPlayer())) {			
				ArrayList<Player> outOfBounds = new ArrayList<Player>();
				Player player = e.getPlayer();
				if(config.getStringList("corner-1").size() == 3 && config.getStringList("corner-2").size() == 3) {
					
					if(e.getTo().getBlockX() > xmax || e.getTo().getBlockX() < xmin) {
						outOfBounds.add(player);
						this.removePlayerFromGame(player);
						player.sendMessage("You're out of x bounds");
					} else {
						outOfBounds.remove(player);
					}
					
					if(e.getTo().getBlockZ() > zmax || e.getTo().getBlockZ() < zmin) {
						outOfBounds.add(player);
						this.removePlayerFromGame(player);
						player.sendMessage("You're out of z bounds");
					} else {
						outOfBounds.remove(player);
					}
					
					if(e.getTo().getBlockY() < (ylimit)) {
						outOfBounds.add(player);
						if(config.getInt("y") == 0) {
							config.set("y", ylimit);
						}
						if(this.inGame.contains(player)) {
							this.removePlayerFromGame(player);
							player.sendMessage("You're out of y bounds");
						}
					} else {
						outOfBounds.remove(player);
					}					
				}
			}
		}
	}
	
	@EventHandler
	public void damage(EntityDamageEvent e) {
		if(e.getEntity() instanceof Player) {			
			Player player = (Player) e.getEntity();
			if(inGame.contains(player)) {			
				if((player.getHealth() - e.getFinalDamage()) <= 0) {
					e.setCancelled(true);
					this.removePlayerFromGame(player);
				}
			}
		}
	}

}
