package gov.usgs.locator;

import gov.usgs.locaux.LocUtil;
import gov.usgs.processingformats.LocationException;
import gov.usgs.processingformats.LocationRequest;
import gov.usgs.processingformats.LocationResult;
import gov.usgs.processingformats.LocationService;
import gov.usgs.processingformats.Utility;
import gov.usgs.traveltime.TTSessionLocal;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class LocService implements LocationService {
	/** Temporary hack to test user selected slab resolutions. */
	private String slabRes = null;
	
  /** Class to manage the travel-time external files. */
  private TTSessionLocal ttLocal = null;
  
  /** Class to manage the locator external files. */
  private LocSessionLocal locLocal = null;

  /** Private logging object. */
  private static final Logger LOGGER = Logger.getLogger(LocService.class.getName());

  /**
   * The LocService constructor. Sets up the travel-times and locator external files.
   *
   * @param modelPath A String containing the earth model path to use
   * @throws gov.usgs.processingformats.LocationException Throws a LocationException upon certain
   *     severe errors. 
   */
  public LocService(String modelPath) throws LocationException {
    // init the tt models
    try {
      ttLocal = new TTSessionLocal(true, true, true, modelPath);
    } catch (IOException | ClassNotFoundException e) {
      LOGGER.severe("Unable to read travel-time auxiliary data.");
      e.printStackTrace();
      throw new LocationException("Unable to read travel-time auxiliary data.");
    }

    // Read the Locator auxiliary files.
    try {
      locLocal = new LocSessionLocal(modelPath);
    } catch (IOException | ClassNotFoundException e) {
      LOGGER.severe("Unable to read Locator auxiliary data.");
      e.printStackTrace();
      throw new LocationException("Unable to read Locator auxiliary data.");
    }
  }

  /**
   * Function to get a location using the provided input, implementing the location service
   * interface.
   *
   * @param request a Final LocationRequest containing the location request
   * @return A LocationResult containing the resulting location
   * @throws gov.usgs.processingformats.LocationException Throws a LocationException upon certain
   *     severe errors.
   */
  @Override
  public LocationResult getLocation(final LocationRequest request) throws LocationException {
    if (request == null) {
      LOGGER.severe("Null request.");
      throw new LocationException("Null request");
    }
    LocUtil.startLocationTimer();

    // always print request as json to log for debugging
    LOGGER.fine("JSON Request: \n" + Utility.toJSONString(request.toJSON()));

    // create locInput from LocationRequest
    LocInput in = new LocInput(request);

    LocationResult result = (LocationResult) getLocation(in);

    LOGGER.info(
        "Event: "
            + request.ID
            + ", "
            + LocUtil.endLocationTimer()
            + ", "
            + request.InputData.size()
            + " numData.");

    // always print result as json to log for debugging, if it is valid
    if (result != null) {
      LOGGER.fine("JSON Result: \n" + Utility.toJSONString(result.toJSON()));
    } else {
      LOGGER.severe("Null result.");
      throw new LocationException("Null Result");
    }

    return result;
  }

  /**
   * Function to get a location using the provided input.
   *
   * @param in a Final LocInput containing the location input
   * @return A LocOutput containing the resulting location output
   * @throws gov.usgs.processingformats.LocationException Throws a LocationException upon certain
   *     severe errors.
   */
  public LocOutput getLocation(final LocInput in) throws LocationException {
    // check to see if the input is valid
    if (!in.isValid()) {
      ArrayList<String> errorList = in.getErrors();

      // combine the errors into a single string
      String errorString = "";
      for (int i = 0; i < errorList.size(); i++) {
        errorString += " " + errorList.get(i);
      }

      LOGGER.severe("Invalid input: " + errorString);
      throw new LocationException("Invalid Input");
    }

    // make sure we have an earth model
    if (in.EarthModel == null) {
      in.EarthModel = "ak135";
    }

    // setup the event
    Event event = new Event(in.EarthModel);
    event.input(in);

    // print input for debugging
    LOGGER.info("Input: \n" + event.getHydraInput(false));
    
    // make sure we have a slab resolution
    if(slabRes == null) {
    	slabRes = "2spd";
    }

    // Get a locator with the required slab model resolution
    Locate loc = null;
		try {
			loc = locLocal.getLocate(event, ttLocal, slabRes);
		} catch (ClassNotFoundException | IOException e) {
      LOGGER.severe("Unable to read slab model data.");
      e.printStackTrace();
      throw new LocationException("Unable to read slab model data.");
		}

    // perform the location
    LocStatus status = loc.doLocation();
    event.setLocatorExitCode(status);

    // print results for debugging
    LOGGER.info("Results: \n" + event.getHydraOutput() + "\n" + event.getNEICOutput());

    // get the output
    LocOutput out = event.output();

    // check output
    if (!out.isValid()) {
      ArrayList<String> errorList = out.getErrors();

      // combine the errors into a single string
      String errorString = "";
      for (int i = 0; i < errorList.size(); i++) {
        errorString += " " + errorList.get(i);
      }

      LOGGER.severe("Invalid output: " + errorString);
    }

    // return the result
    return out;
  }
}
