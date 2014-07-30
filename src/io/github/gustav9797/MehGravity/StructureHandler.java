package io.github.gustav9797.MehGravity;

import java.util.Date;
import java.util.HashMap;
import java.util.Stack;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

class StructureHandler
{
    HashMap<Integer, Structure>    structures;
//    private MehGravity                     plugin;

    StructureHandler(MehGravity plugin)
    {
//        this.plugin = plugin;
        structures = new HashMap<Integer, Structure>();
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new UpdateStructures(plugin, this), 1, 1);
    }

    public int getFreeStructureId()
    {
        return structures.size();
    }

    public Structure createStructure(Block startBlock)
    {
        if (!MehGravity.isWorldAffected(startBlock.getWorld().getName())) {
            return null;
        }
        
        Location          startLocation;
        Structure         structure;
        Stack<Location>   blocksToCheck = new Stack<Location>();
        World             world = startBlock.getWorld();
        
        startLocation = new Location( startBlock.getX() , startBlock.getY() , startBlock.getZ() );
        structure = new Structure( getFreeStructureId() , startBlock.getWorld() );
        blocksToCheck.add(startLocation);
        
        while (!blocksToCheck.isEmpty())
        {
            Location location;
            Block    block;
            Material material;
            
            if (structure.totalBlocks > MehGravity.blockLimit) { return null; }
            
            location = blocksToCheck.pop();

            if (location.getY() <= 0) { return null; }
            
            block = world.getBlockAt(location.getX(), location.getY(), location.getZ());
            material = block.getType();
            
            if (material == Material.BEDROCK) { return null; }
            
            if (Structure.isMaterialWeak(material)) { continue; }

            //gravel and sand has to be treated as air because:
            //    if the plugin and vanilla server tries to move them, they get duplicated
            //    they should never stick to other blocks next to them
            if (material == Material.AIR || material == Material.GRAVEL || material == Material.SAND || structure.hasBlock(location)) {
                continue;
            }
            
            structure.addBlock( block.getState() , new Location(location.getX(),location.getY(),location.getZ()) );
            
            structure.totalBlocks++;
            
            // Note that it ends with location.getY()-1. That will be the next node since it's in the top of the stack.
            blocksToCheck.add(new Location(location.getX(),      location.getY()+1,  location.getZ()     ));
            blocksToCheck.add(new Location(location.getX()+1,    location.getY(),    location.getZ()     ));
            blocksToCheck.add(new Location(location.getX(),      location.getY(),    location.getZ()+1   ));
            blocksToCheck.add(new Location(location.getX()-1,    location.getY(),    location.getZ()     ));
            blocksToCheck.add(new Location(location.getX(),      location.getY(),    location.getZ()-1   ));
            blocksToCheck.add(new Location(location.getX(),      location.getY()-1,  location.getZ()     ));
        }
        return structure;
    }

    public void addStructure(Structure structure)
    {
        if (!structures.containsKey(structure.id)) {
            structure.moveDate = new Date();
            structures.put(structure.id, structure);
        }
    }

    public void removeStructure(int id)
    {
        if (structures.containsKey(id))
            structures.remove(id);
    }
}
