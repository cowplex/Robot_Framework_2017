package org.usfirst.frc.team1504.robot;

import edu.wpi.first.wpilibj.DriverStation;

//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import edu.wpi.first.wpilibj.DriverStation;
//import java.util.Base64;

import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.hal.HAL;
import edu.wpi.first.wpilibj.hal.HALUtil;
import edu.wpi.first.wpilibj.hal.FRCNetComm.tInstances;
import edu.wpi.first.wpilibj.hal.FRCNetComm.tResourceType;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends RobotBase {
	
	private Update_Semaphore _semaphore = Update_Semaphore.getInstance();
	private Logger _logger = Logger.getInstance();
	private Autonomous _autonomous = Autonomous.getInstance();
	private Arduino _arduino = Arduino.getInstance();
	private Groundtruth _groundtruth = Groundtruth.getInstance();
	private Thread _dashboard_task;
	
    /**
     * Create a new Robot
     */
    public Robot() {
    	super();
    	Drive.initialize();
    	
    	DigitBoard.initialize();
    	Digit_Board.initialize();
    	Autonomous.initialize();
    	//Pneumatics.initialize();
   // 	CameraInterface.initialize();
    	Winch.initialize();
    	Shooter.initialize();
    	Autonomus_Setup.initialize();
    	
    	_arduino.setPartyMode(Arduino.PARTY_MODE.OFF);
		_arduino.setMainLightsColor(0, 0, 0);
		_arduino.setFrontsideLights(Arduino.FRONTSIDE_MODE.REVERSE);
		
		// Spin off a thread to blink the team number in morse code
		new Thread(new Runnable() {
			public void run() {
				Arduino arduino = Arduino.getInstance();
				boolean fifteenohfour[] = {false, false, false, false, false, true, true, true, true, true, false, false, false, false, true}; // .----.....-----....-
				int pos = 0;
				while(pos < fifteenohfour.length)
				{
					if(fifteenohfour[pos])
					{
						arduino.setMainLightsColor(0, 255, 0);
						Timer.delay(.3);
					}
			        else
			        {
			        	arduino.setMainLightsColor(0, 0, 255);
			        	Timer.delay(.1);
			        }
					
					arduino.setMainLightsColor(0, 0, 0);
					Timer.delay(.05);
					
					pos++;
				}
				arduino.setMainLightsColor(0, 0, 0);
			}
		}).start();
		
    	//System.out.println(new String(Base64.getDecoder().decode(Map.TEAM_BANNER)));
    }

    /**
     * Robot-wide initialization code should go here.
     *
     * Users should override this method for default Robot-wide initialization which will
     * be called when the robot is first powered on.
     *
     * Called exactly 1 time when the competition starts.
     */
    protected void robotInit() {
    	_dashboard_task = new Thread(new Runnable() {
			public void run() {
				PowerDistributionPanel pdp = new PowerDistributionPanel();
				char edge_track = 0;
				
				boolean warnings[] = {false, false};
				
				while(true)
				{
					SmartDashboard.putNumber("Robot Current", pdp.getTotalCurrent());
					SmartDashboard.putNumber("Robot PDP Temperature", pdp.getTemperature());
					SmartDashboard.putNumber("Robot Voltage", m_ds.getBatteryVoltage());
					SmartDashboard.putNumber("Robot Time", m_ds.getMatchTime());
					SmartDashboard.putNumber("Robot Thread Count", Thread.getAllStackTraces().keySet().size());
										
					// Get image from groundtruth sensor on rising edge of roboRIO User button
					edge_track = (char)( ( (edge_track << 1) + (HALUtil.getFPGAButton() ? 1 : 0) ) & 3);
					if(edge_track == 1) // Get image from groundtruth sensors, output it to the DS
					{
						SmartDashboard.putString("Groundtruth raw image", new String(_arduino.getSensorImage()));
						_groundtruth.setPosition(new double[] {0.0, 0.0, 0.0});
					}
					
					_groundtruth.dashboard_update();
					
					
					// Run warning lights
					if(m_ds.isOperatorControl() && m_ds.isEnabled() && m_ds.getMatchTime() >= 0)
					{
						if(m_ds.getMatchTime() < Map.ROBOT_WARNING_TIME_LONG && !warnings[0])
						{
							warnings[0] = true;
							_arduino.setPulseSpeed(5);
							_arduino.setPartyMode(Arduino.PARTY_MODE.ON);
						}
						else if(m_ds.getMatchTime() < Map.ROBOT_WARNING_TIME_SHORT && !warnings[1])
						{
							warnings[1] = true;
							_arduino.setPulseSpeed(20);
							_arduino.setPartyMode(Arduino.PARTY_MODE.ON);
							_arduino.setGearLights(Arduino.GEAR_MODE.PULSE);
						}
					}
					else if(warnings[0] || warnings[1])// if(m_ds.isDisabled())
					{
						_arduino.setPartyMode(Arduino.PARTY_MODE.OFF);
						_arduino.setMainLightsColor(0, 0, 0);
						warnings[0] = warnings[1] = false;
					}
					
					Timer.delay(.05);
				}
			}
		});
    	_dashboard_task.start();
    	
    	//System.out.println(new String(Base64.getDecoder().decode(Map.ROBOT_BANNER)));
    	Preferences prefs = Preferences.getInstance();
    	String name = prefs.getString("Robot Name", "UNNAMED ROBOT");
        System.out.println(name + " Initialized ( robotInit() ) @ " + IO.ROBOT_START_TIME);
    }

    /**
     * Disabled should go here.
     * Users should overload this method to run code that should run while the field is
     * disabled.
     *
     * Called once each time the robot enters the disabled state.
     */
    protected void disabled() {
        System.out.println("Robot Disabled");
        
        _arduino.setPartyMode(Arduino.PARTY_MODE.ON);
        _arduino.setGearLights(Arduino.GEAR_MODE.PULSE);
        _arduino.setPulseSpeed(1);
    }

    /**
     * Autonomous should go here.
     * Users should add autonomous code to this method that should run while the field is
     * in the autonomous period.
     *
     * Called once each time the robot enters the autonomous state.
     */
    public void autonomous() {
    	System.out.println("Autonomous mode");
    }

    /**
     * Operator control (tele-operated) code should go here.
     * Users should add Operator Control code to this method that should run while the field is
     * in the Operator Control (tele-operated) period.
     *
     * Called once each time the robot enters the operator-controlled state.
     */
    public void operatorControl() {
    	System.out.println("Operator Control");
    	_arduino.setPartyMode(Arduino.PARTY_MODE.OFF);
    	_arduino.setMainLightsColor(0, 0, 0);
    	Timer.delay(.01);
        _arduino.setGearLights(Arduino.GEAR_MODE.INDIVIDUAL_INTENSITY, 90, 90);
        if(m_ds.getAlliance() == DriverStation.Alliance.Blue)
        	_arduino.setMainLightsColor(0, 255, 0);
        else
        	_arduino.setMainLightsColor(0, 0, 255);
    }

    /**
     * Test code should go here.
     * Users should add test code to this method that should run while the robot is in test mode.
     */
    public void test()
    {
    	System.out.println("Test Mode!");
    	while(isTest() && isEnabled())
    		System.out.println(IO.drive_input()[0]);
//    	CameraInterface ci = CameraInterface.getInstance();
//    	//ci.set_mode(CameraInterface.CAMERA_MODE.MULTI);
//    	//ci.set_mode(CameraInterface.CAMERA_MODE.SINGLE);
//    	while (isTest() && isEnabled())
//    	{
//    		// Switch camera views every 5 seconds like a pro
//    		ci.set_active_camera(ci.get_active_camera() == CameraInterface.CAMERAS.GEARSIDE ? CameraInterface.CAMERAS.INTAKESIDE : CameraInterface.CAMERAS.GEARSIDE);
//            System.out.println("Switching active camera to " + ci.get_active_camera().toString());
//            Timer.delay(5);
//    	}
    }

    /**
     * Start a competition.
     * This code tracks the order of the field starting to ensure that everything happens
     * in the right order. Repeatedly run the correct method, either Autonomous or OperatorControl
     * when the robot is enabled. After running the correct method, wait for some state to change,
     * either the other mode starts or the robot is disabled. Then go back and wait for the robot
     * to be enabled again.
     */
    public void startCompetition() {
        HAL.report(tResourceType.kResourceType_Framework,tInstances.kFramework_Simple);

        // first and one-time initialization
        LiveWindow.setEnabled(false);
        robotInit();

        HAL.observeUserProgramStarting();
        while (true) {
            if (isDisabled()) {
                m_ds.InDisabled(true);
                disabled();
                while (isDisabled())
                    Timer.delay(0.01);
                m_ds.InDisabled(false);
            } else if (isAutonomous()) {
                m_ds.InAutonomous(true);
                _logger.start("Auto");
                
                autonomous();
                
                // Zero groundtruth reading on start
                //_groundtruth.setPosition(new double[] {0.0, 0.0, 0.0});
                
                //if(Autonomus_Setup.initialized())
                	_autonomous.setup_path(Autonomus_Setup.getInstance().get_path());
                
                _autonomous.start();
                
                while (isAutonomous() && !isDisabled()) {
                	m_ds.waitForData(0.150);
                	_semaphore.newData();
                }
                
                _autonomous.stop();
                
                _logger.stop();
                m_ds.InAutonomous(false);
            
            } else if (isTest()) {
                //LiveWindow.setEnabled(true);
                m_ds.InTest(true);
                
                test();
                
                while (isTest() && isEnabled())
                    Timer.delay(0.01);
                
                m_ds.InTest(false);
                //LiveWindow.setEnabled(false);
            
            } else {
                m_ds.InOperatorControl(true);
                _logger.start("Tele");
                
                Drive.getInstance().setFrontAngle(0);
                
                operatorControl();
                
                while (isOperatorControl() && !isDisabled()) {
                	m_ds.waitForData(0.150); // Blocks until we get new datas or 150ms elapse
                	_semaphore.newData();
                    //Timer.delay(0.01);
                }
                
                _logger.stop();
                m_ds.InOperatorControl(false);
            }
        } /* while loop */
    }
}
