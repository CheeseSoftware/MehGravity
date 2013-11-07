package io.github.gustav9797.MehGravity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Vector;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class MehGravity extends JavaPlugin implements Listener 
{
	static int blockLimit = 2000;
	private FileConfiguration customConfig = null;
	private File customConfigFile = null;

	public void onEnable() 
	{
		this.saveDefaultConfig();
		blockLimit = MehGravity.this.getCustomConfig().getInt("blocklimit") ; 
		getServer().getPluginManager().registerEvents(this, this);
	}

	public void onDisable() 
	{
		getLogger().info("MehGravity disabled.");
	}
	
	public void reloadCustomConfig() 
	{
	    if (customConfigFile == null) 
	    	customConfigFile = new File(getDataFolder(), "customConfig.yml");
	    customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
	 
	    // Look for defaults in the jar
	    InputStream defConfigStream = this.getResource("customConfig.yml");
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
	        customConfigFile = new File(getDataFolder(), "customConfig.yml");
	    if (!customConfigFile.exists()) {            
	         saveResource("customConfig.yml", false);
	     }
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) 
	{
		new GravityBlockBreak(this, event.getBlock(), event.getPlayer()).runTask(this);
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) 
	{
		BeginGravity(event.getBlockPlaced(), event.getPlayer());
	}

	static Location[] adjacentBlocks = { new Location(1, 0, 0),
			new Location(-1, 0, 0), new Location(0, 1, 0),
			new Location(0, -1, 0), new Location(0, 0, 1),
			new Location(0, 0, -1) };
	
	public void BeginGravity(Block startBlock, Player player)
	{
		// Check if we can find bedrock, saves lots of time..
		Location startLocation = new Location(startBlock.getX(), startBlock.getY(), startBlock.getZ());
		for (int y = startBlock.getY(); y > -10; y--) 
		{
			Block currentBlock = player.getWorld().getBlockAt(startBlock.getX(), y, startBlock.getZ());
			if (currentBlock.getType() != Material.AIR) 
			{
				if (currentBlock.getType() == Material.BEDROCK) 
					return;
			} 
			else
				break;
		}

		// Now that we haven't found bedrock, lets store what blocks it's
		// connected to(within the set blocklimit!)
		int totalBlocks = 0;
		HashMap<Integer, HashMap<Location, Block>> blockList = new HashMap<Integer, HashMap<Location, Block>>();
		Queue<Location> blocksToCheck = new LinkedList<Location>();
		blocksToCheck.add(startLocation);
		blockList.put(startBlock.getY(), new HashMap<Location, Block>());
		blockList.get(startBlock.getY()).put(startLocation, startBlock);
		World world = player.getWorld();
		while (!blocksToCheck.isEmpty()) 
		{
			Location currentParent = blocksToCheck.poll();
			for (int i = 0; i < 6; i++) 
			{
				Location currentLocation = new Location(
						adjacentBlocks[i].getX() + currentParent.getX(),
						adjacentBlocks[i].getY() + currentParent.getY(),
						adjacentBlocks[i].getZ() + currentParent.getZ());
				Block currentBlock = world.getBlockAt(currentLocation.getX(),
						currentLocation.getY(), currentLocation.getZ());

				if (!blockList.containsKey(currentBlock.getY()))
					blockList.put(currentBlock.getY(), new HashMap<Location, Block>());

				if (!blockList.get(currentBlock.getY()).containsValue(currentBlock) && currentBlock.getType() != Material.AIR) 
				{
					blockList.get(currentBlock.getY()).put(currentLocation, currentBlock);
					blocksToCheck.add(currentLocation);
					totalBlocks++;
				}
			}

			if (totalBlocks >= blockLimit)
				return;
		}
		
		// Now we know that we have a massive floating thing that should drop
		// down, lets store it in an array of collections of blocks sorted by y-layer.
		Vector<Integer> yLevels = new Vector<Integer>();
		Iterator<Entry<Integer, HashMap<Location, Block>>> yit = blockList.entrySet().iterator();
		while (yit.hasNext()) 
		{
			Entry<Integer, HashMap<Location, Block>> ypair = yit.next();
			yLevels.add((int) ypair.getKey());
			//yit.remove();
		}
		Collections.sort(yLevels);
		
		//Measure how far down we can move it
		int blocksWeCanMove = findMovingSpaceDown(yLevels, blockList, world);
		
		//Move them down
		Iterator<Integer> i = yLevels.iterator();
		while (i.hasNext()) 
		{
			int currentLayerY = (int)i.next();
			if (blockList.containsKey(currentLayerY)) 
			{
				player.sendMessage("size: " + blockList.get(currentLayerY).size());
				Iterator<Entry<Location, Block>> it = blockList.get(currentLayerY).entrySet().iterator();
				while (it.hasNext()) 
				{
					Entry<Location, Block> pair = it.next();		
					Block from = ((Block) pair.getValue());
					BlockState fromState = from.getState();					
					Block to = world.getBlockAt(from.getLocation().getBlockX(), from.getLocation().getBlockY() - blocksWeCanMove, from.getLocation().getBlockZ());
					BlockState toState = to.getState();				
					to.setType(from.getType());
					player.sendMessage(fromState.getBlock().getType().toString());
					
					switch(from.getType())
					{
						case CHEST:
						{
							Chest fromChest = (Chest) fromState;
							Chest toChest = (Chest) to.getState();
							Inventory fromInventory = fromChest.getBlockInventory();
							Inventory toInventory = toChest.getBlockInventory();
							
							toInventory.setContents(fromChest.getBlockInventory().getContents());
							fromInventory.clear();
							break;
						}
						/*case WALL_SIGN: BUGGING ATM
						{
							player.sendMessage("wall SIGN!!!");
							Sign fromSign = (Sign) fromState;
							Sign toSign = (Sign) to.getState();
							String[] tempSign = toSign.getLines();
							tempSign = fromSign.getLines();
							//toSign.setData(fromSign.getData());
							to.setType(Material.SIGN);
							break;
						}*/
						default:
							break;
					}
					//toSet.setData((byte)ToMoveDownState.getData());
					world.getBlockAt(from.getLocation()).setType(Material.AIR);
					it.remove();
				}
			}
			i.remove();
		}
	}

	public int findMovingSpaceDown(Vector<Integer> yLevels, HashMap<Integer, HashMap<Location, Block>> blockList, World world) 
	{
		Map<ColumnCoord, Integer> minima = new HashMap<ColumnCoord, Integer>();
		Map<ColumnCoord, Integer> maxima = new HashMap<ColumnCoord, Integer>();
		
		int minDistance = Integer.MAX_VALUE;
		
		Iterator<Integer> yiterator = yLevels.iterator();
		while (yiterator.hasNext()) 
		{
			Iterator<Entry<Location, Block>> xziterator = blockList.get(yiterator.next()).entrySet().iterator();
			while (xziterator.hasNext()) 
			{
				Block block = xziterator.next().getValue();
				ColumnCoord coord = new ColumnCoord(block.getX(), block.getZ());
				Integer min = minima.get(coord);
				Integer max = maxima.get(coord);
				if(min == null) 
				{	
					minima.put(coord, block.getY());
					maxima.put(coord, block.getY());
				} 
				else 
				{
					minima.put(coord, Math.min(min, block.getY()));
					maxima.put(coord, Math.max(max, block.getY()));
				}
			}
		}
		
		/*for(Map.Entry<ColumnCoord, Integer> entry : minima.entrySet()) 
		{
			ColumnCoord coord = entry.getKey();
			int currentBlockY = entry.getValue();

			for(int y = currentBlockY - 1; y > -10; y--)
			{
				if(world.getBlockAt(coord.x, y, coord.z).getType().isSolid())
				{
					if(currentBlockY - y - 1 < minDistance)
						minDistance = currentBlockY - y - 1;
					break;
				}
			}

		}*/
        for(Map.Entry<ColumnCoord, Integer> entry : minima.entrySet()) 
        {
                ColumnCoord coord = entry.getKey();
                int lowestMovedY = entry.getValue();
                int highestMovedY = maxima.get(entry.getKey());
 
                int searchBottom = Math.max(lowestMovedY - 1, 0);
                // Search down and find the shortest span
                for(int y = highestMovedY - 1; y >= searchBottom; y--) 
                {
                        // Not entirely intuitive, bottom is above top, as it represents either the bottom and top of the blocks they are connected to.
                        // Find bottom edge, a.k.a air block.
                        Block bottom = world.getBlockAt(coord.x, y, coord.z);
                        if(bottom.getType() != Material.AIR)
                                continue;
 
                        // Progress further, find first non-air block (top edge)
                        for(; y >= 0; y--) 
                        {
                                Block top = world.getBlockAt(coord.x, y, coord.z);
                                if(top.getType() == Material.AIR)
                                        continue;
 
                                // Check if the block is part of the moving mass. If so, just continue with the outer loop (break out of this one)
                                if(blockList.containsKey(top.getY()) && blockList.get(top.getY()).containsKey(new Location(top.getX(),top.getY(),top.getZ())))
                                        break;
 
                                minDistance = Math.min(bottom.getY() - top.getY(), minDistance);
                        }
                }
        }
		return minDistance;
	}
}

class GravityBlockBreak extends BukkitRunnable
{
	Block startBlock;
	Player player;
	MehGravity plugin;
	public GravityBlockBreak(MehGravity plugin, Block startBlock, Player player)
	{
		this.startBlock = startBlock;
		this.player = player;
		this.plugin = plugin;
	}
	
	@Override
	public void run() 
	{
		for(int i = 0; i < 6; i++)
		{
			Block currentBlock = player.getWorld().getBlockAt(
					startBlock.getX() + MehGravity.adjacentBlocks[i].getX(), 
					startBlock.getY() + MehGravity.adjacentBlocks[i].getY(), 
					startBlock.getZ() + MehGravity.adjacentBlocks[i].getZ());
			
			if(currentBlock.getType() != Material.AIR)
				plugin.BeginGravity(currentBlock, player);
		}
	}
}

//// Utility
class ColumnCoord 
{

    public final int x;
    public final int z;

    public ColumnCoord(int x, int z) 
    {
        this.x = x;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) 
    {
        if (this == o) return true;
        if (!(o instanceof ColumnCoord)) return false;
        ColumnCoord key = (ColumnCoord) o;
        return x == key.x && z == key.z;
    }

    @Override
    public int hashCode() 
    {
        int result = x;
        result = 31 * result + z;
        return result;
    }
}