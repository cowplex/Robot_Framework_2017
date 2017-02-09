package org.usfirst.frc.team1504.robot;

public class IO
{
	private static Latch_Joystick _drive_forward = new Latch_Joystick(Map.DRIVE_CARTESIAN_JOYSTICK);
	private static Latch_Joystick _drive_rotation = new Latch_Joystick(Map.DRIVE_POLAR_JOYSTICK);
	
	private static Latch_Joystick _operator_joystick = new Latch_Joystick(Map.OPERATOR_JOYSTICK);
	
	public static final long ROBOT_START_TIME = System.currentTimeMillis();
	
	public static boolean drive_override()
	{
		return _drive_forward.getRawButtonLatch(Map.UTIL_DRIVE_OVERRIDE_BUTTON);
	}
	public static boolean operator_override()
	{
		return _operator_joystick.getRawButtonLatch(Map.UTIL_OPERATOR_OVERRIDE_BUTTON);
	}
	
	/**
	 * Drive stuff
	 */
	
	/**
	 * Handle getting joystick values
	 * @return
	 */
	public static double[] drive_input() {
		double[] inputs = new double[3];

		inputs[0] = Map.DRIVE_INPUT_MAGIC_NUMBERS[0] * Math.pow(Utils.deadzone(_drive_forward.getRawAxis(Map.JOYSTICK_Y_AXIS)), 2) * Math.signum(_drive_forward.getRawAxis(Map.JOYSTICK_Y_AXIS));// y
		inputs[1] = Map.DRIVE_INPUT_MAGIC_NUMBERS[1] * Math.pow(Utils.deadzone(_drive_forward.getRawAxis(Map.JOYSTICK_X_AXIS)), 2) * Math.signum(_drive_forward.getRawAxis(Map.JOYSTICK_X_AXIS));//x
		inputs[2] = Map.DRIVE_INPUT_MAGIC_NUMBERS[2] * Math.pow(Utils.deadzone(_drive_rotation.getRawAxis(Map.JOYSTICK_X_AXIS)), 2) * Math.signum(_drive_rotation.getRawAxis(Map.JOYSTICK_X_AXIS));//w
		return inputs;
	}
	
	public static double drive_wiggle()
	{
		return (_drive_rotation.getRawButton(4) ? -1.0 : 0.0) + (_drive_rotation.getRawButton(5) ? 1.0 : 0.0);
	}

/**
 * Shooter Stuff
 */
	public static boolean shooter_enable()
	{
		return _operator_joystick.getRawButton(Map.SHOOTER_FIRE_BUTTON);
	}
	
/**
 * Winch stuff
 */
	
	public static double winch_input()
	{
		return Utils.deadzone(Math.abs(_operator_joystick.getRawAxis(Map.WINCH_POWER_AXIS))) * Map.WINCH_DIRECTION;
	}
}
