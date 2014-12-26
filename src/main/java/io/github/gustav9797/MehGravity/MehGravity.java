package main.java.io.github.gustav9797.MehGravity;

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
import main.java.org.mcstats.Metrics;
import main.java.org.mcstats.Metrics.Graph;

public final class MehGravity extends JavaPlugin implements Listener // NO_UCD (use default)
{
    public static int                                  blockLimit = 8192;
    private static HashSet<Material>                    staticBlocks;
    private static HashSet<Material>                    nonStickyBlocks;
    private static HashMap<Material, HashSet<Material>> nonStickyBlocksAgainstEachother;
    private static List<String>                         gravityWorlds;
    public StructureHandler                            structureHandler;

    private File       configFile  = null;
    private boolean    useMetrics  = false;
    private Metrics    metrics;

    public void onEnable()
    {
        this.configFile = new File(getDataFolder(), "config.yml");
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        structureHandler = new StructureHandler(this);

        if (!configFile.exists())
        {
            try  //Are we expecting createNewFile to fail? If not, then maybe there's no point to have a try here? --tubelius 20140729 
            {
                configFile.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            this.saveDefaultConfig();
        }
        this.load();

        if (this.useMetrics)
        {
            try {
                this.metrics = new Metrics(this);
                Graph versionGraph = this.metrics.createGraph("MehGravity Version");

                versionGraph.addPlotter(new Metrics.Plotter(this.getDescription().getVersion())
                {
                    @Override
                    public int getValue() { return 1; }

                });

                this.metrics.start();
                this.getLogger().info("Metrics enabled");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            this.getLogger().info("Metrics is not used");
        }

    }

    public void onDisable()
    {
        getLogger().info("MehGravity disabled.");
    }

    public void load()
    {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(configFile);
            this.useMetrics = config.getBoolean("useMetrics");
            MehGravity.blockLimit = config.getInt("blocklimit");
            MehGravity.gravityWorlds = config.getStringList("gravityWorlds");
            setListStaticBlocks(config);
            setListNonStickyBlocks(config);
            setListNonStickyBlocksAgainstEachother(config);
        }
        catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void setListStaticBlocks(YamlConfiguration config) {
        MehGravity.staticBlocks = new HashSet<Material>();
        List<String> listFromConfig = config.getStringList("staticBlocks");
        if (listFromConfig != null) {
            for (String s : listFromConfig) {
                Material m = Material.getMaterial(s);
                if (m != null)
                    MehGravity.staticBlocks.add(m);
            }
        }
    }

    private void setListNonStickyBlocks(YamlConfiguration config) {
        MehGravity.nonStickyBlocks = new HashSet<Material>();
        List<String> listFromConfig = config.getStringList("nonStickyBlocks");
        if (listFromConfig != null) {
            for (String s : listFromConfig) {
                Material m = Material.getMaterial(s);
                if (m != null)
                    MehGravity.nonStickyBlocks.add(m);
            }
        }
    }

    private void setListNonStickyBlocksAgainstEachother(YamlConfiguration config) {
        MehGravity.nonStickyBlocksAgainstEachother = new HashMap<Material, HashSet<Material>>();
        HashMap<Material, HashSet<Material>> shortName = MehGravity.nonStickyBlocksAgainstEachother;
        List<String> listFromConfig = config.getStringList("nonStickyBlocksAgainstEachother");
        if (listFromConfig != null) {
            for (String s : listFromConfig) {
                String[] materials = s.split("-");
                if (materials.length == 2) {
                    Material one = Material.getMaterial(materials[0]);
                    Material two = Material.getMaterial(materials[1]);
                    if (one != null && two != null) {
                        if (shortName.containsKey(one)) {
                            shortName.get(one).add(two);
                        } else {
                            shortName.put(one, new HashSet<Material>(Arrays.asList(two)));
                        }
                    } else {
                        this.getServer().getLogger().warning("Could not load the nonstickypair " + s);
                    }
                }
            }
        }
    }

    public void save()
    {
        YamlConfiguration config = new YamlConfiguration();
        try
        {
            config.set("useMetrics", this.useMetrics);
            config.set("blocklimit", MehGravity.blockLimit);
            config.set("gravityWorlds", MehGravity.gravityWorlds);
            
            HashSet<String> temp = new HashSet<String>();
            for (Material m : MehGravity.staticBlocks) { temp.add(m.name()); }
            config.set("staticBlocks", temp);
            
            temp = new HashSet<String>();
            for (Material m : MehGravity.nonStickyBlocks)
                temp.add(m.name());
            config.set("nonStickyBlocks", temp);
            
            saveConfigNonStickyBlocksAgainstEachother(config);
            
            config.save(configFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveConfigNonStickyBlocksAgainstEachother(YamlConfiguration config) {
        HashSet<String> temp = new HashSet<String>();
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
    }

    public void saveDefaultConfig()
    {
        if (configFile == null)
            configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists())
            saveResource("config.yml", false);
    }

    public boolean hasPerms(Player player)
    {
        //Permission is not set OR it is set, but this player doesn't have it 
        if (    !player.isPermissionSet("mehgravity.nocheck") 
                || (    player.isPermissionSet("mehgravity.nocheck") 
                    &&  !player.hasPermission("mehgravity.nocheck")
                )
            ) {
            return true;
        }
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

    public void checkAround(Block block, int delay)
    {
        new GravityCheckAroundLater(this, block).runTaskLater(this, delay);
    }

    public void check(Block block)
    {
        Structure structure = structureHandler.createStructure(block);
        if (structure != null)
            structureHandler.addStructure(structure);
    }
    
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event)
    {
        for(Block block : event.blockList())
        {
            this.checkAround(block, 0);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (hasPerms(event.getPlayer()))
            checkAround(event.getBlock(), 0);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event)
    {
        if (hasPerms(event.getPlayer()))
            check(event.getBlockPlaced());
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event)
    {
        checkAround(event.getBlock(), 0);
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event)
    {
        checkAround(event.getBlock(), 0);
    }

    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event)
    {
        Block base = event.getBlock();
        Block toCheck = base.getWorld().getBlockAt(
            new org.bukkit.Location(
                base.getWorld(), 
                base.getX() + event.getDirection().getModX(), 
                base.getY() + event.getDirection().getModY(), 
                base.getZ() + event.getDirection().getModZ()
            )
        );
        checkAround(toCheck, 10);
    }

    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent event)
    {
        Block base = event.getBlock();
        Block toCheck = base.getWorld().getBlockAt(
            new org.bukkit.Location(
                base.getWorld(), 
                base.getX() + event.getDirection().getModX(), 
                base.getY() + event.getDirection().getModY(), 
                base.getZ() + event.getDirection().getModZ()
            )
        );
        checkAround(toCheck, 10);
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event)
    {
        // Prevent cactus duplication bug
        Material type = event.getChangedType();
        if (type == Material.CACTUS)
            event.setCancelled(true);
    }
}
