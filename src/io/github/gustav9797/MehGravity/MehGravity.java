package io.github.gustav9797.MehGravity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Vector;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.mcstats.Metrics;

public final class MehGravity extends JavaPlugin implements Listener 
{
	static int blockLimit = 2000;
	static List<String> gravityWorlds;
	private FileConfiguration customConfig = null;
	private File customConfigFile = null;
	private StructureHandler structureHandler;

	public void onEnable() 
	{
		saveDefaultConfig();
		blockLimit = getCustomConfig().getInt("blocklimit") ; 
		gravityWorlds = getCustomConfig().getStringList("gravityWorlds");
		getServer().getPluginManager().registerEvents(this, this);
		structureHandler = new StructureHandler(this);

		//Metrics start
		try {
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		} catch (IOException e) {
		    // Failed to submit the stats :-(
		}
		//Metrics end
	}

	public void onDisable() 
	{
		getLogger().info("MehGravity disabled.");
	}
	
	public void reloadCustomConfig() 
	{
	    if (customConfigFile == null) 
	    	customConfigFile = new File(getDataFolder(), "config.yml");
	    customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
	 
	    // Look for defaults in the jar
	    InputStream defConfigStream = this.getResource("config.yml");
	    if (defConfigStream != null) {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	        customConfig.setDefaults(defConfig);
	    }
	}
	
	public FileConfiguration getCustomConfig() 
	{
	    if (customConfig == null) 
	        reloadCustomConfig();
	    return customConfig;
	}
	
	public void saveCustomConfig() 
	{
	    if (customConfig == null || customConfigFile == null)
	        return;
	    
	    try {
	        getCustomConfig().save(customConfigFile);
	    } catch (IOException ex) {
	        getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
	    }
	}
	
	public void saveDefaultConfig() 
	{
	    if (customConfigFile == null)
	        customConfigFile = new File(getDataFolder(), "config.yml");
	    if (!customConfigFile.exists()) {            
	         saveResource("config.yml", false);
	     }
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) 
	{
		if(HasPerms(event.getPlayer()))
			CheckAround(event.getBlock(), 0);
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) 
	{
		if(HasPerms(event.getPlayer()))
			Check(event.getBlockPlaced());
	}
	
	@EventHandler
	public void onBlockBurn(BlockBurnEvent event)
	{
		CheckAround(event.getBlock(), 0);
	}
	
	@EventHandler
	public void onLeavesDecay(LeavesDecayEvent event)
	{
		CheckAround(event.getBlock(), 0);
	}
	
	@EventHandler
	public void onBlockPistonExtend(BlockPistonExtendEvent event)
	{
		Block base = event.getBlock();
		Block toCheck = base.getWorld().getBlockAt(new org.bukkit.Location(base.getWorld(), base.getX() + event.getDirection().getModX(), base.getY() + event.getDirection().getModY(), base.getZ() + event.getDirection().getModZ()));		
		CheckAround(toCheck, 10);
		//Check(toCheck);
		//getServer().getPlayer("gustav9797").sendMessage(event.getEventName() + " - " + event.getDirection() + " - " + event.getBlock());
		getServer().getPlayer("gustav9797").sendMessage(event.getEventName() + " - " + toCheck);
	}
	
	@EventHandler
	public void onBlockPistonRetract(BlockPistonRetractEvent event)
	{
		Block base = event.getBlock();
		Block toCheck = base.getWorld().getBlockAt(new org.bukkit.Location(base.getWorld(), base.getX() + event.getDirection().getModX(), base.getY() + event.getDirection().getModY(), base.getZ() + event.getDirection().getModZ()));
		CheckAround(toCheck, 10);
		//Check(toCheck);
		//getServer().getPlayer("gustav9797").sendMessage(event.getEventName() + " - " + event.getDirection() + " - " + event.getBlock());
		getServer().getPlayer("gustav9797").sendMessage(event.getEventName() + " - " + toCheck);
	}
	
	public boolean HasPerms(Player player)
	{
		if((!player.isPermissionSet("mehgravity.nocheck") || (player.isPermissionSet("mehgravity.nocheck") && !player.hasPermission("mehgravity.nocheck")))&& (gravityWorlds.contains("AllWorlds") || gravityWorlds.contains(player.getWorld().getName())))
			return true;
		return false;
	}
	
	public void CheckAround(Block block, int delay)
	{
		new GravityCheckAroundLater(this, block).runTaskLater(this, delay);
	}
	
	public void Check(Block block)
	{
		BeginGravity(block);
	}

	static Location[] adjacentBlocks = { new Location(1, 0, 0),
			new Location(-1, 0, 0), new Location(0, 1, 0),
			new Location(0, -1, 0), new Location(0, 0, 1),
			new Location(0, 0, -1) };
	
	@SuppressWarnings("serial")
	static final ArrayList<Material> annoyingBlocks = new ArrayList<Material>() {{ 
		add(Material.WOODEN_DOOR);
		add(Material.IRON_DOOR_BLOCK); 
		add(Material.TRAP_DOOR);
		add(Material.TORCH);
		add(Material.SAPLING);
		add(Material.LONG_GRASS);
		add(Material.YELLOW_FLOWER);
		add(Material.RED_ROSE);
		add(Material.BROWN_MUSHROOM);
		add(Material.RED_MUSHROOM);
		add(Material.LADDER);
		add(Material.SNOW);
		add(Material.VINE);
		add(Material.WATER_LILY);
		add(Material.CARPET);
		add(Material.PAINTING);
		add(Material.SIGN);
		add(Material.SIGN_POST);
		add(Material.WALL_SIGN);
		add(Material.BED);
		add(Material.ITEM_FRAME);
		add(Material.FLOWER_POT);
		add(Material.LEVER);
		add(Material.STONE_PLATE);
		add(Material.WOOD_PLATE);
		add(Material.REDSTONE_TORCH_OFF);
		add(Material.REDSTONE_TORCH_ON);
		add(Material.STONE_BUTTON);
		add(Material.TRIPWIRE_HOOK);
		add(Material.WOOD_BUTTON);
		add(Material.GOLD_PLATE);
		add(Material.IRON_PLATE);
		add(Material.DAYLIGHT_DETECTOR);
		add(Material.REDSTONE_WIRE);
		add(Material.REDSTONE_COMPARATOR);
		add(Material.REDSTONE_COMPARATOR_OFF);
		add(Material.REDSTONE_COMPARATOR_ON);
		add(Material.DIODE);
		add(Material.DIODE_BLOCK_OFF);
		add(Material.DIODE_BLOCK_ON);
		add(Material.RAILS);
		add(Material.POWERED_RAIL);
		add(Material.DETECTOR_RAIL);
		add(Material.ACTIVATOR_RAIL);
		add(Material.TRIPWIRE);
		}};
	
	public void BeginGravity(Block startBlock)
	{
		Location startLocation = new Location(startBlock.getX(), startBlock.getY(), startBlock.getZ());
		Structure structure = new Structure(structureHandler.GetFreeStructureId(), startBlock.getWorld());
		Queue<Location> blocksToCheck = new LinkedList<Location>();
		blocksToCheck.add(startLocation);
		structure.AddBlock(startBlock.getState(), startLocation);
		structure.totalBlocks++;
		World world = startBlock.getWorld();
		while (!blocksToCheck.isEmpty()) 
		{
			//Store all blocks in the structure
			Location currentParent = blocksToCheck.poll();
			for (int y = currentParent.getY(); y > -10; y--) 
			{			
				Block currentBlock = world.getBlockAt(currentParent.getX(), y, currentParent.getZ());
				if (currentBlock.getType() == Material.AIR) //We didn't find bedrock, can't continue search		
					break;
				else if (currentBlock.getType() == Material.BEDROCK)
				{
					return;
				}
			}
			
			for (int i = 0; i < 6; i++) 
			{
				Location currentLocation = new Location(
						adjacentBlocks[i].getX() + currentParent.getX(),
						adjacentBlocks[i].getY() + currentParent.getY(),
						adjacentBlocks[i].getZ() + currentParent.getZ());
				Block currentBlock = world.getBlockAt(currentLocation.getX(), currentLocation.getY(), currentLocation.getZ());
				
				//getServer().getPlayer("gustav9797").sendMessage("checked: " + currentBlock + " hasblock: " + structure.HasBlock(currentLocation));
				if(!structure.HasBlock(currentLocation) && currentBlock.getType() != Material.AIR)
				{
					structure.AddBlock(currentBlock.getState(), currentLocation);
					blocksToCheck.add(currentLocation);
					structure.totalBlocks++;
					//getServer().getPlayer("gustav9797").sendMessage("added a block");
				}
			}

			if (structure.totalBlocks >= blockLimit)
			{
				return;
			}
		}
		//getServer().getPlayer("gustav9797").sendMessage("tot " + structure.totalBlocks);
		
		structureHandler.AddStructure(structure);
		//structure.SortLevels();
		//structure.StoreNonSolidBlocks();
		//structure.MoveOneDown(world);
	}

}