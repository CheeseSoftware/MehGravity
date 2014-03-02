package io.github.gustav9797.MehGravity;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

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
		new UpdateStructures(plugin, this).runTaskTimer(plugin, 0, 1);
	}

	public int GetFreeStructureId()
	{
		return structures.size();
	}

	public Structure CreateStructure(Block startBlock)
	{
		Location startLocation = new Location(startBlock.getX(), startBlock.getY(), startBlock.getZ());
		Structure structure = new Structure(GetFreeStructureId(), startBlock.getWorld());
		Queue<Location> blocksToCheck = new LinkedList<Location>();
		blocksToCheck.add(startLocation);
		structure.AddBlock(startBlock.getState(), startLocation);
		structure.totalBlocks++;
		World world = startBlock.getWorld();
		while (!blocksToCheck.isEmpty())
		{
			// Store all blocks in the structure
			Location currentParent = blocksToCheck.poll();
			for (int y = currentParent.getY(); y > -10; y--)
			{
				Block currentBlock = world.getBlockAt(currentParent.getX(), y, currentParent.getZ());
				if (currentBlock.getType() == Material.AIR) // We didn't find
															// bedrock, can't
															// continue search
					break;
				else if (MehGravity.staticBlocks.contains(currentBlock.getType()))
					return null;
			}

			for (int i = 0; i < 6; i++)
			{
				Location currentLocation = new Location(MehGravity.adjacentBlocks[i].getX() + currentParent.getX(), MehGravity.adjacentBlocks[i].getY() + currentParent.getY(),
						MehGravity.adjacentBlocks[i].getZ() + currentParent.getZ());
				Block currentBlock = world.getBlockAt(currentLocation.getX(), currentLocation.getY(), currentLocation.getZ());

				if (!structure.HasBlock(currentLocation) && currentBlock.getType() != Material.AIR)
				{
					if(MehGravity.staticBlocks.contains(currentBlock.getType()))
							return null;
					structure.AddBlock(currentBlock.getState(), currentLocation);
					blocksToCheck.add(currentLocation);
					structure.totalBlocks++;
				}
			}

			if (structure.totalBlocks >= MehGravity.blockLimit)
			{
				return null;
			}
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
