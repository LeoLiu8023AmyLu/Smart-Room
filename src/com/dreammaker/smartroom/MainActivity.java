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
import android.bluetooth.BluetoothAdapter; //���������������� (����������)���������������������
import android.bluetooth.BluetoothDevice; //����һ��Զ�˵������豸,ʹ��������Զ�������豸���ӻ��߻�ȡԶ�������豸�����ơ���ַ�����ࡢ��״̬
import android.bluetooth.BluetoothSocket; //������һ�������׽��ֵĽӿ�
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
//import android.view.Menu;      //��ʹ�ò˵����������
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

// Activity ������
public class MainActivity extends Activity implements
		AdapterView.OnItemClickListener, OnSeekBarChangeListener {
	private final static int REQUEST_CONNECT_DEVICE = 1;
	// �궨���ѯ�豸���
	private final static String TAG = "LEO";
	// ������̱�ǣ�������̹���
	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB"; // SPP����UUID��

	private InputStream is;				// ������������������������
	private TextView text0;				// ��ʾ������
	private EditText edit0;				// ��������������
	private TextView dis;				// ����������ʾ���
	private ScrollView sv;				// ��ҳ���
	private String smsg = "";			// ��ʾ�����ݻ���
	private String fmsg = "";			// ���������ݻ���
	private String Smartroom_message = "";	// �������������ݻ���
	private String Smartroom_test = "";		// �ж��ǲ��� ��20����ͷ
	private String SmartroomtempText = "";	// �ݴ治������Ϣ
	private String[] SmartRoomtext0;		// ��һ�δ���� �ָ��Ϊ'\\n'
	private String[] SmartRoomtext1;		// ��һ�δ���� �ָ��Ϊ'\\#'
	private int TextNumber = 0;			// ��¼ '\n' ����
	private int FramNumber = 0;			// ��¼ '\n' ����
	private int SeekBarNum;				// ��¼SeekBar ����ֵ
	private OutputStream outStream = null; // ������
	// /////////////////////////////////////////
	// ///////// ������ʾ ////////////////
	private TextView Temperature;		// �¶�������ʾ���
	private TextView Humidness;			// ʪ��������ʾ���
	private TextView PM2_5;				// PM2.5������ʾ���
	private TextView Light_I;			// ��ǿ������ʾ���
	private TextView SMOCK;				// ����������ʾ���
	private TextView Fan;				// ������������ʾ���
	private TextView Vibration;			// ����ʾ
	// private TextView People;			// ������������ʾ���
	// ////////////////////////////////////////

	private Button mButtonLight, mButtonDoor, mButtonCurtain, mButtonlightOff,
			mButtonlight3, mButtonFAN;

	public String filename = "";		// ��������洢���ļ���
	BluetoothDevice _device = null;		// �����豸
	BluetoothSocket _socket = null;		// ����ͨ��socket
	boolean _discoveryFinished = false;
	boolean bRun = true;
	boolean bThread = false;

	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
	// ��ȡ�����������������������豸

	// ////////SeekBar///////
	private SeekBar mSeekBar;
	// ///////////data base/////////////////////
	private SmartRoomDB mROOMsDB; // ���ݿ�
	private Cursor mCursor;
	private String ROOMTIME;		// ʱ��
	private String ROOMT;			// �¶�
	private String ROOMH;			// ʪ��
	private String ROOMPM;			// PM2.5
	private String ROOML;			// ��ǿ
	private String ROOMSMO;			// ����
	private String ROOMVibration;	// ��
	private String ROOMR;			// �ȶ�
	private ListView ROOMsList;
	private int ROOM_ID = 0;
	protected final static int MENU_ADD = Menu.FIRST;
	protected final static int MENU_DELETE = Menu.FIRST + 1;
	protected final static int MENU_UPDATE = Menu.FIRST + 2;

	// ////// DataBase over /////////////

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);							// ���û���Ϊ������ main.xml
		text0 = (TextView) findViewById(R.id.Text0);			// �õ���ʾ�����
		edit0 = (EditText) findViewById(R.id.Edit0);			// �õ��������
		sv = (ScrollView) findViewById(R.id.ScrollView01);		// �õ���ҳ���
		dis = (TextView) findViewById(R.id.in);					// �õ�������ʾ���
		Temperature = (TextView) findViewById(R.id.textViewTv);	// �¶�������ʾ���
		Humidness = (TextView) findViewById(R.id.textViewHv);	// ʪ��������ʾ���
		PM2_5 = (TextView) findViewById(R.id.textViewPMv);		// PM2.5������ʾ���
		Light_I = (TextView) findViewById(R.id.textViewLv);		// ��ǿ������ʾ���
		SMOCK = (TextView) findViewById(R.id.textViewSMOv);		// ����������ʾ���
		Fan = (TextView) findViewById(R.id.textViewFan);		// ������������ʾ���
		Vibration = (TextView) findViewById(R.id.textViewVibrationV); // ������������ʾ���

		// /////////////////////////////////////////////////////////////////
		mSeekBar = (SeekBar) findViewById(R.id.seekBarAdjust); // ����
		// ���� SeekBar �����ֵ
		mSeekBar.setMax(9);
		// ���ü������������������ĸı�״̬
		mSeekBar.setOnSeekBarChangeListener(this);
		// ///////////////////////////////////////////////////////////
		// ///////////////////////////////
		// ��ť�Ŀ��ƴ��룺
		/**
		 * ����壺
		 * 
		 * ������ A ������ B ���´��� ��߼�Ȧ��������Ϊ2�Ƚ����� A2 ���� B2 ����
		 * 
		 * �ţ� C+���� Ϊ���ŵĽǶ� һ����[1,6] 6Ϊ�ر��� 1Ϊ������
		 * 
		 * DΪ�����ź�5S�Ӻ��ٹر���
		 * 
		 * �ƣ� LOΪ���� LFΪ�ص�
		 * 
		 * ���ȣ� F+���� һ��ȡֵ[0,9] 0Ϊ�ر� 9Ϊ���ת��
		 * 
		 * LED ��ɫ�ƣ� E+���� 0 Ϊ�ر� 1 Ϊ��ɫ 2 Ϊ��ɫ 3 Ϊ��ɫ 4 Ϊ��ɫ 5 Ϊ��ɫ 6 Ϊ��ɫ 7 Ϊ��ɫ
		 * 
		 * ģʽ�� ��ģʽ G (���ơ�������) �뿪ģʽ Q (�ر������豸)
		 **/
		// �� ��ť ����ָ��d �򿪵�
		mButtonLight = (Button) findViewById(R.id.buttonlightOn);
		mButtonLight.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String message; // ��������
				byte[] msgBuffer; // ������������
				// try �쳣���� �����������ֱ����������ֹ�������ֱ�ӱ���
				try {
					outStream = _socket.getOutputStream();
				} catch (IOException e) {
					Log.e(TAG, "Light System oprate ON", e);// ���ش�����Ϣ
				}
				message = "LO"; // ��������
				text0.setText("����: " + message);
				msgBuffer = message.getBytes(); // �õ�����
				try {
					outStream.write(msgBuffer);
				} catch (IOException e) {
					Log.e(TAG, "Light System oprate ON", e);
				}
			}
		});

		// �ص�
		mButtonlightOff = (Button) findViewById(R.id.buttonlightOff);
		mButtonlightOff.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String message; // ��������
				byte[] msgBuffer; // ������������
				// try �쳣���� �����������ֱ����������ֹ�������ֱ�ӱ���
				try {
					outStream = _socket.getOutputStream();
				} catch (IOException e) {
					Log.e(TAG, "Light System oprate OFF", e);// ���ش�����Ϣ

				}
				message = "LF"; // ��������
				text0.setText("����: " + message);
				msgBuffer = message.getBytes(); // �õ�����
				try {
					outStream.write(msgBuffer);
				} catch (IOException e) {
					Log.e(TAG, "Light System oprate OFF", e);
				}
			}
		});

		// ��ɫ��
		mButtonlight3 = (Button) findViewById(R.id.buttonLight3);
		mButtonlight3.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String message; // ��������

				byte[] msgBuffer; // ������������
				// try �쳣���� �����������ֱ����������ֹ�������ֱ�ӱ���
				try {
					outStream = _socket.getOutputStream();
				} catch (IOException e) {
					Log.e(TAG, "3 Color LED System oprate ", e);// ���ش�����Ϣ

				}
				message = "E" + SeekBarNum; // ��������
				text0.setText("����: " + message);
				msgBuffer = message.getBytes(); // �õ�����
				try {
					outStream.write(msgBuffer);
				} catch (IOException e) {
					Log.e(TAG, "3 Color LED System oprate", e);
				}

			}

		});

		// ���ȿ��� ��ť ����ָ��F+ number �ı�ת��
		mButtonFAN = (Button) findViewById(R.id.buttonfan);
		mButtonFAN.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String message; // ��������
				byte[] msgBuffer; // ������������
				// try �쳣���� �����������ֱ����������ֹ�������ֱ�ӱ���
				try {
					outStream = _socket.getOutputStream();
				} catch (IOException e) {
					Log.e(TAG, "Fan System oprate", e);// ���ش�����Ϣ
				}
				message = "F" + SeekBarNum; // �ı����ת��
				text0.setText("����: " + message);
				msgBuffer = message.getBytes(); // �õ�����
				try {
					outStream.write(msgBuffer);
				} catch (IOException e) {
					Log.e(TAG, "Fan System oprate", e); // ��������
				}
			}
		});

		// �� ��ť
		mButtonDoor = (Button) findViewById(R.id.buttondoor);
		mButtonDoor.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String message; // ��������
				byte[] msgBuffer; // ������������
				// try �쳣���� �����������ֱ����������ֹ�������ֱ�ӱ���
				try {
					outStream = _socket.getOutputStream();
				} catch (IOException e) {
					Log.e(TAG, "Light System oprate ON", e);// ���ش�����Ϣ
				}
				message = "D"; // ��������
				text0.setText("����: " + message);
				msgBuffer = message.getBytes(); // �õ�����
				try {
					outStream.write(msgBuffer);
				} catch (IOException e) {
					Log.e(TAG, "Door oprate", e);
				}
			}
		});
		// ���� ��ť
		mButtonCurtain = (Button) findViewById(R.id.buttoncurtain);
		mButtonCurtain.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String message; // ��������
				byte[] msgBuffer; // ������������
				// try �쳣���� �����������ֱ����������ֹ�������ֱ�ӱ���
				try {
					outStream = _socket.getOutputStream();
				} catch (IOException e) {
					Log.e(TAG, "Curtain System oprate", e);// ���ش�����Ϣ
				}
				if (SeekBarNum == 4) {

				} else if (SeekBarNum > 4) {
					message = "B" + (SeekBarNum - 4); // �ı䴰��
					text0.setText("����: " + message);
					msgBuffer = message.getBytes(); // �õ�����
					try {
						outStream.write(msgBuffer);
					} catch (IOException e) {
						Log.e(TAG, "Curtain System oprate", e);
					}
				} else {
					message = "A" + (4 - SeekBarNum); // �ı䴰��
					text0.setText("����: " + message);
					msgBuffer = message.getBytes(); // �õ�����
					try {
						outStream.write(msgBuffer);
					} catch (IOException e) {
						Log.e(TAG, "Curtain System oprate", e);
					}
				}

			}
		});
		// //////////////////////////////////////---------------------------------------------///////////////////////////////////

		// ����򿪱��������豸���ɹ�����ʾ��Ϣ����������

		if (_bluetooth == null) {

			Toast.makeText(this, "�޷����ֻ���������ȷ���ֻ��Ƿ����������ܣ�", Toast.LENGTH_LONG)
					.show();
			finish();
			return;
		}

		// �����豸���Ա�����
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

	// /////////////////// OnCreate ����/////////////

	/*
	 * ���������Ľ��ȷ����仯ʱ���ø÷��� seekBar,��ǰ�� SeekBar progress��SeekBar �ĵ�ǰ���� fromUser
	 * �Ƿ����û��ı���ȵ�
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		Fan.setText("0" + progress);
		SeekBarNum = progress;
		Log.v("TAG", "onProgressChanged-->seekBar=" + seekBar.getId()
				+ "progress=" + progress + "fromUser=" + fromUser);
	}

	// ���Ͱ�����Ӧ
	public void onSendButtonClicked(View v) {
		int i = 0;
		int n = 0;
		try {
			OutputStream os = _socket.getOutputStream(); // �������������
			byte[] bos = edit0.getText().toString().getBytes();
			for (i = 0; i < bos.length; i++) {
				if (bos[i] == 0x0a)
					n++;
			}
			byte[] bos_new = new byte[bos.length + n];
			n = 0;
			for (i = 0; i < bos.length; i++) { // �ֻ��л���Ϊ0a,�����Ϊ0d 0a���ٷ���
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

	// ���ջ�������ӦstartActivityForResult()
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE: // ���ӽ������DeviceListActivity���÷���
			// ��Ӧ���ؽ��
			if (resultCode == Activity.RESULT_OK) {
				// ���ӳɹ����� Device List Activity ���÷���
				// MAC��ַ����DeviceListActivity���÷���
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// �õ������豸���
				_device = _bluetooth.getRemoteDevice(address);
				// �÷���ŵõ�socket
				try {
					_socket = _device.createRfcommSocketToServiceRecord(UUID
							.fromString(MY_UUID));
				} catch (IOException e) {
					Toast.makeText(this, "����ʧ�ܣ�", Toast.LENGTH_SHORT).show();
				}
				// ����socket
				Button btn = (Button) findViewById(R.id.Button03);
				try {
					_socket.connect();
					Toast.makeText(this, "����" + _device.getName() + "�ɹ���",
							Toast.LENGTH_SHORT).show();
					btn.setText("�Ͽ�");
				} catch (IOException e) {
					try {
						Toast.makeText(this, "����ʧ�ܣ�", Toast.LENGTH_SHORT)
								.show();
						_socket.close();
						_socket = null;
					} catch (IOException ee) {
						Toast.makeText(this, "����ʧ�ܣ�", Toast.LENGTH_SHORT)
								.show();
					}
					return;
				}
				// �򿪽����߳�
				try {
					is = _socket.getInputStream(); // �õ���������������
				} catch (IOException e) {
					Toast.makeText(this, "��������ʧ�ܣ�", Toast.LENGTH_SHORT).show();
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

	// ���������߳�
	Thread ReadThread = new Thread() {
		public void run() {
			int num = 0;
			byte[] buffer = new byte[2048];
			byte[] buffer_new = new byte[2048];
			int i = 0;
			int n = 0;
			bRun = true;
			// �����߳�
			while (true) {
				try {
					while (is.available() == 0) {
						while (bRun == false) {
						}
					}

					// ��ʱ����
					try {
						Thread.currentThread();
						Thread.sleep(1000); // ��ʱʱ��
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// �����ʱ
					while (true) {
						num = is.read(buffer); // ��������
						n = 0;
						String s0 = new String(buffer, 0, num);
						fmsg += s0; // �����յ�����
						for (i = 0; i < num; i++) {
							if ((buffer[i] == 0x0d) && (buffer[i + 1] == 0x0a)) {
								buffer_new[n] = 0x0a;// �� \r(�س�) \n(����)
								// ת��Ϊ\n(����)
								// TextNumber++;
								i++;
							} else if (buffer[i] == 0x24) {
								// 0x24 �� '$'�������
								TextNumber++; // ��¼���ֵĴ���
								buffer_new[n] = buffer[i];
							} else {
								buffer_new[n] = buffer[i];
							}
							n++;
						}
						String s = new String(buffer_new, 0, n);
						Smartroom_message = s;
						smsg += s; // д����ջ���
						/*
						// ��ʱ����
						try {
							Thread.currentThread();
							Thread.sleep(500); // ��ʱʱ��
						} catch (InterruptedException e) {
							e.printStackTrace();
						}*/

						if (is.available() == 0) {
							break; // ��ʱ��û�����ݲ�����������ʾ
						}
					}
					/*
					// ��ʱ����
					try {
						Thread.currentThread();
						Thread.sleep(500); // ��ʱʱ��
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					*/
					// ������ʾ��Ϣ��������ʾˢ��
					handler.sendMessage(handler.obtainMessage());
					// //
					// //////////
				} catch (IOException e) {
				}
			}
		}
	};
	// ��Ϣ�������

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			Smartroom_test = Smartroom_message.substring(0, 2);
			FramNumber = TextNumber;
			// ������·�(01��02����) ��ͷ
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
				// �����ַ��ָ�����
				SmartRoomtext0 = Smartroom_message.split("\\$"); // �ָ��ַ�
				int number = 0;
				// ���û�н�����
				if (FramNumber == 0) {
					SmartroomtempText = Smartroom_message;
				}
				// ���ֻ��һ�仰
				else if (FramNumber == 1) {
					SmartRoomtext1 = SmartRoomtext0[number].split("\\#"); // �ָ��ַ�
					try {
						setUpViews(); // ���ݿ��ʼ��
						add(); // ��ӵ����ݿ�
					} catch (IOError e) {
						Log.e("DataLeo", "Add in Database!");
					}
					Temperature.setText(SmartRoomtext1[1] + "C"); // �����¶�
					Humidness.setText(SmartRoomtext1[2] + "%"); // ����ʪ��
					PM2_5.setText(SmartRoomtext1[3]); // ����PM2.5
					Light_I.setText(SmartRoomtext1[4]); // �����ǿ
					SMOCK.setText(SmartRoomtext1[5]); // ��������
					Vibration.setText(SmartRoomtext1[6]); // ������
				} else { // ���ںܶ����
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
							// ����� �·�(01��02����) ��ͷ��
							SmartRoomtext1 = SmartRoomtext0[number]
									.split("\\#"); // �ָ��ַ�
							try {
								setUpViews(); // ���ݿ��ʼ��
								add(); // ��ӵ����ݿ�
							} catch (IOError e) {
								Log.e("DataLeo", "Add in Database!");
							}

							Temperature.setText(SmartRoomtext1[1] + "C"); // �����¶�
							Humidness.setText(SmartRoomtext1[2] + "%"); // ����ʪ��
							PM2_5.setText(SmartRoomtext1[3]); // ����PM2.5
							Light_I.setText(SmartRoomtext1[4]); // �����ǿ
							SMOCK.setText(SmartRoomtext1[5]); // ��������
							Vibration.setText(SmartRoomtext1[6]); // ������
						} else { // ��䲻��20��ͷ��ʱ��
							text0.setText(SmartRoomtext0[number]);
						}
					}
					FramNumber = 0;
					TextNumber = 0;
				}
				FramNumber = 0;
				TextNumber = 0;
			} else // ��ͷ�����·�(01��02����) ��ͷ�����
			{
				SmartRoomtext0 = Smartroom_message.split("\\$"); // �ָ��ַ�

				int number = 0;
				if (FramNumber == 1) {
					SmartroomtempText = SmartroomtempText + Smartroom_message; // ������ϻ᲻����������Ҫ��
					if (SmartroomtempText.length() > 34
							&& SmartroomtempText.length() < 36) // ͨ���ַ��������ж�
					{
						SmartRoomtext1 = SmartroomtempText.split("\\#"); // �ָ��ַ�
						try {
							setUpViews(); // ���ݿ��ʼ��
							add(); // ��ӵ����ݿ�
						} catch (IOError e) {
							Log.e("DataLeo", "Add in Database!");
						}
						Temperature.setText(SmartRoomtext1[1] + "C"); // �����¶�
						Humidness.setText(SmartRoomtext1[2] + "%"); // ����ʪ��
						PM2_5.setText(SmartRoomtext1[3]); // ����PM2.5
						Light_I.setText(SmartRoomtext1[4]); // �����ǿ
						SMOCK.setText(SmartRoomtext1[5]); // ��������
						Vibration.setText(SmartRoomtext1[6]); // ������
						SmartroomtempText = "";
					} else {
						text0.setText(SmartRoomtext0[number]); // ��ʾ�����ַ���
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
								|| Smartroom_test.equals("12")) { // �����ͷ���·�(01��02����)
																	// ����������Ҫ������
							SmartRoomtext1 = SmartRoomtext0[number]
									.split("\\#"); // �ָ��ַ�
							try {
								setUpViews(); // ���ݿ��ʼ��
								add(); // ��ӵ����ݿ�
							} catch (IOError e) {
								Log.e("DataLeo", "Add in Database!");
							}
							// ��ʾ
							Temperature.setText(SmartRoomtext1[1] + "C"); // �����¶�
							Humidness.setText(SmartRoomtext1[2] + "%"); // ����ʪ��
							PM2_5.setText(SmartRoomtext1[3]); // ����PM2.5
							Light_I.setText(SmartRoomtext1[4]); // �����ǿ
							SMOCK.setText(SmartRoomtext1[5]); // ��������
							Vibration.setText(SmartRoomtext1[6]); // ������
						}
					}
					// TextNumber=TextNumber-FramNumber;
					FramNumber = 0;
					TextNumber = 0;
				}
				FramNumber = 0;
				TextNumber = 0;
			}
			// SmartroomtempText=""; // ����ݴ��ַ�
			TextNumber = 0;
			smsg += "\n"; // ��һ���س�
			dis.setText(smsg); // ��ʾ����
			sv.scrollTo(0, dis.getMeasuredHeight()); // �����������һҳ
		}
	};

	// �رճ�����ô�����
	public void onDestroy() {
		super.onDestroy();
		if (_socket != null) // �ر�����socket
			try {
				_socket.close();
			} catch (IOException e) {
			}
		_bluetooth.disable(); // �ر���������
	}

	// �˵�������
	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) {// �����˵�
	 * 
	 * MenuInflater inflater = getMenuInflater();
	 * 
	 * inflater.inflate(R.menu.option_menu, menu); return true; }
	 */

	/*
	 * 
	 * @Override public boolean onOptionsItemSelected(MenuItem item) { //�˵���Ӧ����
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

	// ���Ӱ�����Ӧ����

	public void onConnectButtonClicked(View v) {
		if (_bluetooth.isEnabled() == false) { // ����������񲻿�������ʾ
			Toast.makeText(this, " ��������...", Toast.LENGTH_LONG).show();
			return;
		}
		// ��δ�����豸���DeviceListActivity�����豸����
		Button btn = (Button) findViewById(R.id.Button03);
		if (_socket == null) {
			Intent serverIntent = new Intent(this, DeviceListActivity.class); // ��ת��������
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE); // ���÷��غ궨��
		} else {
			// �ر�����socket
			try {
				is.close();
				_socket.close();
				_socket = null;
				bRun = false;
				btn.setText("����");
			} catch (IOException e) {
			}
		}
		return;
	}

	// ���水����Ӧ����
	public void onSaveButtonClicked(View v) {
		Save();
	}

	// ���������Ӧ����

	public void onClearButtonClicked(View v) {
		smsg = "";
		fmsg = "";
		text0.setText(smsg); // ��� ����
		dis.setText(smsg);
		return;
	}

	// �˳�������Ӧ����
	public void onQuitButtonClicked(View v) {
		finish();

	}

	// ���湦��ʵ��
	@SuppressLint("InflateParams")
	private void Save() {

		// ��ʾ�Ի��������ļ���

		LayoutInflater factory = LayoutInflater.from(MainActivity.this); // ͼ��ģ�����������
		final View DialogView = factory.inflate(R.layout.sname, null); // ��sname.xmlģ��������ͼģ��
		new AlertDialog.Builder(MainActivity.this).setTitle("�ļ���")
				.setView(DialogView) // ������ͼģ��
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() // ȷ��������Ӧ����
						{
							public void onClick(DialogInterface dialog,
									int whichButton) {
								EditText text1 = (EditText) DialogView
										.findViewById(R.id.sname); // �õ��ļ����������
								filename = text1.getText().toString(); // �õ��ļ���
								try {
									if (Environment.getExternalStorageState()
											.equals(Environment.MEDIA_MOUNTED)) { // ���SD����׼����
										filename = filename + ".txt"; // ���ļ���ĩβ����.
										// txt
										File sdCardDir = Environment
												.getExternalStorageDirectory(); // �õ�SD����Ŀ¼
										File BuildDir = new File(sdCardDir,
												"/data"); // ��dataĿ¼���粻����������
										if (BuildDir.exists() == false)
											BuildDir.mkdirs();
										File saveFile = new File(BuildDir,
												filename); // �½��ļ���������Ѵ������½��ĵ�
										FileOutputStream stream = new FileOutputStream(
												saveFile); // ���ļ�������
										stream.write(fmsg.getBytes());
										stream.close();
										Toast.makeText(MainActivity.this,
												"�洢�ɹ���", Toast.LENGTH_SHORT)
												.show();
									} else {
										Toast.makeText(MainActivity.this,
												"û�д洢����", Toast.LENGTH_LONG)
												.show();
									}
								} catch (IOException e) {
									return;
								}
							}

						}).setNegativeButton("ȡ��", // ȡ��������Ӧ����,ֱ���˳��Ի������κδ���

						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).show(); // ��ʾ�Ի���
	}

	// ///////////////////////////////////
	// //////////// Data base SQL //////////////////
	public void setUpViews() {
		mROOMsDB = new SmartRoomDB(this);		// ���ݿ�
		mCursor = mROOMsDB.select(); //
		ROOMTIME = SmartRoomtext1[0].toString(); // ʱ��
		ROOMT = SmartRoomtext1[1].toString();	// �¶�
		ROOMH = SmartRoomtext1[2].toString();	// ʪ��
		ROOMPM = SmartRoomtext1[3].toString();	// PM2.5
		ROOML = SmartRoomtext1[4].toString();	// ��ǿ
		ROOMSMO = SmartRoomtext1[5].toString(); // ����
		ROOMVibration = SmartRoomtext1[6].toString(); // ��
		ROOMR = SmartRoomtext1[7].toString();	// �ȶ�
		ROOMsList = (ListView) findViewById(R.id.roomlist); // ���ݱ��б�
		ROOMsList.setAdapter(new ROOMsListAdapter(this, mCursor)); // �����б���ʾ
		ROOMsList.setOnItemClickListener(this); // ѡȡ�б���
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) { // �˵�����
		super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE, MENU_ADD, 0, "ADD"); // ���
		menu.add(Menu.NONE, MENU_DELETE, 0, "DELETE"); // ɾ��
		menu.add(Menu.NONE, MENU_UPDATE, 0, "UPDATE"); // ����
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case MENU_ADD: // �ж�
			add();
			break;
		case MENU_DELETE: // ɾ��
			delete();
			break;
		case MENU_UPDATE: // ����
			update();
			break;
		}
		return true;
	}

	public void add() {
		String mROOMTIME = ROOMTIME;	// ����ʱ��
		String mROOMT = ROOMT;			// �����¶�
		String mROOMH = ROOMH;			// ����ʪ��
		String mROOMPM = ROOMPM;		// ����PM2.5
		String mROOML = ROOML;			// �����ǿ
		String mROOMSMO = ROOMSMO;		// ��������
		String mROOMVibration = ROOMVibration; // ������
		String mROOMR = ROOMR;			// ��������
		// �����ַ���������Ϊ�գ������˳�
		if (mROOMTIME.equals("") || mROOMT.equals("") || mROOMH.equals("")
				|| mROOMPM.equals("") || mROOML.equals("")
				|| mROOMSMO.equals("") || mROOMVibration.equals("")
				|| mROOMR.equals("")) {
			return;
		}
		mROOMsDB.insert(mROOMTIME, mROOMT, mROOMH, mROOMPM, mROOML, mROOMSMO,
				mROOMVibration, mROOMR); // ��ӽ������ݿ�
		mCursor.requery(); // ������
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
		String mROOMTIME = ROOMTIME; // ����ʱ��
		String mROOMT = ROOMT; // �����¶�
		String mROOMH = ROOMH; // ����ʪ��
		String mROOMPM = ROOMPM; // ����PM2.5
		String mROOML = ROOML; // �����ǿ
		String mROOMSMO = ROOMSMO; // ��������
		String mROOMVibration = ROOMVibration; // ������
		String mROOMR = ROOMR; // ��������
		// �������Ϊ�գ������˳�
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
		ROOM_ID = mCursor.getInt(0); // �б�� IDֵ
		ROOMTIME = mCursor.getString(1); // ʱ��
		ROOMT = mCursor.getString(2); // �¶�
		ROOMH = mCursor.getString(3); // ʪ��
		ROOMPM = mCursor.getString(4); // PM2.5
		ROOML = mCursor.getString(5); // ��ǿ
		ROOMSMO = mCursor.getString(6); // ����
		ROOMVibration = mCursor.getString(7); // ��
		ROOMR = mCursor.getString(8); // �����Ӧ
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
					+ mCursor.getString(8)); // ��ʾ����
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
