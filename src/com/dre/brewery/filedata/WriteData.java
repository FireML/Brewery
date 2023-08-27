package com.dre.brewery.filedata;


import java.io.File;

import com.dre.brewery.Brewery;
import org.bukkit.configuration.file.FileConfiguration;
import uk.firedev.poleislib.Loggers;

/**
 * Writes the collected Data to file in Async Thread
 */
public class WriteData implements Runnable {

	private FileConfiguration data;
	private FileConfiguration worldData;

	public WriteData(FileConfiguration data, FileConfiguration worldData) {
		this.data = data;
		this.worldData = worldData;
	}

	@Override
	public void run() {
		File datafile = new File(Brewery.getInstance().getDataFolder(), "data.yml");
		File worlddatafile = new File(Brewery.getInstance().getDataFolder(), "worlddata.yml");

		try {
			data.save(datafile);
		} catch (Exception e) {
			Loggers.logException(e, Brewery.getInstance().getLogger());
		}
		try {
			worldData.save(worlddatafile);
		} catch (Exception e) {
			Loggers.logException(e, Brewery.getInstance().getLogger());
		}

		DataSave.lastSave = 1;
		DataSave.running = null;
		BData.dataMutex.set(0);
	}
}
