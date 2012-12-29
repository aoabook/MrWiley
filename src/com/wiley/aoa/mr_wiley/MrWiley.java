package com.wiley.aoa.mr_wiley;

import java.util.ArrayList;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

public class MrWiley extends SampleCvViewBase {

	private Mat src, hsv, dst, intermediate;
	private ArrayList<MatOfPoint> contours;

	private Mat hierarchy;

	private Handler mHandler;
	
	public MrWiley(Context context, Handler mHandler) {
		super(context);
		this.mHandler = mHandler;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		super.surfaceCreated(holder);
		synchronized (this) {
			src = new Mat();
			hsv = new Mat();
			dst = new Mat();
			intermediate = new Mat();
			hierarchy = new Mat();
			contours = new ArrayList<MatOfPoint>();
		}
	}

	@Override
	protected Bitmap processFrame(VideoCapture capture) {
		capture.retrieve(src, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);

		Imgproc.cvtColor(src, hsv, Imgproc.COLOR_RGB2HSV);

		Core.inRange(hsv, new Scalar(0, 10, 110), new Scalar(6, 255, 255),
				intermediate);
		
		Mat erode = Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, new Size(9, 9));
		Imgproc.erode(intermediate, dst, erode);

		Mat dilate = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(9, 9));
		Imgproc.dilate(dst, intermediate, dilate);

		Imgproc.findContours(intermediate, contours, hierarchy, 0, 2);

		int largestContour = -1;
		double area = 0;
		for (int i = 0; i < contours.size(); i++) {
			double cArea = Imgproc.contourArea(contours.get(i));
			if (cArea > area) {
				area = cArea;
				largestContour = i;
			}
		}

		Rect r = null;
		if (largestContour > -1)
			r = Imgproc.boundingRect(contours.get(largestContour));

		if (r != null) {
			Core.rectangle(intermediate, r.tl(), r.br(), new Scalar(255, 255,
					255), 5);

			if ((r.y + r.height/2) < this.getHeight() / 2) {
				// Move right
				mHandler.obtainMessage(MainActivity.ACTION_RIGHT).sendToTarget();
			} else {
				// Move left
				mHandler.obtainMessage(MainActivity.ACTION_LEFT).sendToTarget();
			}
		}else{
			// Stop
			mHandler.obtainMessage(MainActivity.ACTION_STOP).sendToTarget();
		}
		
		Bitmap bmp = Bitmap.createBitmap(src.cols(), src.rows(),
				Bitmap.Config.ARGB_8888);
		
		Utils.matToBitmap(src, bmp);
		
		if( contours != null)
			contours.clear();
		
		return bmp;
	}
	

	@Override
	public void run() {
		super.run();
		synchronized (this) {
			if (src != null)
				src.release();
			src = null;
			if (hsv != null)
				hsv.release();
			hsv = null;
			if (dst != null)
				dst.release();
			dst = null;
			if (intermediate != null)
				intermediate.release();
			intermediate = null;
			if (hierarchy != null)
				hierarchy.release();
			hierarchy = null;
			contours = null;
		}
	}

}
