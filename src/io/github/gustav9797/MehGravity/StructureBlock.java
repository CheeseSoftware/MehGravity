package io.github.gustav9797.MehGravity;

import org.bukkit.block.BlockState;

public class StructureBlock
{
	public int partOfStructureId;
	public Location location;
	public BlockState originalBlock;

	public StructureBlock(int partOfStructureId, Location location, BlockState originalBlock)
	{
		this.partOfStructureId    = partOfStructureId;
		this.location             = location;
		this.originalBlock        = originalBlock;
	}
}
