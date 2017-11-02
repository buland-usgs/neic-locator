package gov.usgs.locator;

import java.io.IOException;
import java.util.TreeMap;

/**
 * Sample driver for the travel-time package.
 * 
 * @author Ray Buland
 *
 */
public class TtMain {
	public static void main(String[] args) throws Exception {
		// Directory of known models.
		TreeMap<String,AllBrnRef> modelData = null;
		// Simulate a simple session request.
		String earthModel = "ak135";
		double sourceDepth = 10;
		String[] phList = null;
		boolean useful = true;
		boolean noBackBrn = false;
		boolean rstt = false;
		// Simulate a simple travel time request.
		double delta = 5d;
		double elev = 1d;
		boolean tectonic = false;
		// Classes we will need.
		ReadTau readTau = null;
		AllBrnRef allRef = null;
		AllBrnVol allBrn;
		AuxTtRef auxtt;
		TTime ttList;
		
		// Read in data common to all models.
		auxtt = new AuxTtRef(false, false, true);

		// Initialize model storage if necessary.
		if(modelData == null) {
			modelData = new TreeMap<String,AllBrnRef>();
		}
		
		// See if we know this model.
		allRef = modelData.get(earthModel);
		
		// If not, set it up.
		if(allRef == null) {
			try {
				readTau = new ReadTau(earthModel);
				readTau.readHeader();
		//	readTau.dumpSegments();
		//	readTau.dumpBranches();
				readTau.readTable();
		//	readTau.dumpUp(15);
			} catch(IOException e) {
				System.out.println("Unable to read Earth model "+earthModel);
				System.exit(1);
			}
			allRef = new AllBrnRef(readTau, auxtt);
			modelData.put(earthModel, allRef);
	//	allRef.dumpBrn(false);
		}
		
		// At this point, we've either found the reference part of the model 
		// or read it in.  Now Set up the (depth dependent) volatile part.
		allBrn = new AllBrnVol(allRef);
//	allBrn.dumpHead();
//	allBrn.dumpTable();
//	allBrn.dumpMod('P', true);
//	allBrn.dumpMod('S', true);
		// Set up a new session.
		try {
			allBrn.newSession(sourceDepth, phList);
//		allBrn.dumpBrn(true, true, true);
//		allBrn.dumpBrn(false, false, true, true);
			allBrn.dumpTable(true);
			
			// Get the travel times.
			ttList = allBrn.getTT(delta, elev, useful, tectonic, noBackBrn, rstt);
			// Print them.
			ttList.print(sourceDepth, delta);
		} catch(IOException e) {
			System.out.println("Source depth out of range");
		}
	}
}
