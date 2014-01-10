regin-creator
=============

Scripts to create RegINs (Regulatory Interaction Networks) from tab files.

=============

This program allows you to create a RegIN file that can be used with CyTargetLinker from a tab-delimited file containing attributes for the source and target node as well as attributes for the edges.

=============

How to run the script with your own tab-delimited text file?

1. Download the latest release from https://github.com/mkutmon/regin-creator/blob/master/releases/. You will need the jar file and the dependencies zip file which contains a lib directory with all dependencies. Unzip the dependencies.zip and make sure the lib directory is in the same directory as the jar file, so it can find all dependencies.
2. Create a config file like the template here: https://github.com/mkutmon/regin-creator/blob/master/template.config.
 * name = database name (required)
 * organism = species (optional)
 * version = database version (optional)
 * source_columns = list of column numbers (starting with 0!) that are associated with the source nodes (separated with ",")
 * target_columns = list of column numbers (starting with 0!) that are associated with the target nodes (separated with ",")
 * edge_columns = list of column numbers (starting with 0!) that are associated with the edges (separated with ",")
 * interaction_type = which kind of interaction does the file contain
 * source_id_column = column that contains the identifier for source nodes (required)
 * source_type = (gene, transcriptionFactor, drug, miRNA -> predefined) but you can also use others (required)
 * source_bridgedb = path to the bridgedb file that should be used for the source nodes (optional)
 * source_syscode_in = bridgedb system code for the source node identifiers (optional but required if bridgedb is used)
 * source_syscode_out = list of bridgedb system codes that should be supported (optional but required if bridgedb is used) (separated with ",")
 * source_label_column = column number (starting with 0!) which contains the nodel label for source nodes (required)
 * target_id_column = column that contains the identifier for target nodes (required)
 * target_type = (gene, transcriptionFactor, drug, miRNA -> predefined) but you can also use others (required)
 * target_bridgedb = path to the bridgedb file that should be used for the target nodes (optional)
 * target_syscode_in = bridgedb system code for the target node identifiers (optional but required if bridgedb is used)
 * target_syscode_out = list of bridgedb system codes that should be supported (optional but required if bridgedb is used) (separated with ",")
 * target_label_column = column number (starting with 0!) which contains the nodel label for target nodes (required)
          
          
3. Run the converter file with the input file (tab delimited text file containing the interaction data), the config file (as described in point 2) and an output file (e.g. output.xgmml).
 * java -jar converter-x.x.jar -i input.txt -o output.xgmml -c file.config
 
