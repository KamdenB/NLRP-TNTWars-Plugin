package me.kamdenb.tntwars;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;

public class MapGen extends JavaPlugin implements Listener{

	public void loadSchematic(World world, double x, double y, double z) throws IOException {
//		Location location = p.getLocation();
//		WorldEditPlugin  worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
		File schematic = new File(this.getDataFolder() + File.separator + "/schematic/arena.schem");
		ClipboardFormat format = ClipboardFormats.findByFile(schematic);
		ClipboardReader reader = format.getReader(new FileInputStream(schematic));
		Clipboard clipboard = reader.read();
		try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1)){
			Operation operation = new ClipboardHolder(clipboard)
					.createPaste(editSession)
					.to(BlockVector3.at(x, y, z))
					.ignoreAirBlocks(false)
					.build();
		}
	}
	
	public void genMap() {
		
	}
	
}
