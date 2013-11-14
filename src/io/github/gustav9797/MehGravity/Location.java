package io.github.gustav9797.MehGravity;

public class Location {
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
    public boolean equals(Object o) 
    {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;
        Location key = (Location) o;
        return x_ == key.x_ && y_ == key.y_ && z_ == key.z_;
    }

    @Override
    public int hashCode() 
    {
        int result = x_;
        result = 31 * result + 1024 * y_ + z_;
        return result;
    }
	
	public int getX() { return x_; }
	public int getY() { return y_; }
	public int getZ() { return z_; }
}
