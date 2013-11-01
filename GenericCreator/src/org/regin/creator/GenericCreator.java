package org.regin.creator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bridgedb.DataSource;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.regin.creator.graph.Graph;
import org.regin.creator.graph.Graph.Edge;
import org.regin.creator.graph.Graph.Node;
import org.regin.creator.utils.ArgsParser;
import org.regin.creator.utils.ArgsParser.AFilesIn;
import org.regin.creator.utils.ArgsParser.AFilesOut;
import org.regin.creator.utils.ArgsParser.AHelp;
import org.regin.creator.utils.ArgsParser.GraphBuilder;
import org.regin.creator.utils.ConfigAttributes;
import org.regin.creator.utils.ConfigFileReader;
import org.regin.creator.utils.Utils;

public class GenericCreator {
	private final static Logger log = Logger.getLogger(GenericCreator.class.getName());
	static Args pargs;
	private interface Args extends AHelp, AFilesIn, AFilesOut {}
	
	/**
	 * ARGUMENTS: 
	 * -i = input file
	 * -o = output file
	 * -c = config file
	 */
	public static void main(String argv[]) throws Exception {

		pargs = ArgsParser.parse(argv, Args.class);

		GenericCreator converter = new GenericCreator();
		converter.startConversion(pargs);
	}

	private Graph graph;
	private Map<String, List<String>> edges;

	private List<String> foundConnections;
	private Integer countEdges = 0;
	private Integer countTrg = 0;
	private Integer countSrc = 0;
	
	private Map<String, Node> nodes;

	public GenericCreator() {
		edges = new HashMap<String, List<String>>();
		foundConnections = new ArrayList<String>();
		nodes = new HashMap<String, Graph.Node>();
	}

	public void startConversion(Args pargs) throws Exception {
		if(pargs.isInput() && pargs.isConfig() && pargs.isOutput()) {
			File in = pargs.getInput();
			File config = pargs.getConfig();
			if(in.exists() && config.exists()) {				
				ArgsParser.convertAndWrite(pargs, pargs, new GraphBuilder() {
					public Graph buildGraph(File in) throws Exception {
						return convert(in);
					}
				});
			}
		} else {
			System.out.println("check paramters. at least one parameter is missing.");
		}
	}
	
	private ConfigAttributes attr;
	private String networkName;
	private IDMapper sourceIdMapper;
	private IDMapper targetIdMapper;
	private String [] header;	
	private int nomimat = 0;
	
	public Graph convert(File input) {
		graph = new Graph();
		try {
			Utils.setUpLogger(log, new File(input.getParentFile(), input.getName() + ".log"), true);
			
			log.info("Read config file.\n");
			attr = ConfigFileReader.readFile(pargs.getConfig());
			networkName = getNetworkName();
			log.info("Conversion of " + networkName + " started.\n");

			setNetworkAttributes(input);
			setUpIdMappers();
			
			BufferedReader reader = new BufferedReader(new FileReader(input));
			
			header = reader.readLine().split("\t");
			
			String line = null;
			int count = 2;
			while((line = reader.readLine()) != null) {
				line = removeInvalidXMLCharacters(line);
				String [] row = line.split("\t");
				Node source = createSourceNode(row);
				Node target = createTargetNode(row);
				if(source != null && target != null) {
					createEgde(source, target, row);
				} else {
					log.warning("Error in line " + count+ ". Source = \"" + row[attr.getSourceIdColumn()] + "\". Target = \""+ row[attr.getTargetIdColumn()] + "\"\n");
				}
				count++;
			}
			
			reader.close();

			log.info("edges: " + countEdges + "\n" + 
					"source nodes: " + countSrc + "\n" + 
					"target nodes: " + countTrg + "\n");
			if(nomimat > 0) {
				log.info(nomimat + " microRNAs could not be mapped to a MIMAT accession number.\n");
			}
			log.info("conversion of " + networkName + " done.\n");
		} catch (Exception e) {
			log.severe("Could not convert file to RegIN: " + e.getMessage());
			e.printStackTrace();
		}
		edges.clear();
		foundConnections.clear();
		nodes.clear();
		attr = null;
		sourceIdMapper = null;
		targetIdMapper = null;
		return graph;
	}

	private Edge createEgde(Node source, Node target, String[] row) {
		if (edges.containsKey(source.getId())) {
            if (!edges.get(source.getId()).contains(target.getId())) {
                    Edge e = graph.addEdge("" + countEdges, source, target);
    	            setEdgeAttributes(e, row);
    	            e.setAttribute("datasource", networkName);
    	            e.setAttribute("interaction", attr.getInteractionType());
    	            
                    edges.get(source.getId()).add(target.getId());
                    foundConnections.add(source.getId() + "\t" + target.getId());
                    countEdges++;
            }
	    } else {
	            Edge e = graph.addEdge("" + countEdges, source, target);
	            setEdgeAttributes(e, row);
	            e.setAttribute("datasource", networkName);
	            e.setAttribute("interaction", attr.getInteractionType());
	            
	            List<String> list = new ArrayList<String>();
	            list.add(source.getId());
	            foundConnections.add(source.getId() + "\t" + target.getId());
	            edges.put(target.getId(), list);
	            countEdges++;
	    }
		return null;
	}
	
	private void setEdgeAttributes(Edge e, String [] row) {
		for(Integer i : attr.getEdgeColumns()) {
			e.setAttribute(header[i], row[i]);
		}
	}

	private Node createTargetNode(String[] row) throws IDMapperException {
		if(attr.getTargetIdColumn() != null) {
			String identifier = row[attr.getTargetIdColumn()];
			if(!identifier.equals("")) {
				if(nodes.containsKey(identifier)) {
					return nodes.get(identifier);
				} else {
					Node node = graph.addNode(identifier);
					String identifiers = "[" + identifier;
					nodes.put(identifier, node);
					if(targetIdMapper != null) {
						registerNode(node, attr.getTargetSyscodeIn(), targetIdMapper);
						identifiers = identifiers + getIdentifiers(identifier, attr.getTargetSyscodeIn(), attr.getTargetSyscodeOut(), targetIdMapper);
					}
					identifiers = identifiers + "]";
					
					node.setAttribute("identifiers", identifiers);
					node.setAttribute("biologicalType", attr.getTargetType());
					Integer columnLabel = attr.getTargetLabelColumn();
					if(columnLabel != null) {
						node.setAttribute("label", row[columnLabel]);
					} else {
						node.setAttribute("label", identifier);
					}
					setNodeAttributes(node, attr.getTargetColumns(), row);
					countTrg++;
					return node;
				}
			}
		}
		return null;
	}

	private void setNodeAttributes(Node node, Set<Integer> targetColumns, String[] row) {
		for(Integer i : targetColumns) {
			node.setAttribute(header[i], row[i]);
		}
	}

	private String getIdentifiers(String identifier, String targetSyscodeIn,
			List<String> targetSyscodeOut, IDMapper mapper) throws IDMapperException {
		String identifiers = "";
		Set<String> ids = new HashSet<String>();
		ids.add(identifier);
		Xref in = new Xref(identifier, DataSource.getBySystemCode(targetSyscodeIn));
		for(String ds : targetSyscodeOut) {
			Set<Xref> result = mapper.mapID(in, DataSource.getBySystemCode(ds));
			for(Xref x : result) {
				ids.add(x.getId());
			}
		}
		for(String str : ids) {
			if(!str.equals(identifier)) {
				identifiers = identifiers + "," + str;
			}
		}
		return identifiers;
	}


	private void registerNode(Node node, String syscodeIn, IDMapper mapper) throws IDMapperException {
		String id = node.getId();
		Xref xref = new Xref(id, DataSource.getBySystemCode(syscodeIn));
		if(mapper != null) {
			System.out.println("###" + xref.getId());
			// if node is a microRNA check if there is a MIMAT identifier
			if(syscodeIn.equals("Mb")) {
				Set<Xref> result = mapper.mapID(xref, DataSource.getBySystemCode("Mbm"));
				boolean found = false;
				for(Xref x : result) {
					if(x.getId().startsWith("MIMAT")) {
						if(!nodes.containsKey(x.getId())) {
							found = true;
						}
					}
				}
				if(!found) {
					nomimat++;
				}
			}
			Set<Xref> result = mapper.mapID(xref, DataSource.getBySystemCode(syscodeIn));
			for(Xref x : result) {
				if(!x.getId().equals(id)) {
					nodes.put(x.getId(), node);
					System.out.println("\t###" + x.getId());
				}
			}
		}
	}

	private Node createSourceNode(String[] row) throws IDMapperException {
		if(attr.getSourceIdColumn() != null) {
			String identifier = row[attr.getSourceIdColumn()];
			if(!identifier.equals("")) {
				if(nodes.containsKey(identifier)) {
					return nodes.get(identifier);
				} else {
					Node node = graph.addNode(identifier);
					String identifiers = "[" + identifier;
					nodes.put(identifier, node);
					if(sourceIdMapper != null) {
						registerNode(node, attr.getSourceSyscodeIn(), sourceIdMapper);
						identifiers = identifiers + getIdentifiers(identifier, attr.getSourceSyscodeIn(), attr.getSourceSyscodeOut(), sourceIdMapper);
					}
					identifiers = identifiers + "]";
					
					node.setAttribute("identifiers", identifiers);
					node.setAttribute("biologicalType", attr.getSourceType());
					Integer columnLabel = attr.getSourceLabelColumn();
					if(columnLabel != null) {
						node.setAttribute("label", row[columnLabel]);
					} else {
						node.setAttribute("label", identifier);
					}
					setNodeAttributes(node, attr.getSourceColumns(), row);
					countSrc++;
					return node;
				}
			}
		}
		return null;
	}

	private void setNetworkAttributes(File input) {
		graph.setTitle(networkName);
		graph.setAttribute("Source File", input.getName());
		graph.setAttribute("RegIN Name", networkName);
	}

	private void setUpIdMappers() {
		if(attr.getSourceBridgeDb() != null) {
			sourceIdMapper = Utils.initIDMapper(attr.getSourceBridgeDb(), false);
		}
		
		if(attr.getTargetBridgeDb() != null) {
			targetIdMapper = Utils.initIDMapper(attr.getTargetBridgeDb(), false);
		}
	}

	private String getNetworkName() {
		String name = attr.getName();
		if(attr.getOrganism() != null) {
			name = name + "_" + attr.getOrganism();
		}
		if(attr.getVersion() != null) {
			name = name + "_" + attr.getVersion();
		}
		return name;
	}
	
	 /**
     * Removes all invalid Unicode characters that are not suitable to be used
     * either in markup or text inside XML Documents.
     * 
     * Based on these recommendations
     * http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char
     * http://cse-mjmcl.cse.bris.ac.uk/blog/2007/02/14/1171465494443.html
     * 
     * @param s: The resultant String stripped of the offending characters!
     * @return
     */
    public String removeInvalidXMLCharacters(String s) {
            StringBuilder out = new StringBuilder();

            int codePoint;
            int i = 0;

            while (i < s.length()) {
                    // This is the unicode code of the character.
                    codePoint = s.codePointAt(i);
                    if ((codePoint == 0x9) || (codePoint == 0xA) || (codePoint == 0xD)
                                    || ((codePoint >= 0x20) && (codePoint <= 0xD7FF))
                                    || ((codePoint >= 0xE000) && (codePoint <= 0xFFFD))
                                    || ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF))) {
                            out.append(Character.toChars(codePoint));
                    }
                    i += Character.charCount(codePoint);
            }
            return out.toString();
    }
}
