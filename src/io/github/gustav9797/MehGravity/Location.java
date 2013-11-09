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
	
	/*@Override
	public boolean equals(Object other){
	    Location otherLocation = (Location)other;
	    if(x_ == otherLocation.getX() && y_ == otherLocation.getY() && z_ == otherLocation.getZ())
	    	return true;
	    return false;
	}*/
	
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
        result = 31 * result + z_;
        return result;
    }
	
	public int getX() { return x_; }
	public int getY() { return y_; }
	public int getZ() { return z_; }
}
