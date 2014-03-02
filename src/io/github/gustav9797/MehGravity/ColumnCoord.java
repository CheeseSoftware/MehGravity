package io.github.gustav9797.MehGravity;

public class ColumnCoord
{

	public final int x;
	public final int z;

	public ColumnCoord(int x, int z)
	{
		this.x = x;
		this.z = z;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (!(o instanceof ColumnCoord))
			return false;
		ColumnCoord key = (ColumnCoord) o;
		return x == key.x && z == key.z;
	}

	@Override
	public int hashCode()
	{
		int result = x;
		result = 31 * result + z;
		return result;
	}
}