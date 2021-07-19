package gov.usgs.locaux;

import java.io.Serializable;

import gov.usgs.locator.BayesianDepth;
import gov.usgs.locator.DepthSource;

/**
 * This abstract class enables different zone statistics implementations to share the common 
 * interpolation code.  Implementation independent methods are coded here, while implementation 
 * specific methods must be coded in the extending classes.
 * 
 * @author Ray Buland
 *
 */
public abstract class AbstractZoneStats implements Serializable {

	/**
   * A long containing the serializable class version number, used during deserialization to verify
   * compatibility.
	 */
	private static final long serialVersionUID = 1L;

	/** A class that interpolates ZoneStat Bayesian depths. */
  private ZoneInterpolate interpolate = null;
  
  /** A double containing the fixed spacing between latitude rows in degrees. */
  protected double latSpacing = Double.NaN;
	
  /** A double containing the geographic colatitude of the first row in degrees. */
  protected double firstRowLat = Double.NaN;
  
  /** A double containing the geograhic colatitude of the last latitude row in degrees. */
  protected double lastRowLat = Double.NaN;
  
  /** A double containing the geographic coLatitude of a trial point in degrees (0-180). */
  protected double coLat = Double.NaN;
  
  /** A double containing the longitude of a trial point in degrees (0-360). */
  protected double coLon = Double.NaN;
  
  /** An integer containing the latitude index corresponding to coLat above. */
  protected int latIndex = -1;
  
  /** An integer containing the longitude index corresponding to coLon above. */
  protected int lonIndex = -1;

  /**
   * The coLat value is computed by the latest call to canonicalCoords.
   * 
   * @return Canonical geographic colatitude of a trial point in degrees (0-180)
   */
  public double getCoLat() {
  	return coLat;
  }
  
  /**
   * The coLon value is computed by the latest call to canonicalCoords.
   * 
   * @return Canonical geographic longitude of a trial point in degrees (0-360)
   */
  public double getCoLon() {
  	return coLon;
  }
  
  /**
   * The latIndex is computed by the latest call to getIndices.
   * 
   * @return Latitude index of a trial point
   */
  public int getLatIndex() {
  	return latIndex;
  }
  
  /**
   * The lonIndex is computed by the latest call to getIndices.
   * 
   * @return Longitude index of a trial point
   */
  public int getLonIndex() {
  	return lonIndex;
  }

  /**
   * Compute the Bayesian depth from the ZoneStats information.  Note that this 
   * returns the values from the nearest grid point.
   * 
   * @param latitude Geographic latitude in degrees
   * @param longitude Geographic longitude in degrees
   * @return A summary of the depth statistics
   */
  public BayesianDepth getBayesDepth(double latitude, double longitude) {
  	canonicalCoords(latitude, longitude);
  	getIndices();
  	return getBayesDepth(latIndex, lonIndex);
  }
  
	/**
	 * Compute the Bayesian depth from the ZoneStats information.  Note that this 
	 * returns the values from the nearest grid point.
	 * 
	 * @param latIndex The colatitude row index
	 * @param lonIndex The longitude row index
	 * @return A summary of the depth statistics
	 */
	protected abstract BayesianDepth getBayesDepth(int latIndex, int lonIndex);
	
	/**
	 * Compute an interpolated Bayesian depth from the ZoneStats information.
	 * 
	 * @param latitude Geographic latitude in degrees
	 * @param longitude Geographic longitude in degrees
	 * @return Bayesian depth estimate in kilometers
	 */
	public BayesianDepth interpolateBayesDepth(double latitude, double longitude) {
    // Instantiate the interpolation routines if necessary.
    if(interpolate == null) {
    	interpolate = new ZoneInterpolate();
    }
    // Do the interpolation.
    return interpolate.interpolateBayesDepth(latitude, longitude, this);
	}
  
	/**
	 * @return the depth source identification enumeration string
	 */
  protected abstract DepthSource getDepthSource();
	
	/**
	 * Convert coordinates from geographic latitude and longitude to geographic 
	 * colatitude and longitude in the range 0 to 360 degrees.  These coordinates are 
	 * remembered as coLat and coLon.
	 * 
	 * @param lat Geographic latitude in degrees
	 * @param lon Geographic longitude in degrees
	 */
	protected abstract void canonicalCoords(double lat, double lon);
  
	/**
	 * Compute the ZoneStats indices nearest the canonical geographic coordinates.  
	 * These indices are remembered as latIndex and lonIndex.
	 */
	protected void getIndices() {
		latIndex = newLatIndex(coLat);
		lonIndex = newLonIndex(latIndex, coLon);
	}
	
	/**
	 * Compute the latitude index of the nearest latitude row.
	 * 
	 * @param coLat Geographical colatitude in degrees
	 * @return Index of the nearest latitude row
	 */
	protected abstract int newLatIndex(double coLat);
	
	/**
	 * Compute the longitude index of the nearest longitude sample.
	 * 
	 * @param latIndex Index of the nearest latitude row
	 * @param coLon Geographic longitude in degrees (0-360)
	 * @return Index of the nearest longitude sample
	 */
	protected abstract int newLonIndex(int latIndex, double coLon);
	
	/**
	 * When looking for neighboring samples in longitude, it may be necessary to 
	 * wrap the longitude index.
	 * 
	 * @param latIndex Colatitude row index
	 * @param lonIndex Longitude column index
	 * @return Valid longitude column index
	 */
	protected abstract int wrapLonIndex(int latIndex, int lonIndex);
	
	/**
	 * Compute the coordinates for any pair of latitude/longitude indices.  Note 
	 * that for point models, these are the coordinates of the point, but for 
	 * cell models, these are the center point of the cell.  The indices and 
	 * coordinates are remembered as latIndex/lonIndex and coLat/coLon respectively.
	 * 
	 * @param latIndex Latitude index
	 * @param lonIndex Longitude index
	 */
	protected void getCoords(int latIndex, int lonIndex) {
		this.latIndex = latIndex;
		coLat = latFromIndex(latIndex);
		this.lonIndex = lonIndex;
		coLon = lonFromIndex(latIndex, lonIndex);
	}
	
	/**
	 * Compute the geographic colatitude of the indexed latitude row (i.e., the 
	 * colatitude of the samples in this row).
	 * 
	 * @param latIndex Index of a latitude row
	 * @return Geographic colatitude in degrees
	 */
	protected abstract double latFromIndex(int latIndex);
	
	/**
	 * Compute the geographic longitude of the indexed sample point.
	 * 
	 * @param latIndex Index of a latitude row
	 * @param lonIndex Index of a longitude sample
	 * @return Geographic longitude in degrees (0-360)
	 */
	protected abstract double lonFromIndex(int latIndex, int lonIndex);
}