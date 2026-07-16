package es.upm.grise.cruiseControl;
//Hay que importar las clases de excepciones si no, el codigo no compila
import es.upm.grise.cruiseControl.exceptions.CannotSetSpeedLimitException;
import es.upm.grise.cruiseControl.exceptions.IncorrectSpeedLimitException;
import es.upm.grise.cruiseControl.exceptions.IncorrectSpeedSetException;
import es.upm.grise.cruiseControl.exceptions.SpeedSetAboveSpeedLimitException;

public class CruiseControl {

	private RoadInformation roadInformation;
	private Speedometer speedometer;
	private Integer speedLimit;
	private Integer speedSet;
	private boolean enabled = false;

	/*
	 * Constructor
	 */

	public CruiseControl(RoadInformation roadInformation, Speedometer speedometer) {
		this.roadInformation = roadInformation;
		this.speedometer = speedometer;
	}

	/*
	 * Method to code/test
	 */

	public void setSpeedSet(int speedSet) throws IncorrectSpeedSetException, SpeedSetAboveSpeedLimitException {

		// SpeedSet debe ser mayor que cero.
		if (speedSet <= 0)
			throw new IncorrectSpeedSetException();

		// Si el conductor ya fijo speedLimit, speedSet no puede ser mas grande que speedLimit.
		if (speedLimit != null && speedSet > speedLimit)
			throw new SpeedSetAboveSpeedLimitException();

		// Aqui ya la velocidad la controla CruiseControl.
		this.speedSet = speedSet;
		this.enabled = true;
	}

	/*
	 * Method to code/test
	 */

	public void setSpeedLimit(int speedLimit) throws IncorrectSpeedLimitException, CannotSetSpeedLimitException {

		// SpeedLimit debe ser mayor que cero.
		if (speedLimit <= 0)
			throw new IncorrectSpeedLimitException();

		// Si speedSet ya fue dicho, no se puede fijar speedLimit.
		if (speedSet != null)
			throw new CannotSetSpeedLimitException();

		this.speedLimit = speedLimit;
	}

	/*
	 * Method to code/test
	 */

	public void disable() {
		//Se desactiva y speedSet vuelve otra vez es null.
		this.enabled = false;
		this.speedSet = null;
	}

	/*
	 * Method to code/test
	 */

	public Response nextCommand() {

		// Este metodo no existia entonces lo invoca EngineController.
		Response response = new Response();

		// Si la velocidad no se controla
		if (speedSet == null || !enabled) {
			response.command = Command.IDLE;
			return response;
		}

		int currentSpeed = speedometer.getCurrentSpeed();

		// Reglas

		// 1) Si la velocidad actual supera speedSet entonces REDUCE.
		if (currentSpeed > speedSet)
			response.command = Command.REDUCE;

		// 2) Si la velocidad actual es inferior al minimo de la via entonces INCREASE ( para evitae infraccion).
		if (currentSpeed < roadInformation.getMinSpeed())
			response.command = Command.INCREASE;

		// 3) Si la velocidad actual es inferior a speedSet entonces INCREASE.
		if (currentSpeed < speedSet)
			response.command = Command.INCREASE;

		// 4) Si la velocidad actual es superior al maximo de la via entonces REDUCE (prevalece sobre INCREASE).
		if (currentSpeed > roadInformation.getMaxSpeed())
			response.command = Command.REDUCE;

		// 5) Si la velocidad actual es igual a speedSet entonces KEEP.
		if (currentSpeed == speedSet)
			response.command = Command.KEEP;

		return response;
	}

	/*
	 * Others getters and setters
	 */

	public boolean isEnabled() {
		return enabled;
	}

	public Integer getSpeedLimit() {
		return speedLimit;
	}

	public Integer getSpeedSet() {
		return speedSet;
	}

}
