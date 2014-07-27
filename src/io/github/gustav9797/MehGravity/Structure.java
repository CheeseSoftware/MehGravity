package io.github.gustav9797.MehGravity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.block.Furnace;
import org.bukkit.block.Hopper;
import org.bukkit.block.Jukebox;
import org.bukkit.block.NoteBlock;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.inventory.Inventory;
import org.bukkit.material.PistonBaseMaterial;
import org.bukkit.material.Torch;
import org.bukkit.material.TrapDoor;

public class Structure
{
    public int id;
    public World world;
    public int totalBlocks;
    public int yMin = Integer.MAX_VALUE;
    public int yMax = 0;
    private HashMap<Location, StructureBlock> blocks;
    private Queue<StructureBlock> sortedLevelBlocks;
    private Queue<Location> sensitiveBlocks;
    public Date moveDate;

    // Adjacent locations
    static Location[] adjacentBlocks =
    { new Location(0, -1, 0), new Location(1, 0, 0), new Location(-1, 0, 0), new Location(0, 1, 0), new Location(0, 0, 1), new Location(0, 0, -1) };

    // Non-supporting materials
    @SuppressWarnings("serial")
    static final ArrayList<Material> weakBlocks = new ArrayList<Material>()
    {
        {
            add(Material.SNOW);
            add(Material.WATER);
            add(Material.LAVA);
            add(Material.RED_ROSE);
            add(Material.YELLOW_FLOWER);
            add(Material.CARPET);
            add(Material.FIRE);
            add(Material.LONG_GRASS);
            add(Material.DEAD_BUSH);
            add(Material.BROWN_MUSHROOM);
            add(Material.RED_MUSHROOM);
            
            // Added by tubelius 20140419
            add(Material.CAKE_BLOCK);
            add(Material.CARROT);
            add(Material.CROPS);
            add(Material.DOUBLE_PLANT);
            add(Material.MELON_STEM);
            add(Material.POTATO);
            add(Material.PUMPKIN_STEM);
            add(Material.REDSTONE_WIRE);
            add(Material.SAPLING);
            add(Material.STATIONARY_LAVA);
            add(Material.STATIONARY_WATER);
            add(Material.STONE_BUTTON);
            add(Material.TRIPWIRE);
            add(Material.TRIPWIRE_HOOK);
            add(Material.WATER_LILY);
            add(Material.WOOD_BUTTON);
        }
    };

    // List of blocks that pop/break when a supporting block is removed
    @SuppressWarnings("serial")
    static final ArrayList<Material> annoyingBlocks = new ArrayList<Material>()
    {
        {
            add(Material.WOODEN_DOOR);
            add(Material.IRON_DOOR_BLOCK);
            add(Material.TRAP_DOOR);
            add(Material.TORCH);
            add(Material.SAPLING);
            add(Material.LONG_GRASS);
            add(Material.YELLOW_FLOWER);
            add(Material.RED_ROSE);
            add(Material.BROWN_MUSHROOM);
            add(Material.RED_MUSHROOM);
            add(Material.LADDER);
            add(Material.SNOW);
            add(Material.VINE);
            add(Material.WATER_LILY);
            add(Material.CARPET);
            add(Material.PAINTING);
            add(Material.SIGN_POST);
            add(Material.WALL_SIGN);
//            add(Material.BED);
            add(Material.ITEM_FRAME);
            add(Material.FLOWER_POT);
            add(Material.LEVER);
            add(Material.STONE_PLATE);
            add(Material.WOOD_PLATE);
            add(Material.REDSTONE_TORCH_OFF);
            add(Material.REDSTONE_TORCH_ON);
            add(Material.STONE_BUTTON);
            add(Material.TRIPWIRE_HOOK);
            add(Material.WOOD_BUTTON);
            add(Material.GOLD_PLATE);
            add(Material.IRON_PLATE);
            add(Material.DAYLIGHT_DETECTOR);
            add(Material.REDSTONE_WIRE);
            add(Material.REDSTONE_COMPARATOR);
            add(Material.REDSTONE_COMPARATOR_OFF);
            add(Material.REDSTONE_COMPARATOR_ON);
            add(Material.DIODE);
            add(Material.DIODE_BLOCK_OFF);
            add(Material.DIODE_BLOCK_ON);
            add(Material.RAILS);
            add(Material.POWERED_RAIL);
            add(Material.DETECTOR_RAIL);
            add(Material.ACTIVATOR_RAIL);
            add(Material.TRIPWIRE);
            add(Material.FLOWER_POT);
            add(Material.CACTUS);
            add(Material.SKULL);
        }
    };

    public static boolean isMaterialWeak(Material material)
    {
        return Structure.weakBlocks.contains(material) || material == Material.AIR;
    }

    public Structure(int id, World world)
    {
        this.id = id;
        totalBlocks = 0;
        blocks = new HashMap<Location, StructureBlock>();
        sortedLevelBlocks = new LinkedList<StructureBlock>();
        sensitiveBlocks = new LinkedList<Location>();
        this.world = world;
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

    public void AddBlock(BlockState blockState, Location location)
    {
        if (!blocks.containsKey(location))
            blocks.put(location, new StructureBlock(id, location, blockState));
    }

    public void SortLevels()
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

    public boolean HasBlock(Location location)
    {
        return blocks.containsKey(location);
    }

    public void StoreNonSolidBlocks()
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

    public int Size()
    {
        return blocks.size();
    }

    public int FindMovingSpaceDown(World world)
    {
        Map<ColumnCoord, Integer> minima = new HashMap<ColumnCoord, Integer>();
        Map<ColumnCoord, Integer> maxima = new HashMap<ColumnCoord, Integer>();

        Iterator<Entry<Location, StructureBlock>> it = blocks.entrySet().iterator();
        while (it.hasNext())
        {

            // BlockState block = it.next().getValue().originalBlock;
            Location block = it.next().getKey();
            ColumnCoord coord = new ColumnCoord(block.getX(), block.getZ());
            Integer min = minima.get(coord);
            Integer max = maxima.get(coord);
            if (min == null)
            {
                minima.put(coord, block.getY());
                maxima.put(coord, block.getY());
            }
            else
            {
                minima.put(coord, Math.min(min, block.getY()));
                maxima.put(coord, Math.max(max, block.getY()));
            }
        }

        int currentMaxFall = Integer.MAX_VALUE;
        for (Map.Entry<ColumnCoord, Integer> entry : maxima.entrySet())
        {
            int minY = minima.get(entry.getKey());
            int maxY = entry.getValue();
            for (int currentY = maxY; currentY >= minY; currentY--)
            {
                if (isMaterialWeak(world.getBlockAt(entry.getKey().x, currentY - 1, entry.getKey().z).getType()))
                {
                    int tempCurrentMaxFall = 0;
                    for (int y = currentY - 1; true; y--)
                    {
                        Material currentBlockMaterial = world.getBlockAt(entry.getKey().x, y, entry.getKey().z).getType();
                        if (y < 0)
                        {
                            currentMaxFall = Math.min(1024, currentMaxFall);
                            break;
                        }
                        else if (isMaterialWeak(currentBlockMaterial))
                        {
                            tempCurrentMaxFall++;
                        }
                        else if (blocks.containsKey(new Location(entry.getKey().x, y, entry.getKey().z)))
                        {
                            currentY = y + 1;
                            break;
                        }
                        else
                        {
                            currentMaxFall = Math.min(tempCurrentMaxFall, currentMaxFall);
                            int tempY = 0;
                            for (tempY = y; tempY >= minY; tempY--)
                            {
                                if (blocks.containsKey(new Location(entry.getKey().x, tempY, entry.getKey().z)))
                                {
                                    currentY = tempY;
                                    break;
                                }
                            }
                            break;
                        }

                    }
                }
                else if (blocks.containsKey(new Location(entry.getKey().x, currentY, entry.getKey().z)))
                {
                    if (!blocks.containsKey(new Location(entry.getKey().x, currentY - 1, entry.getKey().z)))
                    {
                        return 0;
                    }
                }
            }
        }
        if (currentMaxFall > 1024)
            return 0;
        return currentMaxFall;
    }

    @SuppressWarnings("deprecation")
    public void MoveOneDown(World world)
    {
        // Move solid blocks down
        Iterator<StructureBlock> i = sortedLevelBlocks.iterator();
        while (i.hasNext())
        {
            StructureBlock current = i.next();
            boolean removeOldBlock = true;    //by default old blocks are removed, but there are exceptions
            
            if (current.location.getY() - 1 < 0)
            {
                current.originalBlock.getBlock().setType(Material.AIR);
                i.remove();
                continue;
            }

            if (!sensitiveBlocks.contains(current.location))
            {
                BlockState from = current.originalBlock;
                BlockState fromState = from;
                Block to = world.getBlockAt(from.getLocation().getBlockX(), from.getLocation().getBlockY() - 1, from.getLocation().getBlockZ());
                if(Structure.isMaterialWeak(to.getType()))
                    to.breakNaturally();
                to.setType(from.getType());
                to.setData(from.getBlock().getData());

                switch (from.getType())
                {
                    case COMMAND:
                    {
                        CommandBlock fromBlock = (CommandBlock) fromState;
                        CommandBlock toBlock = (CommandBlock) to.getState();
                        toBlock.setCommand(fromBlock.getCommand());
                        toBlock.update(); //I guess it should be like this
                        break;
                    }
                    case MOB_SPAWNER:
                    {
                        CreatureSpawner fromSpawner = (CreatureSpawner) fromState;
                        CreatureSpawner toSpawner = (CreatureSpawner) to.getState();
                        toSpawner.setDelay(fromSpawner.getDelay());
                        toSpawner.setSpawnedType(fromSpawner.getSpawnedType());
                        toSpawner.update(); //Added by tubelius 20140512
                        break;
                    }
                    case REDSTONE_TORCH_ON:
                    {
                        // Set it to air to make it get forcefully placed back
                        // and trigger redstone
                        to.setType(Material.AIR);
                        break;
                    }
                    case CHEST:
                    case TRAPPED_CHEST:
                    {
                        Chest fromChest = (Chest) fromState;
                        Chest toChest = (Chest) to.getState();
                        Inventory fromInventory = fromChest.getInventory();
                        Inventory toInventory = toChest.getInventory();
                        if (fromInventory.getSize() == toInventory.getSize()) {   
                            //chest is complete --> move content
                            toInventory.setContents(fromInventory.getContents());
                            fromInventory.clear();
                            //the other side of double chest may have been skipped when the new chest was incomplete --> delete that side
                            if (from.getType() == current.originalBlock.getBlock().getRelative(BlockFace.NORTH).getType()) {
                                current.originalBlock.getBlock().getRelative(BlockFace.NORTH).setType(Material.AIR);
                            } else if (from.getType() == current.originalBlock.getBlock().getRelative(BlockFace.EAST).getType()) {
                                current.originalBlock.getBlock().getRelative(BlockFace.EAST).setType(Material.AIR);
                            } else if (from.getType() == current.originalBlock.getBlock().getRelative(BlockFace.SOUTH).getType()) {
                                current.originalBlock.getBlock().getRelative(BlockFace.SOUTH).setType(Material.AIR);
                            } else if (from.getType() == current.originalBlock.getBlock().getRelative(BlockFace.WEST).getType()) {
                                current.originalBlock.getBlock().getRelative(BlockFace.WEST).setType(Material.AIR);
                            }
                        } else {
                            //maybe only one side of double chest has been moved (keep looping the blocks to finish the chest)
                            //keep the old block until the chest is complete and inventory has been moved
                            removeOldBlock = false;
                        }
                        break;
                    }
                    case FURNACE:
                    case BURNING_FURNACE:
                    {
                        Furnace fromFurnace = (Furnace) fromState;
                        Furnace toFurnace = (Furnace) to.getState();
                        Inventory fromInventory = fromFurnace.getInventory();
                        Inventory toInventory = toFurnace.getInventory();
                        toInventory.setContents(fromInventory.getContents());
                        toFurnace.setBurnTime(fromFurnace.getBurnTime());
                        toFurnace.setCookTime(fromFurnace.getCookTime());
                        fromInventory.clear();
                        break;
                    }
                    case HOPPER:
                    {
                        Hopper fromHopper = (Hopper) fromState;
                        Hopper toHopper = (Hopper) to.getState();
                        Inventory fromInventory = fromHopper.getInventory();
                        Inventory toInventory = toHopper.getInventory();
                        toInventory.setContents(fromInventory.getContents());
                        fromInventory.clear();
                        break;
                    }
                    case DROPPER:
                    {
                        Dropper fromDropper = (Dropper) fromState;
                        Dropper toDropper = (Dropper) to.getState();
                        Inventory fromInventory = fromDropper.getInventory();
                        Inventory toInventory = toDropper.getInventory();
                        toInventory.setContents(fromInventory.getContents());
                        fromInventory.clear();
                        break;
                    }
                    case BEACON:
                    {
                        Beacon fromBeacon = (Beacon) fromState;
                        Beacon toBeacon = (Beacon) to.getState();
                        Inventory fromInventory = fromBeacon.getInventory();
                        Inventory toInventory = toBeacon.getInventory();
                        toInventory.setContents(fromInventory.getContents());
                        fromInventory.clear();
                        break;
                    }
                    case DISPENSER:
                    {
                        Dispenser fromDispenser = (Dispenser) fromState;
                        Dispenser toDispenser = (Dispenser) to.getState();
                        Inventory fromInventory = fromDispenser.getInventory();
                        Inventory toInventory = toDispenser.getInventory();
                        toInventory.setContents(fromInventory.getContents());
                        fromInventory.clear();
                        break;
                    }
                    case JUKEBOX:
                    {
                        Jukebox fromJukebox = (Jukebox) fromState;
                        Jukebox toJukebox = (Jukebox) to.getState();
                        if (fromJukebox.isPlaying())
                        {
                            toJukebox.setPlaying(fromJukebox.getPlaying());
                        }
                        fromJukebox.setPlaying(null);
                        break;
                    }
                    case NOTE_BLOCK:
                    {
                        NoteBlock fromNoteBlock = (NoteBlock) fromState;
                        NoteBlock toNoteBlock = (NoteBlock) to.getState();
                        toNoteBlock.setNote(fromNoteBlock.getNote());
                        break;
                    }
                    case PISTON_BASE:
                    case PISTON_STICKY_BASE:
                    {
                        PistonBaseMaterial fromPiston = (PistonBaseMaterial) fromState.getData();
                        PistonBaseMaterial toPiston = (PistonBaseMaterial) to.getState().getData();
                        toPiston.setFacingDirection(fromPiston.getFacing());
                        toPiston.setPowered(fromPiston.isPowered());
                        break;
                    }
                    default:
                        break;
                }
//                from.getBlock().setType(Material.AIR);
                if (removeOldBlock) { from.getBlock().setType(Material.AIR); }
                i.remove();
            }
        }
        // Place all non-solid blocks back
        i = sortedLevelBlocks.iterator();
        while (i.hasNext())
        {
            StructureBlock current = i.next();
            BlockState fromState = blocks.get(current.location).originalBlock;
            //Location aboveLocation = new Location(current.location.getX(), current.location.getY() + 1, current.location.getZ());
            //BlockState fromStateAbove = null;
            //if (blocks.containsKey(aboveLocation))
                //fromStateAbove = blocks.get(aboveLocation).originalBlock;
            
            Block to = world.getBlockAt(current.location.getX(), current.location.getY() - 1, current.location.getZ());
            if(Structure.isMaterialWeak(to.getType()))
                to.breakNaturally();
            
            if (fromState.getType() != Material.WOODEN_DOOR && fromState.getType() != Material.IRON_DOOR_BLOCK)
            {
                boolean hasBlockToSitOn = false;
                // Make sure we don't place a sensitive block in the air
                for (int j = 0; j < Structure.adjacentBlocks.length; j++)
                {
                    Block toCheck = world.getBlockAt(to.getX() + Structure.adjacentBlocks[j].getX(), to.getY() + Structure.adjacentBlocks[j].getY(), to.getZ() + Structure.adjacentBlocks[j].getZ());
                    if (toCheck.getType() != Material.AIR && !Structure.annoyingBlocks.contains(toCheck.getType()))
                    {
                        hasBlockToSitOn = true;
                        break;
                    }
                }
                if (!hasBlockToSitOn)
                {
                    to.setType(Material.AIR);
                    i.remove();
                    continue;
                }
                // if(fromState.getType() == Material.FLOWER_POT)
                // ((FlowerPot)fromState.getData()).setContents(null);
                to.setType(fromState.getType());
                to.setData(fromState.getBlock().getData());
            }

            switch (fromState.getType())
            {
                case SKULL:
                {
                    Skull toSkull = (Skull) to.getState();
                    Skull fromSkull = (Skull) fromState;
                    // toSkull.getBlock().setData(fromSkull.getData().getData());
                    toSkull.setRotation(fromSkull.getRotation());
                    toSkull.setSkullType(fromSkull.getSkullType());
                    toSkull.update();
                    break;
                }
                case FLOWER_POT:
                {
                    // I give up with flower pots

                    // FlowerPot toFlowerPot
                    // =(FlowerPot)to.getState().getData();
                    // toFlowerPot.setContents(new
                    // MaterialData(Material.RED_ROSE));
                    break;
                }
                case TORCH:
                {
                    Torch fromTorch = (Torch) fromState.getData();
                    Torch toTorch = (Torch) to.getState().getData();
                    toTorch.setFacingDirection(fromTorch.getFacing());
                    break;
                }
                case SIGN:
                case SIGN_POST:
                case WALL_SIGN:
                {
                    Sign fromSign = (Sign) fromState;
                    Sign toSign = (Sign) to.getState();
                    org.bukkit.material.Sign fromSignMat = (org.bukkit.material.Sign) fromSign.getData();
                    toSign.setData(fromSignMat);
                    String[] fromLines = fromSign.getLines();
                    for (int index = 0; index < fromLines.length; index++)
                    {
                        toSign.setLine(index, fromLines[index]);
                    }
                    toSign.update();
                    break;
                }
                case TRAP_DOOR:
                {
                    TrapDoor fromTrapDoor = (TrapDoor) fromState.getData();
                    TrapDoor toTrapDoor = (TrapDoor) to.getState().getData();
                    toTrapDoor.setOpen(fromTrapDoor.isOpen());
                    break;
                }
                case WOODEN_DOOR:
                {
                    Block top = to.getRelative(BlockFace.UP, 1);
                    if (top.getRelative(BlockFace.DOWN).getType() == Material.WOODEN_DOOR)
                        break;

                    to.setType(Material.WOODEN_DOOR);
                    to.setData(fromState.getBlock().getData());

                    top.setType(Material.WOODEN_DOOR);
                    top.setData((byte) 8);

                    // Now check if it's a double-door or single-door
                    int directionFacing = to.getData();
                    switch (directionFacing)
                    {
                        case 0: // Door is facing west
                        {
                            Block b = top.getRelative(BlockFace.NORTH);
                            if (b.getType() == Material.WOODEN_DOOR)
                                top.setData((byte) 9);
                            break;
                        }
                        case 1: // Door is facing north
                        {
                            Block b = top.getRelative(BlockFace.EAST);
                            if (b.getType() == Material.WOODEN_DOOR)
                                top.setData((byte) 9);
                            break;
                        }
                        case 2: // Door is facing east
                        {
                            Block b = top.getRelative(BlockFace.SOUTH);
                            if (b.getType() == Material.WOODEN_DOOR)
                                top.setData((byte) 9);
                            break;
                        }
                        case 3: // Door is facing south
                        {
                            Block b = top.getRelative(BlockFace.WEST);
                            if (b.getType() == Material.WOODEN_DOOR)
                                top.setData((byte) 9);
                            break;
                        }
                    }
                    break;
                }
                case IRON_DOOR_BLOCK:
                {
                    Block top = to.getRelative(BlockFace.UP, 1);
                    if (top.getRelative(BlockFace.DOWN).getType() == Material.IRON_DOOR_BLOCK)
                        break;

                    to.setType(Material.IRON_DOOR_BLOCK);
                    to.setData(fromState.getBlock().getData());

                    top.setType(Material.IRON_DOOR_BLOCK);
                    top.setData((byte) 8);

                    // Now check if it's a double-door or single-door
                    int directionFacing = to.getData();
                    switch (directionFacing)
                    {
                        case 0: // Door is facing west
                        {
                            Block b = top.getRelative(BlockFace.NORTH);
                            if (b.getType() == Material.IRON_DOOR_BLOCK)
                                top.setData((byte) 9);
                            break;
                        }
                        case 1: // Door is facing north
                        {
                            Block b = top.getRelative(BlockFace.EAST);
                            if (b.getType() == Material.IRON_DOOR_BLOCK)
                                top.setData((byte) 9);
                            break;
                        }
                        case 2: // Door is facing east
                        {
                            Block b = top.getRelative(BlockFace.SOUTH);
                            if (b.getType() == Material.IRON_DOOR_BLOCK)
                                top.setData((byte) 9);
                            break;
                        }
                        case 3: // Door is facing south
                        {
                            Block b = top.getRelative(BlockFace.WEST);
                            if (b.getType() == Material.IRON_DOOR_BLOCK)
                                top.setData((byte) 9);
                            break;
                        }
                    }
                    break;
                }
                default:
                    break;
            }
            i.remove();
        }

        // Now move down location for each block in blocks
        Iterator<Entry<Location, StructureBlock>> it = blocks.entrySet().iterator();
        HashMap<Location, StructureBlock> temp = new HashMap<Location, StructureBlock>();
        while (it.hasNext())
        {
            Entry<Location, StructureBlock> entry = it.next();
            it.remove();
            Location l = entry.getKey();
            l.setY(l.getY() - 1);
            // l = entry.getValue().location;
            // l.setY(l.getY() - 1);

            if (l.getY() - 1 < 0)
            {
                world.getBlockAt(l.getX(), l.getY(), l.getZ()).setType(Material.AIR);
                continue;
            }

            entry.getValue().originalBlock = world.getBlockAt(l.getX(), l.getY(), l.getZ()).getState();
            temp.put(l, entry.getValue());
        }
        blocks.clear();
        blocks = temp;
        moveDate = new Date();
    }
}
