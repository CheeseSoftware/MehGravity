package io.github.gustav9797.MehGravity;

import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

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
		while (i.hasNext())
		{
			Structure toCheck = i.next().getValue();
			if (toCheck.moveDate != null)
			{
				long difference = new Date().getTime() - toCheck.moveDate.getTime();
				if (difference > 200)
				{
					World world = toCheck.world;
					int blocksFall = toCheck.FindMovingSpaceDown(world);
					if (blocksFall >= 1)
					{
						toCheck.SortLevels();
						toCheck.StoreNonSolidBlocks();
						toCheck.MoveOneDown(world);
					}
					else
					{
						// Now we should do one last gravity check and remove
						// the structure
						StructureBlock partOfStructure = toCheck.getExampleBlock();
						Structure newStructure = structureHandler.CreateStructure(partOfStructure.originalBlock.getBlock());
						i.remove();
						if (newStructure != null)
						{
							structureHandler.AddStructure(newStructure);
						}
					}
				}
			}
		}
	}
}