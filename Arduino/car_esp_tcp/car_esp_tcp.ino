/*
  Author Tien Dat Huynh
  Date created 22-Apr-19 11:27PM
  TCP server power by ESP8266 WiFi
*/
#include <ESP8266WiFi.h>
#include <WiFiClient.h>

//Define variables
const char* ssid = "Car ESP8266";
const char* password = "bat3glendi@abc123";

WiFiServer server(3000);

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  delay(10);
  WiFi.mode(WIFI_AP);
  WiFi.softAP(ssid, password, 1);
  IPAddress espIP = WiFi.softAPIP();
  Serial.println(espIP);

//  WiFi.begin(ssid, password);
//
//  while (WiFi.status() != WL_CONNECTED) {
//    delay(1000);
//  }
//  Serial.println(WiFi.localIP());
  
  server.begin();
}

void loop() {
  // put your main code here, to run repeatedly:
  WiFiClient client = server.available();

  if (client) {
    Serial.println("C ConnecteD.");
    client.println("You are connected");
    while (client.connected())
    {
      while (client.available() > 0)
      {
        char c = client.read();
        Serial.print(c);
        client.println(c);
      }
      delay(10);
    }
    Serial.println("C DISconnecteD");
  }
}
