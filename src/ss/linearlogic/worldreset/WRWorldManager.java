package ss.linearlogic.worldreset;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class WRWorldManager {

	private WorldReset plugin;

	public WRWorldManager(WorldReset wr) {
		this.plugin = wr;
	}

	public void importWorlds() {
		boolean errors = false;
		File backupDir = new File(this.plugin.getDataFolder(), "backups");
		for (File source : backupDir.listFiles()) {
			if (source.isDirectory()) {
				File target = new File(this.plugin.getServer().getWorldContainer(), source.getName());
				if (target.exists() && target.isDirectory()) { //delete the old world folder
					if (!delete(target)) {
						plugin.logSevere("Failed to reset world \"" + source.getName() + "\" - could not delete old " +
								"world folder.");
						errors = true;
						continue;
					}
				}

				try {
		        	copyDir(source, target); //import the new world folder from the plugin's backup directory
				} catch(IOException e) {
		        	e.printStackTrace();
					plugin.logSevere("Failed to reset world \"" + source.getName() + "\" - could not import the " +
							"world from backup.");
					errors = true;
				}
				plugin.logInfo("Import of world \"" + source.getName() + "\" " + (errors ? "failed!" : "succeeded!"));
				errors = false;
			}
		}
    }
 
	public void deleteWorlds() {
		boolean worldsListed = false;
		for (String worldName : plugin.getConfig().getStringList("random-seed.worlds")) {
			if (!worldsListed)
				worldsListed = true;
			File target = new File(plugin.getServer().getWorldContainer(), worldName);
			if (!target.exists()) {
				plugin.logSevere("Could not load world \"" + worldName + "\" with a random seed: no such world " +
						"exists in the server directory!");
				return;
			}
			if (target.isDirectory()) {
				if (!delete(target)) {
					plugin.logSevere("Failed to delete world \"" + worldName + "\", perhaps the folder is locked?");
					continue;
				}
				plugin.logInfo("Successfully loaded a random seed for world \"" + worldName + "\"!");
			}
		}
		if (!worldsListed)
			plugin.logWarning("The random seed option is enabled but no worlds are listed to be deleted and " +
					"regenerated with random seeds.");
	}

	private boolean delete(File file) {
		if (file.isDirectory())
			for (File subfile : file.listFiles())
				if (!delete(subfile))
					return false;
		if (!file.delete())
			return false;
		return true;
	}

    private static void copyDir(File source, File target) throws IOException {
    	if(source.isDirectory()) {
    		if(!target.exists())
    		   target.mkdir();
    		String files[] = source.list();
    		for (String file : files) {
    		   File srcFile = new File(source, file);
    		   File destFile = new File(target, file);
    		   copyDir(srcFile, destFile);
    		}
    	} else {
    		InputStream in = new FileInputStream(source);
    	    OutputStream out = new FileOutputStream(target); 
    	    byte[] buffer = new byte[1024];
	        int length;
	        //copy the file content in bytes 
	        while ((length = in.read(buffer)) > 0)
	        	out.write(buffer, 0, length);
	        in.close();
	        out.close();
    	}
    }
}
