/*
	系统引脚定义：
 	#-->模拟端口
 	Sharp pin 5 (Vo)	=> Arduino A0	//PM2.5 电压数值输入
 	Sensor			=> Arduino A1	//Smoke System In 烟雾读取	A1
 	ADpin 			=> Arduino A2	//光敏电阻的接入口	A2
 	Peplepin		=> Arduino A3	//人体探测接入口	A3
 	SDA			=> Arduino A4	// I2C 数据端口		A4
 	SLC			=> Arduino A5	// I2C 时钟端口		A5
 	#-->数字端口
 	Sharp pin 3 (LED)	=> Arduino P13	//LED灯 开关
 	LCD12864_CS_PORT	=> Arduino P12	//LCD12864 RS引脚串行模式别名
 	LCD12864_SID_PORT	=> Arduino P11	//LCD12864 RW引脚串行模式别名
 	LCD12864_SCLK_PORT	=> Arduino P10	//LCD12864 E 引脚串行模式别名
 	Sensor_DO		=> Arduino P9	// Smoke System Alarm In 报警点评 Pin9
 	DHT11 myDHT11(8)	=> Arduino P8	// DHT11 数字读取引脚	Pin8
 	Buzzer			=> Arduino P7	// 蜂鸣器 引脚				Pin7
 	Vibration 	        => Arduino P6	// 震动传感器				Pin6
 	DS1302_CLK 		=> Arduino P4	//实时时钟时钟线引脚
 	DS1302_IO 		=> Arduino P3	//实时时钟数据线引脚
 	DS1302_RST 		=> Arduino P2	//实时时钟复位线引脚

 	#-->VCC 5v 端口
 	Sharp pin 1 (V-LED)     => 5V (connected to 150ohm resister)
 	Sharp pin 6 (Vcc)	=> 5V

 	#-->GND 端口
 	Sharp pin 2 (LED-GND)   => Arduino GND pin
 	Sharp pin 4 (S-GND)     => Arduino GND pin

 */

/*
	通信格式定义为：
 	月-日_时:分:秒#温度#湿度#PM2.5#光强#烟雾#震动#人体感应$
 	03-14_12:54:31#21#41#057#B#N#V#N$   
 	其中'#'为分割符 '$'为结束符
 */

///////////库文件///////////
#include <SPI.h>		//SPI 通信协议库文件
#include <stdlib.h>
#include <Arduino.h>	//Arduino 基本库文件
#include "DS1302.h"		//时钟库文件
#include "lcd12864_S.h"	//LCD12864 显示库文件
#include "DHT11.h"		//数字温湿度模块 库文件
#include <Wire.h>		// I2C 通讯 库文件
////////////////////////////////////////
bool Flag_KEY_Set = 0;
///////////////////////////////////////////////////
#define Buzzer 7		// 蜂鸣器 引脚		Pin7
/////////////////////////////////////////////////
DHT11 myDHT11(8);		// DHT11 数字读取引脚 Pin8
///////////////////////////////////////////////
#define ADpin A2        //光敏电阻的接入口	A2
int ADBuffer = 0;       //
///////////////////////////////////////////////////////////////////////////////
#define Peplepin A3		//人体探测接入口	A3
int PepleValue = 0;
//////////////////////////////////////////////////////////////////////////////
#define Sensor A1		//Smoke System In 烟雾读取	A1 
#define Sensor_DO 9		// Smoke System Alarm In 报警点评 Pin9
unsigned int SensorValue = 0; //
//////////////////////////////////////////////////////////////////////////////
#define Vibration_DO 6		//  振动报警 Pin6 
unsigned int VibValue = 0;
//////////////////////////////////////////////////////////////////////////////

int dustPin = 0;		// Vaul In Dust PM2.5 电压数值输入
int ledPower = 13;	// Led Power LED灯的电源
int delayTime = 280;	// 延时时间1
int delayTime2 = 40;	// 延时时间2
float offTime = 9680;	// 偏移时间
int dustVal = 0;		//初始化 灰尘计数的值
int i = 0;
float ppm = 0;
char s[32];		//输出字符串
float voltage = 0;
float dustdensity = 0;
float ppmpercf = 0;
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////setup//////////////////////////////////////////////////////////////////////
void setup()
{
  ////////////////////////////////////
  DS1302_Init();		// 开启振荡
  /////////////////////////////////////////////////
  Wire.begin();			// 开启I2C 通信端口
  //////////////////////////////////////////////////
  pinMode(Buzzer, OUTPUT);	//蜂鸣器引脚设定为输出
  //////////////////////////////////////
  LCD12864_Reset();			//初始化液晶
  LCD12864_HAIZI_SET();		//设置为普通模式
  LCD12864_Clear();			//清屏指令
  LCD12864_SET_Address(1, 3);				//设置指针指向第一行第一个字的位置
  LCD12864_write_word("System Loading");	//连续写字符，字符数不能超过一行能容纳的范围16Byte。
  LCD12864_SET_Address(2, 1);				//设置指针指向第一行第一个字的位置
  LCD12864_write_word("Welcome EveryOne");
  LCD12864_SET_Address(3, 1);
  LCD12864_write_word("Here~ ");
  LCD12864_SET_Address(4, 1);
  LCD12864_write_word("Made By Us");
  ///////////////////////////////////////////////////
  ///////////////////////////////////////////////////////
  pinMode(Sensor_DO, INPUT);
  pinMode(Sensor, INPUT);
  pinMode(ledPower, OUTPUT);
  ///////////////////////////////////////////////////////////
  // give the ethernet module time to boot up:
  i = 0;
  ppm = 0;
  delay(2000);			//Delay 2s
  i = 0;
  ppm = 0;
  LCD12864_Clear();		//清屏指令
  Serial.begin(9600);	//初始化串口通讯频率9600
  Serial.println("Welcome!");  //串口发出消息
}

void loop()
{

  /////////////**************************//////////////
  DS1302_GetTime(&DS1302Buffer);	//获取当前RTCC值
  //Display_RTCC();                 // 打印Time
  ////////////////////////////////////////////////////////////////////////////////////////////////////////
  LCD12864_SET_Address(1, 1);
  myDHT11.DHT11_Read();
  /*
  // 串口通信统一最后按照顺序输出
   // 格式为 2015-3-8 12:54:31#21#41#057#B#N#N@
   //Serial.print("TMEP = "); // 温度
   //Serial.print(myDHT11.TEM_Buffer_Int); //写温度值
   //Serial.println(" C");
   //Serial.print("HUMI = ");    //湿度
   //Serial.print(myDHT11.HUMI_Buffer_Int);//写湿度值
   //Serial.println(" %RH");
   */
  LCD12864_write_word("T=");			//显示温度
  LCD12864_Data_Write((myDHT11.TEM_Buffer_Int / 10) + 48); ///温度十位
  LCD12864_Data_Write((myDHT11.TEM_Buffer_Int % 10) + 48); ///温度个位
  LCD12864_write_word("'C");			//温度单位
  LCD12864_write_word(" H=");			//显示湿度
  LCD12864_Data_Write((myDHT11.HUMI_Buffer_Int / 10) + 48); ///湿度十位
  LCD12864_Data_Write((myDHT11.HUMI_Buffer_Int % 10) + 48); ///湿度个位
  LCD12864_write_word("%RH");			//显示湿度单位
  ///////////////////////////////////////////////////////////////////////
  LCD12864_SET_Address(2, 1);			//换行
  ///////////////////////////////////////////
  //////  PM2.5 ///////////////////////
  if ((int)DS1302Buffer.Second % 5 == 0)    // Update time
  {
    i = i + 1;
    digitalWrite(ledPower, LOW); 	// power on the LED
    delayMicroseconds(delayTime);

    dustVal = analogRead(dustPin); 	// read the dust value
    ppm = ppm + dustVal;          //计算PPM

    delayMicroseconds(delayTime2);
    digitalWrite(ledPower, HIGH); 	// turn the LED off
    delayMicroseconds(offTime);
    voltage = ppm / i * 0.0049;	// 计算电压值
    dustdensity = 0.17 * voltage - 0.1; //灰尘密度
    ppmpercf = (voltage - 0.0256) * 120000;
    if (ppmpercf < 0)
      ppmpercf = 0;
    if (dustdensity < 0 )
      dustdensity = 0;
    if (dustdensity > 0.5)
      dustdensity = 0.5;
    i = 0;
    ppm = 0;
  }
  //串口通信统一最后按照顺序输出
  //Serial.print("PM2.5:  ");
  //Serial.println((int)(dustdensity*1000));
  /////////////////////////////////////////////////////////////
  LCD12864_write_word("PM2.5: ");
  LCD12864_Data_Write((int)(dustdensity * 10) + 48); ///Dust 10^2位
  LCD12864_Data_Write((int)(dustdensity * 100) % 10 + 48); ///Dust 10^1位
  LCD12864_Data_Write(((int)(dustdensity * 1000) % 10) + 48); ///Dust 10^1位
  ///////////*****************************////////////////////////////
  /////////////////////////Light Bright 光强//////////////////////
  ADBuffer = analogRead(ADpin);		//读取AD值
  //串口通信统一最后按照顺序输出
  //Serial.print("Light AD = ");
  //Serial.println(ADBuffer);	// light
  LCD12864_write_word(" L:");
  if (ADBuffer > 990)			//ADBuffer值大于设定值，相当于光照强度小于设定值
  {
    //digitalWrite(LED,LOW);		//点亮LED
    LCD12864_Data_Write(9);
  }
  else
  {
    //digitalWrite(LED,HIGH);		//关闭LED
    LCD12864_Data_Write(10);
  }
  ///////////////////////////////////////////////////////////////////////////////////////
  LCD12864_SET_Address(3, 1);		//换行 第三行显示
  LCD12864_write_word("SMOKE");
  SensorValue = analogRead(Sensor);		//读取Sensor引脚的模拟值，该值大小0-1023
  //串口通信统一最后按照顺序输出
  //Serial.print("SMOKE AD Value = ");
  //Serial.println(SensorValue);			//将模拟值输出到串口
  if ( digitalRead(Sensor_DO) == LOW )		//当DO引脚接收到低电平时候说明，模拟值超过比较器阀值
  { //通过调节传感器上的电位器可以改变阀值
    //Serial.println("SMOKE Alarm!");		//报警
    BuzzerAlarm();
    LCD12864_Data_Write(4);
  }
  else
  {
    LCD12864_write_word(" ");
  }
  //////////////////////////////////////////////////
  LCD12864_SET_Address(3, 5);
  PepleValue = analogRead(Peplepin);		//读取AD值
  //Serial.print("PepleValue = ");
  //Serial.println(PepleValue);
  if (PepleValue > 500)
  {
    LCD12864_write_word("P!");
  }
  else
  {
    LCD12864_write_word("  ");
  }
  PepleValue = 0;
  ////////////////////////////////////////////////////
  // 振动报警
  LCD12864_SET_Address(3, 7);

  for (int m = 0; m < 5; m++)
  {
    if ( digitalRead(Vibration_DO) == HIGH )	//当DO引脚接收到低电平时候说明振动超过阈值
    { //通过调节传感器上的电位器可以改变阀值
      VibValue++;
    }
    delay(2);
  }
  if (VibValue > 2)
  {
    LCD12864_write_word("V!");		// 振动报警
    delay(1000);
  }
  else
  {
    LCD12864_write_word("  ");		//不报警
  }
  VibValue = 0;
  /////////////////////////////////////////////////////////////////////////////
  ///////////////////////     时间显示     ///////////////////////////////////////
  LCD12864_SET_Address(4, 1);		//换行 第四行
  //////Year  年 ////////////////////////
  /*
  if(DS1302Buffer.Year < 10)
   {
   //Serial.print("200");
   LCD12864_write_word("200");
   LCD12864_Data_Write((int)(DS1302Buffer.Year%10)+48);
   }
   else
   {
   //Serial.print("20");
   LCD12864_write_word("20");
   LCD12864_Data_Write((int)(DS1302Buffer.Year%100)+48);
   LCD12864_Data_Write((int)(DS1302Buffer.Year%10)+48);
   }
   //Serial.print('-');
   LCD12864_write_word("-");
   */

  //////Month    月  ////////////////////////
  //Serial.print(DS1302Buffer.Month);
  LCD12864_Data_Write((int)((DS1302Buffer.Month / 10) % 10) + 48);
  LCD12864_Data_Write((int)(DS1302Buffer.Month % 10) + 48);
  //Serial.print('-');
  LCD12864_write_word("-");
  //////Day  日  //////////////////////
  //Serial.print(DS1302Buffer.Day);
  LCD12864_Data_Write((int)((DS1302Buffer.Day / 10) % 10) + 48);
  LCD12864_Data_Write((int)(DS1302Buffer.Day % 10) + 48);
  //Serial.print("   ");
  LCD12864_write_word(" ");
  //////Week////////////////////////////////////
  switch (DS1302Buffer.Week)
  {
    case 1:
      //Serial.println("Mon");				 //显示星期一
      LCD12864_write_word("Mon");
      break;
    case 2:
      //Serial.println("Tue");				 //显示星期二
      LCD12864_write_word("Tue");
      break;
    case 3:
      //Serial.println("Wed");				 //显示星期三
      LCD12864_write_word("Wed");
      break;
    case 4:
      //Serial.println("Thu");				 //显示星期四
      LCD12864_write_word("Thu");
      break;
    case 5:
      //Serial.println("Fri");				 //显示星期五
      LCD12864_write_word("Fri");
      break;
    case 6:
      //Serial.println("Sat");				 //显示星期六
      LCD12864_write_word("Sat");
      break;
    case 7:
      //Serial.println("Sun");				 //显示星期日
      LCD12864_write_word("Sun");
      break;
    default :
      break;
  }
  LCD12864_write_word(" ");
  //////Hour////////////////////////////////////
  //Serial.print(DS1302Buffer.Hour);
  LCD12864_Data_Write((int)((DS1302Buffer.Hour / 10) % 10) + 48);
  LCD12864_Data_Write((int)(DS1302Buffer.Hour % 10) + 48);
  //Serial.print(':');
  LCD12864_write_word(":");
  //Serial.print(DS1302Buffer.Minute);
  LCD12864_Data_Write((int)((DS1302Buffer.Minute / 10) % 100) + 48);
  LCD12864_Data_Write((int)(DS1302Buffer.Minute % 10) + 48);
  /*
  // 因为 刷新率的缘故不显示 秒
   //Serial.print(':');
   LCD12864_write_word(":");
   //Serial.println(DS1302Buffer.Second);
   LCD12864_Data_Write((int)((DS1302Buffer.Second/10)%100)+48);
   LCD12864_Data_Write((int)(DS1302Buffer.Second%10)+48);
   */
  //////////////////////////////////////////////////////
  //////////  串口输出 //////////////////////////////////
  /*
  // time 时间显示
  if( (int)DS1302Buffer.Second%30 == 0)
  {
    Display_RTCC();              // Time
    Serial.print("#");
    // 温度
    Serial.print(myDHT11.TEM_Buffer_Int); //写温度值
    Serial.print("#");
    // 湿度
    Serial.print(myDHT11.HUMI_Buffer_Int);//写湿度值
    Serial.print("#");
    // PM2.5
    Serial.print((int)(dustdensity*1000));
    Serial.print("#");
    // light
    Serial.print(ADBuffer);
    Serial.print("#");
    // 烟雾报警
    if( digitalRead(Sensor_DO) == LOW )		//当DO引脚接收到低电平时候说明，模拟值超过比较器阀值
    {						//通过调节传感器上的电位器可以改变阀值
      Serial.print("A");		//报警
    }
    else
    {
      Serial.print("N");		//No 报警
    }
    Serial.print("#");
    // Vibration 报警
    if( digitalRead(Vibration_DO) == LOW )		//当DO引脚接收到低电平时候说明，模拟值超过比较器阀值
    {						//通过调节传感器上的电位器可以改变阀值
      Serial.print("N");		//No报警
    }
    else
    {
      Serial.print("V");		//No 报警
    }
    Serial.print("#");
    //////// 人体热感应
    if(PepleValue>500)
    {
      Serial.print("Y");

    }
    else
    {
      Serial.print("N");
    }
    Serial.print('\n');
    Serial.print("$");
  }
  */
  //////////////////////////////////////////////////////
  //
  /////    I2C    //////////////////////////////////////////


  if ( (int)DS1302Buffer.Second % 15 == 0)
  {
    Wire.beginTransmission(3);	//开启 I2C 端口3
    //Time
    /*
    //////Year////////////////////////////////////
    if (DS1302Buffer.Year < 10)
    {
      //Serial.print("200");
      Wire.write("200");
      Wire.write((int)(DS1302Buffer.Year % 10) + 48);
    }
    else
    {
      //Serial.print("20");
      Wire.write("20");
      Wire.write((int)(DS1302Buffer.Year / 10) + 48);
      Wire.write((int)(DS1302Buffer.Year % 10) + 48);
    }
    //Serial.print('-');
    Wire.write("-");
    */
    //////Month////////////////////////////////////
    //Serial.print(DS1302Buffer.Month);
    Wire.write((int)((DS1302Buffer.Month / 10) % 10) + 48);
    Wire.write((int)(DS1302Buffer.Month % 10) + 48);
    //Serial.print('-');
    Wire.write("-");
    //////Day////////////////////////////////////
    //Serial.print(DS1302Buffer.Day);
    Wire.write((int)((DS1302Buffer.Day / 10) % 10) + 48);
    Wire.write((int)(DS1302Buffer.Day % 10) + 48);
    //Serial.print("   ");
    Wire.write("_");
    /*
    //////Week////////////////////////////////////
     switch(DS1302Buffer.Week)
     {
     case 1:
     //Serial.println("Mon");				 //显示星期一
     Wire.write("Mon");
     break;
     case 2:
     //Serial.println("Tue");				 //显示星期二
     Wire.write("Tue");
     break;
     case 3:
     //Serial.println("Wed");				 //显示星期三
     Wire.write("Wed");
     break;
     case 4:
     //Serial.println("Thu");				 //显示星期四
     Wire.write("Thu");
     break;
     case 5:
     //Serial.println("Fri");				 //显示星期五
     Wire.write("Fri");
     break;
     case 6:
     //Serial.println("Sat");				 //显示星期六
     Wire.write("Sat");
     break;
     case 7:
     //Serial.println("Sun");				 //显示星期日
     Wire.write("Sun");
     break;
     default :
     break;
     }
     Wire.write(" ");
     */
    //////Hour////////////////////////////////////
    //Serial.print(DS1302Buffer.Hour);
    Wire.write((int)((DS1302Buffer.Hour / 10) % 10) + 48);
    Wire.write((int)(DS1302Buffer.Hour % 10) + 48);
    //Serial.print(':');
    Wire.write(":");
    /////////// Minute ////////////////////
    //Serial.print(DS1302Buffer.Minute);
    Wire.write((int)((DS1302Buffer.Minute / 10) % 100) + 48);
    Wire.write((int)(DS1302Buffer.Minute % 10) + 48);

    //Serial.print(':');
    Wire.write(":");
    //Serial.println(DS1302Buffer.Second);
    /////////// Second /////////////////////////////
    Wire.write((int)((DS1302Buffer.Second / 10) % 100) + 48);
    Wire.write((int)(DS1302Buffer.Second % 10) + 48);
    Wire.endTransmission();
    delay(100);
    /////////////////////////////////////////////////////////////////////////////////////
    Wire.beginTransmission(3);
    Wire.write("#");
    // 温度
    Wire.write((myDHT11.TEM_Buffer_Int / 10) + 48); ///温度十位
    Wire.write((myDHT11.TEM_Buffer_Int % 10) + 48); ///温度个位
    //Wire.write(myDHT11.TEM_Buffer_Int); //写温度值
    Wire.write("#");
    // 湿度
    Wire.write((myDHT11.HUMI_Buffer_Int / 10) + 48); ///湿度十位
    Wire.write((myDHT11.HUMI_Buffer_Int % 10) + 48); ///湿度个位
    //Wire.write(myDHT11.HUMI_Buffer_Int);//写湿度值
    Wire.write("#");
    // PM2.5
    Wire.write((int)(dustdensity * 10) + 48); ///Dust 10^2位
    Wire.write((int)(dustdensity * 100) % 10 + 48); ///Dust 10^1位
    Wire.write(((int)(dustdensity * 1000) % 10) + 48); ///Dust 10^1位
    //Wire.write((int)(dustdensity*1000));
    Wire.write("#");
    // light
    if (ADBuffer > 990)					//ADBuffer值大于设定值，相当于光照强度小于设定值
    {
      //digitalWrite(LED,LOW);		//点亮LED
      Wire.write("D");
    }
    else
    {
      //digitalWrite(LED,HIGH);		//关闭LED
      Wire.write("B");
    }
    //Wire.write(ADBuffer);
    Wire.write("#");
    // 烟雾报警
    if ( digitalRead(Sensor_DO) == LOW )		//当DO引脚接收到低电平时候说明，模拟值超过比较器阀值
    { //通过调节传感器上的电位器可以改变阀值
      Wire.write("A");		//报警
    }
    else
    {
      Wire.write("N");		//No 报警
    }
    Wire.write("#");
    // 振动报警
    if ( digitalRead(Vibration_DO) == LOW )
    { //通过调节传感器上的电位器可以改变阀值
      Wire.write("N");		//无振动
    }
    else
    {
      Wire.write("V");		//振动报警
    }
    Wire.write("#");

    //////// 人体热感应
    if (PepleValue > 500)
    {
      Wire.write("Y");
    }
    else
    {
      Wire.write("N");
    }
    Wire.write('\n');	// 换行符
    Wire.write("$");	// 结束符
    Wire.endTransmission();
    delay(1000);		//延迟1秒
  }
  ///////////////////////////////////////////////////////////////////////
  delay(300);  //刷新时间
}
//////////////////////////////////////////////////////////////////////////
/////////////////////////END/////////////////////////////////////////////


////////////////////   下面是处理函数   //////////////////////


/////////////////////////////////////////////////////////////////////////////
///////////////////////////Clock/////////////////////////////////////
void Display_RTCC()
{

  if (DS1302Buffer.Year < 10)
  {
    Serial.print("200");
    //Wire.write("200");
  }
  else
  {
    Serial.print("20");
    //Wire.write("20");
  }
  Serial.print(DS1302Buffer.Year);
  //Wire.write(DS1302Buffer.Year);
  Serial.print('-');
  //Wire.write("-");
  Serial.print(DS1302Buffer.Month);
  //Wire.write(DS1302Buffer.Month);
  Serial.print('-');
  //Wire.write("-");
  Serial.print(DS1302Buffer.Day);
  //Wire.write(DS1302Buffer.Day);

  Serial.print(" ");
  //Wire.write(" ");
  switch (DS1302Buffer.Week)
  {
    case 1:
      Serial.print("Mon");				 //显示星期一
      //Wire.write("Mon");
      break;
    case 2:
      Serial.print("Tue");				 //显示星期二
      //Wire.write("Tue");
      break;
    case 3:
      Serial.print("Wed");				 //显示星期三
      //Wire.write("Wed");
      break;
    case 4:
      Serial.print("Thu");				 //显示星期四
      //Wire.write("Thu");
      break;
    case 5:
      Serial.print("Fri");				 //显示星期五
      //Wire.write("Fri");
      break;
    case 6:
      Serial.print("Sat");				 //显示星期六
      //Wire.write("Sat");
      break;
    case 7:
      Serial.print("Sun");				 //显示星期日
      //Wire.write("Sun");
      break;
    default :
      break;
  }
  Serial.print(" ");
  //Wire.write(" ");
  Serial.print(DS1302Buffer.Hour);
  //Wire.write(DS1302Buffer.Hour);
  Serial.print(':');
  //Wire.write(":");
  Serial.print(DS1302Buffer.Minute);
  //Wire.write(DS1302Buffer.Minute);
  Serial.print(':');
  //Wire.write(":");
  Serial.print(DS1302Buffer.Second);
  //Wire.write(DS1302Buffer.Second);
}

/////////////////////////////////////////////////////////////
/////////////  Buzzer Alarm ////////////////////////////////
void BuzzerAlarm()
{
  for (int PotBuffer = 512; PotBuffer < 768; PotBuffer++)
  {
    for (int i = 0 ; i < 10 ; i++)		//循环10次
    {
      digitalWrite(Buzzer, HIGH);		//设置输出高电平
      delayMicroseconds(PotBuffer);	        //延时PotBuffer值 us
      digitalWrite(Buzzer, LOW);			//设置输出低电平
      delayMicroseconds(PotBuffer);		//延时PotBuffer值 us
    }
  }
  for (int PotBuffer = 768; PotBuffer > 512; PotBuffer--)
  {
    for (int i = 0 ; i < 10 ; i++)		//循环10次
    {
      digitalWrite(Buzzer, HIGH);		//设置输出高电平
      delayMicroseconds(PotBuffer);	        //延时PotBuffer值 us
      digitalWrite(Buzzer, LOW);			//设置输出低电平
      delayMicroseconds(PotBuffer);		//延时PotBuffer值 us
    }
  }
}

////////////////////////////////////////////////////////////////////////////////







