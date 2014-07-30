package io.github.gustav9797.MehGravity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

//import me.tubelius.autoprice.GetData;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

class Structure
{
    public  int                         id;
    public  World                       world;
    public  int                         totalBlocks;
    public  int                         yMin = Integer.MAX_VALUE;
    public  int                         yMax = 0;
    HashMap<Location, StructureBlock>   blocks;
    Queue<StructureBlock>               sortedLevelBlocks;
    Queue<Location>                     sensitiveBlocks;
    public  Date                        moveDate;
    MoveDown                            moveDown    = new MoveDown(this);


    public Structure(int id, World world)
    {
        this.id = id;
        totalBlocks = 0;
        blocks = new HashMap<Location, StructureBlock>();
        sortedLevelBlocks = new LinkedList<StructureBlock>();
        sensitiveBlocks = new LinkedList<Location>();
        this.world = world;
    }
    
    // Adjacent locations
    static Location[] adjacentBlocks = { 
        new Location(0, -1, 0), 
        new Location(1, 0, 0), 
        new Location(-1, 0, 0), 
        new Location(0, 1, 0), 
        new Location(0, 0, 1), 
        new Location(0, 0, -1) 
    };

    // Non-supporting materials
    @SuppressWarnings("serial")
    static final ArrayList<Material> weakBlocks = new ArrayList<Material>()
    {
        {
            add(Material.BROWN_MUSHROOM);
            add(Material.CAKE_BLOCK);
            add(Material.CARPET);
            add(Material.CARROT);
            add(Material.CROPS);
            add(Material.DEAD_BUSH);
            add(Material.DOUBLE_PLANT);
            add(Material.FIRE);
            add(Material.LAVA);
            add(Material.LONG_GRASS);
            add(Material.MELON_STEM);
            add(Material.POTATO);
            add(Material.PUMPKIN_STEM);
            add(Material.RED_MUSHROOM);
            add(Material.RED_ROSE);
            add(Material.REDSTONE_WIRE);
            add(Material.SAPLING);
            add(Material.SNOW);
            add(Material.STATIONARY_LAVA);
            add(Material.STATIONARY_WATER);
            add(Material.STONE_BUTTON);
            add(Material.TRIPWIRE);
            add(Material.TRIPWIRE_HOOK);
            add(Material.WATER);
            add(Material.WATER_LILY);
            add(Material.WOOD_BUTTON);
            add(Material.YELLOW_FLOWER);
        }
    };

    // List of blocks that pop/break when a supporting block is removed
    @SuppressWarnings("serial")
    static final ArrayList<Material> annoyingBlocks = new ArrayList<Material>() {
        {
            add(Material.ACTIVATOR_RAIL);
            add(Material.BROWN_MUSHROOM);
            add(Material.CACTUS);
            add(Material.CARPET);
            add(Material.DAYLIGHT_DETECTOR);
            add(Material.DETECTOR_RAIL);
            add(Material.DIODE);
            add(Material.DIODE_BLOCK_OFF);
            add(Material.DIODE_BLOCK_ON);
            add(Material.FLOWER_POT);
            add(Material.FLOWER_POT);
            add(Material.GOLD_PLATE);
            add(Material.IRON_DOOR_BLOCK);
            add(Material.IRON_PLATE);
            add(Material.ITEM_FRAME);
            add(Material.LADDER);
            add(Material.LEVER);
            add(Material.LONG_GRASS);
            add(Material.PAINTING);
            add(Material.POWERED_RAIL);
            add(Material.RAILS);
            add(Material.RED_MUSHROOM);
            add(Material.RED_ROSE);
            add(Material.REDSTONE_COMPARATOR);
            add(Material.REDSTONE_COMPARATOR_OFF);
            add(Material.REDSTONE_COMPARATOR_ON);
            add(Material.REDSTONE_TORCH_OFF);
            add(Material.REDSTONE_TORCH_ON);
            add(Material.REDSTONE_WIRE);
            add(Material.SAPLING);
            add(Material.SIGN_POST);
            add(Material.SKULL);
            add(Material.SNOW);
            add(Material.STONE_BUTTON);
            add(Material.STONE_PLATE);
            add(Material.TORCH);
            add(Material.TRAP_DOOR);
            add(Material.TRIPWIRE);
            add(Material.TRIPWIRE_HOOK);
            add(Material.WALL_SIGN);
            add(Material.WATER_LILY);
            add(Material.VINE);
            add(Material.WOOD_BUTTON);
            add(Material.WOOD_PLATE);
            add(Material.WOODEN_DOOR);
            add(Material.YELLOW_FLOWER);
        }
    };

    public static boolean isMaterialWeak(Material material)
    {
        return Structure.weakBlocks.contains(material) || material == Material.AIR;
    }

    public StructureBlock getExampleBlock()
    {
        Iterator<Entry<Location, StructureBlock>> i = blocks.entrySet().iterator();
        while (i.hasNext())
        {
            return i.next().getValue();
        }
        return null;
    }

    public void addBlock(BlockState blockState, Location location)
    {
        if (!blocks.containsKey(location)) {
            blocks.put(location, new StructureBlock(id, location, blockState));
        }
    }

    public void sortLevels()
    {
        sortedLevelBlocks = new LinkedList<StructureBlock>();
        Iterator<Entry<Location, StructureBlock>> i = blocks.entrySet().iterator();
        while (i.hasNext())
        {
            StructureBlock current = i.next().getValue();
            if (current.location.getY() < yMin)
                yMin = current.location.getY();
            if (current.location.getY() > yMax)
                yMax = current.location.getY();
        }

        for (int y = yMin; y <= yMax; y++)
        {
            i = blocks.entrySet().iterator();
            while (i.hasNext())
            {
                StructureBlock current = i.next().getValue();
                if (current.location.getY() == y)
                {
                    sortedLevelBlocks.add(current);
                }
            }
        }
    }

    public boolean hasBlock(Location location)
    {
        return blocks.containsKey(location);
    }

    public void storeNonSolidBlocks()
    {
        sensitiveBlocks = new LinkedList<Location>();
        Iterator<Entry<Location, StructureBlock>> i = blocks.entrySet().iterator();
        while (i.hasNext())
        {
            StructureBlock current = i.next().getValue();

            if (Structure.annoyingBlocks.contains(current.originalBlock.getType()))
            {
                sensitiveBlocks.add(current.location);
                if (current.originalBlock.getType() == Material.IRON_DOOR_BLOCK || current.originalBlock.getType() == Material.WOODEN_DOOR)
                {
                    Block above = current.originalBlock.getBlock().getRelative(BlockFace.UP);
                    Block below = current.originalBlock.getBlock().getRelative(BlockFace.DOWN);
                    if (above.getType() == Material.IRON_DOOR_BLOCK || above.getType() == Material.WOODEN_DOOR)
                    {
                        current.originalBlock.getBlock().setType(Material.AIR);
                        above.setType(Material.AIR);
                        continue;
                    }
                    else if (below.getType() == Material.IRON_DOOR_BLOCK || below.getType() == Material.WOODEN_DOOR)
                    {
                        below.setType(Material.AIR);
                        current.originalBlock.getBlock().setType(Material.AIR);
                        continue;
                    }
                }
                current.originalBlock.getBlock().setType(Material.AIR);
            }
        }
    }

    public int size()
    {
        return blocks.size();
    }

    public int getMaximumFallDistance(World world)//FindMovingSpaceDown(World world)
    {
        //Check maximum fall distance for each column per X&Z coordinate
        Map<ColumnCoord, Integer> columnsMinHeight = new HashMap<ColumnCoord, Integer>();
        Map<ColumnCoord, Integer> columnsMaxHeight = new HashMap<ColumnCoord, Integer>();

        Iterator<Entry<Location, StructureBlock>> it = blocks.entrySet().iterator();
        while (it.hasNext())
        {
            Location block = it.next().getKey();
            ColumnCoord columnXZ = new ColumnCoord(block.getX(), block.getZ());
            Integer minY = columnsMinHeight.get(columnXZ);
            Integer maxY = columnsMaxHeight.get(columnXZ);
            if (minY == null)
            {
                columnsMinHeight.put(columnXZ, block.getY());
                columnsMaxHeight.put(columnXZ, block.getY());
            }
            else
            {
                columnsMinHeight.put(columnXZ, Math.min(minY, block.getY()));
                columnsMaxHeight.put(columnXZ, Math.max(maxY, block.getY()));
            }
        }

        //Calculate & return the maximum fall distance for whole structure based on the column data
        return getMaximumFallDistance(columnsMinHeight,columnsMaxHeight);
    }

    private int getMaximumFallDistance(Map<ColumnCoord, Integer> columnsMinHeight, Map<ColumnCoord, Integer> columnsMaxHeight) {
        int currentMaxFall = Integer.MAX_VALUE;
        for (Map.Entry<ColumnCoord, Integer> entry : columnsMaxHeight.entrySet()) { //Loop columns in each X&Z coordinate
            int minY = columnsMinHeight.get(entry.getKey());
            int maxY = entry.getValue();
            for (int currentY = maxY; currentY >= minY; currentY--) {   //Loop all blocks in a XZ-column from top to bottom 
                if (isMaterialWeak(world.getBlockAt(entry.getKey().x, currentY - 1, entry.getKey().z).getType())) {
                    currentMaxFall = getCurrentMaxFallForWeakBlock(currentY, entry, currentMaxFall, minY);
                }
                else if (blocks.containsKey(new Location(entry.getKey().x, currentY, entry.getKey().z))) {
                    if (!blocks.containsKey(new Location(entry.getKey().x, currentY - 1, entry.getKey().z))) {
                        return 0;
                    }
                }
            }
        }
        if (currentMaxFall > 1024) {
            return 0;
        }
        return currentMaxFall;
    }

    private int getCurrentMaxFallForWeakBlock(int currentY, Entry<ColumnCoord, Integer> entry, int currentMaxFall, int minY) {
        int tempCurrentMaxFall = 0;
        for (int y = currentY - 1; true; y--) {
            Material currentBlockMaterial = world.getBlockAt(entry.getKey().x, y, entry.getKey().z).getType();
            if (y < 0) {
                currentMaxFall = Math.min(1024, currentMaxFall);
                break;
            }
            else if (isMaterialWeak(currentBlockMaterial)) {
                tempCurrentMaxFall++;
            }
            else if (blocks.containsKey(new Location(entry.getKey().x, y, entry.getKey().z))) {
                currentY = y + 1;
                break;
            } 
            else {
                currentMaxFall = Math.min(tempCurrentMaxFall, currentMaxFall);
                int tempY = 0;
                for (tempY = y; tempY >= minY; tempY--) {
                    if (blocks.containsKey(new Location(entry.getKey().x, tempY, entry.getKey().z))) {
                        currentY = tempY;
                        break;
                    }
                }
                break;
            }
        }
        return currentMaxFall;
    }

    void removeItems(Material material, org.bukkit.Location location, float maximumDistance) {
        //find items to remove
        List<Item> itemsToRemove = new ArrayList<Item>(); 
        for (Item item : location.getWorld().getEntitiesByClass(Item.class)) {
            ItemStack stack = item.getItemStack();
            if (stack.getType() == material) {
                if (item.getLocation().distance(location) <= maximumDistance) {
                    itemsToRemove.add(item);
                }
            }
        }
        
        //remove items
        for (Item item : itemsToRemove) {
            item.remove();
        }
    }
}
