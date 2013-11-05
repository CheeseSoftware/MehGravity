package io.github.gustav9797.MehGravity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Vector;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public final class MehGravity extends JavaPlugin implements Listener 
{
	static int blockLimit = 2000; // Should be config option

	public void onEnable() 
	{
		getServer().getPluginManager().registerEvents(this, this);
	}

	public void onDisable() 
	{
		getLogger().info("MehGravity disabled.");
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
		//event.getPlayer().sendMessage("Didn't find bedrock");

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

				if (!blockList.get(currentBlock.getY()).containsValue(currentBlock) && currentBlock.getType().isSolid()) 
				{
					blockList.get(currentBlock.getY()).put(currentLocation, currentBlock);
					blocksToCheck.add(currentLocation);
					totalBlocks++;
					/*event.getPlayer().sendMessage(
							"Added: block X:" + currentLocation.getX() + " Y:"
									+ currentLocation.getY() + " Z:"
									+ currentLocation.getZ() + " material:"
									+ currentBlock.getType());*/
				}
			}

			if (totalBlocks >= blockLimit) {
				//event.getPlayer().sendMessage("Hit the max amount of blocks, returning!");
				return;
			}
		}
		//event.getPlayer().sendMessage("We have found a total of " + totalBlocks + " blocks!");

		// Now we know that we have a massive floating thing that should drop
		// down, so lets move it down!
		Vector<Integer> yLevels = new Vector<Integer>();
		Iterator yit = blockList.entrySet().iterator();
		while (yit.hasNext()) 
		{
			Map.Entry ypair = (Map.Entry) yit.next();
			yLevels.add((int) ypair.getKey());
			//yit.remove();
		}
		Collections.sort(yLevels);
		
		//Measure how far down we can move it
		Iterator i = yLevels.iterator();
		int blocksWeCanMove = -1;
		first:
		while (i.hasNext()) 
		{
			int iblabla = (int)i.next();
			if (blockList.containsKey(iblabla)) 
			{
				Iterator it = blockList.get(iblabla).entrySet().iterator();
				while (it.hasNext()) 
				{
					Map.Entry pair = (Map.Entry) it.next();
					Block toCheck = (Block)pair.getValue();
					Location toCheckLocation = new Location(toCheck.getX(), toCheck.getY(), toCheck.getZ());
					int layer = iblabla - 1;
					while (blockList.containsKey(layer)) 
					{
						if(blockList.get(layer).containsValue(toCheckLocation))
						{
							blocksWeCanMove = 0;
							break first;
						}
						layer--;
					}
					for(int y = toCheck.getY(); y > -10; y--)
					{
						if(world.getBlockAt(toCheck.getX(), y, toCheck.getZ()).getType().isSolid() && (!blockList.containsKey(y) || !blockList.get(y).containsValue(world.getBlockAt(toCheck.getX(), y, toCheck.getZ()))))
							break first;
						blocksWeCanMove++;
					}
					
				}
			}
		}
		
		//Move them down
		//event.getPlayer().sendMessage("Possible blocks to move down:" + blocksWeCanMove);
		/*i = yLevels.iterator();
		while (i.hasNext()) 
		{
			int iblabla = (int)i.next();
			//event.getPlayer().sendMessage("Trying to get Y level of " + iblabla);
			if (blockList.containsKey(iblabla)) 
			{
				Iterator it = blockList.get(iblabla).entrySet().iterator();
				while (it.hasNext()) 
				{
					Map.Entry pair = (Map.Entry) it.next();
					Block ToMoveDown = ((Block) pair.getValue()).getState().getBlock();
					Block toSet = world.getBlockAt(ToMoveDown.getLocation().getBlockX(), ToMoveDown.getLocation().getBlockY() - blocksWeCanMove, ToMoveDown.getLocation().getBlockZ());
					toSet.setType(ToMoveDown.getType());
					toSet.setData(ToMoveDown.getData());
					world.getBlockAt(ToMoveDown.getLocation()).setType(Material.AIR);
					it.remove();
				}
			}
			//else
				//event.getPlayer().sendMessage("Failed");
			i.remove();
		}*/
		
		i = yLevels.iterator();
		while (i.hasNext()) 
		{
			int iblabla = (int)i.next();
			//event.getPlayer().sendMessage("Trying to get Y level of " + iblabla);
			if (blockList.containsKey(iblabla)) 
			{
				Iterator it = blockList.get(iblabla).entrySet().iterator();
				while (it.hasNext()) 
				{
					Map.Entry pair = (Map.Entry) it.next();
					Block ToMoveDown = ((Block) pair.getValue()).getState().getBlock();
					//Block toSet = world.getBlockAt(ToMoveDown.getLocation().getBlockX(), ToMoveDown.getLocation().getBlockY() - blocksWeCanMove, ToMoveDown.getLocation().getBlockZ());
					//toSet.setType(ToMoveDown.getType());
					//toSet.setData(ToMoveDown.getData());
					
					world.spawnFallingBlock(ToMoveDown.getLocation(), ToMoveDown.getType(), ToMoveDown.getData());
					world.getBlockAt(ToMoveDown.getLocation()).setType(Material.AIR);
					it.remove();
				}
			}
			//else
				//event.getPlayer().sendMessage("Failed");
			i.remove();
		}
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
					startBlock.getX() + plugin.adjacentBlocks[i].getX(), 
					startBlock.getY() + plugin.adjacentBlocks[i].getY(), 
					startBlock.getZ() + plugin.adjacentBlocks[i].getZ());
			
			if(currentBlock.getType().isSolid())
				plugin.BeginGravity(currentBlock, player);
		}
	}
}