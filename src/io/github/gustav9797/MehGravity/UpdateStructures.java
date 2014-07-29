package io.github.gustav9797.MehGravity;

import java.util.ArrayDeque;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Queue;

import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

class UpdateStructures extends BukkitRunnable
{
    MehGravity plugin;
    StructureHandler structureHandler;

    public UpdateStructures(MehGravity plugin, StructureHandler structureHandler)
    {
        this.plugin           = plugin;
        this.structureHandler = structureHandler;
    }

    @Override
    public void run()
    {
        Iterator<Entry<Integer, Structure>> i = structureHandler.structures.entrySet().iterator();
        Queue<Structure> toAdd = new ArrayDeque<Structure>();
        while (i.hasNext())
        {
            Structure toCheck = i.next().getValue();
            if (toCheck.moveDate != null)
            {
                long difference = new Date().getTime() - toCheck.moveDate.getTime();
                if (difference > 200)
                {
                    World world = toCheck.world;
                    int blocksFall = toCheck.getMaximumFallDistance(world);
                    if (blocksFall >= 1 && toCheck.size() > 0)
                    {
                        toCheck.sortLevels();
                        toCheck.storeNonSolidBlocks();
                        toCheck.moveDown.moveOneDown(world);
                    }
                    else
                    {
                        //one last gravity check and remove the structure
                        StructureBlock partOfStructure = toCheck.getExampleBlock();
                        if (partOfStructure != null)
                        {
                            Structure newStructure = structureHandler.createStructure(partOfStructure.originalBlock.getBlock());
                            i.remove();
                            if (newStructure != null && newStructure.getMaximumFallDistance(world) >= 1)
                                toAdd.add(newStructure);
                        }
                        else
                            i.remove();
                    }
                }
            }
        }
        while (toAdd.size() > 0) {
            Structure structure = toAdd.poll();
            if (structure != null)
                structureHandler.addStructure(structure);
        }
        toAdd.clear();
    }
}