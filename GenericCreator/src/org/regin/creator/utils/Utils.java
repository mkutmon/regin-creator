package org.regin.creator.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.bridgedb.BridgeDb;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.bio.BioDataSource;

public class Utils {

	public static void setUpLogger(Logger log, File logFile, boolean append) throws SecurityException, IOException {
		FileHandler fh = new FileHandler(logFile.getAbsolutePath(), append);
		log.addHandler(fh);
		log.setLevel(Level.ALL);
		SimpleFormatter formatter = new SimpleFormatter();
		fh.setFormatter(formatter);
	}

	public static IDMapper initIDMapper(File file, boolean transitive) {
		BioDataSource.init();
		try {
			Class.forName("org.bridgedb.rdb.IDMapperRdb");
		} catch (ClassNotFoundException ex) {
			return null;
		}

		try {
			IDMapper mapper = BridgeDb.connect("idmapper-pgdb:" + file.getAbsolutePath());
			return mapper;
		} catch (IDMapperException e) {
			return null;
		}
	}

}
