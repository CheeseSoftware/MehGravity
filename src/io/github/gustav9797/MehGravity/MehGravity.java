package io.github.gustav9797.MehGravity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Vector;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.block.Furnace;
import org.bukkit.block.Hopper;
import org.bukkit.block.Jukebox;
import org.bukkit.block.NoteBlock;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.material.PistonBaseMaterial;
import org.bukkit.material.Torch;
import org.bukkit.material.TrapDoor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.mcstats.Metrics;

public final class MehGravity extends JavaPlugin implements Listener 
{
	static int blockLimit = 2000;
	static List<String> gravityWorlds;
	private FileConfiguration customConfig = null;
	private File customConfigFile = null;

	public void onEnable() 
	{
		saveDefaultConfig();
		blockLimit = getCustomConfig().getInt("blocklimit") ; 
		gravityWorlds = getCustomConfig().getStringList("gravityWorlds");
		getServer().getPluginManager().registerEvents(this, this);

		//Metrics start
		try {
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		} catch (IOException e) {
		    // Failed to submit the stats :-(
		}
		//Metrics end
	}

	public void onDisable() 
	{
		getLogger().info("MehGravity disabled.");
	}
	
	public void reloadCustomConfig() 
	{
	    if (customConfigFile == null) 
	    	customConfigFile = new File(getDataFolder(), "config.yml");
	    customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
	 
	    // Look for defaults in the jar
	    InputStream defConfigStream = this.getResource("config.yml");
	    if (defConfigStream != null) {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	        customConfig.setDefaults(defConfig);
	    }
	}
	
	public FileConfiguration getCustomConfig() 
	{
	    if (customConfig == null) 
	        reloadCustomConfig();
	    return customConfig;
	}
	
	public void saveCustomConfig() 
	{
	    if (customConfig == null || customConfigFile == null)
	        return;
	    
	    try {
	        getCustomConfig().save(customConfigFile);
	    } catch (IOException ex) {
	        getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
	    }
	}
	
	public void saveDefaultConfig() 
	{
	    if (customConfigFile == null)
	        customConfigFile = new File(getDataFolder(), "config.yml");
	    if (!customConfigFile.exists()) {            
	         saveResource("config.yml", false);
	     }
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) 
	{
		if(HasPerms(event.getPlayer()))
			CheckAround(event.getBlock(), 1);
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) 
	{
		if(HasPerms(event.getPlayer()))
			Check(event.getBlockPlaced());
	}
	
	@EventHandler
	public void onBlockBurn(BlockBurnEvent event)
	{
		CheckAround(event.getBlock(), 1);
	}
	
	@EventHandler
	public void onLeavesDecay(LeavesDecayEvent event)
	{
		CheckAround(event.getBlock(), 1);
	}
	
	@EventHandler
	public void onBlockPistonExtend(BlockPistonExtendEvent event)
	{
		Block base = event.getBlock();
		Block toCheck = base.getWorld().getBlockAt(new org.bukkit.Location(base.getWorld(), base.getX() + event.getDirection().getModX(), base.getY() + event.getDirection().getModY(), base.getZ() + event.getDirection().getModZ()));		
		CheckAround(toCheck, 10);
		//Check(toCheck);
		//getServer().getPlayer("gustav9797").sendMessage(event.getEventName() + " - " + event.getDirection() + " - " + event.getBlock());
		getServer().getPlayer("gustav9797").sendMessage(event.getEventName() + " - " + toCheck);
	}
	
	@EventHandler
	public void onBlockPistonRetract(BlockPistonRetractEvent event)
	{
		Block base = event.getBlock();
		Block toCheck = base.getWorld().getBlockAt(new org.bukkit.Location(base.getWorld(), base.getX() + event.getDirection().getModX(), base.getY() + event.getDirection().getModY(), base.getZ() + event.getDirection().getModZ()));
		CheckAround(toCheck, 10);
		//Check(toCheck);
		//getServer().getPlayer("gustav9797").sendMessage(event.getEventName() + " - " + event.getDirection() + " - " + event.getBlock());
		getServer().getPlayer("gustav9797").sendMessage(event.getEventName() + " - " + toCheck);
	}
	
	public boolean HasPerms(Player player)
	{
		if((!player.isPermissionSet("mehgravity.nocheck") || (player.isPermissionSet("mehgravity.nocheck") && !player.hasPermission("mehgravity.nocheck")))&& (gravityWorlds.contains("AllWorlds") || gravityWorlds.contains(player.getWorld().getName())))
			return true;
		return false;
	}
	
	public void CheckAround(Block block, int delay)
	{
		new GravityBlockBreak(this, block).runTaskLater(this, delay);
	}
	
	public void Check(Block block)
	{
		BeginGravity(block);
	}

	static Location[] adjacentBlocks = { new Location(1, 0, 0),
			new Location(-1, 0, 0), new Location(0, 1, 0),
			new Location(0, -1, 0), new Location(0, 0, 1),
			new Location(0, 0, -1) };
	
	static ArrayList<Material> annoyingBlocks = new ArrayList<Material>() {{ 
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
		add(Material.SIGN);
		add(Material.SIGN_POST);
		add(Material.WALL_SIGN);
		add(Material.BED);
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
		}};
	
	public void BeginGravity(Block startBlock)
	{
		// Check if we can find bedrock, saves lots of time..
		Location startLocation = new Location(startBlock.getX(), startBlock.getY(), startBlock.getZ());
		/*for (int y = startBlock.getY(); y > -10; y--) 
		{
			Block currentBlock = player.getWorld().getBlockAt(startBlock.getX(), y, startBlock.getZ());
			if (currentBlock.getType() != Material.AIR) 
			{
				if (currentBlock.getType() == Material.BEDROCK) 
					return;
			} 
			else
				break;
		}*/

		// Now that we haven't found bedrock, lets store what blocks it's
		// connected to(within the set blocklimit!)
		int totalBlocks = 0;
		HashMap<Integer, HashMap<Location, BlockState>> blockList = new HashMap<Integer, HashMap<Location, BlockState>>();
		Queue<Location> blocksToCheck = new LinkedList<Location>();
		blocksToCheck.add(startLocation);
		blockList.put(startBlock.getY(), new HashMap<Location, BlockState>());
		blockList.get(startBlock.getY()).put(startLocation, startBlock.getState());
		World world = startBlock.getWorld();
		while (!blocksToCheck.isEmpty()) 
		{
			Location currentParent = blocksToCheck.poll();
			for (int y = currentParent.getY(); y > -10; y--) 
			{			
				Block currentBlock = world.getBlockAt(currentParent.getX(), y, currentParent.getZ());
				//Location currentLocation = new Location(currentBlock.getX(), y, currentBlock.getZ());
				if (currentBlock.getType() == Material.AIR) //We didn't find bedrock, can't continue search		
				{
					//player.sendMessage("found air"+ "at: " + currentLocation);
					break;
				}
				else if (currentBlock.getType() == Material.BEDROCK)
				{
					//player.sendMessage("found bedrock"+ "at: " + currentLocation);
					return;
				}
				/*else if(!(blockList.containsKey(y) && blockList.get(y).containsKey(currentLocation)))
				{
					//player.sendMessage("added to structure"+ "at: " + currentLocation);
					if (!blockList.containsKey(y))
						blockList.put(y, new HashMap<Location, BlockState>());
					blockList.get(y).put(currentLocation, currentBlock.getState());
					totalBlocks++;
				}*/
			}
			
			for (int i = 0; i < 6; i++) 
			{
				Location currentLocation = new Location(
						adjacentBlocks[i].getX() + currentParent.getX(),
						adjacentBlocks[i].getY() + currentParent.getY(),
						adjacentBlocks[i].getZ() + currentParent.getZ());
				Block currentBlock = world.getBlockAt(currentLocation.getX(),
						currentLocation.getY(), currentLocation.getZ());

				if (!blockList.containsKey(currentBlock.getY()))
					blockList.put(currentBlock.getY(), new HashMap<Location, BlockState>());

				if (!blockList.get(currentBlock.getY()).containsValue(currentBlock.getState()) && currentBlock.getType() != Material.AIR) 
				{
					blockList.get(currentBlock.getY()).put(currentLocation, currentBlock.getState());
					blocksToCheck.add(currentLocation);
					totalBlocks++;
				}
			}

			if (totalBlocks >= blockLimit)
				return;
		}
		
		// Now we know that we have a massive floating thing that should drop
		// down, lets store it in an array of collections of blocks sorted by y-layer.
		Vector<Integer> yLevels = new Vector<Integer>();
		Iterator<Entry<Integer, HashMap<Location, BlockState>>> yit = blockList.entrySet().iterator();
		while (yit.hasNext()) 
		{
			Entry<Integer, HashMap<Location, BlockState>> ypair = yit.next();
			yLevels.add((int) ypair.getKey());
			//yit.remove();
		}
		Collections.sort(yLevels);
		
		//Measure how far down we can move it
		int blocksWeCanMove = findMovingSpaceDown(yLevels, blockList, world);
		
		//Declare a queue for non-solid blocks that will break if they are placed on air to be placed later
		Queue<Pair<BlockState, Block>> toPlaceLater = new LinkedList<Pair<BlockState, Block>>();
		
		//Store all non-solid blocks
		Iterator<Integer> i = yLevels.iterator();
		while (i.hasNext()) 
		{
			int currentLayerY = (int)i.next();
			if (blockList.containsKey(currentLayerY)) 
			{
				Iterator<Entry<Location, BlockState>> it = blockList.get(currentLayerY).entrySet().iterator();
				while (it.hasNext()) 
				{
					Entry<Location, BlockState> pair = it.next();		
					BlockState from = ((BlockState) pair.getValue());
					Block to = world.getBlockAt(from.getLocation().getBlockX(), from.getLocation().getBlockY() - blocksWeCanMove, from.getLocation().getBlockZ());
					//player.sendMessage("checking material: " + from.getType());
					if(annoyingBlocks.contains(from.getType()))
					{
						Pair<BlockState, Block> toAdd = new Pair<BlockState, Block>(from, to);
						toPlaceLater.add(toAdd);
						if(from.getType() == Material.IRON_DOOR_BLOCK || from.getType() == Material.WOODEN_DOOR)
						{
							Block above = from.getBlock().getRelative(BlockFace.UP);
							Block below = from.getBlock().getRelative(BlockFace.DOWN);
							if(above.getType() == Material.IRON_DOOR_BLOCK || above.getType() == Material.WOODEN_DOOR)
							{
								from.getBlock().setType(Material.AIR);
								above.setType(Material.AIR);
								it.remove();
								continue;
							}
							else if(below.getType() == Material.IRON_DOOR_BLOCK || below.getType() == Material.WOODEN_DOOR)
							{
								below.setType(Material.AIR);
								from.getBlock().setType(Material.AIR);
								toPlaceLater.remove(toAdd);
								it.remove();
								continue;
							}
						}
						from.getBlock().setType(Material.AIR);
						it.remove();
					}
				}
			}
		}
		
		//Move solid blocks down
		i = yLevels.iterator();
		while (i.hasNext()) 
		{
			int currentLayerY = (int)i.next();
			if (blockList.containsKey(currentLayerY)) 
			{
				Iterator<Entry<Location, BlockState>> it = blockList.get(currentLayerY).entrySet().iterator();
				while (it.hasNext()) 
				{
					Entry<Location, BlockState> pair = it.next();		
					BlockState from = ((BlockState) pair.getValue());
					BlockState fromState = from;
					Block to = world.getBlockAt(from.getLocation().getBlockX(), from.getLocation().getBlockY() - blocksWeCanMove, from.getLocation().getBlockZ());
					to.setType(from.getType());
					to.setData(from.getBlock().getData());
					switch(from.getType())
					{
						case CHEST: case TRAPPED_CHEST:
						{	
							Chest fromChest = (Chest) fromState;
							Chest toChest = (Chest) to.getState();
							Inventory fromInventory = fromChest.getInventory();
							Inventory toInventory = toChest.getInventory();
							toInventory.setContents(fromInventory.getContents());
							fromInventory.clear();
							break;
						}
						case FURNACE: case BURNING_FURNACE:
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
							if(fromJukebox.isPlaying())
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
						case PISTON_BASE: case PISTON_STICKY_BASE:
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
					from.getBlock().setType(Material.AIR);
				}
			}
		}
		
		//Place all non-solid blocks back
		Iterator<Pair<BlockState, Block>> lastIterator = toPlaceLater.iterator();
		while(lastIterator.hasNext())
		{
			Pair<BlockState, Block> current = lastIterator.next();
			BlockState fromState = current.getFirst();
			Block to = current.getSecond();
			if(fromState.getType() != Material.WOODEN_DOOR && fromState.getType() != Material.IRON_DOOR_BLOCK)
			{
				boolean hasBlockToSitOn = false;
				for(int j = 0; j < adjacentBlocks.length; j++)
				{
					Block toCheck = world.getBlockAt(to.getX() + adjacentBlocks[j].getX(), to.getY() + adjacentBlocks[j].getY(), to.getZ() + adjacentBlocks[j].getZ());
					if(toCheck.getType() != Material.AIR && !annoyingBlocks.contains(toCheck.getType()))
					{
						hasBlockToSitOn = true;
						break;
					}
				}
				if(!hasBlockToSitOn)
				{
					to.setType(Material.AIR);
					continue;
				}
				to.setType(fromState.getType());
				to.setData(fromState.getBlock().getData());
			}

			switch(fromState.getType())
			{
				case TORCH:
				{
					Torch fromTorch = (Torch) fromState.getData();
					Torch toTorch = (Torch) to.getState().getData();
					toTorch.setFacingDirection(fromTorch.getFacing());
					break;
				}
				case SIGN_POST: case WALL_SIGN:
				{					
					Sign fromSign = (Sign) fromState;
					Sign toSign = (Sign) to.getState();			
					org.bukkit.material.Sign fromSignMat = (org.bukkit.material.Sign) fromSign.getData();
					toSign.setData(fromSignMat);							
					String[] fromLines = fromSign.getLines();
					for(int index = 0; index < fromLines.length; index++)
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
					if(top.getRelative(BlockFace.DOWN).getType() == Material.WOODEN_DOOR)
						break;

					to.setType(Material.WOODEN_DOOR);
					to.setData(fromState.getBlock().getData());

					top.setType(Material.WOODEN_DOOR);
					top.setData((byte) 8);
					
					//Now check if it's a double-door or single-door
					int directionFacing = to.getData();
					switch(directionFacing)
					{
						case 0: //Door is facing west
						{
							Block b = top.getRelative(BlockFace.NORTH);
							if(b.getType() == Material.WOODEN_DOOR)
								top.setData((byte) 9);
							break;
						}
						case 1: //Door is facing north
						{
							Block b = top.getRelative(BlockFace.EAST);
							if(b.getType() == Material.WOODEN_DOOR)
								top.setData((byte) 9);
							break;
						}
						case 2: //Door is facing east
						{
							Block b = top.getRelative(BlockFace.SOUTH);
							if(b.getType() == Material.WOODEN_DOOR)
								top.setData((byte) 9);
							break;
						}
						case 3: //Door is facing south
						{
							Block b = top.getRelative(BlockFace.WEST);
							if(b.getType() == Material.WOODEN_DOOR)
								top.setData((byte) 9);
							break;
						}
					}
					break;
				}
				case IRON_DOOR_BLOCK:
				{
					Block top = to.getRelative(BlockFace.UP, 1);
					if(top.getRelative(BlockFace.DOWN).getType() == Material.IRON_DOOR_BLOCK)
						break;

					to.setType(Material.IRON_DOOR_BLOCK);
					to.setData(fromState.getBlock().getData());

					top.setType(Material.IRON_DOOR_BLOCK);
					top.setData((byte)8);
					
					//Now check if it's a double-door or single-door
					int directionFacing = to.getData();
					switch(directionFacing)
					{
						case 0: //Door is facing west
						{
							Block b = top.getRelative(BlockFace.NORTH);
							if(b.getType() == Material.IRON_DOOR_BLOCK)
								top.setData((byte) 9);
							break;
						}
						case 1: //Door is facing north
						{
							Block b = top.getRelative(BlockFace.EAST);
							if(b.getType() == Material.IRON_DOOR_BLOCK)
								top.setData((byte) 9);
							break;
						}
						case 2: //Door is facing east
						{
							Block b = top.getRelative(BlockFace.SOUTH);
							if(b.getType() == Material.IRON_DOOR_BLOCK)
								top.setData((byte) 9);
							break;
						}
						case 3: //Door is facing south
						{
							Block b = top.getRelative(BlockFace.WEST);
							if(b.getType() == Material.IRON_DOOR_BLOCK)
								top.setData((byte) 9);
							break;
						}
					}
					break;
				}
				default:
					break;
			}
		}
	}

	public int findMovingSpaceDown(Vector<Integer> yLevels, HashMap<Integer, HashMap<Location, BlockState>> blockList, World world) 
	{
		Map<ColumnCoord, Integer> minima = new HashMap<ColumnCoord, Integer>();
		Map<ColumnCoord, Integer> maxima = new HashMap<ColumnCoord, Integer>();
		
		Iterator<Integer> yiterator = yLevels.iterator();
		while (yiterator.hasNext()) 
		{
			Iterator<Entry<Location, BlockState>> xziterator = blockList.get(yiterator.next()).entrySet().iterator();
			while (xziterator.hasNext()) 
			{
				BlockState block = xziterator.next().getValue();
				ColumnCoord coord = new ColumnCoord(block.getX(), block.getZ());
				Integer min = minima.get(coord);
				Integer max = maxima.get(coord);
				if(min == null) 
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
		}
				
		int currentMaxFall = Integer.MAX_VALUE;
		for(Map.Entry<ColumnCoord, Integer> entry : maxima.entrySet()) 
        {
			int minY = minima.get(entry.getKey());
			int maxY = entry.getValue();
			for(int currentY = maxY; currentY >= minY; currentY--)
			{
				if(world.getBlockAt(entry.getKey().x, currentY - 1, entry.getKey().z).getType() == Material.AIR)
				{
					int tempCurrentMaxFall = 0;
					for(int y = currentY - 1; true; y--)
					{
						if(world.getBlockAt(entry.getKey().x, y, entry.getKey().z).getType() == Material.AIR)
						{
							tempCurrentMaxFall++;
						}
						else if(yLevels.contains(y) && blockList.get(y).containsKey(new Location(entry.getKey().x, y, entry.getKey().z)))
						{
							currentY = y;
							break;
						}
						else
						{
							currentMaxFall = Math.min(tempCurrentMaxFall, currentMaxFall);
							//currentY = y;
							int tempY = 0;
							for(tempY = y; tempY >= minY; tempY--)
							{
								if(yLevels.contains(tempY) && blockList.get(tempY).containsKey(new Location(entry.getKey().x, tempY, entry.getKey().z)))
								{
									currentY = tempY;
									break;
								}
							}
							break;
						}
						
					}
				}
			}
        }
		return currentMaxFall;
	}
}

class GravityBlockBreak extends BukkitRunnable
{
	Block startBlock;
	MehGravity plugin;
	public GravityBlockBreak(MehGravity plugin, Block startBlock)
	{
		this.startBlock = startBlock;
		this.plugin = plugin;
	}
	
	@Override
	public void run() 
	{
		for(int i = 0; i < 6; i++)
		{
			Block currentBlock = startBlock.getWorld().getBlockAt(
					startBlock.getX() + MehGravity.adjacentBlocks[i].getX(), 
					startBlock.getY() + MehGravity.adjacentBlocks[i].getY(), 
					startBlock.getZ() + MehGravity.adjacentBlocks[i].getZ());
			
			if(currentBlock.getType() != Material.AIR)
				plugin.BeginGravity(currentBlock);
		}
	}
}

//// Utility
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
    public boolean equals(Object o) 
    {
        if (this == o) return true;
        if (!(o instanceof ColumnCoord)) return false;
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