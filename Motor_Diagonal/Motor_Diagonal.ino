
//checklist part 2 & 3
#include <DualVNH5019MotorShield.h>
#include <EnableInterrupt.h>
#include <PID_v1.h>
#include <SharpIR.h>


#define LEFT_ENCODER 3 //left motor encoder A to pin 5
#define RIGHT_ENCODER 11 //right motor encoder A to pin 13

/* ===================== Definition/Variables for Sensor ========================== */

#define s0 A0;  // Right Back Sensor  
#define s1 A1;  // Right First Sensor
#define s2 A2;  // Front right sensor
#define s3 A3;  // Front Center
#define s4 A4   // Front Left Sensor
#define s5 A5;  // Long range

//#define ir A0
//#define model 1080 
DualVNH5019MotorShield md;
//SharpIR SharpIR(ir,model);

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
PID rightPID(&leftEncoderValue, &Output, &rightEncoderValue, 0, 0, 0.0, DIRECT);
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
  double ticks = dist / oneCM;  //dis/0.0335
  return ticks;     //No. of interupt for the process 
}
/* ====================== Rotation Ticks Left ============================= */
double rotationTicksLeft(double angle){
 // double degree = 90;
  //double ticks = 450;
  double perDegree = 4.27; 
  return angle*perDegree;
}

/* ====================== Rotation Ticks Right ============================= */
double rotationTicksRight(double angle){
 // double degree = 90;
 // double ticks = 415;
  double perDegree = 4.44;
  return angle*perDegree;
  //if turn too much decrease the value if turn too little increase the value 
}

/* =============================== Go Straight ============================= */
void goStraight(double ticks){
  leftEncoderRes();
  rightEncoderRes(); 
  while ((leftEncoderValue < ticks) || (rightEncoderValue < ticks)) {     
       md.setSpeeds(-(250),250-Output);
       straightPID.Compute(); 
   }
   md.setBrakes(400,400);
   delay(5000);
   rightEncoderRes();
   leftEncoderRes();   
      
}

/* ========================= Rotation Ticks ================================ */
void turnLeft(double ticks){
  leftEncoderRes();
  rightEncoderRes();
  while((leftEncoderValue < ticks) || (rightEncoderValue < ticks)){
     md.setSpeeds(-(250),-(250-Output));
     leftPID.Compute();
  }
  md.setBrakes(400,400);
  delay(500);
  rightEncoderRes();
  leftEncoderRes();
}

void turnRight(double ticks){
  leftEncoderRes();
  rightEncoderRes();
  while((leftEncoderValue < ticks) || (rightEncoderValue < ticks)){
     md.setSpeeds((250),(250-Output));
     rightPID.Compute();
  }
  md.setBrakes(400,400);
  delay(500);
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
   /* ======================== For Sensor ============================= */
  pinMode(A0, INPUT);
  pinMode(A1, INPUT);
  pinMode(A2, INPUT);
  pinMode(A3, INPUT); 
  pinMode(A4, INPUT);
  pinMode(A5, INPUT);
}

void loop(){
 /* double forwardTicks;
  double rightTicks;
  double leftTicks;
  int S0 = distanceS0();
  while(!(S0 >= 10 && S0<=12)){
    md.setSpeeds((250),250-Output);
    straightPID.Compute();
    S0=distanceS0();
  }
    Serial.println("End");
    md.setBrakes(400,400);
    rightTicks = rotationTicksRight(45);
    turnRight(rightTicks);
    Serial.println("Hi0");
    forwardTicks = distToTicks(42);
    goStraight(forwardTicks);
    leftTicks = rotationTicksLeft(90);
    turnLeft(leftTicks);
    Serial.println("Hi1");
    forwardTicks = distToTicks(51);
    goStraight(forwardTicks);
    rightTicks = rotationTicksRight(30);
    turnRight(rightTicks);
    Serial.println("Hi2");
   // forwardTicks = distToTicks(30);
   // goStraight(forwardTicks); 
   
    md.setBrakes(400,400);
    delay(10000);*/
   // double forwardTicks = distToTicks(150);
   // goStraight(forwardTicks);
    // left for 50 degree 
   double leftTicks = rotationTicksLeft(90);
   turnLeft(leftTicks);
  
  // right for 90 degree
   //double rightTicks = rotationTicksRight(90);
   //turnRight(rightTicks);
  }
  
   
    
 
 
  
// left for 50 degree 
// double leftTicks = rotationTicksLeft(1080);
// turnLeft(leftTicks);
  
  // right for 90 degree
  // double rightTicks = rotationTicksRight(1080);
  // turnRight(rightTicks);
  



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

/* ======================= Getting RAW Sensor Value ======================= */
int getRaw(float pin){
  int ir_val[100];
  for (int i = 0; i < 100; i++) ir_val[i] = analogRead(pin);
  sort(ir_val, 100);
  return ir_val[100/2];      // send back the median
}
/* =================== Conversion For Different Sensor =================== */
// Right Back Sensor
int convertS0(int ir_val){
  int x = ir_val;
  double p1 =   -0.007316;
  double p2 =      6.06;
  double p3 =      3568;
  double q1 =      -29.14;
  double distanceCM = (p1*x*x + p2*x + p3) / (x + q1);
  int distance_mm = round(distanceCM*10);
  int distance_cm = distance_mm/10;
  return distance_cm; 
}

// Right First Sensor
int convertS1(int ir_val){
int x = ir_val;
  double p1 =      -2.704;
  double p2 =        5876;
  double q1 =       -1.001;
  double distanceCM = (p1*x + p2) / (x + q1);
  int distance_mm = round(distanceCM*10);
  int distance_cm = distance_mm/10;
  if(distance_cm == 29){
    distance_cm ++;
  }
  return distance_cm; 
}

//Front Right Sensor
int convertS2(int ir_val){
  int x = ir_val;
  double p1 =      -1.208;
  double p2 =        6057;
  double q1 =       18.65;
  double distanceCM = (p1*x + p2) / (x + q1);
  int distance_mm = round(distanceCM*10);
  int distance_cm = distance_mm/10;
  if(distance_cm >= 10 && distance_cm <=11){
    distance_cm --;
  }else if(distance_cm >= 16 && distance_cm <= 19){
    distance_cm ++;
  }
  return distance_cm; 
}

// Front Center 
int convertS3(int ir_val){
  int x = ir_val;
  double p1 =      -1.809;
  double p2 =        6319;
  double q1 =       1.683;

  double distanceCM = (p1*x + p2) / (x + q1);
  int distance_mm = round(distanceCM*10);
  int distance_cm = distance_mm/10;

  return distance_cm;
}

//Front Left Sensor
int convertS4(int ir_val){
  int x = ir_val;
  double p1 =      -8.249;
  double p2 =        1.073e+04;
  double q1 =       150.3;

  double distanceCM = (p1*x + p2) / (x + q1);
  int distance_mm = round(distanceCM*10);
  int distance_cm = distance_mm/10;
  if(distance_cm >= 13 && distance_cm <= 17){
    distance_cm ++;
  }else if(distance_cm >= 21 && distance_cm <=26){
    distance_cm ++;
  }else if (distance_cm >= 19){
    distance_cm ++;
  }
  return distance_cm;
}

//Long Range
int convertS5(int ir_val) {
  int x = ir_val;
  double p1 =   -0.007989;
  double p2 =      64.1;
  double p3 =      -3435;
  double q1 =      -103.6;
  double distanceCM = (p1*x*x + p2*x + p3) / (x + q1);
  int distance_mm = round(distanceCM*10);
  int distance_cm = distance_mm/10;
  if(distance_cm >= 3 && distance_cm <= 11){
    distance_cm ++;
  }
  return distance_cm;
}
/* ========================= Get Distance Request ======================== */
// Get Distance of S1
int distanceS0(){
  int ir_val = getRaw(A0);
  return convertS0(ir_val);
}
// Get Distance of S2
int distanceS1(){
  int ir_val = getRaw(A1);
  return convertS1(ir_val);
}
// Get Distance of S3
int distanceS2(){
  int ir_val = getRaw(A2);
  return convertS2(ir_val);
}
// Get Distance of S4
int distanceS3(){
  int ir_val = getRaw(A3);
  return convertS3(ir_val);
}
// Get Distance of S5
int distanceS4(){
  int ir_val = getRaw(A4);
  return convertS4(ir_val);
}
// Get Distance of S6
int distanceS5(){
  int ir_val = getRaw(A5);
  return convertS5(ir_val);
}
/* =============================== Sorting =============================== */
void sort(int a[], int n) {
  for (int i = 1; i < n; i++) {
    int next = a[i];
    int j;
    for (j = i - 1; j >= 0 && a[j] > next; j--) a[j + 1] = a[j];
    a[j + 1] = next;
  }
}
