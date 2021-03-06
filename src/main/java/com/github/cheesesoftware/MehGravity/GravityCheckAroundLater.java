package com.github.cheesesoftware.MehGravity;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

class GravityCheckAroundLater extends BukkitRunnable
{
    private Block      startBlock;
    private MehGravity plugin;

    public GravityCheckAroundLater(MehGravity plugin, Block startBlock)
    {
        this.startBlock   = startBlock;
        this.plugin       = plugin;
    }

    @Override
    public void run()
    {
        for (int i = 0; i < 6; i++)
        {
            Block currentBlock = startBlock.getWorld().getBlockAt(
                startBlock.getX() + Structure.adjacentBlocks[i].getX(), 
                startBlock.getY() + Structure.adjacentBlocks[i].getY(),
                startBlock.getZ() + Structure.adjacentBlocks[i].getZ()
            );
            if (currentBlock.getType() != Material.AIR)
            {
                Structure structure = plugin.structureHandler.createStructure(currentBlock);
                if (structure != null) {
                    plugin.structureHandler.addStructure(structure);
                }
            }
        }
    }
}
