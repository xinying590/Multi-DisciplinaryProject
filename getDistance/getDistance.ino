
#define s0 A0;  // Right Back Sensor  
#define s1 A1;  // Right First Sensor
#define s2 A2;  // Front right sensor
#define s3 A3;  // Front Center
#define s4 A4   // Front Left Sensor
#define s5 A5;  // Long range
void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  /* ======================== For Sensor ============================= */
  pinMode(A0, INPUT);
  pinMode(A1, INPUT);
  pinMode(A2, INPUT);
  pinMode(A3, INPUT); 
  pinMode(A4, INPUT);
  pinMode(A5, INPUT);
}

void loop() {
  // put your main code here, to run repeatedly:
  Serial.println(getRaw(A4));
  
}
int getRaw(float pin){
  int ir_val[100];
  for (int i = 0; i < 100; i++) ir_val[i] = analogRead(pin);
  sort(ir_val, 100);
  return ir_val[100/2];      // send back the median
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
