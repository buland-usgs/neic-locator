package gov.usgs.locator.test;

import java.io.IOException;
import gov.usgs.traveltime.ReadTau;
import gov.usgs.traveltime.AllBrnRef;
import gov.usgs.traveltime.AuxTtRef;
/**
 * Test program for ReadTau.
 * 
 * @author Ray Buland
 *
 */
public class ReadTauBin {

	/**
	 * Exercise the *.hed and *.tbl reader.  Currently the raw dump 
	 * methods are commented out and the more useful versions are 
	 * being used.  Extended to exercise the data storage Java classes 
	 * as well.
	 * 
	 * @param args No arguments are used.
	 * @throws IOException Throws an exception if the input files are 
	 * not as expected.
	 */
	public static void main(String[] args) throws IOException {
		ReadTau reader;
		AllBrnRef ttData;
		AuxTtRef auxtt;
		
		// Read in data common to all models.
		auxtt = new AuxTtRef(false, false, false, false);
		
		// Read the model tau data.
		reader = new ReadTau("ak135");
		reader.readHeader();
	//	reader.dumpGlobal();
		reader.dumpSegments();
		reader.dumpBranches();
	//	reader.dumpModel(false);
	//	reader.dumpAll();
		reader.readTable();
	//	reader.dumpUp(10);
	//	reader.dumpUp(80);
		
		// Test the new data classes.
		ttData = new AllBrnRef(reader, auxtt);
		ttData.dumpHead();
	//	ttData.dumpMod('P', false);
	//	ttData.dumpMod('S', false);
	//	ttData.dumpBrn(0, true);
	//	ttData.dumpBrn(1, true);
	//	ttData.dumpBrn(85, true);
		for(int j=0; j<ttData.getNoBranches(); j++) {
			ttData.dumpBrn(j, false);
		}
	//	ttData.dumpUp('P', 10);
	//	ttData.dumpUp('S', 10);
	}
}
