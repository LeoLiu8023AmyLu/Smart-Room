
#include <SimpleSDAudio.h>
/////////////////////////////////////////////////////////////////
String inputString = "";         // a string to hold incoming data
boolean stringComplete = false;  // whether the string is complete
///////////////////////////////////////////////////////////////////

                                                 // Callback target, prints output to serial
void DirCallback(char *buf) { Serial.println(buf);}
char AudioFileName[16];                          // Create static buffer 
#define BIGBUFSIZE (2*512)                       // bigger than 2*512 is often only possible on Arduino megas!
uint8_t bigbuf[BIGBUFSIZE];
                                                 // helper function to determine free ram at runtime
int freeRam () {
  extern int __heap_start, *__brkval; 
  int v; 
  return (int) &v - (__brkval == 0 ? (int) &__heap_start : (int) __brkval); 
}
void setup()
{                                // Open serial communications and wait for port to open:
  Serial.begin(9600);
   while (!Serial) {
    ;                              // wait for serial port to connect. Needed for Leonardo only
  }
  Serial.print(F("Free Ram: "));
  Serial.println(freeRam());
  SdPlay.setWorkBuffer(bigbuf, BIGBUFSIZE); 
  Serial.print(F("\nInitializing SimpleSDAudio V" SSDA_VERSIONSTRING " ..."));  
  // If your SD card CS-Pin is not at Pin 4, enable and adapt the following line:
  //SdPlay.setSDCSPin(10);
  // Select between SSDA_MODE_FULLRATE or SSDA_MODE_HALFRATE (62.5kHz or 31.25kHz)
  // and the output modes SSDA_MODE_MONO_BRIDGE, SSDA_MODE_MONO or SSDA_MODE_STEREO
  if (!SdPlay.init(SSDA_MODE_FULLRATE | SSDA_MODE_MONO)) {
    Serial.println(F("initialization failed. Things to check:"));
    Serial.println(F("* is a card is inserted?"));
    Serial.println(F("* Is your wiring correct?"));
    Serial.println(F("* maybe you need to change the chipSelect pin to match your shield or module?"));
    Serial.print(F("Error code: "));
    Serial.println(SdPlay.getLastError());
    while(1);
  } else {
    Serial.println(F("Wiring is correct and a card is present.")); 
  }
}

void loop(void) {
  uint8_t count=0, c, flag;
  Serial.println(F("Files on card:"));
  SdPlay.dir(&DirCallback);
ReEnter: 
  count = 0;
  Serial.println(F("\r\nEnter filename (send newline after input):"));
  do {
    while(!Serial.available()) ;
    c = Serial.read();
    if(c > ' ') AudioFileName[count++] = c;
  } while((c != 0x0d) && (c != 0x0a) && (count < 14));
  AudioFileName[count++] = 0;
  
  Serial.print(F("Looking for file... "));
  if(!SdPlay.setFile(AudioFileName)) {
    Serial.println(F(" not found on card! Error code: "));
    Serial.println(SdPlay.getLastError());
    goto ReEnter;
  } else {
   Serial.println(F("found.")); 
  }    
  Serial.println(F("Press s for stop, p for play, h for pause, f to select new file, d for deinit, v to view status.")); 
  flag = 1;
  while(flag) {
    SdPlay.worker();
    if(Serial.available()) {
      c = Serial.read();
      switch(c) {
         case 'S':
           SdPlay.stop();
           Serial.println(F("Stopped."));
           break; 
           
         case 'P':
           SdPlay.play();
           Serial.println(F("Play."));
           break;
           
         case 'H':
           SdPlay.pause();
           Serial.println(F("Pause."));
           break;   
   
         case 'D':
           SdPlay.deInit();
           Serial.println(F("SdPlay deinitialized. You can now safely remove card. System halted."));
           while(1) ;
           break;  
             
         case 'F':
           flag = 0;
           break; 
     
         case 'V':
           Serial.print(F("Status: isStopped="));
           Serial.print(SdPlay.isStopped());
           Serial.print(F(", isPlaying="));
           Serial.print(SdPlay.isPlaying());
           Serial.print(F(", isPaused="));
           Serial.print(SdPlay.isPaused());
           Serial.print(F(", isUnderrunOccured="));
           Serial.print(SdPlay.isUnderrunOccured());
           Serial.print(F(", getLastError="));
           Serial.println(SdPlay.getLastError());
           Serial.print(F("Free RAM: "));
           Serial.println(freeRam());
           break;      
      } 
    }
  }
  
  
}
