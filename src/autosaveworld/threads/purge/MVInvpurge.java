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

package autosaveworld.threads.purge;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;

import autosaveworld.core.AutoSaveWorld;

public class MVInvpurge {

	private AutoSaveWorld plugin;

	public MVInvpurge(AutoSaveWorld plugin)
	{
		this.plugin = plugin;
	}
	
	
	public void doMVInvPurgeTask(long awaytime)
	{
		try {
		MultiverseInventories mvpl = (MultiverseInventories) Bukkit.getPluginManager().getPlugin("Multiverse-Inventories");
		File mcinvpfld = new File("plugins/Multiverse-Inventories/players/");
		int deleted = 0;
		//We will get all files from MVInv player directory, and get player names from there
		for (String plfile : mcinvpfld.list())
		{
			String plname = plfile.substring(0, plfile.indexOf("."));
				
				if (!isActive(plname,awaytime)) {
					plugin.debug("Removing "+plname+" MVInv files");
					//remove files from MVInv world folders
					for (World wname : Bukkit.getWorlds()) {
						mvpl.getWorldManager().getWorldProfile(wname.getName()).removeAllPlayerData(Bukkit.getOfflinePlayer(plname));
					}
					//remove files from MVInv player folder
					new File(mcinvpfld,plfile).delete();
					//remove files from MVInv groups folder
					for (WorldGroupProfile gname: mvpl.getGroupManager().getGroups())
					{
						File mcinvgfld = new File("plugins/Multiverse-Inventories/groups/");
						new File(mcinvgfld,gname.getName()+File.separator+plfile).delete();
					}
					//count deleted player file
					deleted += 1;
				}
		}
		
		plugin.debug("MVInv purge finished, deleted "+deleted+" player files, Warning: on some Multiverse-Inventories versions you should divide this number by 2 to know the real count");
		
		} catch (Exception e) {e.printStackTrace();}
	}
	
	
	private boolean isActive(String player, long awaytime)
	{
		OfflinePlayer offpl = Bukkit.getOfflinePlayer(player);
		boolean active = true;
		if (System.currentTimeMillis() - offpl.getLastPlayed() >= awaytime)
		{
			active = false;
		}
		if (offpl.isOnline())
		{
			active = true;
		}
		return active;
	}
	
}
