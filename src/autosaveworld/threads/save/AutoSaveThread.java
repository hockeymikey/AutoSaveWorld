/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 */

package autosaveworld.threads.save;

import org.bukkit.World;

import autosaveworld.core.AutoSaveWorld;
import autosaveworld.config.AutoSaveConfig;
import autosaveworld.config.AutoSaveConfigMSG;

public class AutoSaveThread extends Thread {


	private AutoSaveWorld plugin = null;
	private AutoSaveConfig config;
	private AutoSaveConfigMSG configmsg;
	public AutoSaveThread(AutoSaveWorld plugin, AutoSaveConfig config, AutoSaveConfigMSG configmsg) {
		this.plugin = plugin;
		this.config = config;
		this.configmsg = configmsg;
	}


	public void stopThread() {
		this.run = false;
	}

	public void startsave() {
		command = true;
		i = config.saveInterval;
	}
	
	// The code to run...weee
	private int i;
	private volatile boolean run = true;
	public boolean command = false;
	public void run() {

		plugin.debug("AutoSaveThread Started");
		Thread.currentThread().setName("AutoSaveWorld AutoSaveThread");
		
		while (run) {
			// Prevent AutoSave from never sleeping
			// If interval is 0, sleep for 5 seconds and skip saving
			if(config.saveInterval == 0) {
				try {Thread.sleep(5000);} catch(InterruptedException e) {}
				continue;
			}
			
			//sleep
			for (i = 0; i < config.saveInterval; i++) {
				if (!run) {break;}
				try {Thread.sleep(1000);} catch (InterruptedException e) {}
			}

			//save
			if (run&&(config.saveEnabled||command)) {
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { 
					public void run() {performSave();}});
			}
		}
		
		plugin.debug("Graceful quit of AutoSaveThread");

	}
	
	
	
	
	private void savePlayers() {
		plugin.debug("Saving players");
		plugin.getServer().savePlayers();
	}

	private void saveWorlds() {
		for (World world : plugin.getServer().getWorlds()) {
			plugin.debug(String.format("Saving world: %s", world.getName()));
			world.save();
		}
	}


	public void performSave() {
		
		
		if (plugin.getServer().getOnlinePlayers().length == 0 && !command) {
			// No players online, don't bother saving.
			plugin.debug("Skipping save, no players online.");
			return;
		}
		
		command = false;
		
		if (plugin.saveInProgress) {
			plugin.warn("Multiple concurrent saves attempted! Save interval is likely too short!");
			return;
		}
		if (plugin.backupInProgress) {
			plugin.warn("AutoBackup is in process. AutoSave cancelled");
			return;
		}
		
		try {
			
			// Lock
			plugin.saveInProgress = true;

			try {
				
				if (config.saveBroadcast) {plugin.broadcast(configmsg.messageSaveBroadcastPre);}

				// Save the players
				savePlayers();
				plugin.debug("Saved Players");

				// Save the worlds
				saveWorlds();
				plugin.debug("Saved Worlds");

				if (config.saveBroadcast) {plugin.broadcast(configmsg.messageSaveBroadcastPost);}
			} catch (Exception e) 
			{
				if (config.saveBroadcast){plugin.broadcast("&4AutoSave Failed");}
				if (config.varDebug) {e.printStackTrace();}
			}

			plugin.LastSave =new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(System.currentTimeMillis());

		} finally {
			// Release
			plugin.saveInProgress = false;
		}
	}


}
