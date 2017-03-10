package com.dreammaker.smartroom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.lang.String;
import com.dreammaker.smartroom.DeviceListActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter; //代表本地蓝牙适配器 (蓝牙发射器)，是所有蓝牙交互的入口
import android.bluetooth.BluetoothDevice; //代表一个远端的蓝牙设备,使用它请求远端蓝牙设备连接或者获取远端蓝牙设备的名称、地址、种类、绑定状态
import android.bluetooth.BluetoothSocket; //代表了一个蓝牙套接字的接口
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
//import android.view.Menu;      //如使用菜单加入此三包
//import android.view.MenuInflater;
//import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
//////////////////////////////////
import android.content.Context;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SeekBar.OnSeekBarChangeListener;
//////////////////////////////////////

// Activity 主程序：
public class MainActivity extends Activity implements
		AdapterView.OnItemClickListener, OnSeekBarChangeListener {
	private final static int REQUEST_CONNECT_DEVICE = 1;
	// 宏定义查询设备句柄
	private final static String TAG = "LEO";
	// 定义进程标记，方便进程过滤
	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB"; // SPP服务UUID号

	private InputStream is;				// 输入流，用来接收蓝牙数据
	private TextView text0;				// 提示栏解句柄
	private EditText edit0;				// 发送数据输入句柄
	private TextView dis;				// 接收数据显示句柄
	private ScrollView sv;				// 翻页句柄
	private String smsg = "";			// 显示用数据缓存
	private String fmsg = "";			// 保存用数据缓存
	private String Smartroom_message = "";	// 保存用最新数据缓存
	private String Smartroom_test = "";		// 判断是不是 “20”开头
	private String SmartroomtempText = "";	// 暂存不完整信息
	private String[] SmartRoomtext0;		// 第一次大分组 分割符为'\\n'
	private String[] SmartRoomtext1;		// 第一次大分组 分割符为'\\#'
	private int TextNumber = 0;			// 记录 '\n' 个数
	private int FramNumber = 0;			// 记录 '\n' 个数
	private int SeekBarNum;				// 记录SeekBar 的数值
	private OutputStream outStream = null; // 发送流
	// /////////////////////////////////////////
	// ///////// 数据显示 ////////////////
	private TextView Temperature;		// 温度数据显示句柄
	private TextView Humidness;			// 湿度数据显示句柄
	private TextView PM2_5;				// PM2.5数据显示句柄
	private TextView Light_I;			// 光强数据显示句柄
	private TextView SMOCK;				// 烟雾数据显示句柄
	private TextView Fan;				// 滑动块数据显示句柄
	private TextView Vibration;			// 振动显示
	// private TextView People;			// 人热量数据显示句柄
	// ////////////////////////////////////////

	private Button mButtonLight, mButtonDoor, mButtonCurtain, mButtonlightOff,
			mButtonlight3, mButtonFAN;

	public String filename = "";		// 用来保存存储的文件名
	BluetoothDevice _device = null;		// 蓝牙设备
	BluetoothSocket _socket = null;		// 蓝牙通信socket
	boolean _discoveryFinished = false;
	boolean bRun = true;
	boolean bThread = false;

	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
	// 获取本地蓝牙适配器，即蓝牙设备

	// ////////SeekBar///////
	private SeekBar mSeekBar;
	// ///////////data base/////////////////////
	private SmartRoomDB mROOMsDB; // 数据库
	private Cursor mCursor;
	private String ROOMTIME;		// 时间
	private String ROOMT;			// 温度
	private String ROOMH;			// 湿度
	private String ROOMPM;			// PM2.5
	private String ROOML;			// 光强
	private String ROOMSMO;			// 烟雾
	private String ROOMVibration;	// 振动
	private String ROOMR;			// 热度
	private ListView ROOMsList;
	private int ROOM_ID = 0;
	protected final static int MENU_ADD = Menu.FIRST;
	protected final static int MENU_DELETE = Menu.FIRST + 1;
	protected final static int MENU_UPDATE = Menu.FIRST + 2;

	// ////// DataBase over /////////////

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);							// 设置画面为主画面 main.xml
		text0 = (TextView) findViewById(R.id.Text0);			// 得到提示栏句柄
		edit0 = (EditText) findViewById(R.id.Edit0);			// 得到输入框句柄
		sv = (ScrollView) findViewById(R.id.ScrollView01);		// 得到翻页句柄
		dis = (TextView) findViewById(R.id.in);					// 得到数据显示句柄
		Temperature = (TextView) findViewById(R.id.textViewTv);	// 温度数据显示句柄
		Humidness = (TextView) findViewById(R.id.textViewHv);	// 湿度数据显示句柄
		PM2_5 = (TextView) findViewById(R.id.textViewPMv);		// PM2.5数据显示句柄
		Light_I = (TextView) findViewById(R.id.textViewLv);		// 光强数据显示句柄
		SMOCK = (TextView) findViewById(R.id.textViewSMOv);		// 烟雾数据显示句柄
		Fan = (TextView) findViewById(R.id.textViewFan);		// 滚动条数据显示句柄
		Vibration = (TextView) findViewById(R.id.textViewVibrationV); // 滚动条数据显示句柄

		// /////////////////////////////////////////////////////////////////
		mSeekBar = (SeekBar) findViewById(R.id.seekBarAdjust); // 风扇
		// 设置 SeekBar 的最大值
		mSeekBar.setMax(9);
		// 设置监听器，监听进度条的改变状态
		mSeekBar.setOnSeekBarChangeListener(this);
		// ///////////////////////////////////////////////////////////
		// ///////////////////////////////
		// 按钮的控制代码：
		/**
		 * 命令定义：
		 * 
		 * 窗帘： A 拉起窗帘 B 放下窗帘 后边加圈数，设置为2比较适中 A2 拉起 B2 放下
		 * 
		 * 门： C+数字 为开门的角度 一般是[1,6] 6为关闭门 1为开启门
		 * 
		 * D为开启门后5S钟后再关闭门
		 * 
		 * 灯： LO为开灯 LF为关灯
		 * 
		 * 风扇： F+数字 一般取值[0,9] 0为关闭 9为最大转速
		 * 
		 * LED 三色灯： E+数字 0 为关闭 1 为红色 2 为绿色 3 为蓝色 4 为黄色 5 为紫色 6 为青色 7 为白色
		 * 
		 * 模式： 起床模式 G (开灯、开窗帘) 离开模式 Q (关闭所有设备)
		 **/
		// 灯 按钮 发出指令d 打开灯
		mButtonLight = (Button) findViewById(R.id.buttonlightOn);
		mButtonLight.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String message; // 定义文字
				byte[] msgBuffer; // 定义文字数组
				// try 异常处理 如果出错，可以直接跳过，防止程序出错直接崩溃
				try {
					outStream = _socket.getOutputStream();
				} catch (IOException e) {
					Log.e(TAG, "Light System oprate ON", e);// 返回错误信息
				}
				message = "LO"; // 定义文字
				text0.setText("发送: " + message);
				msgBuffer = message.getBytes(); // 得到编码
				try {
					outStream.write(msgBuffer);
				} catch (IOException e) {
					Log.e(TAG, "Light System oprate ON", e);
				}
			}
		});

		// 关灯
		mButtonlightOff = (Button) findViewById(R.id.buttonlightOff);
		mButtonlightOff.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String message; // 定义文字
				byte[] msgBuffer; // 定义文字数组
				// try 异常处理 如果出错，可以直接跳过，防止程序出错直接崩溃
				try {
					outStream = _socket.getOutputStream();
				} catch (IOException e) {
					Log.e(TAG, "Light System oprate OFF", e);// 返回错误信息

				}
				message = "LF"; // 定义文字
				text0.setText("发送: " + message);
				msgBuffer = message.getBytes(); // 得到编码
				try {
					outStream.write(msgBuffer);
				} catch (IOException e) {
					Log.e(TAG, "Light System oprate OFF", e);
				}
			}
		});

		// 三色灯
		mButtonlight3 = (Button) findViewById(R.id.buttonLight3);
		mButtonlight3.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String message; // 定义文字

				byte[] msgBuffer; // 定义文字数组
				// try 异常处理 如果出错，可以直接跳过，防止程序出错直接崩溃
				try {
					outStream = _socket.getOutputStream();
				} catch (IOException e) {
					Log.e(TAG, "3 Color LED System oprate ", e);// 返回错误信息

				}
				message = "E" + SeekBarNum; // 定义文字
				text0.setText("发送: " + message);
				msgBuffer = message.getBytes(); // 得到编码
				try {
					outStream.write(msgBuffer);
				} catch (IOException e) {
					Log.e(TAG, "3 Color LED System oprate", e);
				}

			}

		});

		// 风扇控制 按钮 发出指令F+ number 改变转速
		mButtonFAN = (Button) findViewById(R.id.buttonfan);
		mButtonFAN.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String message; // 定义文字
				byte[] msgBuffer; // 定义文字数组
				// try 异常处理 如果出错，可以直接跳过，防止程序出错直接崩溃
				try {
					outStream = _socket.getOutputStream();
				} catch (IOException e) {
					Log.e(TAG, "Fan System oprate", e);// 返回错误信息
				}
				message = "F" + SeekBarNum; // 改变风扇转速
				text0.setText("发送: " + message);
				msgBuffer = message.getBytes(); // 得到编码
				try {
					outStream.write(msgBuffer);
				} catch (IOException e) {
					Log.e(TAG, "Fan System oprate", e); // 反馈错误
				}
			}
		});

		// 门 按钮
		mButtonDoor = (Button) findViewById(R.id.buttondoor);
		mButtonDoor.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String message; // 定义文字
				byte[] msgBuffer; // 定义文字数组
				// try 异常处理 如果出错，可以直接跳过，防止程序出错直接崩溃
				try {
					outStream = _socket.getOutputStream();
				} catch (IOException e) {
					Log.e(TAG, "Light System oprate ON", e);// 返回错误信息
				}
				message = "D"; // 定义文字
				text0.setText("发送: " + message);
				msgBuffer = message.getBytes(); // 得到编码
				try {
					outStream.write(msgBuffer);
				} catch (IOException e) {
					Log.e(TAG, "Door oprate", e);
				}
			}
		});
		// 窗帘 按钮
		mButtonCurtain = (Button) findViewById(R.id.buttoncurtain);
		mButtonCurtain.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String message; // 定义文字
				byte[] msgBuffer; // 定义文字数组
				// try 异常处理 如果出错，可以直接跳过，防止程序出错直接崩溃
				try {
					outStream = _socket.getOutputStream();
				} catch (IOException e) {
					Log.e(TAG, "Curtain System oprate", e);// 返回错误信息
				}
				if (SeekBarNum == 4) {

				} else if (SeekBarNum > 4) {
					message = "B" + (SeekBarNum - 4); // 改变窗帘
					text0.setText("发送: " + message);
					msgBuffer = message.getBytes(); // 得到编码
					try {
						outStream.write(msgBuffer);
					} catch (IOException e) {
						Log.e(TAG, "Curtain System oprate", e);
					}
				} else {
					message = "A" + (4 - SeekBarNum); // 改变窗帘
					text0.setText("发送: " + message);
					msgBuffer = message.getBytes(); // 得到编码
					try {
						outStream.write(msgBuffer);
					} catch (IOException e) {
						Log.e(TAG, "Curtain System oprate", e);
					}
				}

			}
		});
		// //////////////////////////////////////---------------------------------------------///////////////////////////////////

		// 如果打开本地蓝牙设备不成功，提示信息，结束程序

		if (_bluetooth == null) {

			Toast.makeText(this, "无法打开手机蓝牙，请确认手机是否有蓝牙功能！", Toast.LENGTH_LONG)
					.show();
			finish();
			return;
		}

		// 设置设备可以被搜索
		new Thread() {
			public void run() {
				if (_bluetooth.isEnabled() == false) {
					_bluetooth.enable();
				}
			}
		}.start();
		// //////
	}

	// ////////////////----------------------///////////////////////////////////////

	// /////////////////// OnCreate 结束/////////////

	/*
	 * 当进度条的进度发生变化时调用该方法 seekBar,当前的 SeekBar progress，SeekBar 的当前进度 fromUser
	 * 是否是用户改变进度的
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		Fan.setText("0" + progress);
		SeekBarNum = progress;
		Log.v("TAG", "onProgressChanged-->seekBar=" + seekBar.getId()
				+ "progress=" + progress + "fromUser=" + fromUser);
	}

	// 发送按键响应
	public void onSendButtonClicked(View v) {
		int i = 0;
		int n = 0;
		try {
			OutputStream os = _socket.getOutputStream(); // 蓝牙连接输出流
			byte[] bos = edit0.getText().toString().getBytes();
			for (i = 0; i < bos.length; i++) {
				if (bos[i] == 0x0a)
					n++;
			}
			byte[] bos_new = new byte[bos.length + n];
			n = 0;
			for (i = 0; i < bos.length; i++) { // 手机中换行为0a,将其改为0d 0a后再发送
				if (bos[i] == 0x0a) {
					bos_new[n] = 0x0d;
					n++;
					bos_new[n] = 0x0a;
				} else {
					bos_new[n] = bos[i];
				}
				n++;
			}
			os.write(bos_new);
		} catch (IOException e) {
		}
	}

	// 接收活动结果，响应startActivityForResult()
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE: // 连接结果，由DeviceListActivity设置返回
			// 响应返回结果
			if (resultCode == Activity.RESULT_OK) {
				// 连接成功，由 Device List Activity 设置返回
				// MAC地址，由DeviceListActivity设置返回
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// 得到蓝牙设备句柄
				_device = _bluetooth.getRemoteDevice(address);
				// 用服务号得到socket
				try {
					_socket = _device.createRfcommSocketToServiceRecord(UUID
							.fromString(MY_UUID));
				} catch (IOException e) {
					Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
				}
				// 连接socket
				Button btn = (Button) findViewById(R.id.Button03);
				try {
					_socket.connect();
					Toast.makeText(this, "连接" + _device.getName() + "成功！",
							Toast.LENGTH_SHORT).show();
					btn.setText("断开");
				} catch (IOException e) {
					try {
						Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT)
								.show();
						_socket.close();
						_socket = null;
					} catch (IOException ee) {
						Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT)
								.show();
					}
					return;
				}
				// 打开接收线程
				try {
					is = _socket.getInputStream(); // 得到蓝牙数据输入流
				} catch (IOException e) {
					Toast.makeText(this, "接收数据失败！", Toast.LENGTH_SHORT).show();
					return;
				}
				if (bThread == false) {
					ReadThread.start();
					bThread = true;
				} else {
					bRun = true;
				}
			}
			break;
		default:
			break;
		}
	}

	// 接收数据线程
	Thread ReadThread = new Thread() {
		public void run() {
			int num = 0;
			byte[] buffer = new byte[2048];
			byte[] buffer_new = new byte[2048];
			int i = 0;
			int n = 0;
			bRun = true;
			// 接收线程
			while (true) {
				try {
					while (is.available() == 0) {
						while (bRun == false) {
						}
					}

					// 延时程序
					try {
						Thread.currentThread();
						Thread.sleep(1000); // 延时时间
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// 完成延时
					while (true) {
						num = is.read(buffer); // 读入数据
						n = 0;
						String s0 = new String(buffer, 0, num);
						fmsg += s0; // 保存收到数据
						for (i = 0; i < num; i++) {
							if ((buffer[i] == 0x0d) && (buffer[i + 1] == 0x0a)) {
								buffer_new[n] = 0x0a;// 把 \r(回车) \n(换行)
								// 转换为\n(换行)
								// TextNumber++;
								i++;
							} else if (buffer[i] == 0x24) {
								// 0x24 是 '$'这个符号
								TextNumber++; // 记录出现的次数
								buffer_new[n] = buffer[i];
							} else {
								buffer_new[n] = buffer[i];
							}
							n++;
						}
						String s = new String(buffer_new, 0, n);
						Smartroom_message = s;
						smsg += s; // 写入接收缓存
						/*
						// 延时程序
						try {
							Thread.currentThread();
							Thread.sleep(500); // 延时时间
						} catch (InterruptedException e) {
							e.printStackTrace();
						}*/

						if (is.available() == 0) {
							break; // 短时间没有数据才跳出进行显示
						}
					}
					/*
					// 延时程序
					try {
						Thread.currentThread();
						Thread.sleep(500); // 延时时间
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					*/
					// 发送显示消息，进行显示刷新
					handler.sendMessage(handler.obtainMessage());
					// //
					// //////////
				} catch (IOException e) {
				}
			}
		}
	};
	// 消息处理队列

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			Smartroom_test = Smartroom_message.substring(0, 2);
			FramNumber = TextNumber;
			// 如果是月份(01、02……) 开头
			if (Smartroom_test.equals("01") || Smartroom_test.equals("02")
					|| Smartroom_test.equals("03")
					|| Smartroom_test.equals("04")
					|| Smartroom_test.equals("05")
					|| Smartroom_test.equals("06")
					|| Smartroom_test.equals("07")
					|| Smartroom_test.equals("08")
					|| Smartroom_test.equals("09")
					|| Smartroom_test.equals("10")
					|| Smartroom_test.equals("11")
					|| Smartroom_test.equals("12")) {
				// 出现字符分割问题
				SmartRoomtext0 = Smartroom_message.split("\\$"); // 分隔字符
				int number = 0;
				// 如果没有结束符
				if (FramNumber == 0) {
					SmartroomtempText = Smartroom_message;
				}
				// 如果只有一句话
				else if (FramNumber == 1) {
					SmartRoomtext1 = SmartRoomtext0[number].split("\\#"); // 分隔字符
					try {
						setUpViews(); // 数据库初始化
						add(); // 添加到数据库
					} catch (IOError e) {
						Log.e("DataLeo", "Add in Database!");
					}
					Temperature.setText(SmartRoomtext1[1] + "C"); // 传输温度
					Humidness.setText(SmartRoomtext1[2] + "%"); // 传输湿度
					PM2_5.setText(SmartRoomtext1[3]); // 传输PM2.5
					Light_I.setText(SmartRoomtext1[4]); // 传输光强
					SMOCK.setText(SmartRoomtext1[5]); // 传输烟雾
					Vibration.setText(SmartRoomtext1[6]); // 传输振动
				} else { // 存在很多语句
					for (number = 0; number < (FramNumber); number++) {
						Smartroom_test = SmartRoomtext0[number].substring(0, 2);
						if (Smartroom_test.equals("01")
								|| Smartroom_test.equals("02")
								|| Smartroom_test.equals("03")
								|| Smartroom_test.equals("04")
								|| Smartroom_test.equals("05")
								|| Smartroom_test.equals("06")
								|| Smartroom_test.equals("07")
								|| Smartroom_test.equals("08")
								|| Smartroom_test.equals("09")
								|| Smartroom_test.equals("10")
								|| Smartroom_test.equals("11")
								|| Smartroom_test.equals("12")) {
							// 语句是 月份(01、02……) 开头的
							SmartRoomtext1 = SmartRoomtext0[number]
									.split("\\#"); // 分隔字符
							try {
								setUpViews(); // 数据库初始化
								add(); // 添加到数据库
							} catch (IOError e) {
								Log.e("DataLeo", "Add in Database!");
							}

							Temperature.setText(SmartRoomtext1[1] + "C"); // 传输温度
							Humidness.setText(SmartRoomtext1[2] + "%"); // 传输湿度
							PM2_5.setText(SmartRoomtext1[3]); // 传输PM2.5
							Light_I.setText(SmartRoomtext1[4]); // 传输光强
							SMOCK.setText(SmartRoomtext1[5]); // 传输烟雾
							Vibration.setText(SmartRoomtext1[6]); // 传输振动
						} else { // 语句不是20开头的时候
							text0.setText(SmartRoomtext0[number]);
						}
					}
					FramNumber = 0;
					TextNumber = 0;
				}
				FramNumber = 0;
				TextNumber = 0;
			} else // 开头不是月份(01、02……) 开头的情况
			{
				SmartRoomtext0 = Smartroom_message.split("\\$"); // 分隔字符

				int number = 0;
				if (FramNumber == 1) {
					SmartroomtempText = SmartroomtempText + Smartroom_message; // 看看组合会不会是我们需要的
					if (SmartroomtempText.length() > 34
							&& SmartroomtempText.length() < 36) // 通过字符串长度判读
					{
						SmartRoomtext1 = SmartroomtempText.split("\\#"); // 分隔字符
						try {
							setUpViews(); // 数据库初始化
							add(); // 添加到数据库
						} catch (IOError e) {
							Log.e("DataLeo", "Add in Database!");
						}
						Temperature.setText(SmartRoomtext1[1] + "C"); // 传输温度
						Humidness.setText(SmartRoomtext1[2] + "%"); // 传输湿度
						PM2_5.setText(SmartRoomtext1[3]); // 传输PM2.5
						Light_I.setText(SmartRoomtext1[4]); // 传输光强
						SMOCK.setText(SmartRoomtext1[5]); // 传输烟雾
						Vibration.setText(SmartRoomtext1[6]); // 传输振动
						SmartroomtempText = "";
					} else {
						text0.setText(SmartRoomtext0[number]); // 显示返回字符串
						FramNumber = 0;
						TextNumber = 0;
					}
				} else if (FramNumber == 0) {
					text0.setText(SmartRoomtext0[number]);
					FramNumber = 0;
					TextNumber = 0;
				} else {
					for (number = 1; number < (TextNumber); number++) {
						Smartroom_test = SmartRoomtext0[number].substring(0, 2);
						if (Smartroom_test.equals("01")
								|| Smartroom_test.equals("02")
								|| Smartroom_test.equals("03")
								|| Smartroom_test.equals("04")
								|| Smartroom_test.equals("05")
								|| Smartroom_test.equals("06")
								|| Smartroom_test.equals("07")
								|| Smartroom_test.equals("08")
								|| Smartroom_test.equals("09")
								|| Smartroom_test.equals("10")
								|| Smartroom_test.equals("11")
								|| Smartroom_test.equals("12")) { // 如果开头是月份(01、02……)
																	// 则是我们需要的数据
							SmartRoomtext1 = SmartRoomtext0[number]
									.split("\\#"); // 分隔字符
							try {
								setUpViews(); // 数据库初始化
								add(); // 添加到数据库
							} catch (IOError e) {
								Log.e("DataLeo", "Add in Database!");
							}
							// 显示
							Temperature.setText(SmartRoomtext1[1] + "C"); // 传输温度
							Humidness.setText(SmartRoomtext1[2] + "%"); // 传输湿度
							PM2_5.setText(SmartRoomtext1[3]); // 传输PM2.5
							Light_I.setText(SmartRoomtext1[4]); // 传输光强
							SMOCK.setText(SmartRoomtext1[5]); // 传输烟雾
							Vibration.setText(SmartRoomtext1[6]); // 传输振动
						}
					}
					// TextNumber=TextNumber-FramNumber;
					FramNumber = 0;
					TextNumber = 0;
				}
				FramNumber = 0;
				TextNumber = 0;
			}
			// SmartroomtempText=""; // 清除暂存字符
			TextNumber = 0;
			smsg += "\n"; // 加一个回车
			dis.setText(smsg); // 显示数据
			sv.scrollTo(0, dis.getMeasuredHeight()); // 跳至数据最后一页
		}
	};

	// 关闭程序掉用处理部分
	public void onDestroy() {
		super.onDestroy();
		if (_socket != null) // 关闭连接socket
			try {
				_socket.close();
			} catch (IOException e) {
			}
		_bluetooth.disable(); // 关闭蓝牙服务
	}

	// 菜单处理部分
	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) {// 建立菜单
	 * 
	 * MenuInflater inflater = getMenuInflater();
	 * 
	 * inflater.inflate(R.menu.option_menu, menu); return true; }
	 */

	/*
	 * 
	 * @Override public boolean onOptionsItemSelected(MenuItem item) { //菜单响应函数
	 * 
	 * switch (item.getItemId()) { case R.id.scan:
	 * 
	 * if(_bluetooth.isEnabled()==false){ Toast.makeText(this, "Open BT......",
	 * 
	 * Toast.LENGTH_LONG).show(); return true; } // Launch the
	 * 
	 * DeviceListActivity to see devices and do scan Intent serverIntent = new
	 * 
	 * Intent(this, DeviceListActivity.class);
	 * 
	 * startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE); return
	 * 
	 * true; case R.id.quit: finish(); return true; case R.id.clear: smsg="";
	 * 
	 * ls.setText( smsg); return true; case R.id.save: Save(); return true; }
	 * 
	 * return false; }
	 */

	// 连接按键响应函数

	public void onConnectButtonClicked(View v) {
		if (_bluetooth.isEnabled() == false) { // 如果蓝牙服务不可用则提示
			Toast.makeText(this, " 打开蓝牙中...", Toast.LENGTH_LONG).show();
			return;
		}
		// 如未连接设备则打开DeviceListActivity进行设备搜索
		Button btn = (Button) findViewById(R.id.Button03);
		if (_socket == null) {
			Intent serverIntent = new Intent(this, DeviceListActivity.class); // 跳转程序设置
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE); // 设置返回宏定义
		} else {
			// 关闭连接socket
			try {
				is.close();
				_socket.close();
				_socket = null;
				bRun = false;
				btn.setText("连接");
			} catch (IOException e) {
			}
		}
		return;
	}

	// 保存按键响应函数
	public void onSaveButtonClicked(View v) {
		Save();
	}

	// 清除按键响应函数

	public void onClearButtonClicked(View v) {
		smsg = "";
		fmsg = "";
		text0.setText(smsg); // 清除 顶端
		dis.setText(smsg);
		return;
	}

	// 退出按键响应函数
	public void onQuitButtonClicked(View v) {
		finish();

	}

	// 保存功能实现
	@SuppressLint("InflateParams")
	private void Save() {

		// 显示对话框输入文件名

		LayoutInflater factory = LayoutInflater.from(MainActivity.this); // 图层模板生成器句柄
		final View DialogView = factory.inflate(R.layout.sname, null); // 用sname.xml模板生成视图模板
		new AlertDialog.Builder(MainActivity.this).setTitle("文件名")
				.setView(DialogView) // 设置视图模板
				.setPositiveButton("确定", new DialogInterface.OnClickListener() // 确定按键响应函数
						{
							public void onClick(DialogInterface dialog,
									int whichButton) {
								EditText text1 = (EditText) DialogView
										.findViewById(R.id.sname); // 得到文件名输入框句柄
								filename = text1.getText().toString(); // 得到文件名
								try {
									if (Environment.getExternalStorageState()
											.equals(Environment.MEDIA_MOUNTED)) { // 如果SD卡已准备好
										filename = filename + ".txt"; // 在文件名末尾加上.
										// txt
										File sdCardDir = Environment
												.getExternalStorageDirectory(); // 得到SD卡根目录
										File BuildDir = new File(sdCardDir,
												"/data"); // 打开data目录，如不存在则生成
										if (BuildDir.exists() == false)
											BuildDir.mkdirs();
										File saveFile = new File(BuildDir,
												filename); // 新建文件句柄，如已存在仍新建文档
										FileOutputStream stream = new FileOutputStream(
												saveFile); // 打开文件输入流
										stream.write(fmsg.getBytes());
										stream.close();
										Toast.makeText(MainActivity.this,
												"存储成功！", Toast.LENGTH_SHORT)
												.show();
									} else {
										Toast.makeText(MainActivity.this,
												"没有存储卡！", Toast.LENGTH_LONG)
												.show();
									}
								} catch (IOException e) {
									return;
								}
							}

						}).setNegativeButton("取消", // 取消按键响应函数,直接退出对话框不做任何处理

						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).show(); // 显示对话框
	}

	// ///////////////////////////////////
	// //////////// Data base SQL //////////////////
	public void setUpViews() {
		mROOMsDB = new SmartRoomDB(this);		// 数据库
		mCursor = mROOMsDB.select(); //
		ROOMTIME = SmartRoomtext1[0].toString(); // 时间
		ROOMT = SmartRoomtext1[1].toString();	// 温度
		ROOMH = SmartRoomtext1[2].toString();	// 湿度
		ROOMPM = SmartRoomtext1[3].toString();	// PM2.5
		ROOML = SmartRoomtext1[4].toString();	// 光强
		ROOMSMO = SmartRoomtext1[5].toString(); // 烟雾
		ROOMVibration = SmartRoomtext1[6].toString(); // 振动
		ROOMR = SmartRoomtext1[7].toString();	// 热度
		ROOMsList = (ListView) findViewById(R.id.roomlist); // 数据表列表
		ROOMsList.setAdapter(new ROOMsListAdapter(this, mCursor)); // 调用列表显示
		ROOMsList.setOnItemClickListener(this); // 选取列表传入
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) { // 菜单处理
		super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE, MENU_ADD, 0, "ADD"); // 添加
		menu.add(Menu.NONE, MENU_DELETE, 0, "DELETE"); // 删除
		menu.add(Menu.NONE, MENU_UPDATE, 0, "UPDATE"); // 更新
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case MENU_ADD: // 判断
			add();
			break;
		case MENU_DELETE: // 删除
			delete();
			break;
		case MENU_UPDATE: // 更新
			update();
			break;
		}
		return true;
	}

	public void add() {
		String mROOMTIME = ROOMTIME;	// 导入时间
		String mROOMT = ROOMT;			// 导入温度
		String mROOMH = ROOMH;			// 导入湿度
		String mROOMPM = ROOMPM;		// 导入PM2.5
		String mROOML = ROOML;			// 导入光强
		String mROOMSMO = ROOMSMO;		// 导入烟雾
		String mROOMVibration = ROOMVibration; // 导入振动
		String mROOMR = ROOMR;			// 导入热量
		// 各个字符串都不能为空，或者退出
		if (mROOMTIME.equals("") || mROOMT.equals("") || mROOMH.equals("")
				|| mROOMPM.equals("") || mROOML.equals("")
				|| mROOMSMO.equals("") || mROOMVibration.equals("")
				|| mROOMR.equals("")) {
			return;
		}
		mROOMsDB.insert(mROOMTIME, mROOMT, mROOMH, mROOMPM, mROOML, mROOMSMO,
				mROOMVibration, mROOMR); // 添加进入数据库
		mCursor.requery(); // ？？？
		ROOMsList.invalidateViews();
		ROOMTIME = "";
		ROOMT = "";
		ROOMH = "";
		ROOMPM = "";
		ROOML = "";
		ROOMSMO = "";
		ROOMVibration = "";
		ROOMR = "";
		Toast.makeText(this, "Add Successed!", Toast.LENGTH_SHORT).show();

	}

	public void delete() {
		if (ROOM_ID == 0) {
			return;
		}
		mROOMsDB.delete(ROOM_ID);
		mCursor.requery();
		ROOMsList.invalidateViews();
		ROOMTIME = "";
		ROOMT = "";
		ROOMH = "";
		ROOMPM = "";
		ROOML = "";
		ROOMSMO = "";
		ROOMVibration = "";
		ROOMR = "";
		Toast.makeText(this, "Delete Successed!", Toast.LENGTH_SHORT).show();

	}

	public void update() {
		String mROOMTIME = ROOMTIME; // 导入时间
		String mROOMT = ROOMT; // 导入温度
		String mROOMH = ROOMH; // 导入湿度
		String mROOMPM = ROOMPM; // 导入PM2.5
		String mROOML = ROOML; // 导入光强
		String mROOMSMO = ROOMSMO; // 导入烟雾
		String mROOMVibration = ROOMVibration; // 导入振动
		String mROOMR = ROOMR; // 导入热量
		// 各项都不能为空，或者退出
		if (mROOMTIME.equals("") || mROOMT.equals("") || mROOMH.equals("")
				|| mROOMPM.equals("") || mROOML.equals("")
				|| mROOMSMO.equals("") || mROOMVibration.equals("")
				|| mROOMR.equals("")) {
			return;
		}
		mROOMsDB.update(ROOM_ID, mROOMTIME, mROOMT, mROOMH, mROOMPM, mROOML,
				mROOMSMO, mROOMVibration, mROOMR);
		mCursor.requery();
		ROOMsList.invalidateViews();
		ROOMTIME = "";
		ROOMT = "";
		ROOMH = "";
		ROOMPM = "";
		ROOML = "";
		ROOMSMO = "";
		ROOMVibration = "";
		ROOMR = "";
		Toast.makeText(this, "Update Successed!", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mCursor.moveToPosition(position);
		ROOM_ID = mCursor.getInt(0); // 列表的 ID值
		ROOMTIME = mCursor.getString(1); // 时间
		ROOMT = mCursor.getString(2); // 温度
		ROOMH = mCursor.getString(3); // 湿度
		ROOMPM = mCursor.getString(4); // PM2.5
		ROOML = mCursor.getString(5); // 光强
		ROOMSMO = mCursor.getString(6); // 烟雾
		ROOMVibration = mCursor.getString(7); // 振动
		ROOMR = mCursor.getString(8); // 人体感应
	}

	public class ROOMsListAdapter extends BaseAdapter {
		private Context mContext;
		private Cursor mCursor;

		public ROOMsListAdapter(Context context, Cursor cursor) {
			mContext = context;
			mCursor = cursor;
		}

		@Override
		public int getCount() {
			return mCursor.getCount();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView mTextView = new TextView(mContext);
			mCursor.moveToPosition(position);
			mTextView.setText(mCursor.getString(1) + " " + mCursor.getString(2)
					+ "C " + mCursor.getString(3) + "%RH "
					+ mCursor.getString(4) + " " + mCursor.getString(5) + " "
					+ mCursor.getString(6) + " " + mCursor.getString(7) + " "
					+ mCursor.getString(8)); // 显示内容
			return mTextView;
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		Log.v("TAG",
				"onStartTrackingTouch  start--->" + "+seekBar="
						+ seekBar.getProgress());

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		Log.v("TAG",
				"onStartTrackingTouch  start--->" + "+seekBar="
						+ seekBar.getProgress());

	}
}
