package com.wiley.aoa.mr_wiley;

import java.io.IOException;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wiley.wroxaccessories.UsbConnection12;
import com.wiley.wroxaccessories.WroxAccessory;

public class MainActivity extends Activity {

	protected static final String TAG = "MrWiley";

	private MrWiley mrWiley;

	private WroxAccessory mAccessory;
	private UsbManager mUsbManager;
	private UsbConnection12 connection;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this,
				mOpenCVCallBack)) {
			Log.i(TAG, "Couldn't connect to OpenCV");
		}
		mUsbManager = (UsbManager) getSystemService(USB_SERVICE);
		connection = new UsbConnection12(this, mUsbManager);
		mAccessory = new WroxAccessory(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mrWiley != null && !mrWiley.openCamera())
			finish();
		try {
			mAccessory.connect(WroxAccessory.USB_ACCESSORY_12, connection);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mrWiley != null)
			mrWiley.releaseCamera();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			mAccessory.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			if (status == LoaderCallbackInterface.SUCCESS) {
				Log.i(TAG, "Connected to OpenCV");
				mrWiley = new MrWiley(mAppContext, mHandler);
				setContentView(mrWiley);

				if (!mrWiley.openCamera())
					finish();
			} else {
				super.onManagerConnected(status);
			}
		}
	};

	protected static final byte ACTION_LEFT = 0;
	protected static final byte ACTION_RIGHT = 1;
	protected static final byte ACTION_STOP = 2;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			byte[] buffer = new byte[1];
			switch (msg.what) {
			case ACTION_LEFT:
				buffer[0] = ACTION_LEFT;
				break;
			case ACTION_RIGHT:
				buffer[0] = ACTION_RIGHT;
				break;
			case ACTION_STOP:
				buffer[0] = ACTION_STOP;
				break;
			}
			try {
				mAccessory.publish("mw", buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};
}
