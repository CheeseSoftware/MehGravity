package io.github.gustav9797.MehGravity;

import java.util.Date;
import java.util.HashMap;
import java.util.Stack;

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
		
		// TODO: Reimplement this unknown feature.
		boolean isNonSticky;
		Location startLocation;
		Structure structure;
		Stack<Location> blocksToCheck = new Stack<Location>();
		World world = startBlock.getWorld();
		
		isNonSticky = MehGravity.nonStickyBlocks
				.contains(startBlock.getType());
		
		startLocation = new Location(
				startBlock.getX(), 
				startBlock.getY(), 
				startBlock.getZ());
		
		structure = new Structure(
				GetFreeStructureId(), 
				startBlock.getWorld());
		
		blocksToCheck.add(startLocation);
		
		while (!blocksToCheck.isEmpty())
		{
			Location location;
			Block block;
			Material material;
			
			if (structure.totalBlocks > MehGravity.blockLimit)
				return null;
			
			location = blocksToCheck.pop();

			if (location.getY() <= 0)
				return null;
			
			block = world.getBlockAt(location.getX(), location.getY(), location.getZ());
			material = block.getType();
			
			if (material == Material.BEDROCK)
				return null;
			
			if(Structure.isMaterialWeak(material))
			{
				continue;
			}

			//tubelius 20140629 gravel and sand has to be tretated as air because:
			//	if the plugin and vanilla server tries to move them, they get duplicated
			//	they should never stick to other blocks next to them
			//if (/*!isNonSticky || */material == Material.AIR || structure.HasBlock(location))
			if (material == Material.AIR || material == Material.GRAVEL || material == Material.SAND || structure.HasBlock(location))
				continue;
			
			structure.AddBlock(block.getState(), 
					new Location(location.getX(), location.getY(), location.getZ()));
			
			structure.totalBlocks++;
			
			// Note that it ends with location.getY()-1. That will be the next node since it's in the top of the stack.
			blocksToCheck.add(new Location(location.getX(), location.getY()+1, location.getZ()));
			blocksToCheck.add(new Location(location.getX()+1, location.getY(), location.getZ()));
			blocksToCheck.add(new Location(location.getX(), location.getY(), location.getZ()+1));
			blocksToCheck.add(new Location(location.getX()-1, location.getY(), location.getZ()));
			blocksToCheck.add(new Location(location.getX(), location.getY(), location.getZ()-1));
			blocksToCheck.add(new Location(location.getX(), location.getY()-1, location.getZ()));
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
