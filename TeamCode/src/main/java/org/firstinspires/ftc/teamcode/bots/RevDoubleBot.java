package org.firstinspires.ftc.teamcode.bots;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Created by sjeltuhin on 9/12/17.
 */

public class RevDoubleBot {
    public DcMotor leftDriveBack = null;
    public DcMotor rightDriveBack = null;

    public DcMotor leftDriveFront = null;
    public DcMotor rightDriveFront = null;

    public DcMotor leftArmBase = null;
    public DcMotor rightArmBase = null;

    public DcMotor elbowMotor = null;


    public DcMotor lift = null;
    private double liftPos = 0;

    public Servo    leftClaw    = null;
    public Servo    rightClaw   = null;
    public Servo    jewelKicker   = null;
    public Servo    kickerTip   = null;

    public Servo    relicClaw    = null;

    private boolean clawShut = false;

    private ElapsedTime     runtime = new ElapsedTime();

    private static final double SERVO_START_VALUE = 0.9;
    private static final double KICKER_UP_VALUE = 0.9;
    private static final double KICKER_MID_VALUE = 0.5;
    private static final double KICKER_DOWN_VALUE = 0.19;

    private static final double KICKER_TIP_INIT = 1;
    private static final double KICKER_TIP_OPEN = 0.5;
    private static final double KICKER_TIP_SENSORSIDE = 0;

    private static final double LEFT_CLAW_START = SERVO_START_VALUE;
    private static final double RIGHT_CLAW_START = 0.2;


    private static final double LEFT_CLAW_SQUEEZE = 0.3;
    private static final double RIGHT_CLAW_SQUEEZE = 0.75;

    private static double [] liftStepDegrees = {70, 95, 105};
    private int currentStep = 0;
    private static final int LIFT_MAX_STEPS = 3;

    private double armBasePos = 0;
    private static final double ARM_RANGE = 180;

    private double elbowPos = 0;
    private static final double ELBOW_RANGE = 300;

    private double ANTI_GRAVITY_POWER = 0.02;

    private double LIFT_SPEED = 0.5;

    private static final double RELIC_CLAW_OPEN = 8;
    private static final double RELIC_CLAW_SHUT = 0;



    //REV

    static final double     COUNTS_PER_MOTOR_REV    = 288 ;    // Rev Core Hex motor
    static final double     DRIVE_GEAR_REDUCTION    = 1.0 ;     // This is < 1.0 if geared UP. was 2 in the sample
    static final double     WHEEL_DIAMETER_INCHES   = 4.05 ;     // For figuring circumference
    static final double     COUNTS_PER_INCH_REV     = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_INCHES * 3.1415);
    static final double     COUNTS_PER_DEGREE_REV    = COUNTS_PER_MOTOR_REV/360 ;

    //AndyMark
    static final double     COUNTS_PER_MOTOR_AM    = 718 ;    // eg: AndyMark Motor Encoder
    static final double     COUNTS_PER_DEGREE_AM    = COUNTS_PER_MOTOR_AM/360 ;

    static final double     COUNTS_PER_MOTOR_TQ    = 1440 ;    // eg: Torquenado Motor Encoder
    static final double     COUNTS_PER_DEGREE_TQ    = COUNTS_PER_MOTOR_TQ/360 ;


    /* local OpMode members. */
    HardwareMap hwMap           =  null;
    private ElapsedTime period  = new ElapsedTime();

    /* Constructor */
    public RevDoubleBot(){

    }

    /* Initialize standard Hardware interfaces */
    public void init(HardwareMap ahwMap) {
        // Save reference to Hardware map
        hwMap = ahwMap;

        // Define and Initialize Motors
        leftDriveBack  = hwMap.get(DcMotor.class, "left_drive_back");
        rightDriveBack = hwMap.get(DcMotor.class, "right_drive_back");
        leftDriveFront  = hwMap.get(DcMotor.class, "left_drive_front");
        rightDriveFront = hwMap.get(DcMotor.class, "right_drive_front");

        leftDriveBack.setDirection(DcMotor.Direction.REVERSE);
        rightDriveBack.setDirection(DcMotor.Direction.REVERSE);
        leftDriveFront.setDirection(DcMotor.Direction.FORWARD);
        rightDriveFront.setDirection(DcMotor.Direction.FORWARD);

        resetEncoders();

        leftDriveBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightDriveBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftDriveFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightDriveFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // Set all motors to zero power
        leftDriveBack.setPower(0);
        rightDriveBack.setPower(0);
        leftDriveFront.setPower(0);
        rightDriveFront.setPower(0);


        //lift
        lift = hwMap.get(DcMotor.class, "lift");
        lift.setDirection(DcMotor.Direction.REVERSE);
        lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        this.liftStop();

        //claws
        leftClaw  = hwMap.get(Servo.class, "left_claw");
        rightClaw = hwMap.get(Servo.class, "right_claw");
        this.leftClaw.scaleRange(0, 1);
        this.rightClaw.scaleRange(0, 1);

        this.leftClaw.setPosition(LEFT_CLAW_START);
        this.rightClaw.setPosition(RIGHT_CLAW_START);

        //jewelkicker
        jewelKicker = hwMap.get(Servo.class, "kicker");
        kickerTip = hwMap.get(Servo.class, "kicker_tip");
        this.jewelKicker.scaleRange(0, 1);
        this.jewelKicker.setPosition(KICKER_UP_VALUE);

        this.kickerTip.scaleRange(0, 1);
        initKickerTip();



        //relic arm
        leftArmBase = hwMap.get(DcMotor.class, "left_arm");
        rightArmBase = hwMap.get(DcMotor.class, "right_arm");


        leftArmBase.setDirection(DcMotor.Direction.FORWARD);
        rightArmBase.setDirection(DcMotor.Direction.FORWARD);


        leftArmBase.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightArmBase.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        leftArmBase.setPower(0);
        rightArmBase.setPower(0);

        //elbow
        elbowMotor = hwMap.get(DcMotor.class, "elbow");
        elbowMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        elbowMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        elbowMotor.setPower(0);


        relicClaw = hwMap.get(Servo.class, "relic_claw");
        relicClaw.scaleRange(0, 1);
        relicClaw.setPosition(RELIC_CLAW_OPEN);
    }

    protected void resetEncoders(){
        leftDriveBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightDriveBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftDriveFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightDriveFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    }

    public void initMode(DcMotor.RunMode mode){
        leftDriveBack.setMode(mode);
        rightDriveBack.setMode(mode);
        leftDriveFront.setMode(mode);
        rightDriveFront.setMode(mode);
    }

    public void stop (){
        this.leftDriveBack.setPower(0);
        this.rightDriveBack.setPower(0);
        this.leftDriveFront.setPower(0);
        this.rightDriveFront.setPower(0);
    }

    public void move(double drive, double turn){
        double rightPower    = Range.clip(drive + turn, -1.0, 1.0) ;
        double leftPower   = Range.clip(drive - turn, -1.0, 1.0) ;
        //use cubic modifier
        rightPower = rightPower*rightPower*rightPower;
        leftPower = leftPower*leftPower*leftPower;

        this.leftDriveBack.setPower(leftPower);
        this.rightDriveBack.setPower(rightPower);
        this.leftDriveFront.setPower(leftPower);
        this.rightDriveFront.setPower(rightPower);
    }


    public void strafeLeft(double speed){
        double power    = Range.clip(speed, -1.0, 1.0) ;
        this.leftDriveBack.setPower(power);
        this.rightDriveBack.setPower(-power);
        this.leftDriveFront.setPower(-power);
        this.rightDriveFront.setPower(power);
    }

    public void strafeRight(double speed){
        double power    = Range.clip(speed, -1.0, 1.0) ;
        this.leftDriveBack.setPower(-power);
        this.rightDriveBack.setPower(power);
        this.leftDriveFront.setPower(power);
        this.rightDriveFront.setPower(-power);
    }

    public void strafeLeftPos(double speed, double posInches){
        double power    = Range.clip(speed, -1.0, 1.0) ;
        this.leftDriveBack.setPower(power);
        this.rightDriveBack.setPower(-power);
        this.leftDriveFront.setPower(-power);
        this.rightDriveFront.setPower(power);
    }

    public void pivotLeft(double speed){
        this.leftDriveBack.setPower(-speed);
        this.rightDriveBack.setPower(speed);
        this.leftDriveFront.setPower(-speed);
        this.rightDriveFront.setPower(speed);
    }

    public void pivotRight(double speed){
        this.leftDriveBack.setPower(speed);
        this.rightDriveBack.setPower(-speed);
        this.leftDriveFront.setPower(speed);
        this.rightDriveFront.setPower(-speed);
    }

    public void turnLeft(double speed){
        this.leftDriveBack.setPower(0);
        this.rightDriveBack.setPower(speed);
        this.leftDriveFront.setPower(0);
        this.rightDriveFront.setPower(speed);
    }

    public void turnRight(double speed){
        this.leftDriveBack.setPower(speed);
        this.rightDriveBack.setPower(0);
        this.leftDriveFront.setPower(speed);
        this.rightDriveFront.setPower(0);
    }

    public void rotateLeftClaw(double position){
        double p = Range.clip(LEFT_CLAW_START - position, LEFT_CLAW_SQUEEZE, LEFT_CLAW_START);
        this.leftClaw.setPosition(p);
    }

    public void rotateRightClaw(double position){
        double p = Range.clip(RIGHT_CLAW_START + position, RIGHT_CLAW_START, RIGHT_CLAW_SQUEEZE);
        this.rightClaw.setPosition(p);
    }

    public void moveClaw(double position){
        this.rotateLeftClaw(position);
        this.rotateRightClaw(position);
    }

    public void sqeezeClaw(){
        this.leftClaw.setPosition(LEFT_CLAW_SQUEEZE);
        this.rightClaw.setPosition(RIGHT_CLAW_SQUEEZE);
    }

    public void openClaw(){
        this.leftClaw.setPosition(LEFT_CLAW_START);
        this.rightClaw.setPosition(RIGHT_CLAW_START);
    }

    public double liftUp(Telemetry telemetry){
        if (currentStep >= LIFT_MAX_STEPS){
            return liftPos;
        }

        double rotation  = liftStepDegrees[currentStep];
        currentStep++;
        return moveLift(LIFT_SPEED, rotation, telemetry);
    }

    public double liftDown(Telemetry telemetry){
        if (currentStep <= 0){
            return liftPos;
        }
        currentStep--;
        double rotation = liftStepDegrees[currentStep];
        return moveLift(LIFT_SPEED, -rotation, telemetry);
    }

    public void liftStop(){
        this.lift.setPower(0);
    }

    public double moveLift(double speed, double degrees, Telemetry telemetry){
        moveMotorDegrees(this.lift, speed, degrees, COUNTS_PER_DEGREE_TQ, telemetry);
        this.liftPos = degrees;
        return liftPos;
    }

    public void openRelicClaw(){
        this.relicClaw.setPosition(RELIC_CLAW_OPEN);
        this.clawShut = false;
    }

    public void closeRelicClaw(){
        this.relicClaw.setPosition(RELIC_CLAW_SHUT);
        this.clawShut = true;
    }

    public boolean isRelicClawShut(){
        return this.clawShut;
    }

    public void moveArm(double speed, double val, Telemetry telemetry){
        try{
            double degrees = val * ARM_RANGE;
            if (degrees > 0) {
                if (degrees > armBasePos && degrees <= ARM_RANGE) {
                    //opening
                    //do work
                    doArmWork(speed, degrees, telemetry);
                    armBasePos = degrees;
                } else {
                    return;
                }
            }
            else if (degrees < 0){
                if (degrees < armBasePos && degrees >= -ARM_RANGE){
                    //bring it down
                    speed = -speed;
                    //do work
                    doArmWork(speed, degrees, telemetry);
                    armBasePos = degrees;
                }
                else{
                    return;
                }
            }
            else if (degrees == 0){
                //reset
                armBasePos = 0;
            }

        }
        catch (Exception ex){
            telemetry.addData("Issues running with encoders to position", ex);
            telemetry.update();
        }
    }

    private void doArmWork(double speed, double degrees, Telemetry telemetry){
        int newLeftTarget = this.leftArmBase.getCurrentPosition() + (int) (degrees * COUNTS_PER_DEGREE_AM);
        //int newRightTarget = this.rightArmBase.getCurrentPosition() + (int) (degrees * COUNTS_PER_DEGREE_AM);
        this.leftArmBase.setTargetPosition(newLeftTarget);
        //this.rightArmBase.setTargetPosition(newRightTarget);

        this.leftArmBase.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        //this.rightArmBase.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        runtime.reset();

        this.leftArmBase.setPower(Math.abs(speed));
        this.rightArmBase.setPower(speed);

        boolean stop = false;
        while (!stop) {
            stop =  (!this.leftArmBase.isBusy());
            // Display it for the driver.
            telemetry.addData("Path1", "Running to %7d", newLeftTarget);
            telemetry.addData("Path2", "Arm pos: %7d",
                    this.leftArmBase.getCurrentPosition());
            telemetry.update();
        }
    }

    public void moveElbowMotor(double speed, double val, Telemetry telemetry){
        try{
            double degrees =  val * ELBOW_RANGE;
            if (degrees > 0) {
                if (degrees > elbowPos && degrees <= ELBOW_RANGE) {
                    //opening
                    //do work
                    moveMotorDegrees(elbowMotor, speed, degrees, COUNTS_PER_DEGREE_REV, telemetry);
                    elbowPos = degrees;
                } else {
                    return;
                }
            }
            else if (degrees < 0){
                if (degrees < elbowPos && degrees >= -ELBOW_RANGE){
                    //bring it down
                    speed = -speed;
                    //do work
                    moveMotorDegrees(elbowMotor, speed, degrees, COUNTS_PER_DEGREE_REV, telemetry);
                    elbowPos = degrees;
                }
                else{
                    return;
                }
            }
            else if (degrees == 0){
                //reset
                armBasePos = 0;
            }
        }
        catch (Exception ex){
            telemetry.addData("Issues running elbow", ex);
            telemetry.update();
        }
    }

    private void moveMotorDegrees(DcMotor motor, double speed, double degrees, double motorCounts, Telemetry telemetry){
        try{
            int newTarget = motor.getCurrentPosition() + (int) (degrees * motorCounts);
            motor.setTargetPosition(newTarget);

            motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            runtime.reset();
            motor.setPower(Math.abs(speed));

            boolean stop = false;
            while (!stop) {
                stop = !motor.isBusy();
                // Display it for the driver.
                telemetry.addData("Path1", "Running to %7d", newTarget);
                telemetry.addData("Path2", "Current: %7d",
                        motor.getCurrentPosition());
                telemetry.update();
            }
            motor.setPower(ANTI_GRAVITY_POWER);
        }
        catch (Exception ex){
            telemetry.addData("Issues running with encoders to position", ex);
            telemetry.update();
        }
    }


    public void dropKicker(){
        boolean stop = true;
        jewelKicker.setPosition(KICKER_MID_VALUE);
        while(stop){
            double pos = jewelKicker.getPosition();
            stop = pos > KICKER_MID_VALUE;
        }
        stop = true;
        openKickerTip();
        while (stop){
            double pos = kickerTip.getPosition();
            stop = pos > KICKER_TIP_OPEN;
        }

        jewelKicker.setPosition(KICKER_DOWN_VALUE);
    }

    public void dropKickerMidway(){
        jewelKicker.setPosition(KICKER_MID_VALUE);
    }

    public void openKickerTip(){
        kickerTip.setPosition(KICKER_TIP_OPEN);
    }

    public void initKickerTip(){
        kickerTip.setPosition(KICKER_TIP_INIT);
    }

    public void kickSensorSide(){
        kickerTip.setPosition(KICKER_TIP_SENSORSIDE);
    }

    public void kickEmptySide(){
        kickerTip.setPosition(KICKER_TIP_INIT);
    }

    public void liftKicker(){
        jewelKicker.setPosition(KICKER_UP_VALUE);
        kickerTip.setPosition(KICKER_TIP_INIT);
    }

    public void encoderDrive(double speed,
                             double leftInches, double rightInches,
                             double timeoutS, Telemetry telemetry) {

        try {
            // Determine new target position, and pass to motor controller
            int newLeftTarget = this.leftDriveBack.getCurrentPosition() + (int) (leftInches * COUNTS_PER_INCH_REV);
            int newRightTarget = this.rightDriveBack.getCurrentPosition() + (int) (rightInches * COUNTS_PER_INCH_REV);
            int newLeftFrontTarget = this.leftDriveFront.getCurrentPosition() + (int) (leftInches * COUNTS_PER_INCH_REV);
            int newRightFrontTarget = this.rightDriveFront.getCurrentPosition() + (int) (rightInches * COUNTS_PER_INCH_REV);

            this.leftDriveBack.setTargetPosition(newLeftTarget);
            this.rightDriveBack.setTargetPosition(newRightTarget);
            this.leftDriveFront.setTargetPosition(newLeftFrontTarget);
            this.rightDriveFront.setTargetPosition(newRightFrontTarget);


            // Turn On RUN_TO_POSITION
            leftDriveBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            rightDriveBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            leftDriveFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            leftDriveFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // reset the timeout time and start motion.
            runtime.reset();
            this.leftDriveBack.setPower(Math.abs(speed));
            this.rightDriveBack.setPower(Math.abs(speed));
            this.leftDriveFront.setPower(Math.abs(speed));
            this.rightDriveFront.setPower(Math.abs(speed));

            boolean stop = false;
            while (!stop) {
                boolean timeUp = timeoutS > 0 && runtime.seconds() >= timeoutS;
                stop = timeUp || (!this.leftDriveBack.isBusy() || !this.rightDriveBack.isBusy()
                || this.leftDriveFront.isBusy() || this.rightDriveFront.isBusy());
                // Display it for the driver.
                telemetry.addData("Path1", "Running to %7d :%7d", newLeftTarget, newRightTarget);
                telemetry.addData("Path2", "Back: %7d :%7d front: %7d :%7d",
                        this.leftDriveBack.getCurrentPosition(),
                        this.rightDriveBack.getCurrentPosition(),
                        this.leftDriveFront.getCurrentPosition(),
                        this.rightDriveFront.getCurrentPosition());
                telemetry.update();
            }

            // Stop all motion;
            this.stop();

            // Turn off RUN_TO_POSITION
            leftDriveBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            rightDriveBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            leftDriveFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            rightDriveFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
        catch (Exception ex){
            telemetry.addData("Issues running with encoders to position", ex);
            telemetry.update();
        }
    }

    public void encoderStrafe(double speed,
                             double distanceInches,
                             double timeoutS, Telemetry telemetry) {

        try {
            double val = Math.abs(distanceInches);
            int newLeftTarget = 0 ;
            int newRightTarget = 0;
            int newLeftFrontTarget = 0;
            int newRightFrontTarget = 0;
            if (distanceInches < 0){
                //going left
                newLeftTarget = this.leftDriveBack.getCurrentPosition() + (int) (val * COUNTS_PER_INCH_REV);
                newRightTarget = this.rightDriveBack.getCurrentPosition() - (int) (val * COUNTS_PER_INCH_REV);
                newLeftFrontTarget = this.leftDriveFront.getCurrentPosition() - (int) (val * COUNTS_PER_INCH_REV);
                newRightFrontTarget = this.rightDriveFront.getCurrentPosition() + (int) (val * COUNTS_PER_INCH_REV);
            }
            else{
                //going right
                newLeftTarget = this.leftDriveBack.getCurrentPosition() - (int) (val * COUNTS_PER_INCH_REV);
                newRightTarget = this.rightDriveBack.getCurrentPosition() + (int) (val * COUNTS_PER_INCH_REV);
                newLeftFrontTarget = this.leftDriveFront.getCurrentPosition() + (int) (val * COUNTS_PER_INCH_REV);
                newRightFrontTarget = this.rightDriveFront.getCurrentPosition() - (int) (val * COUNTS_PER_INCH_REV);
            }


            this.leftDriveBack.setTargetPosition(newLeftTarget);
            this.rightDriveBack.setTargetPosition(newRightTarget);
            this.leftDriveFront.setTargetPosition(newLeftFrontTarget);
            this.rightDriveFront.setTargetPosition(newRightFrontTarget);


            // Turn On RUN_TO_POSITION
            leftDriveBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            rightDriveBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            leftDriveFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            leftDriveFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // reset the timeout time and start motion.
            runtime.reset();
            this.leftDriveBack.setPower(Math.abs(speed));
            this.rightDriveBack.setPower(Math.abs(speed));
            this.leftDriveFront.setPower(Math.abs(speed));
            this.rightDriveFront.setPower(Math.abs(speed));

            boolean stop = false;
            while (!stop) {
                boolean timeUp = timeoutS > 0 && runtime.seconds() >= timeoutS;
                stop = timeUp || (!this.leftDriveBack.isBusy() || !this.rightDriveBack.isBusy()
                        || this.leftDriveFront.isBusy() || this.rightDriveFront.isBusy());
                // Display it for the driver.
                telemetry.addData("Path1", "Running to %7d :%7d", newLeftTarget, newRightTarget);
                telemetry.addData("Path2", "Back: %7d :%7d front: %7d :%7d",
                        this.leftDriveBack.getCurrentPosition(),
                        this.rightDriveBack.getCurrentPosition(),
                        this.leftDriveFront.getCurrentPosition(),
                        this.rightDriveFront.getCurrentPosition());
                telemetry.update();
            }

            // Stop all motion;
            this.stop();

            // Turn off RUN_TO_POSITION
            leftDriveBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            rightDriveBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            leftDriveFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            rightDriveFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
        catch (Exception ex){
            telemetry.addData("Issues running with encoders to position", ex);
            telemetry.update();
        }
    }

    public void encoderPivot(double speed,
                              double degrees,
                              double timeoutS, Telemetry telemetry) {

        try {
            double val = Math.abs(degrees);
            int newLeftTarget = 0 ;
            int newRightTarget = 0;
            int newLeftFrontTarget = 0;
            int newRightFrontTarget = 0;
            if (degrees < 0){
                //pivot left
                newLeftTarget = this.leftDriveBack.getCurrentPosition() - (int) (val * COUNTS_PER_DEGREE_REV);
                newRightTarget = this.rightDriveBack.getCurrentPosition() + (int) (val * COUNTS_PER_DEGREE_REV);
                newLeftFrontTarget = this.leftDriveFront.getCurrentPosition() - (int) (val * COUNTS_PER_DEGREE_REV);
                newRightFrontTarget = this.rightDriveFront.getCurrentPosition() + (int) (val * COUNTS_PER_DEGREE_REV);
            }
            else{
                //pivot right
                newLeftTarget = this.leftDriveBack.getCurrentPosition() + (int) (val * COUNTS_PER_DEGREE_REV);
                newRightTarget = this.rightDriveBack.getCurrentPosition() - (int) (val * COUNTS_PER_DEGREE_REV);
                newLeftFrontTarget = this.leftDriveFront.getCurrentPosition() + (int) (val * COUNTS_PER_DEGREE_REV);
                newRightFrontTarget = this.rightDriveFront.getCurrentPosition() - (int) (val * COUNTS_PER_DEGREE_REV);
            }


            this.leftDriveBack.setTargetPosition(newLeftTarget);
            this.rightDriveBack.setTargetPosition(newRightTarget);
            this.leftDriveFront.setTargetPosition(newLeftFrontTarget);
            this.rightDriveFront.setTargetPosition(newRightFrontTarget);


            // Turn On RUN_TO_POSITION
            leftDriveBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            rightDriveBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            leftDriveFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            leftDriveFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // reset the timeout time and start motion.
            runtime.reset();
            this.leftDriveBack.setPower(Math.abs(speed));
            this.rightDriveBack.setPower(Math.abs(speed));
            this.leftDriveFront.setPower(Math.abs(speed));
            this.rightDriveFront.setPower(Math.abs(speed));

            boolean stop = false;
            while (!stop) {
                boolean timeUp = timeoutS > 0 && runtime.seconds() >= timeoutS;
                stop = timeUp || (!this.leftDriveBack.isBusy() || !this.rightDriveBack.isBusy()
                        || this.leftDriveFront.isBusy() || this.rightDriveFront.isBusy());
                // Display it for the driver.
                telemetry.addData("Path1", "Running to %7d :%7d", newLeftTarget, newRightTarget);
                telemetry.addData("Path2", "Back: %7d :%7d front: %7d :%7d",
                        this.leftDriveBack.getCurrentPosition(),
                        this.rightDriveBack.getCurrentPosition(),
                        this.leftDriveFront.getCurrentPosition(),
                        this.rightDriveFront.getCurrentPosition());
                telemetry.update();
            }

            // Stop all motion;
            this.stop();

            // Turn off RUN_TO_POSITION
            leftDriveBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            rightDriveBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            leftDriveFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            rightDriveFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
        catch (Exception ex){
            telemetry.addData("Issues running with encoders to position", ex);
            telemetry.update();
        }
    }
}
