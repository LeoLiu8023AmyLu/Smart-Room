/*
 * This sketch will list all files in the root directory and 
 * then do a recursive list of all directories on the SD card.
 * Only the mp3 files will be listed in the original playlist. 
 * Then we use original Playlist to generate the actual Playlist.
 * Then feeding VS1003 with the bytes read from the mp3 file.
 *
 */
#include <SdFat.h>
#include <SdFatUtil.h>
#include <SdFile.h>
#include <SdBaseFile.h>
#include <SPI.h>
 
// SD FileSystem Object
SdFat sd;
SdFile file;                // General Operating file
 
short songnumbers = 0;         // songs in SD Card
short open_count = 0;        // open_count is for holding the opencounts in genOrigListFile()
short totalsongs = 0;   // totalsongs in playlist.
 
// Variable for holding the latest timestamp
uint16_t latestWriteTime = 0;
uint16_t latestWriteDate = 0;
 
/* VS1003 Part */
//set vs1003 pins
short xCs=7;
short xReset=9;
short dreq= 3;
short xDcs=8;
 
// Vs1003 Reset, for Initialize the vs1003
void Mp3Reset(){
  int volume = 0x30;
 
  digitalWrite(xReset,LOW);
  delay(100);
  digitalWrite(xCs,HIGH);
  digitalWrite(xDcs,HIGH);
  digitalWrite(xReset,HIGH);
  commad(0X00,0X08,0X04); //Write into the MODE
  delay(10);
  if(digitalRead(dreq) == HIGH)
  {
    commad(0X03,0XC0,0X00);//Set the clock of VS1003
    delay(10);
    commad(0X05,0XBB,0X81);//Set VS1003 to 44kps Sterero
    delay(10);
    commad(0X02,0X00,0X55);//Set the heavy sound
    delay(10);
    commad(0X0B,volume,volume);//highest volume:0x0000, lowest volume:0xFEFE
    delay(10); 
    SPI.transfer(0);
    SPI.transfer(0);
    SPI.transfer(0);        
    SPI.transfer(0);
    digitalWrite(xCs,HIGH);
    digitalWrite(xReset,HIGH);
    digitalWrite(xDcs,HIGH);
    digitalWrite(4,LOW);
  }
}
// for sending SPI commands
void commad(unsigned char addr,unsigned char hdat,unsigned char ldat)
{  
  if(digitalRead(dreq)==HIGH)
  {
    digitalWrite(xCs,LOW);
    SPI.transfer(0X02);
    SPI.transfer(addr);
    SPI.transfer(hdat);
    SPI.transfer(ldat);    
    digitalWrite(xCs,HIGH);
  }
}
//PlayMP3 pulls 32 byte chunks from the SD card and throws them at the VS1003
//We monitor the DREQ (data request pin). If it goes low then we determine if
//we need new data or not. If yes, pull new from SD card. Then throw the data
//at the VS1003 until it is full.
void playMP3(char* fileName) 
{ 
  //Open the file in read mode.
  if (!file.open(fileName, O_READ))
  { 
    Serial.print("Failed to Open:");
    Serial.println(fileName);
    return;
  }
  Serial.println("Track open");
  //Buffer of 32 bytes. VS1053 can take 32 bytes at a go.
  uint8_t mp3DataBuffer[32];
  //track.read(mp3DataBuffer, sizeof(mp3DataBuffer)); //Read the first 32 bytes of the song
  uint8_t need_data = 0; 
  Serial.println("Start MP3 decoding");
  while(1) 
  {
    while(!digitalRead(dreq))
    { 
      //DREQ is low while the receive buffer is full
      //You can do something else here, the buffer of the MP3 is full and happy.
      //Maybe set the volume or test to see how much we can delay before we hear audible glitches
 
      //If the MP3 IC is happy, but we need to read new data from the SD, now is a great time to do so
      if(need_data == 0) 
      {
        //Try reading 32 new bytes of the song
        if(!file.read(mp3DataBuffer, sizeof(mp3DataBuffer))) 
        { 
          //Try reading 32 new bytes of the song
          //Oh no! There is no data left to read!
          //Time to exit
          break;
        }
        need_data = 1;
      }     
    }
    //This is here in case we haven't had any free time to load new data
    if(need_data == 0)
    { 
      //Go out to SD card and try reading 32 new bytes of the song
      if(!file.read(mp3DataBuffer, sizeof(mp3DataBuffer))) 
      {
        //Oh no! There is no data left to read!
        //Time to exit
        break;
      }
      need_data = 1;
    }
 
    //Once DREQ is released (high) we now feed 32 bytes of data to the VS1053 from our SD read buffer
    digitalWrite(xDcs, LOW); //Select Data
    for(int y = 0 ; y < sizeof(mp3DataBuffer) ; y++) 
    {
      SPI.transfer(mp3DataBuffer[y]); // Send SPI byte
    }
 
    digitalWrite(xDcs, HIGH); //Deselect Data
    //We've just dumped 32 bytes into VS1053 so our SD read buffer is empty. Set flag so we go get more data
    need_data = 0; 
  }
 
  //Wait for DREQ to go high indicating transfer is complete
  while(!digitalRead(dreq));
  //Deselect Data
  digitalWrite(xDcs, HIGH);
 
  //Close out this track        
  file.close();
 
  Serial.print("Track ");
  Serial.print(fileName);
  Serial.println(" done!");
}
 
//***************************************************************
//                 SD Card Operation Functions Start
//***************************************************************
 
/* This function is for get all of the songs in SD card */
/* return value: how many mp3 files in the SD Card */
short getSongsinSD(SdBaseFile * dir, int numTabs) {
  // entry is for recording the file information
  SdFile entry;
  // length is 8(filename)+1(.)+3(suffix)+1(NUL) == 13
  char filename[13];
 
  // open Next item, until the end of the world.
  while(entry.openNext(dir, O_READ)) {    
    // If this entry is a sub-directory, go to sub directories to get numbers. 
    if(entry.isSubDir())
    {
      // Get the current index, use this index for recursively print the sub-directories
      uint16_t index = dir->curPosition()/32 -1;
      SdBaseFile s;
      if(s.open(dir, index, O_READ))
      {
        if(numTabs <=2)
        {
          getSongsinSD(&s, numTabs + 1); 
        }
      }
      entry.close();      
    }   
    else // Common files will directly be displayed.
    {
      // Calculate how many MP3 exists in SD card.
      entry.getFilename(filename);      
      if((filename[strlen(filename)-3] == 'M') &&
        (filename[strlen(filename)-2] == 'P') &&
        (filename[strlen(filename)-1] == '3'))
      {
        // An mp3 item will add 1 of songnumbers.
        songnumbers++;
      }    
      entry.close();
    }
  }
  return songnumbers;
}
/* This function is for generating the original mp3 list */
/* mp3 list file will use the latest timestamp in SD Card */
short genOrigListFile(const char *f, SdBaseFile * dir, int numTabs) {
  // entry is for recording the file information
  SdFile entry;
  // length is 8(filename)+1(.)+3(suffix)+1(NUL) == 13
  char filename[13];
  /* Create file from "f" for recording the result */
  open_count++;
  if(!file.isOpen())
  {
    /* Create one */
    if(!file.open(f, O_CREAT | O_TRUNC | O_WRITE))
    {
      /* Create failed will return 3 */
      return 3;
    }
  }
  // Set the file's current position to zero
  dir->rewind();
 
  // open Next item, until the end of the world.
  while(entry.openNext(dir, O_READ)) {   
 
    // If this entry is a sub-directory, Print itself and sub-entries
    if(entry.isSubDir())
    {     
      // Use spaces for indicating the directory layers.
      for(uint8_t i=0; i < numTabs; i++) {
        //Serial.print("  ");
        file.write("  ");     
      }
 
      memset(filename, '\0', 13);
      entry.getFilename(filename);
 
      uint8_t lastchar = strlen(filename);
      //filename[lastchar+1]='\0';
      filename[lastchar]='/';
      file.write(filename);
      file.write("\r\n");
      dir_t dir_time;
      entry.dirEntry(&dir_time);
 
      if((dir_time.lastWriteDate > latestWriteDate))
      {
        latestWriteDate = dir_time.lastWriteDate;
        latestWriteTime = dir_time.lastWriteTime;
      }
      if((dir_time.lastWriteDate == latestWriteDate))
      {
        if(dir_time.lastWriteTime > latestWriteTime)
        {
          latestWriteTime = dir_time.lastWriteTime;
        }
      }
 
      // Get the current index, use this index for recursively print the sub-directories
      uint16_t index = dir->curPosition()/32 -1;
      SdBaseFile s;
      if(s.open(dir, index, O_READ))
      {
        if(numTabs <=2 )
        {
          genOrigListFile(f, &s, numTabs + 1); 
        }
      }
      entry.close();      
    }    
    // Common files will directly be displayed.
    else
    {      
      // Calculate how many MP3 exists in SD card.
      memset(filename, '\0', 13);
      entry.getFilename(filename);
      if((filename[strlen(filename)-3] == 'M') &&
        (filename[strlen(filename)-2] == 'P') &&
        (filename[strlen(filename)-1] == '3'))
      {
 
        // Use spaces for indicating the directory layers.
        for(uint8_t i=0; i < numTabs; i++) {
          //Serial.print("  ");
          file.write("  ");     
        }
        //entry.getFilename(filename);
        file.write(filename);
        file.write("\r\n");
 
        dir_t dir_time;
        entry.dirEntry(&dir_time);
 
        if((dir_time.lastWriteDate > latestWriteDate))
        {
          latestWriteDate = dir_time.lastWriteDate;
          latestWriteTime = dir_time.lastWriteTime;
        }
        if((dir_time.lastWriteDate == latestWriteDate))
        {
          if(dir_time.lastWriteTime > latestWriteTime)
          {
            latestWriteTime = dir_time.lastWriteTime;
          }
        }
      }         
 
      entry.close();
    }
  }
  // open_count will decrease for avoiding open it several times.
  open_count--;
  // Close opened file at the end of all routine, failed if we received 4.
  if(open_count == 0)
  {
    // update the timestamp.
    dir_t dir_play;
    file.dirEntry(&dir_play);
 
    // Update the timestamp for generated file. later we will use this timestamp for updating.     
    if (!file.timestamp(T_WRITE, (1980+(latestWriteDate>>9)), ((latestWriteDate>>5)&0XF), (latestWriteDate & 0X1F), (latestWriteTime >> 11), ((latestWriteTime >> 5) & 0X3F), ((latestWriteTime & 0X1F)*2))) 
    {
      Serial.println("set Write time Failed");
    }
 
    file.sync();          
 
    if(!file.close())
    {
      return 4;
    }
  }
 
  return 0;
}
 
 
/* This function will get raw file list from Original file 
 * then output with the full name to the dest file */
short genPlayList(const char *src, const char *dest)
{
  char str[100];
  short n=0;
  short SpaceCounts, i, j, k;
  char *dirname[4] = {
    0  };
  // 10 layers means (8+1)*10layer+13(file)
  char *prefix = (char *)malloc(9*4 + 13);
 
 
  for( j = 0; j < 4; j++)
  {
    // malloc size is 8(dir name) + 1("/") + 1(NULL) = 10
    dirname[j] = (char *)malloc(10);
  } 
 
 
  file.open(src, O_READ);
  if(!file.isOpen())
  {
    return 1;
  }
 
  // pl == PlayList file
  SdFile plfile(dest, O_CREAT | O_TRUNC | O_WRITE);
  if(!plfile.isOpen())
  {
    return 2;
  }
 
 
  while((n = file.fgets(str, sizeof(str))) > 0)
  {
    // Change the '\r' to the NUL, means the end of the string.
    short len = strlen(str) -1;
 
    if(str[len] == '\n')
    {     
      str[len] = 0;
    }
    if(str[len -1] == '\r')
    {     
      str[len - 1] = 0;
    }
 
    SpaceCounts = 0;
    for( i = 0; i < strlen(str); i++)
    {
      if(str[i] == ' ')
      {
        SpaceCounts++;
      }
    }
 
 
    // Directly print the MP3 files under the root directory 
    if((!strncmp(str+strlen(str)-3, "MP3", 4)) && (SpaceCounts == 0))
    {
      plfile.write(str);
      plfile.write("\r\n");
    }
 
    // Processing directory entries
    if(str[strlen(str) - 1] == '/')
    {
      // Remove the SpaceCounts and copy the rest of the name into string array dirname[]
      strcpy(dirname[(SpaceCounts/2)], (char *)(str+SpaceCounts));
    }
 
    // Print the MP3 which locates in sub-directories
    if((SpaceCounts >= 2) && (str[strlen(str) - 1] != '/'))
    {
      memset(prefix, '\0', 9*4 + 13);
      for( k = 0; k <= (SpaceCounts/2); k++ )
      {
        strcat(prefix, dirname[k]);
      }
      strcat(prefix, str+SpaceCounts);
      plfile.write(prefix, strlen(prefix));
      plfile.write("\r\n");
      delay(10);
    } 
 
  } // end of while()
 
 
  for(j = 0; j < 3; j++)
  {
    free(dirname[j]);
  }  
  free(prefix);
 
  // Generate the real player file. 
  if(!file.close())
  {
    return 5;
  }
  if(!plfile.close())
  {
    return 6;
  }
 
  return 0;  
}
 
/* This function is for scanning all of the directories and sub-items
 * to fetch the latest timestamp, we will use the latest timestamp for
 * judging update playlist or not 
 */
void getLatestStamp(SdBaseFile * dir, short numTabs) {
  // entry is for recording the file information
  SdFile entry;
  // length is 8(filename)+1(.)+3(suffix)+1(NUL) == 13
  char filename[13]; 
 
  // open Next item, until the end of the world.
  while(entry.openNext(dir, O_READ)) {    
    // If this entry is a sub-directory, Print itself and sub-entries
    if(entry.isSubDir())
    {
      // Get the latest timestamp of the directory.
      dir_t dir_time;
      entry.dirEntry(&dir_time);
 
      if((dir_time.lastWriteDate > latestWriteDate))
      {
        latestWriteDate = dir_time.lastWriteDate;
        latestWriteTime = dir_time.lastWriteTime;
      }
      if((dir_time.lastWriteDate == latestWriteDate))
      {
        if(dir_time.lastWriteTime > latestWriteTime)
        {
          latestWriteTime = dir_time.lastWriteTime;
        }
      }
 
      // Get the current index, use this index for recursively print the sub-directories
      uint16_t index = dir->curPosition()/32 -1;
      SdBaseFile s;
      if(s.open(dir, index, O_READ))
      {
        if(numTabs <=2) getLatestStamp(&s, numTabs + 1);
      }
      entry.close();      
    }
 
    // Common files goes here.
    else
    {      
      // Calculate how many MP3 exists in SD card.
      entry.getFilename(filename);      
      if((filename[strlen(filename)-3] == 'M') &&
        (filename[strlen(filename)-2] == 'P') &&
        (filename[strlen(filename)-1] == '3'))
      {
        // Get the latest timestamp of the directory.
        dir_t dir_time;
        entry.dirEntry(&dir_time);
 
        if((dir_time.lastWriteDate > latestWriteDate))
        {
          latestWriteDate = dir_time.lastWriteDate;
          latestWriteTime = dir_time.lastWriteTime;
        }
        if((dir_time.lastWriteDate == latestWriteDate))
        {
          if(dir_time.lastWriteTime > latestWriteTime)
          {
            latestWriteTime = dir_time.lastWriteTime; 
          }
        }
      }    
      entry.close();
    }
  }
}
 
 
/* This function will get the file's timestamp, we want to
 * fetch the orginal playlist's timestamp, use it to judging update
 * playlist or not.
 */
short getfileStamp(const char *f, uint16_t *date, uint16_t *time)
{
  SdFile localfile(f, O_READ);
  if(!localfile.isOpen())
  {
    Serial.println("Open Local File Failed!");
    return 2;
  }
 
  // Get the latest timestamp of the directory.
  dir_t dir_time;
  localfile.dirEntry(&dir_time);
 
  *date = dir_time.lastWriteDate;
  *time = dir_time.lastWriteTime;
 
  if(!localfile.close())
  {
    return 4;
  }
  return 0;
}
 
/* This function is for getting the numbers in PlayList */
short getListLength(char *playlistfile)
{
  short tmpnum = 0;
  char line[25];
  int n;
 
  delay(100);
  // Read Items from the Playlist file.
  // open PlayList file
  SdFile rdfile(playlistfile, O_READ);
  if (!rdfile.isOpen()) 
  {
    Serial.println("Open PlayList Failed!");
  }
 
  // read lines from the file
  while ((n = rdfile.fgets(line, sizeof(line))) > 0) 
  {
    if (line[n - 1] == '\n') 
    {
      tmpnum++;
    } 
    else 
    {
      ;
    }                              
  }        
  rdfile.close();
  return tmpnum;
}
 
 
void selectSong(char *playlistfile, char *song, short select_number)
{
  short tmpnum = select_number;
  char line[50];
  int n;
 
  delay(100);
  // Read Items from the Playlist file.
  // open PlayList file
  SdFile rdfile(playlistfile, O_READ);
  if (!rdfile.isOpen()) 
  {
    Serial.println("Open PlayList Failed!");
    return;
  }
 
  // read lines from the file
  while ((n = rdfile.fgets(line, sizeof(line))) > 0) 
  {
    if (line[n - 1] == '\n') 
    {
      tmpnum--;
      //songs_in_list++;
    } 
    else 
    {
      ;
    }
    if(tmpnum == 0)
    {
      strncpy(song, line, strlen(line)-1);
      song[strlen(line)-1]='\0';
    }
 
  }        
  rdfile.close();
}
 
 
 
void setup()
{ 
  bool updateflag = 0;        
 
  delay(10);
  Serial.begin(9600);
 
  // See Free RAM        
  PgmPrint("Free RAM: ");
  Serial.println(FreeRam());  
 
  // set 1003
  Serial.println("Set VS1003..."); 
  SPI.begin();
  SPI.setBitOrder(MSBFIRST); //send most-significant bit first
  SPI.setDataMode(SPI_MODE0);
  SPI.setClockDivider(SPI_CLOCK_DIV16); 
  pinMode(7,INPUT);
  pinMode(8,OUTPUT);
  pinMode(6,OUTPUT);
  pinMode(9,OUTPUT);
  digitalWrite(4,HIGH);
  Mp3Reset();
  Serial.println("DONE VS1003...Begin SD Card"); 
 
 
  // initialize the SD card at SPI_HALF_SPEED to avoid bus errors with
  // breadboards.  use SPI_FULL_SPEED for better performance.
  if (!sd.begin(4, SPI_FULL_SPEED)) sd.initErrorHalt();
 
  // Start at beginning of the root directory.
  // rewind is used for set current position to zero.
  sd.vwd()->rewind();
 
  // Testing Functionality:
  // How many MP3 sons in SD Card
  short numbers = getSongsinSD(sd.vwd(), 0);
  Serial.print("Songs in SD Card is ");
  Serial.println(numbers);
  // See Free RAM        
  PgmPrint("Free RAM: ");
  Serial.println(FreeRam());  
 
 
  // 1. If no playlist file exists, scan the whole SD to generate one
  // 2. Else Check the timestamp of the playlist to see if update is needed
  // 3. If update is needed, generate playlist again.
  sd.vwd()->rewind();
  if(!sd.exists("PL_OK.TXT"))                // Condition 1
  {
    Serial.println("No PL_OK.TXT found!");
    updateflag = 1;
  }
  else                                        // Condition 2
  {
    Serial.println("PL_OK has been created, need to check its timestamp!");
    // Set global Variables to 0 for avoiding potential mis-using
    latestWriteTime = 0;
    latestWriteDate = 0;
    sd.vwd()->rewind();
    getLatestStamp(sd.vwd(), 0);
    // Get the PlayList File's timestamp, for comparing.
    uint16_t ldate = 0;
    uint16_t ltime = 0;
    short rvalue = getfileStamp("PL_OK.TXT", &ldate,&ltime);
    if( (ldate == latestWriteDate) && ( ltime == latestWriteTime) )
    {
      Serial.println("PlayList is the newest version, update not needed!");
    }
    else
    {
      Serial.println("Need Update PlayList!");
      updateflag = 1;
    }
  }
 
  sd.vwd()->rewind();
  if(updateflag)                                // Condition 3, update playlist or not
  {
    // Call genOrigListFile() to fetch all of the MP3 and directory layer information
    short error_reason = genOrigListFile("PL_OK.TXT", sd.vwd(), 0);
    if(error_reason != 0)
    {
      Serial.print("genOrigListFile error number is ");
      Serial.println(error_reason);
    }
 
 
    // Call genPlayList() to generate the actual PlayList file
    error_reason = genPlayList("PL_OK.TXT", "PLT.TXT");
    if(error_reason != 0)
    {
      Serial.print("genPlayList error number is ");
      Serial.println(error_reason);
    }
    Serial.println("Generate OrigiList and PlayList Done!");
  }
  else
  {
    Serial.println("No Update!");
  }
 
 
  totalsongs = getListLength("PLT.TXT");
  Serial.print("Songs in PLT.TXT is");
  Serial.println(totalsongs);
 
  delay(1000);
  PgmPrint("Free RAM: ");
  Serial.println(FreeRam());  
 
 
  Serial.println("Done setup()");
}
 
 
void loop() {
  PgmPrint("Free RAM: ");
  Serial.println(FreeRam());  
  delay(1000);
  short i = 1;
  char song[50];
  for (i = 1; i<totalsongs; i++)
  {
    selectSong("PLT.TXT",song, i);
    sd.vwd()->rewind();
    playMP3(song);
  }
}

