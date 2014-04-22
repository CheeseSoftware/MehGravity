package io.github.gustav9797.MehGravity;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
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
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;

public final class MehGravity extends JavaPlugin implements Listener
{
	public static int blockLimit = 2000;
	public static HashSet<Material> staticBlocks;
	public static HashSet<Material> nonStickyBlocks;
	public static HashMap<Material, HashSet<Material>> nonStickyBlocksAgainstEachother;
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
				Graph versionGraph = this.metrics.createGraph("MehGravity Version");

				versionGraph.addPlotter(new Metrics.Plotter(this.getDescription().getVersion())
				{
					@Override
					public int getValue()
					{
						return 1;
					}

				});

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

			MehGravity.staticBlocks = new HashSet<Material>();
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

			MehGravity.nonStickyBlocks = new HashSet<Material>();
			temp = config.getStringList("nonStickyBlocks");
			if (temp != null)
			{
				for (String s : temp)
				{
					Material m = Material.getMaterial(s);
					if (m != null)
						MehGravity.nonStickyBlocks.add(m);
				}
			}

			MehGravity.nonStickyBlocksAgainstEachother = new HashMap<Material, HashSet<Material>>();
			HashMap<Material, HashSet<Material>> shortName = MehGravity.nonStickyBlocksAgainstEachother;
			temp = config.getStringList("nonStickyBlocksAgainstEachother");
			if (temp != null)
			{
				for (String s : temp)
				{
					String[] materials = s.split("-");
					if (materials.length == 2)
					{
						Material one = Material.getMaterial(materials[0]);
						Material two = Material.getMaterial(materials[1]);
						if (one != null && two != null)
						{
							if(shortName.containsKey(one))
								shortName.get(one).add(two);
							else
								shortName.put(one, new HashSet<Material>(Arrays.asList(two)));
						}
						else
							this.getServer().getLogger().warning("Could not load the nonstickypair " + s);
					}
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
			
			HashSet<String> temp = new HashSet<String>();
			for (Material m : MehGravity.staticBlocks)
				temp.add(m.name());
			config.set("staticBlocks", temp);
			
			temp = new HashSet<String>();
			for (Material m : MehGravity.nonStickyBlocks)
				temp.add(m.name());
			config.set("nonStickyBlocks", temp);
			
			temp = new HashSet<String>();
			for(Material key : MehGravity.nonStickyBlocksAgainstEachother.keySet())
			{
				HashSet<Material> values = MehGravity.nonStickyBlocksAgainstEachother.get(key);
				for(Material value : values)
				{
					String out = key.name() + "-" + value.name();
					temp.add(out);
				}
			}
			config.set("nonStickyBlocksAgainstEachother", temp);
			
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

	public boolean HasPerms(Player player)
	{
		if ((!player.isPermissionSet("mehgravity.nocheck") || (player.isPermissionSet("mehgravity.nocheck") && !player.hasPermission("mehgravity.nocheck"))))
			return true;
		return false;
	}

	public static boolean isWorldAffected(String worldName)
	{
		World world = Bukkit.getServer().getWorld(worldName);
		if (world != null)
		{
			return !gravityWorlds.contains("-" + worldName) && (gravityWorlds.contains("AllWorlds") || gravityWorlds.contains(worldName));
		}
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
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event)
	{
		for(Block block : event.blockList())
		{
			this.CheckAround(block, 0);
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
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
	public void onBlockPhysics(BlockPhysicsEvent event)
	{
		// Prevent cactus dupe bug
		Material type = event.getChangedType();
		if (type == Material.CACTUS)
			event.setCancelled(true);
	}
}