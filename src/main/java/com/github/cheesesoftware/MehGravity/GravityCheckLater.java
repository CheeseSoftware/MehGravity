package com.github.cheesesoftware.MehGravity;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

class GravityCheckLater extends BukkitRunnable // NO_UCD (unused code)
{
    Block      startBlock;
    MehGravity plugin;

    public GravityCheckLater(MehGravity plugin, Block startBlock)
    {
        this.startBlock = startBlock;
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        if (startBlock.getType() != Material.AIR)
        {
            Structure structure = plugin.structureHandler.createStructure(startBlock);
            if (structure != null)
                plugin.structureHandler.addStructure(structure);
        }
    }
}