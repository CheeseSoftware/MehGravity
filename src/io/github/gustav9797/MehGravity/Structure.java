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
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Bed;
import org.bukkit.material.PistonBaseMaterial;
import org.bukkit.material.Torch;
import org.bukkit.material.TrapDoor;

public class Structure
{
    public  int                                 id;
    public  World                               world;
    public  int                                 totalBlocks;
    public  int                                 yMin = Integer.MAX_VALUE;
    public  int                                 yMax = 0;
    private HashMap<Location, StructureBlock>   blocks;
    private Queue<StructureBlock>               sortedLevelBlocks;
    private Queue<Location>                     sensitiveBlocks;
    public  Date                                moveDate;

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

    @SuppressWarnings("deprecation")
    public void moveOneDown(World world)
    {
        // Move solid blocks down
        Iterator<StructureBlock> i = sortedLevelBlocks.iterator();
        while (i.hasNext())
        {
            StructureBlock current = i.next();
            boolean removeOldBlock = true;    //by default old blocks are removed, but there are exceptions
            
            if (current.location.getY() - 1 < 0) {
                current.originalBlock.getBlock().setType(Material.AIR);
                i.remove();
                continue;
            }

            if (!sensitiveBlocks.contains(current.location)) {
                BlockState from = current.originalBlock;
                BlockState fromState = from;
                Block to = world.getBlockAt(from.getLocation().getBlockX(), from.getLocation().getBlockY() - 1, from.getLocation().getBlockZ());
                if (Structure.isMaterialWeak(to.getType())) {
                    to.breakNaturally();
                }
                to.setType(from.getType());
                to.setData(from.getBlock().getData());
                removeOldBlock = moveSolidBlockDown(fromState, to, current, from, removeOldBlock);
                if (removeOldBlock) { from.getBlock().setType(Material.AIR); }
                i.remove();
            }
        }
        // Place all non-solid blocks back
        moveNonSolidBlocksOneDown(i);
        // Now move down location for each block in blocks
        updateLocationForMovedBlocks();
    }

    private void updateLocationForMovedBlocks() {
        Iterator<Entry<Location, StructureBlock>> it = blocks.entrySet().iterator();
        HashMap<Location, StructureBlock> temp = new HashMap<Location, StructureBlock>();
        while (it.hasNext())
        {
            Entry<Location, StructureBlock> entry = it.next();
            it.remove();
            Location l = entry.getKey();
            l.setY(l.getY() - 1);

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

    @SuppressWarnings("deprecation")
    private void moveNonSolidBlocksOneDown(Iterator<StructureBlock> i) {
        i = sortedLevelBlocks.iterator();
        while (i.hasNext())
        {
            
            StructureBlock  current     = i.next();
            BlockState      fromState   = blocks.get(current.location).originalBlock;
            
            Block to = world.getBlockAt(current.location.getX(), current.location.getY() - 1, current.location.getZ());
            if (Structure.isMaterialWeak(to.getType())) { to.breakNaturally(); }
            
            if (fromState.getType() != Material.WOODEN_DOOR && fromState.getType() != Material.IRON_DOOR_BLOCK) {
                boolean hasBlockToSitOn = false;
                // Make sure we don't place a sensitive block in the air
                for (int j = 0; j < Structure.adjacentBlocks.length; j++) {
                    Block toCheck = world.getBlockAt(to.getX() + Structure.adjacentBlocks[j].getX(), to.getY() + Structure.adjacentBlocks[j].getY(), to.getZ() + Structure.adjacentBlocks[j].getZ());
                    if (toCheck.getType() != Material.AIR && !Structure.annoyingBlocks.contains(toCheck.getType())) {
                        hasBlockToSitOn = true;
                        break;
                    }
                }
                if (!hasBlockToSitOn) {
                    to.setType(Material.AIR);
                    i.remove();
                    continue;
                }
                to.setType(fromState.getType());
                to.setData(fromState.getBlock().getData());
            }
            moveNonSolidBlockDown(fromState, to, current);
            i.remove();
        }
    }

    private void moveNonSolidBlockDown(BlockState fromState, Block to, StructureBlock current) {
        switch (fromState.getType())
        {
            case SKULL: {
                moveSkull(fromState, to);
                break;
            }
            case FLOWER_POT: {  //WIP !
                moveFlowerPot(fromState, to);
                break;
            }
            case TORCH: {
                moveTorch(fromState, to);
                break;
            }
            case SIGN:
            case SIGN_POST:
            case WALL_SIGN: {
                moveWallSign(fromState, to);
                break;
            }
            case TRAP_DOOR: {
                moveTrapDoor(fromState, to);
                break;
            }
            case WOODEN_DOOR: {
                moveWoodenDoor(fromState, to);
                break;
            }
            case IRON_DOOR_BLOCK: {
                moveIronDoor(fromState, to);
                break;
            }
            default:
                break;
        }
    }

    private void moveFlowerPot(BlockState fromState, Block to) {
        // I give up with flower pots

        // FlowerPot toFlowerPot
        // =(FlowerPot)to.getState().getData();
        // toFlowerPot.setContents(new
        // MaterialData(Material.RED_ROSE));
    }

    private void moveTrapDoor(BlockState fromState, Block to) {
        TrapDoor fromTrapDoor = (TrapDoor) fromState.getData();
        TrapDoor toTrapDoor = (TrapDoor) to.getState().getData();
        toTrapDoor.setOpen(fromTrapDoor.isOpen());
    }

    private void moveTorch(BlockState fromState, Block to) {
        Torch fromTorch = (Torch) fromState.getData();
        Torch toTorch = (Torch) to.getState().getData();
        toTorch.setFacingDirection(fromTorch.getFacing());
    }

    private void moveSkull(BlockState fromState, Block to) {
        Skull toSkull = (Skull) to.getState();
        Skull fromSkull = (Skull) fromState;
        toSkull.setRotation(fromSkull.getRotation());
        toSkull.setSkullType(fromSkull.getSkullType());
        toSkull.update();
    }

    private void moveWallSign(BlockState fromState, Block to) {
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
    }

    @SuppressWarnings("deprecation")
    private void moveIronDoor(BlockState fromState, Block to) {
        Block top = to.getRelative(BlockFace.UP, 1);
        if (top.getRelative(BlockFace.DOWN).getType() != Material.IRON_DOOR_BLOCK) {
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
        }
    }

    @SuppressWarnings("deprecation")
    private void moveWoodenDoor(BlockState fromState, Block to) {
        Block top = to.getRelative(BlockFace.UP, 1);
        if (top.getRelative(BlockFace.DOWN).getType() != Material.WOODEN_DOOR) {
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
        }
    }

    private boolean moveSolidBlockDown(BlockState fromState, Block to, StructureBlock current, BlockState from, boolean removeOldBlock) {
        switch (from.getType()) {
            case COMMAND: {
                moveCommandBlock(fromState, to);
                break;
            }
            case MOB_SPAWNER: {
                moveCreatureSpawner(fromState, to);
                break;
            }
            case REDSTONE_TORCH_ON: {
                //Set it to air to make it get forcefully placed back and trigger redstone
                to.setType(Material.AIR);
                break;
            }
            case CHEST:
            case TRAPPED_CHEST: {
                removeOldBlock = moveChest(fromState, to, current, from);
                break;
            }
            case BED_BLOCK: {
                removeOldBlock = false; //we have a special processing for the bed
                moveBed(fromState, to, from);
                break;
            }
            case FURNACE:
            case BURNING_FURNACE: {
                moveFurnace(fromState, to);
                break;
            }
            case HOPPER: {
                moveHopper(fromState, to);
                break;
            }
            case DROPPER: {
                moveDropper(fromState, to);
                break;
            }
            case BEACON: {
                moveBeacon(fromState, to);
                break;
            }
            case DISPENSER: {
                moveDispenser(fromState, to);
                break;
            }
            case JUKEBOX: {
                moveJukebox(fromState, to);
                break;
            }
            case NOTE_BLOCK: {
                NoteBlock fromNoteBlock = (NoteBlock) fromState;
                NoteBlock toNoteBlock = (NoteBlock) to.getState();
                toNoteBlock.setNote(fromNoteBlock.getNote());
                break;
            }
            case PISTON_BASE:
            case PISTON_STICKY_BASE: {
                movePiston(fromState, to);
                break;
            }
            default:
                break;
        }
        return removeOldBlock;
    }

    private void moveCreatureSpawner(BlockState fromState, Block to) {
        CreatureSpawner fromSpawner = (CreatureSpawner) fromState;
        CreatureSpawner toSpawner = (CreatureSpawner) to.getState();
        toSpawner.setDelay(fromSpawner.getDelay());
        toSpawner.setSpawnedType(fromSpawner.getSpawnedType());
        toSpawner.update();
    }

    private void moveCommandBlock(BlockState fromState, Block to) {
        CommandBlock fromBlock = (CommandBlock) fromState;
        CommandBlock toBlock = (CommandBlock) to.getState();
        toBlock.setCommand(fromBlock.getCommand());
        toBlock.update();
    }

    private void movePiston(BlockState fromState, Block to) {
        PistonBaseMaterial fromPiston = (PistonBaseMaterial) fromState.getData();
        PistonBaseMaterial toPiston = (PistonBaseMaterial) to.getState().getData();
        toPiston.setFacingDirection(fromPiston.getFacing());
        toPiston.setPowered(fromPiston.isPowered());
    }

    private void moveJukebox(BlockState fromState, Block to) {
        Jukebox fromJukebox = (Jukebox) fromState;
        Jukebox toJukebox = (Jukebox) to.getState();
        if (fromJukebox.isPlaying())
        {
            toJukebox.setPlaying(fromJukebox.getPlaying());
        }
        fromJukebox.setPlaying(null);
    }

    private void moveDispenser(BlockState fromState, Block to) {
        Dispenser fromDispenser = (Dispenser) fromState;
        Dispenser toDispenser = (Dispenser) to.getState();
        Inventory fromInventory = fromDispenser.getInventory();
        Inventory toInventory = toDispenser.getInventory();
        toInventory.setContents(fromInventory.getContents());
        fromInventory.clear();
    }

    private void moveBeacon(BlockState fromState, Block to) {
        Beacon fromBeacon = (Beacon) fromState;
        Beacon toBeacon = (Beacon) to.getState();
        Inventory fromInventory = fromBeacon.getInventory();
        Inventory toInventory = toBeacon.getInventory();
        toInventory.setContents(fromInventory.getContents());
        fromInventory.clear();
    }

    private void moveDropper(BlockState fromState, Block to) {
        Dropper fromDropper = (Dropper) fromState;
        Dropper toDropper = (Dropper) to.getState();
        Inventory fromInventory = fromDropper.getInventory();
        Inventory toInventory = toDropper.getInventory();
        toInventory.setContents(fromInventory.getContents());
        fromInventory.clear();
    }

    private void moveHopper(BlockState fromState, Block to) {
        Hopper fromHopper = (Hopper) fromState;
        Hopper toHopper = (Hopper) to.getState();
        Inventory fromInventory = fromHopper.getInventory();
        Inventory toInventory = toHopper.getInventory();
        toInventory.setContents(fromInventory.getContents());
        fromInventory.clear();
    }

    private void moveFurnace(BlockState fromState, Block to) {
        Furnace fromFurnace = (Furnace) fromState;
        Furnace toFurnace = (Furnace) to.getState();
        Inventory fromInventory = fromFurnace.getInventory();
        Inventory toInventory = toFurnace.getInventory();
        toInventory.setContents(fromInventory.getContents());
        toFurnace.setBurnTime(fromFurnace.getBurnTime());
        toFurnace.setCookTime(fromFurnace.getCookTime());
        fromInventory.clear();
    }

    @SuppressWarnings("deprecation")
    private void moveBed(BlockState fromState, Block to, BlockState from) {
        Bed fromBed = (Bed) from.getData();
        
        if (fromBed.isHeadOfBed() == false) {    
            //move the whole bed when we find foot part of bed
            to.setTypeIdAndData(fromState.getTypeId(), fromState.getRawData(), false);
            Block fromBlockHead = from.getBlock().getRelative(fromBed.getFacing());                            
            Block toBlockHead = to.getRelative(fromBed.getFacing());
            toBlockHead.setTypeIdAndData(fromBlockHead.getTypeId(), fromBlockHead.getState().getRawData(), false);

            //remove both old blocks
            from.getBlock().getRelative(fromBed.getFacing()).setType(Material.AIR);
            from.setType(Material.AIR);
            
            //remove the bed entities that appear for some reason when moving the bed
            removeItems(Material.BED , from.getLocation(), 2);
        }
    }

    private boolean moveChest(BlockState fromState, Block to, StructureBlock current, BlockState from) {
        //finishes the moving the chest, returns removeOldBlock
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
            return true;    //removeOldBlock
        } else {
            //maybe only one side of double chest has been moved (keep looping the blocks to finish the chest)
            //keep the old block until the chest is complete and inventory has been moved
            return false;   //removeOldBlock
        }
    }

    private void removeItems(Material material, org.bukkit.Location location, float maximumDistance) {
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
