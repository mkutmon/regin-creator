package org.regin.creator.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ConfigFileReader {
	
	public static ConfigAttributes readFile(File configFile) throws IOException {
		ConfigAttributes attributes = new ConfigAttributes();

		if(configFile.exists()) {
			BufferedReader reader = new BufferedReader(new FileReader(configFile));
			String line;
			while((line = reader.readLine()) != null) {
				String [] split = line.split("=");
				if(split.length==2) {
					if(split[0].equals(ConfigAttributes.NAME)) { 
						attributes.setName(split[1]);
					} else if (split[0].equals(ConfigAttributes.VERSION)) { 
						attributes.setVersion(split[1]);
					} else if (split[0].equals(ConfigAttributes.EDGE_COLUMNS)) { 
						attributes.setEdgeColumns(split[1]);
					} else if (split[0].equals(ConfigAttributes.ORGANISM)) { 
						attributes.setOrganism(split[1]);
					} else if (split[0].equals(ConfigAttributes.SOURCE_BRIDGEDB)) { 
						attributes.setSourceBridgeDb(split[1]);
					} else if (split[0].equals(ConfigAttributes.SOURCE_COLUMNS)) { 
						attributes.setSourceColumns(split[1]);
					} else if (split[0].equals(ConfigAttributes.SOURCE_ID_COLUMN)) { 
						attributes.setSourceLabelColumn(split[1]);
					} else if (split[0].equals(ConfigAttributes.SOURCE_LABEL_COLUMN)) { 
						attributes.setSourceIdColumn(split[1]);
					} else if (split[0].equals(ConfigAttributes.SOURCE_SYSCODE_IN)) { 
						attributes.setSourceSyscodeIn(split[1]);
					} else if (split[0].equals(ConfigAttributes.SOURCE_SYSCODES_OUT)) { 
						attributes.setSourceSyscodeOut(split[1]);
					} else if (split[0].equals(ConfigAttributes.SOURCE_TYPE)) { 
						attributes.setSourceType(split[1]);
					} else if (split[0].equals(ConfigAttributes.TARGET_BRIDGEDB)) { 
						attributes.setTargetBridgeDb(split[1]);
					} else if (split[0].equals(ConfigAttributes.TARGET_COLUMNS)) { 
						attributes.setTargetColumns(split[1]);
					} else if (split[0].equals(ConfigAttributes.TARGET_ID_COLUMN)) { 
						attributes.setTargetIdColumn(split[1]);
					} else if (split[0].equals(ConfigAttributes.TARGET_LABEL_COLUMN)) { 
						attributes.setTargetLabelColumn(split[1]);
					} else if (split[0].equals(ConfigAttributes.TARGET_SYSCODE_IN)) { 
						attributes.setTargetSyscodeIn(split[1]);
					} else if (split[0].equals(ConfigAttributes.TARGET_SYSCODES_OUT)) { 
						attributes.setTargetSyscodeOut(split[1]);
					} else if (split[0].equals(ConfigAttributes.TARGET_TYPE)) { 
						attributes.setTargetType(split[1]);
					} else if (split[0].equals(ConfigAttributes.INTERACTION_TYPE)) { 
						attributes.setInteractionType(split[1]);
					}
				} else {
					System.out.println("invalid attribute\t" + line);
				}
			}
			
			reader.close();
		}
		return attributes;
	}
}
