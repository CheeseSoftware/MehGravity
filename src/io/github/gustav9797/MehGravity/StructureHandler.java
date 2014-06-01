package io.github.gustav9797.MehGravity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class StructureHandler
{
	HashMap<Integer, Structure> structures;
	MehGravity plugin;

	public StructureHandler(MehGravity plugin)
	{
		this.plugin = plugin;
		structures = new HashMap<Integer, Structure>();
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new UpdateStructures(plugin, this), 1, 1);
	}

	public int GetFreeStructureId()
	{
		return structures.size();
	}

	public Structure CreateStructure(Block startBlock)
	{
		if(!MehGravity.isWorldAffected(startBlock.getWorld().getName()))
			return null;
		
		boolean isNonSticky;
		Location startLocation;
		Structure structure;
		Queue<Location> blocksToCheck;
		World world;
		
		world = startBlock.getWorld();
		
		isNonSticky = MehGravity.nonStickyBlocks
				.contains(startBlock.getType());
		
		startLocation = new Location(
				startBlock.getX(), 
				startBlock.getY(), 
				startBlock.getZ());
		
		structure = new Structure(
				GetFreeStructureId(), 
				startBlock.getWorld());
		
		blocksToCheck = new LinkedList<Location>();
		
		blocksToCheck.add(startLocation);
		
		//structure.AddBlock(startBlock.getState(), startLocation);
		//structure.totalBlocks++;
		
		while (!blocksToCheck.isEmpty())
		{
			if (structure.totalBlocks > MehGravity.blockLimit)
				return null;
			
			Location location = blocksToCheck.poll();

			if (location.getY() <= 0)
				return null;
			
			Block block = world.getBlockAt(location.getX(), location.getY(), location.getZ());

			Material material = block.getType();
			
			if(Structure.isMaterialWeak(material))
			{
				continue;
			}
			
			if (/*!isNonSticky || */material == Material.AIR || structure.HasBlock(location))
				continue;
			
			structure.AddBlock(block.getState(), 
					new Location(location.getX(), location.getY(), location.getZ()));
			structure.totalBlocks++;
			
			blocksToCheck.add(new Location(location.getX(), location.getY()-1, location.getZ()));
			blocksToCheck.add(new Location(location.getX()+1, location.getY(), location.getZ()));
			blocksToCheck.add(new Location(location.getX(), location.getY(), location.getZ()+1));
			blocksToCheck.add(new Location(location.getX()-1, location.getY(), location.getZ()));
			blocksToCheck.add(new Location(location.getX(), location.getY(), location.getZ()-1));
			blocksToCheck.add(new Location(location.getX(), location.getY()+1, location.getZ()));
		}
		return structure;
	}

	public void AddStructure(Structure structure)
	{
		if (!structures.containsKey(structure.id))
		{
			structure.moveDate = new Date();
			structures.put(structure.id, structure);
		}
	}

	public void RemoveStructure(int id)
	{
		if (structures.containsKey(id))
			structures.remove(id);
	}
}
