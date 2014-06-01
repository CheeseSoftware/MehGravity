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
			// Store all blocks in the structure
			//Location minY = null;
			//for(Location l : blocksToCheck)
			//{
			//	if(minY == null || l.getY() < minY.getY())
			//		minY = l;
			//}			
			//Location currentParent = minY;
			//blocksToCheck.remove(minY);
			
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
			
			/*for (int y = location.getY()-1; y >= 0; y--)
			{
				Location currentLocation;
				Block currentBlock = world.getBlockAt(location.getX(), y, location.getZ());
				if (Structure.isMaterialWeak(currentBlock.getType()))
					break; // We didn't find bedrock, can't continue search
				
				else if (MehGravity.staticBlocks.contains(currentBlock.getType()))
					return null;
				
				currentLocation = new Location(location.getX(), y, location.getZ());
				
				if (currentBlock.getType() == Material.AIR || structure.HasBlock(currentLocation))
				{
					break;
				}
				else
				{
					//structure.AddBlock(block.getState(), currentLocation);
					blocksToCheck.add(location);
					//structure.totalBlocks++;
				}
					
			}*/
			
			blocksToCheck.add(new Location(location.getX(), location.getY()-1, location.getZ()));
			blocksToCheck.add(new Location(location.getX()+1, location.getY(), location.getZ()));
			blocksToCheck.add(new Location(location.getX(), location.getY(), location.getZ()+1));
			blocksToCheck.add(new Location(location.getX()-1, location.getY(), location.getZ()));
			blocksToCheck.add(new Location(location.getX(), location.getY(), location.getZ()-1));
			blocksToCheck.add(new Location(location.getX(), location.getY()+1, location.getZ()));
			
			/*for (int i = 0; i < 6; i++)
			{
				Location currentLocation = new Location(Structure.adjacentBlocks[i].getX() + currentParent.getX(), Structure.adjacentBlocks[i].getY() + currentParent.getY(),
						Structure.adjacentBlocks[i].getZ() + currentParent.getZ());
				
				if(currentLocation.getY() <= 0)
					return null;

				Block currentBlock = world.getBlockAt(currentLocation.getX(), currentLocation.getY(), currentLocation.getZ());
				
				Material parentMaterial = parentBlock.getType();
				Material currentMaterial = currentBlock.getType();
				
				if(Structure.isMaterialWeak(currentMaterial))
					continue;

				if (isNonSticky && currentBlock.getType() != Material.AIR && !structure.HasBlock(currentLocation))
				{
					if (currentMaterial == parentMaterial)
					{
						structure.AddBlock(currentBlock.getState(), currentLocation);
						blocksToCheck.add(currentLocation);
						structure.totalBlocks++;
						//plugin.getServer().getPlayer("gustav9797").sendBlockChange(currentBlock.getLocation(), Material.SPONGE, (byte) 0);
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
					//plugin.getServer().getPlayer("gustav9797").sendBlockChange(currentBlock.getLocation(), Material.SPONGE, (byte) 0);
				}
			}*/
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
