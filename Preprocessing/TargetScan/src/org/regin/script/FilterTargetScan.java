package org.regin.script;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FilterTargetScan {

	public static void main(String [] args) throws IOException {
		if(args.length == 2) {
			File file = new File(args[0]);
			if(file.exists()) {
				String prefix = args[1];
				// column 4
				BufferedReader reader = new BufferedReader(new FileReader(file));
				BufferedWriter writer = new BufferedWriter(new FileWriter(new File(file.getParentFile(), "filtered_" + file.getName())));
				String line;
				String header = reader.readLine();
				writer.write(header + "\n");
				while((line = reader.readLine()) != null) {
					String [] row = line.split("\t");
					if(row[4].startsWith(prefix)) {
						writer.write(line + "\n");
					}
				}
				writer.close();
				reader.close();
			}
		}
	}
}
