package com.example.missopencv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.SurfaceView;

public class MainActivity extends Activity implements  CvCameraViewListener2 {
    private static final String  TAG              = "MainActivity";

    private boolean              mIsColorSelected = false;
    private Mat                  mRgba;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;

    CascadeClassifier cascadeClassifier;

    //https://github.com/opencv/opencv/blob/master/data/haarcascades/haarcascade_frontalface_default.xml
    //https://raw.githubusercontent.com/opencv/opencv/3.4.1/data/haarcascades/haarcascade_eye.xml
    //https://raw.githubusercontent.com/opencv/opencv_contrib/master/modules/face/data/cascades/haarcascade_mcs_nose.xml
    String fileName="haarcascade_frontalface_default.xml";

    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);


        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    //This is for get the camera data
    public void onCameraViewStarted(int width, int height) {
        //https://docs.opencv.org/2.4/modules/core/doc/basic_structures.html
        mRgba = new Mat(height, width, CvType.CV_8UC4);



        String pathCascadeNoseFile=initAssetFile(fileName);
        File file =new File(pathCascadeNoseFile);
        if(file.exists()) {
            System.out.println("File Exist");
            cascadeClassifier = new CascadeClassifier(pathCascadeNoseFile);
        }else{
            System.out.println("File Don't Exist");
        }
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }


    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        mRgba =rotateMatCW(mRgba);

        Mat gray=mRgba;
        MatOfRect matOfRect= new MatOfRect();
        cascadeClassifier.detectMultiScale(gray ,matOfRect,1.3,5,1,new Size(50,50),new Size(400,400));

        System.out.println("Print info "+matOfRect.size());

        for (int i=0;i<matOfRect.toArray().length;i++) {
            Rect rect=matOfRect.toArray()[i];
            Imgproc.rectangle(mRgba, new Point(rect.x, rect.y), new Point(rect.width, rect.height), new Scalar(76, 255, 0));
        }




        return mRgba;
    }

    //Open CV only accept path for the xml, the asset manager don't use File object, so this function
    //read the assets stream and outputs a File and retrives the path
    //https://stackoverflow.com/questions/53557853/error-while-loading-yaml-model-file-using-opencv-in-android
    public String initAssetFile(String filename)  {
        File file = new File(getFilesDir(), filename);
        if (!file.exists()) try {
            InputStream is = getAssets().open(filename);
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            is.read(data); os.write(data); is.close(); os.close();
        } catch (IOException e) { e.printStackTrace(); }
        Log.d(TAG,"prepared local file: "+filename);
        return file.getAbsolutePath();
    }


    //I was having some asetion error it was stupid but this helps to firgure it out
    //https://stackoverflow.com/questions/13772704/opencv-nmattobitmap-assertion-failed
    //Rotation was achieve thanks to this
    //https://www.tutorialspoint.com/javaexamples/rotate_image.htm
    Mat rotateMatCW(Mat  src ) {
        Mat result = new Mat();
        Mat rotationMatrix = Imgproc.getRotationMatrix2D(new Point(src.cols()/2,src.rows()/2),270,1);
        //Rotating the given image
        Imgproc.warpAffine(src, result,rotationMatrix, new Size(src.cols(), src.rows()));
        return result;
    }

}