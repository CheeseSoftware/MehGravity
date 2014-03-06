package io.github.gustav9797.MehGravity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

public final class MehGravity extends JavaPlugin implements Listener
{
	public static int blockLimit = 2000;
	public static List<Material> staticBlocks;
	public static List<String> gravityWorlds;
	public StructureHandler structureHandler;

	private File configFile = null;
	private boolean useMetrics = false;
	private Metrics metrics;

	public void onEnable()
	{
		this.configFile = new File(getDataFolder(), "config.yml");
		saveDefaultConfig();
		getServer().getPluginManager().registerEvents(this, this);
		structureHandler = new StructureHandler(this);

		if (!configFile.exists())
		{
			try
			{
				configFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			this.saveDefaultConfig();
		}
		this.Load();

		if (this.useMetrics)
		{
			try
			{
				this.metrics = new Metrics(this);
				this.metrics.start();
				this.getLogger().info("Metrics enabled");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else
			this.getLogger().info("Metrics is not used");

	}

	public void onDisable()
	{
		getLogger().info("MehGravity disabled.");
	}

	public void Load()
	{
		YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.load(configFile);
			this.useMetrics = config.getBoolean("useMetrics");
			MehGravity.blockLimit = config.getInt("blocklimit");
			MehGravity.gravityWorlds = config.getStringList("gravityWorlds");
			MehGravity.staticBlocks = new ArrayList<Material>();
			List<String> temp = config.getStringList("staticBlocks");
			if (temp != null)
			{
				for (String s : temp)
				{
					Material m = Material.getMaterial(s);
					if (m != null)
						MehGravity.staticBlocks.add(m);
				}
			}
		}
		catch (IOException | InvalidConfigurationException e)
		{
			e.printStackTrace();
		}
	}

	public void Save()
	{
		YamlConfiguration config = new YamlConfiguration();
		try
		{
			config.set("useMetrics", this.useMetrics);
			config.set("blocklimit", MehGravity.blockLimit);
			config.set("gravityWorlds", MehGravity.gravityWorlds);
			List<String> temp = new ArrayList<String>();
			for (Material m : MehGravity.staticBlocks)
				temp.add(m.name());
			config.set("staticBlocks", temp);
			config.save(configFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void saveDefaultConfig()
	{
		if (configFile == null)
			configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists())
			saveResource("config.yml", false);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (event.getBlock().getType() == Material.FLOWER_POT)
			event.setCancelled(true);
		if (HasPerms(event.getPlayer()))
			CheckAround(event.getBlock(), 0);
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if (HasPerms(event.getPlayer()))
			Check(event.getBlockPlaced());
	}

	@EventHandler
	public void onBlockBurn(BlockBurnEvent event)
	{
		CheckAround(event.getBlock(), 0);
	}

	@EventHandler
	public void onLeavesDecay(LeavesDecayEvent event)
	{
		CheckAround(event.getBlock(), 0);
	}

	@EventHandler
	public void onBlockPistonExtend(BlockPistonExtendEvent event)
	{
		Block base = event.getBlock();
		Block toCheck = base.getWorld().getBlockAt(
				new org.bukkit.Location(base.getWorld(), base.getX() + event.getDirection().getModX(), base.getY() + event.getDirection().getModY(), base.getZ() + event.getDirection().getModZ()));
		CheckAround(toCheck, 10);
	}

	@EventHandler
	public void onBlockPistonRetract(BlockPistonRetractEvent event)
	{
		Block base = event.getBlock();
		Block toCheck = base.getWorld().getBlockAt(
				new org.bukkit.Location(base.getWorld(), base.getX() + event.getDirection().getModX(), base.getY() + event.getDirection().getModY(), base.getZ() + event.getDirection().getModZ()));
		CheckAround(toCheck, 10);
	}

	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent event)
	{
		// Prevent sand and gravel from falling
		event.setCancelled(true);
	}

	@EventHandler
	public void onBlockPhysics(BlockPhysicsEvent event)
	{
		// Prevent cactus dupe bug
		Material type = event.getChangedType();
		if (type == Material.CACTUS)
			event.setCancelled(true);
	}

	public boolean HasPerms(Player player)
	{
		if ((!player.isPermissionSet("mehgravity.nocheck") || (player.isPermissionSet("mehgravity.nocheck") && !player.hasPermission("mehgravity.nocheck")))
				&& (gravityWorlds.contains("AllWorlds") || gravityWorlds.contains(player.getWorld().getName())))
			return true;
		return false;
	}

	public void CheckAround(Block block, int delay)
	{
		new GravityCheckAroundLater(this, block).runTaskLater(this, delay);
	}

	public void Check(Block block)
	{
		Structure structure = structureHandler.CreateStructure(block);
		if (structure != null)
			structureHandler.AddStructure(structure);
	}

	static Location[] adjacentBlocks =
	{ new Location(1, 0, 0), new Location(-1, 0, 0), new Location(0, 1, 0), new Location(0, -1, 0), new Location(0, 0, 1), new Location(0, 0, -1) };

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
			add(Material.FLOWER_POT);
			add(Material.CACTUS);
		}
	};
}