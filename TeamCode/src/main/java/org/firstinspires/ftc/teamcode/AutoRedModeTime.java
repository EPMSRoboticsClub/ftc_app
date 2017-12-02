/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.bots.BasicBotConfig;
import org.firstinspires.ftc.teamcode.bots.RevSingleBot;
import org.firstinspires.ftc.teamcode.bots.RobotDirection;
import org.firstinspires.ftc.teamcode.gamefield.GameStats;
import org.firstinspires.ftc.teamcode.skills.ColorCracker;
import org.firstinspires.ftc.teamcode.skills.CryptoColumn;
import org.firstinspires.ftc.teamcode.skills.DetectedColor;
import org.firstinspires.ftc.teamcode.skills.ImageRecognition;


@Autonomous(name="AutoRed Time", group ="Robot9160")
//@Disabled
public class AutoRedModeTime extends LinearOpMode {

    private boolean foundVuMark = false;

    RevSingleBot robot = new RevSingleBot();   // Use our standard robot configuration
    private ElapsedTime runtime = new ElapsedTime();
    ImageRecognition imageRecognition = new ImageRecognition();
    private static double TIME_CUT_OFF = 5.0;  //stop recognition at 6 sec. Then just guess.
    private static float COLOR_CUT_OFF = 5;  //stop color detection at 5 sec.
    private ColorCracker jewelHunter = new ColorCracker();
    DetectedColor dc = DetectedColor.NONE;
    static final double     DRIVE_SPEED             = 0.5;

    @Override public void runOpMode() {
        robot.init(hardwareMap);
        jewelHunter.init(hardwareMap);

        imageRecognition.init(hardwareMap);

        telemetry.addData(">", "Press Play to start");
        telemetry.update();
        waitForStart();

        //do image recognition
        imageRecognition.start();
        runtime.reset();

        CryptoColumn result = CryptoColumn.None;

        grabGlyph();
        boolean stop = false;

        while (opModeIsActive() && !stop) {
            stop = (runtime.seconds() >= TIME_CUT_OFF || foundVuMark);
            result = imageRecognition.track(telemetry);
            if (result != CryptoColumn.None){
                foundVuMark = true;
                break;
            }
        }
        if (opModeIsActive()) {
            proceed(result);
            sleep(1000);
            complete();
        }
    }

    protected void grabGlyph(){
        robot.sqeezeClaw();
        robot.liftUp();
    }


    protected void proceed(CryptoColumn column){
        telemetry.addData("Auto", "Proceeding to column %s", column.name());
        int diff = kickJewel();
        switch (column){
            case Right:
                moveToRight(diff);
                break;
            case Center:
                moveToCenter(diff);
                break;
            case Left:
                moveToLeft(diff);
                break;
            default:
                //let's go somewhere
                moveToCenter(diff);
                break;
        }
    }

    protected void complete(){
        if (opModeIsActive()) {
            //turn right
            robot.driveByTime(DRIVE_SPEED, RobotDirection.Right, 1.2, telemetry);
            sleep(1000);
            robot.liftDown();
            sleep(3000);
            robot.openClaw();
            sleep(1000);
            //push forward
            robot.driveByTime(DRIVE_SPEED, RobotDirection.Straight, 0.8, telemetry);
            sleep(500);
            robot.driveByTime(-DRIVE_SPEED, RobotDirection.Straight, 0.8, telemetry);
        }
    }

    protected int kickJewel(){
        int diff = 0;
        telemetry.addData("Status", "Kicking the jewel");
        telemetry.update();
        //drop kicker
        robot.dropKicker();
        sleep(1000);
        try {
            runtime.reset();
            this.dc = jewelHunter.detectColor(telemetry, COLOR_CUT_OFF);
            sleep(1000);
            switch (this.dc){
                case RED:
                    //move forward 2 inches and lift the sensor arm
                    diff = 5;
                    robot.driveByTime(DRIVE_SPEED, RobotDirection.Straight, 0.8, telemetry);
                    break;
                case BLUE:
                    diff = -5;
                    //move back 2 inches and lift the arm
                    robot.driveByTime(-DRIVE_SPEED, RobotDirection.Straight, 0.8, telemetry);
                    break;
                default:
                    break;
            }

            telemetry.addData("Auto", "Color = %s", this.dc.name());
            telemetry.addData("Auto", "Diff to move = %d", diff);
            telemetry.update();
        }
        catch (Exception ex){
            telemetry.addData("Issues with color sensor", ex);
            telemetry.update();
        }
        finally {
            sleep(1000);
            robot.liftKicker();
            sleep(1000);
        }
        return diff;
    }

    //move to place the ball

    protected void moveToRight(int diff){
        telemetry.addData("Auto", "I am going to the right column");
        telemetry.update();
        robot.driveByTime(DRIVE_SPEED, RobotDirection.Straight, 1.9, telemetry);
    }

    protected void moveToLeft(int diff){
        telemetry.addData("VuMark", "I am going to the left cell");
        telemetry.update();
        robot.driveByTime(DRIVE_SPEED, RobotDirection.Straight, 2.0, telemetry);
    }

    protected void moveToCenter(int diff){
        telemetry.update();
        telemetry.addData("VuMark", "I am going to the center cell");
        robot.driveByTime(DRIVE_SPEED, RobotDirection.Straight, 1.8, telemetry);
    }
}
