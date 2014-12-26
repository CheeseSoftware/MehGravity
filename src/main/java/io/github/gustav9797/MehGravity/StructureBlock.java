package main.java.io.github.gustav9797.MehGravity;

import org.bukkit.block.BlockState;

class StructureBlock
{
    @SuppressWarnings("unused")
    private int        partOfStructureId;
    public Location    location;
    public BlockState  originalBlock;

    public StructureBlock(int partOfStructureId, Location location, BlockState originalBlock)
    {
        this.partOfStructureId    = partOfStructureId;
        this.location             = location;
        this.originalBlock        = originalBlock;
    }
}
