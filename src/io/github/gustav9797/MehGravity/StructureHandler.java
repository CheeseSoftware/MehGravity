package io.github.gustav9797.MehGravity;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

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
	
	public void AddStructure(Structure structure)
	{
		if(!structures.containsKey(structure.id))
		{
			structure.moveDate = new Date();
			structures.put(structure.id, structure);
		}
	}
	
	public void RemoveStructure(int id)
	{
		if(structures.containsKey(id))
			structures.remove(id);
	}
}

class UpdateStructures extends BukkitRunnable
{
	MehGravity plugin;
	StructureHandler structureHandler;
	public UpdateStructures(MehGravity plugin, StructureHandler structureHandler)
	{
		this.plugin = plugin;
		this.structureHandler = structureHandler;
	}
	
	@Override
	public void run() 
	{
		Iterator<Entry<Integer, Structure>> i = structureHandler.structures.entrySet().iterator();
		while(i.hasNext())
		{
			Structure toCheck = i.next().getValue();
			//System.out.println("checking " + toCheck.id);
			if(toCheck.moveDate != null)
			{
				long difference = new Date().getTime() - toCheck.moveDate.getTime();
				if(difference > 200)
				{
					World world = toCheck.world;
					int blocksFall = toCheck.FindMovingSpaceDown(world);
					//System.out.println("fall " + blocksFall);
					if(blocksFall >= 1)
					{
						toCheck.SortLevels();
						toCheck.StoreNonSolidBlocks();
						toCheck.MoveOneDown(world);
					}
					else
						i.remove();
				}
			}
		}
	}
}
