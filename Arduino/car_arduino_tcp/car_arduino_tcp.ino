#include <SoftwareSerial.h>
//khai bao pin
const int LED_left = 4;
const int LED_right = 5;
const int LED_light = 6;
const int Buzzer = 7;
const int inA1 = 8;
const int inA2 = 9;
const int inB1 = 10;
const int inB2 = 11;

//khai bao bien
char command;
int val_light = 0;
int val_signal = 0;
SoftwareSerial esp(2, 3); //Rx, Tx

void setup()
{
  Serial.begin(115200);
  esp.begin(115200);
  Serial.println("System started");

  //pinMode
  pinMode(LED_left, OUTPUT);
  pinMode(LED_right, OUTPUT);
  pinMode(LED_light, OUTPUT);
  pinMode(Buzzer, OUTPUT);
  pinMode(inA1, OUTPUT);
  pinMode(inA2, OUTPUT);
  pinMode(inB1, OUTPUT);
  pinMode(inB2, OUTPUT);
}
void loop()
{
  if (Serial.available())
  {
    command = Serial.read();
    Serial.print(command);
    direct_c(command);
  }
  if (esp.available())
  {
    char comman = esp.read();
    Serial.print(comman);
    direct_c(comman);
  }

  if (val_signal > 0)
  {
    blind_led();
  }
}

//w: go
//s: back
//a: left
//d: right
//i: light
//k: buzzer
//j: signal left
//l: sinal right

void direct(String cmd)
{
  //light
  if (cmd == "i")
  {
    Serial.println("OK");
    if (val_light == 0)
    {
      digitalWrite(LED_light, HIGH);
      val_light = 1;
    }
    else {
      digitalWrite(LED_light, LOW);
      val_light = 0;
    }
  }
  //buzzer
  else if (cmd == "k") {
    digitalWrite(Buzzer, HIGH);
    delay(500);
    digitalWrite(Buzzer, LOW);
  }
  //xinhan
  else if (cmd == "j") {
    if (val_signal == 0 || val_signal == 2)
      val_signal = 1;
    else if (val_signal == 1)
      val_signal = 0;
  }
  else if (cmd == "l") {
    if (val_signal == 0 || val_signal == 1)
      val_signal = 2;
    else if (val_signal == 2)
      val_signal = 0;
  }

  //direction
  else if (cmd == "w") {
    Run();
  }
  else if (cmd == "s") {
    Back();
  }
  else if (cmd == "a") {
    TurnLeft();
  }
  else if (cmd == "d") {
    TurnRight();
  }
}

void direct_c(char cmd)
{
  //light
  if (cmd == 'i')
  {
    if (val_light == 0)
    {
      digitalWrite(LED_light, HIGH);
      val_light = 1;
    }
    else {
      digitalWrite(LED_light, LOW);
      val_light = 0;
    }
  }
  //buzzer
  else if (cmd == 'k') {
    digitalWrite(Buzzer, HIGH);
    delay(500);
    digitalWrite(Buzzer, LOW);
  }
  //xinhan
  else if (cmd == 'j') {
    if (val_signal == 0 || val_signal == 2)
      val_signal = 1;
    else if (val_signal == 1)
      val_signal = 0;
  }
  else if (cmd == 'l') {
    if (val_signal == 0 || val_signal == 1)
      val_signal = 2;
    else if (val_signal == 2)
      val_signal = 0;
  }

  //direction
  else if (cmd == 'w') {
    Run();
  }
  else if (cmd == 's') {
    Back();
  }
  else if (cmd == 'a') {
    TurnLeft();
  }
  else if (cmd == 'd') {
    TurnRight();
  }
}

/*define
  1 signal left
  2 signal right
*/
void blind_led()
{
  if (val_signal == 1)
  {
    digitalWrite(LED_left, HIGH);
    delay(500);
    digitalWrite(LED_left, LOW);
    delay(500);
  }
  else if (val_signal == 2)
  {
    digitalWrite(LED_right, HIGH);
    delay(500);
    digitalWrite(LED_right, LOW);
    delay(500);
  }
  else if (val_signal == 0)
  {
    digitalWrite(LED_right, LOW);
    digitalWrite(LED_left, LOW);
  }
}

//Direct motor
void Stop() {
  digitalWrite(inA1, LOW);
  digitalWrite(inA2, LOW);
  digitalWrite(inB1, LOW);
  digitalWrite(inB2, LOW);
}

void Run() {
  digitalWrite(inA1, HIGH);
  digitalWrite(inA2, LOW);
  digitalWrite(inB1, LOW);
  digitalWrite(inB2, HIGH);
  delay(500);
  Stop();
}

void Back() {
  digitalWrite(inA1, LOW);
  digitalWrite(inA2, HIGH);
  digitalWrite(inB1, HIGH);
  digitalWrite(inB2, LOW);
  delay(500);
  Stop();
}
void TurnLeft() {
  digitalWrite(inA1, HIGH);
  digitalWrite(inA2, LOW);
  digitalWrite(inB1, HIGH);
  digitalWrite(inB2, LOW);
  delay(150);
  Stop();
}
void TurnRight() {
  digitalWrite(inA1, LOW);
  digitalWrite(inA2, HIGH);
  digitalWrite(inB1, LOW);
  digitalWrite(inB2, HIGH);
  delay(150);
  Stop();
}
