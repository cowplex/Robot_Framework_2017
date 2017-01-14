package org.usfirst.frc.team1504.robot;

import edu.wpi.first.wpilibj.vision.*;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;

import org.opencv.core.Mat;

import edu.wpi.cscore.*;
import edu.wpi.first.wpilibj.CameraServer;

public class Vision implements VisionRunner.Listener<GripPipelineee>{
	
	UsbCamera _usb = new UsbCamera("camera", 1); //or path
	private static final Vision _instance = new Vision();
	private GripPipelineee _pipe = new GripPipelineee();
	private VisionThread _thread = new VisionThread(_usb, _pipe, this);
	private ADXRS450_Gyro _gyro = new ADXRS450_Gyro();
	public double _target = 0.0;
	private enum AimState {WAIT_FOR_IMAGE_GOOD, GET_IMAGE, AIM_ROBOT, AIMED, BAD_IMAGE}
	public AimState _state;

	private Vision() //(int port)
	{
		System.out.println("Vision initialized");
		getImage(_usb); //, port);
	}
	
	public static Vision getInstance()
	{
		return _instance;
	}
	
	public void settle_camera()
	{
		_state = AimState.WAIT_FOR_IMAGE_GOOD;
		
		//_gyro.reset();
		_state = AimState.GET_IMAGE;
		update();
	}
	
	public void setParams(double hue1, double hue2, double sat1, double sat2, double val1, double val2, double circ1, double circ2)
	{
		_pipe._hue1 = hue1;
		_pipe._hue2 = hue2;
		_pipe._sat1 = sat1;
		_pipe._sat2 = sat2;
		_pipe._val1 = val1;
		_pipe._val2 = val2;
		_pipe._circ1 = circ1;
		_pipe._circ2 = circ2;

	}
	
	public void getImage(UsbCamera usb) { //, int port) { 

		usb = _usb;
        _usb = CameraServer.getInstance().startAutomaticCapture(0); //port);

	}
	
	private double offset_aim_factor()
	{
		return _target - _gyro.getAngle(); // offset
	}
	
	public void checkAim()
	{
		if(offset_aim_factor() < Map.VISION_INTERFACE_AIM_DEADZONE)
		{
			_state = AimState.AIMED;
		}
		
		else
		{
			_state = AimState.AIM_ROBOT;
		}
	}
	
	public void update()
	{
		double[] area = _pipe._output[4];
		double[] position = _pipe._output[0];
		
		if(area.length == 0)
		{
			_state = AimState.BAD_IMAGE;
			return;
		}
		
		int largest = 0;
		for(int i = 0; i < area.length; i++)
		{
			if(area[i] < area[largest])
			{
				largest = i;
			}
		}
		
		_target = largest;
		_target = (2 * position[largest] / Map.VISION_INTERFACE_VIDEO_WIDTH) - 1;
		_target *= Map.VISION_INTERFACE_VIDEO_FOV / -2.0;
		
		checkAim();
			
	}
	
	public double getInputCorrection(boolean first_aim)
	{
		System.out.println(offset_aim_factor() + " - " + _state.toString());
		if(first_aim)			
			settle_camera();
		
		if(_state == AimState.AIM_ROBOT)
		{
			// Compute the speed we need to turn the robot to point at the target
			if(Math.abs(offset_aim_factor()) > Map.VISION_INTERFACE_AIM_DEADZONE)
			{
				return  0.31 * Math.signum(offset_aim_factor()); 
				
			}
			else
				settle_camera();
		}
		
		return 0.0;
	}

	@Override
	public void copyPipelineOutputs(GripPipelineee pipeline) {
		// TODO Auto-generated method stub
		
	}

}