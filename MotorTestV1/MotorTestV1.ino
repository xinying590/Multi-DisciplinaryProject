#include <DualVNH5019MotorShield.h>
#include <EnableInterrupt.h>
#include <PID_v1.h>

#define LEFT_ENCODER 3 //left motor encoder A to pin 5
#define RIGHT_ENCODER 11 //right motor encoder A to pin 13

DualVNH5019MotorShield md;

double leftEncoderValue = 0;
double rightEncoderValue = 0;
double startLeftEncoderValue, startRightEncoderValue; 
double difference;                // Use to find the difference
double Setpoint, Input, Output;
double TURN_L = 4.71, TURN_R = 4.52;
int x = 0;

/* ================================= Notes ================================= */
// 562.25 pulse = 1 revolution 
// Diameter = 6cm
// Circumference = 18.8495559215cm
// For 90 degrees turn = 492 ticks.
// Diameter = 21 cm (ROBOT)
// Circumference = 65.9734457254

/* ================================== PID ================================== */
//double Kp=1.7, Ki=0, Kd=0;
PID straightPID(&leftEncoderValue, &Output, &rightEncoderValue, 2.5, 1.2, 0.0, DIRECT);
//PID singleUnitPID(&leftEncoderValue, &Output, &rightEncoderValue, 11.5, 1.0, 0.0, DIRECT);
PID leftPID(&leftEncoderValue, &Output, &rightEncoderValue, 0.0, 0.0, 0.0, DIRECT);
PID rightPID(&leftEncoderValue, &Output, &rightEncoderValue, 2, 0.8, 0.0, DIRECT);
//PID(&input, &output, &setpoint, Kp, Ki, Kd, Direction)
//Parameters: Input - the variable we are trying to control
//          : Output - the variable that will be adjusted by PID
//          : Setpoint - the value we want the input to maintain
//          : Direction - either DIRECT or REVERSE

/* ============================Distance to Ticks ============================= */
double distToTicks(double dist){
  double circumference = 18.85;
  double pulse = 562.25;
  double oneCM = circumference / pulse;
  double ticks = dist / oneCM;
  return ticks;
}
/* ====================== Rotation Ticks Left ============================= */
double rotationTicksLeft(double angle){
 // double degree = 90;
  //double ticks = 450;
  double perDegree = 4.35; 
  return angle*perDegree;
}

/* ====================== Rotation Ticks Right ============================= */
double rotationTicksRight(double angle){
  double degree = 90;
 // double ticks = 415;
  double perDegree = 4.51;
  return angle*perDegree;
  //if turn too much decrease the value if turn too little increase the value 
}

/* =============================== Go Straight ============================= */
void goStraight(double ticks){
   md.setSpeeds(-(250),250-Output);
   straightPID.Compute();  
    Serial.print("Left:"); 
    Serial.print(leftEncoderValue);
    Serial.print(", Right:");
    Serial.print(rightEncoderValue);
    Serial.print(", Diff:");
    Serial.println(Output);
   if((leftEncoderValue > ticks) || (rightEncoderValue > ticks)) {     
        md.setBrakes(400,400);
        delay(5000);

      }
}

/* ========================= Rotation Ticks ================================ */
void turnLeft(double ticks){
  leftEncoderRes();
  rightEncoderRes();
  while((leftEncoderValue < ticks) || (rightEncoderValue < ticks)){
     md.setSpeeds((250),-(250+Output));
     leftPID.Compute();
    Serial.print("Left:"); 
    Serial.print(leftEncoderValue);
    Serial.print(", Right:");
    Serial.print(rightEncoderValue);
    Serial.print(", Diff:");
    Serial.println(Output);
  }
  md.setBrakes(400,400);
  delay(5000);
  rightEncoderRes();
  leftEncoderRes();
}

void turnRight(double ticks){
  leftEncoderRes();
  rightEncoderRes();
  while((leftEncoderValue < ticks) || (rightEncoderValue < ticks)){
     md.setSpeeds(-(250),(250+Output));
     rightPID.Compute();
  }
  md.setBrakes(400,400);
  delay(100);
  rightEncoderRes();
  leftEncoderRes();
}


/* ================================= Setup ================================= */
void setup() {
  Serial.begin(115200);
  md.init();
  pinMode (LEFT_ENCODER, INPUT); //set digital pin 3 as input
  pinMode (RIGHT_ENCODER, INPUT); //set digital pin 11 as input
  enableInterrupt(LEFT_ENCODER, leftEncoderInc, RISING);  // Reading the Encoder
  enableInterrupt(RIGHT_ENCODER, rightEncoderInc, RISING);// Reading the Encoder
  leftPID.SetOutputLimits(-50, 50);
  leftPID.SetMode(AUTOMATIC);
  leftPID.SetSampleTime(5);
  straightPID.SetOutputLimits(-50,50);
  straightPID.SetMode(AUTOMATIC);
  rightPID.SetOutputLimits(-50,50);
  rightPID.SetMode(AUTOMATIC);
}

void loop(){
  double forwardTicks = distToTicks(150);
 goStraight(forwardTicks);
  
//  left for 50 degree 
  
 // double leftTicks = rotationTicksLeft(1080);
  //turnLeft(leftTicks);
  /*right for 90 degree
   double rightTicks = rotationTicksRight(90);
   turnRight(rightTicks);
   */
}

/* ============================ Encoder Counter ============================ */
void leftEncoderInc(void){
  leftEncoderValue++;
  }
void rightEncoderInc(void){
  rightEncoderValue++;
  }
void leftEncoderRes(void){
  leftEncoderValue = 0;
}
void rightEncoderRes(void){
  rightEncoderValue = 0;
}
