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
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new UpdateStructures(plugin, this), 1, 1);
	}

	public int GetFreeStructureId()
	{
		return structures.size();
	}

	public Structure CreateStructure(Block startBlock)
	{
		boolean isNonSticky = MehGravity.nonStickyBlocks.contains(startBlock.getType());
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
			Block parentBlock = world.getBlockAt(currentParent.getX(), currentParent.getY(), currentParent.getZ());

			for (int y = currentParent.getY(); y > -10; y--)
			{
				Block currentBlock = world.getBlockAt(currentParent.getX(), y, currentParent.getZ());
				if (currentBlock.getType() == Material.AIR || Structure.weakBlocks.contains(currentBlock.getType())) // We didn't find
															// bedrock, can't
															// continue search
					break;
				else if (MehGravity.staticBlocks.contains(currentBlock.getType()))
					return null;
			}

			for (int i = 0; i < 6; i++)
			{
				Location currentLocation = new Location(Structure.adjacentBlocks[i].getX() + currentParent.getX(), Structure.adjacentBlocks[i].getY() + currentParent.getY(),
						Structure.adjacentBlocks[i].getZ() + currentParent.getZ());
				Block currentBlock = world.getBlockAt(currentLocation.getX(), currentLocation.getY(), currentLocation.getZ());
				
				Material parentMaterial = parentBlock.getType();
				Material currentMaterial = currentBlock.getType();
				
				if(Structure.weakBlocks.contains(currentMaterial))
					continue;

				if (isNonSticky && currentBlock.getType() != Material.AIR && !structure.HasBlock(currentLocation))
				{
					if (currentMaterial == parentMaterial)
					{
						structure.AddBlock(currentBlock.getState(), currentLocation);
						blocksToCheck.add(currentLocation);
						structure.totalBlocks++;
					}
				}
				else if (currentBlock.getType() != Material.AIR && !structure.HasBlock(currentLocation))
				{
					if (MehGravity.staticBlocks.contains(currentBlock.getType()))
						return null;

					if (parentMaterial != currentMaterial && parentMaterial != Material.AIR)
					{
						// if we found a slippery block
						if (MehGravity.nonStickyBlocks.contains(currentMaterial) && !MehGravity.nonStickyBlocks.contains(parentMaterial))
						{
							//if it is free-standing, add it to the structure
							if (this.CreateStructure(currentBlock) == null)
								continue;
						}

						if (MehGravity.nonStickyBlocksAgainstEachother.containsKey(parentMaterial))
						{
							if (MehGravity.nonStickyBlocksAgainstEachother.get(parentMaterial).contains(currentMaterial))
								continue;
						}
						if (MehGravity.nonStickyBlocksAgainstEachother.containsKey(currentMaterial))
						{
							if (MehGravity.nonStickyBlocksAgainstEachother.get(currentMaterial).contains(parentMaterial))
								continue;
						}
					}

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
