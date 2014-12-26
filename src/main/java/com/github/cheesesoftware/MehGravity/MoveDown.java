package com.github.cheesesoftware.MehGravity;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.inventory.Inventory;
import org.bukkit.material.Bed;
import org.bukkit.material.PistonBaseMaterial;
import org.bukkit.material.Torch;
import org.bukkit.material.TrapDoor;

class MoveDown {
    //Pointer to the class calling this class
    private Structure hostClass;    
    public MoveDown(Structure calledBy) {
        this.hostClass = calledBy;
    }
    
    @SuppressWarnings("deprecation")
    public void moveOneDown(World world)
    {
        // Move solid hostClass.blocks down
        Iterator<StructureBlock> i = hostClass.sortedLevelBlocks.iterator();
        while (i.hasNext())
        {
            StructureBlock current = i.next();
            boolean removeOldBlock = true;    //by default old hostClass.blocks are removed, but there are exceptions
            
            if (current.location.getY() - 1 < 0) {
                current.originalBlock.getBlock().setType(Material.AIR);
                i.remove();
                continue;
            }

            if (!hostClass.sensitiveBlocks.contains(current.location)) {
                BlockState from = current.originalBlock;
                BlockState fromState = from;
                Block to = hostClass.world.getBlockAt(from.getLocation().getBlockX(), from.getLocation().getBlockY() - 1, from.getLocation().getBlockZ());
                if (Structure.isMaterialWeak(to.getType())) {
                    to.breakNaturally();
                }
                to.setType(from.getType());
                to.setData(from.getBlock().getData());
                moveBlockAttachables(from.getLocation());
                removeOldBlock = moveSolidBlockDown(fromState, to, current, from, removeOldBlock);
                if (removeOldBlock) { from.getBlock().setType(Material.AIR); }
                i.remove();
            }
        }
        // Place all non-solid hostClass.blocks back
        moveNonSolidBlocksOneDown(i);
        // Now move down location for each block in hostClass.blocks
        updateLocationForMovedBlocks();
    }
    
    private void moveBlockAttachables(org.bukkit.Location location) {
        for (Hanging hangingEntity : location.getWorld().getEntitiesByClass(Hanging.class)) {
            //where is it attached to?
            org.bukkit.Location attachedToLocation = null;
            if (hangingEntity.getType() == EntityType.LEASH_HITCH) {
                //leash is attached to the block at it's own location (we need to get block's location to get rid of decimals in coordinates)
                attachedToLocation =  hangingEntity.getLocation().getBlock().getLocation();   
            } else {
                //it's attached to a block at getAttachedFace direction
                attachedToLocation =  hangingEntity.getLocation().getBlock().getRelative(hangingEntity.getAttachedFace()).getLocation();
            }
            //is it attached to the block that is falling?
            if (attachedToLocation.equals(location)) {
                //Target location?
                org.bukkit.Location targetLocation = hangingEntity.getLocation().subtract(0, 1, 0);
                if (hangingEntity.getType() == EntityType.PAINTING) {
                    //we may need to move one extra block depending on the size
                    if ( ((Painting) hangingEntity).getArt().getBlockHeight() % 2 == 0) {
                        targetLocation = targetLocation.subtract(0, 1, 0);  //down one more
                    } 
                    if ( ((Painting) hangingEntity).getArt().getBlockWidth() % 2 == 0) {
                        if (hangingEntity.getFacing() == BlockFace.SOUTH) {
                            targetLocation = targetLocation.subtract(1, 0, 0);  //x-1
                        } else if (hangingEntity.getFacing() == BlockFace.WEST) {
                            targetLocation = targetLocation.subtract(0, 0, 1);  //y-1
                        }
                    }
                }
                //Move it
                moveHanging(hangingEntity,targetLocation);
            }
        }
    }

    
    private void moveHanging(Hanging hangingEntity, org.bukkit.Location targetLocation) {
        if (hangingEntity.getType() == EntityType.LEASH_HITCH) {    //Move leash
            for(Entity entityNearLeash : hangingEntity.getNearbyEntities(15, 15, 15)) {
                if (entityNearLeash instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity) entityNearLeash;
                    if (livingEntity.isLeashed() && livingEntity.getLeashHolder().equals(hangingEntity)) {
                        //add a new leash
                        Entity newLeashHitch = targetLocation.getWorld().spawn(targetLocation, LeashHitch.class);
                        livingEntity.setLeashHolder(newLeashHitch);
                    }
                }
            }                    
            //Remove the old leash
            hangingEntity.remove();
        } else {    //Move painting or item frame
            hangingEntity.teleport(targetLocation);
            hangingEntity.setFacingDirection(hangingEntity.getFacing(), true);
        }
    }

    private void updateLocationForMovedBlocks() {
        Iterator<Entry<Location, StructureBlock>> it = hostClass.blocks.entrySet().iterator();
        HashMap<Location, StructureBlock> temp = new HashMap<Location, StructureBlock>();
        while (it.hasNext())
        {
            Entry<Location, StructureBlock> entry = it.next();
            it.remove();
            Location l = entry.getKey();
            l.setY(l.getY() - 1);

            if (l.getY() - 1 < 0)
            {
                hostClass.world.getBlockAt(l.getX(), l.getY(), l.getZ()).setType(Material.AIR);
                continue;
            }

            entry.getValue().originalBlock = hostClass.world.getBlockAt(l.getX(), l.getY(), l.getZ()).getState();
            temp.put(l, entry.getValue());
        }
        hostClass.blocks.clear();
        hostClass.blocks    = temp;
        hostClass.moveDate  = new Date();
    }

    @SuppressWarnings("deprecation")
    private void moveNonSolidBlocksOneDown(Iterator<StructureBlock> i) {
        i = hostClass.sortedLevelBlocks.iterator();
        while (i.hasNext()) {   
            StructureBlock  current     = i.next();
            BlockState      fromState   = hostClass.blocks.get(current.location).originalBlock;
            
            Block to = hostClass.world.getBlockAt(current.location.getX(), current.location.getY() - 1, current.location.getZ());
            if (Structure.isMaterialWeak(to.getType())) { to.breakNaturally(); }
            
            if (fromState.getType() != Material.WOODEN_DOOR && fromState.getType() != Material.IRON_DOOR_BLOCK) {
                boolean hasBlockToSitOn = false;
                // Make sure we don't place a sensitive block in the air
                for (int j = 0; j < Structure.adjacentBlocks.length; j++) {
                    Block toCheck = hostClass.world.getBlockAt(to.getX() + Structure.adjacentBlocks[j].getX(), to.getY() + Structure.adjacentBlocks[j].getY(), to.getZ() + Structure.adjacentBlocks[j].getZ());
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

            //remove both old hostClass.blocks
            from.getBlock().getRelative(fromBed.getFacing()).setType(Material.AIR);
            from.setType(Material.AIR);
            
            //remove the bed entities that appear for some reason when moving the bed
            hostClass.removeItems(Material.BED , from.getLocation(), 2);
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
}
