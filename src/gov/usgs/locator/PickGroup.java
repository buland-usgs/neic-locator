package gov.usgs.locator;

import java.util.ArrayList;

/**
 * Pick groups contain all the picks observed at one station for 
 * one event.  This construct is useful because the station 
 * geometry is the same for all picks in the group, so travel times 
 * are only computed once for each group.  Note that the Locator 
 * flow depends on the Bulletin Hydra style input file.  In 
 * particular, that picks for each station are together.  In 
 * preserving the Hydra order, picks in each group are also in 
 * time order.  The Locator doesn't actually require this, but it 
 * is convenient for bulltin style output.  Because the Locator 
 * server may get input for all sorts of different sources, the 
 * Hydra order is imposed on all event input.
 * 
 * @author Ray Buland
 *
 */
public class PickGroup {
	// Inputs:
	Station station;			// Station information
	// Outputs:
	double delta;					// Source-receiver distance in degrees
	double azimuth;				// Receiver azimuth from the source in degrees
	// Picks:
	ArrayList<Pick> picks;

	/**
	 * Initialize the pick group with the station and the first pick.
	 * 
	 * @param station Station information
	 * @param pick Pick information
	 */
	public PickGroup(Station station, Pick pick) {
		this.station = station;
		picks = new ArrayList<Pick>();
		picks.add(pick);
		delta = Double.NaN;
		azimuth = Double.NaN;
	}
	
	/**
	 * Add another pick to the group.
	 * 
	 * @param pick Pick information
	 */
	public void add(Pick pick) {
		picks.add(pick);
	}
	
	/**
	 * Update the pick group when the hypocenter is updated.
	 * 
	 * @param hypo Hypocenter information
	 */
	public void update(Hypocenter hypo) {
		// Distance and azimuth are group level parameters.
		delta = LocUtil.delAz(hypo, station);
		azimuth = LocUtil.azimuth;
		// Update travel time for each pick in the group.
		for(int j=0; j<picks.size(); j++) {
			picks.get(j).updateTt(hypo);
		}
	}
	
	/**
	 * Count the number of picks in the group that are used 
	 * in the location.
	 * 
	 * @return Number of used picks
	 */
	public int picksUsed() {
		int noUsed = 0;
		for(int j=0; j<picks.size(); j++) {
			if(picks.get(j).used) noUsed++;
		}
		return noUsed;
	}
	
	/**
	 * Print out the input pick information in a format similar to 
	 * the Hydra event input file.
	 */
	public void printIn() {
		Pick pick;
		
		for(int j=0; j<picks.size(); j++) {
			pick = picks.get(j);
			System.out.format("%10s %-5s %3s %2s %2s %8.4f %9.4f %5.2f "+
					"%3.1f %-8s %12s %5b %-13s %-8s %3.1f\n", pick.dbID, 
					station.staID.staCode, pick.chaCode, station.staID.netCode, 
					station.staID.locCode, station.latitude, station.longitude, 
					station.elevation, pick.quality, pick.phCode, 
					LocUtil.getRayTime(pick.arrivalTime), pick.use, 
					pick.authType, pick.obsCode, pick.affinity);
		}
	}
	
	/**
	 * Print the pick part of a Bulletin Hydra style output file.
	 */
	public void printHydra() {
		Pick pick;
		
		for(int j=0; j<picks.size(); j++) {
			pick = picks.get(j);
			System.out.format("%-10s %-5s %-3s %-2s %-2s %-8s%6.1f %5.1f "+
					"%3.0f %1s %4.2f %6.4f\n", pick.dbID, station.staID.staCode, 
					pick.chaCode, station.staID.netCode, station.staID.locCode, 
					pick.phCode, pick.residual, delta, azimuth, 
					LocUtil.getBoolChar(pick.used), pick.weight, pick.importance);
		}
	}
	
	/**
	 * Print out picks in the group in a way similar to the NEIC web format.
	 */
	public void printNEIC() {
		Pick pick;
		
		for(int j=0; j<picks.size(); j++) {
			pick = picks.get(j);
			switch(pick.authType) {
				case CONTRIB_HUMAN: case LOCAL_HUMAN:
					System.out.format("%-2s %-5s %-3s %-2s  %5.1f     %3.0f   %-8s %12s "+
							" manual    %6.1f    %4.2f\n", station.staID.netCode, 
							station.staID.staCode, pick.chaCode, station.staID.locCode, 
							delta, azimuth, pick.phCode, LocUtil.getNEICtime(pick.arrivalTime), 
							pick.residual, pick.weight);
					break;
				default:
					System.out.format("%-2s %-5s %-3s %-2s  %5.1f     %3.0f   %-8s %12s  "+
							"automatic %6.1f    %4.2f\n", station.staID.netCode, 
							station.staID.staCode, pick.chaCode, station.staID.locCode, 
							delta, azimuth, pick.phCode, LocUtil.getNEICtime(pick.arrivalTime), 
							pick.residual, pick.weight);
					break;
			}
		}
	}
}
