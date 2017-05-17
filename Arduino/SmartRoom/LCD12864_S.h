#ifndef __LCD12864_P_H__
#define __LCD12864_P_H__

#include <Arduino.h>

//串行模式函数声明
extern void LCD12864_SendByte(unsigned char bbyte);
extern void LCD12864_COM_Write(unsigned char ddata);
extern void LCD12864_Data_Write(unsigned char ddata);
extern void LCD12864_write_word(const char *s);
extern void LCD12864_Reset();
extern void LCD12864_PHOTO_SET();
extern void LCD12864_HAIZI_SET();
extern void LCD12864_PHOTO_WRITE(const unsigned char *img);
extern void LCD12864_Clear();
extern void LCD12864_SET_Address( unsigned char i,unsigned char j);

#endif

