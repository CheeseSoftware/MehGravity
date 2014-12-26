package main.java.io.github.gustav9797.MehGravity;

class ColumnCoord
{

    public final int x;
    public final int z;

    public ColumnCoord(int x, int z)
    {
        this.x = x;
        this.z = z;
    }

    @Override
    public boolean equals(Object object)
    {
        if (this == object) { return true; }
        if (!(object instanceof ColumnCoord)) { return false; }
        ColumnCoord key = (ColumnCoord) object;
        return x == key.x && z == key.z;
    }

    @Override
    public int hashCode()
    {
        return 31 * x + z;
    }
}
