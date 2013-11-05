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
	public boolean equals(Object other){
	    Location otherLocation = (Location)other;
	    if(x_ == otherLocation.getX() && y_ == otherLocation.getY() && z_ == otherLocation.getZ())
	    	return true;
	    return false;
	}
	
	public int getX() { return x_; }
	public int getY() { return y_; }
	public int getZ() { return z_; }
}
