package io.github.gustav9797.MehGravity;

public class Location
{
	private int x_;
	private int y_;
	private int z_;

	public Location(int x, int y, int z)
	{
		x_ = x;
		y_ = y;
		z_ = z;
	}

	@Override
	public String toString()
	{
		return ("X:" + x_ + " Y:" + y_ + " Z:" + z_);
	}

	@Override
	public boolean equals(Object other)
	{
		if (other == null)                {   return false;   }
		if (other == this)                {   return true;    }
		if (!(other instanceof Location)) {   return false;   }
		Location l = (Location) other;
		return x_ == l.x_ && y_ == l.y_ && z_ == l.z_;
	}

	@Override
	public int hashCode()
	{
		return x_ ^ y_ * 137 ^ z_ * 11317;
	}

	public int getX()
	{
		return x_;
	}

	public int getY()
	{
		return y_;
	}

	public int getZ()
	{
		return z_;
	}

	public void setX(int x)
	{
		x_ = x;
	}

	public void setY(int y)
	{
		y_ = y;
	}

	public void setZ(int z)
	{
		z_ = z;
	}
}
