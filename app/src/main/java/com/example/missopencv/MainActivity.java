//I started the project following this tutorial
//https://android.jlelse.eu/a-beginners-guide-to-setting-up-opencv-android-library-on-android-studio-19794e220f3c
//After making the example work I starte to understand making changes to the example
//And finally use the pdf example
//https://docs.opencv.org/3.4.1/d7/d8b/tutorial_py_face_detection.html
//To understand some of the cascadeClassifier, add some issue but finally make it work

package com.example.missopencv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements  CvCameraViewListener2 {
    private static final String  TAG              = "MainActivity";

    private boolean              mIsColorSelected = false;
    private Mat                  mRgba;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;

    //Had some memory use issie so decide avoid create any object more than once if it was not necessary
    CascadeClassifier cascadeClassifier;
    boolean paintFace=false;
    boolean paintNose=true;
    MatOfRect matOfRect;
    Mat gray;
    Mat result;

    //Play with some diferent cascade cassifiers, the face was very good so I keep it.
    //https://github.com/opencv/opencv/blob/master/data/haarcascades/haarcascade_frontalface_default.xml
    //https://raw.githubusercontent.com/opencv/opencv/3.4.1/data/haarcascades/haarcascade_eye.xml
    //https://raw.githubusercontent.com/opencv/opencv_contrib/master/modules/face/data/cascades/haarcascade_mcs_nose.xml
    String fileName="haarcascade_frontalface_default.xml";

    private CameraBridgeViewBase mOpenCvCameraView;

    //Not sure how this fits in the Activity life cycle
    //The onPause function use the object to add a listenner
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

        //Ask for permission to use the camera
        //https://developer.android.com/training/permissions/requesting.html
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Toast toast = Toast.makeText(this, "Permission is not granted", Toast.LENGTH_LONG);
            toast.show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {

            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        1);

            }
        }

        if(checkCameraHardware(this)) {
            mOpenCvCameraView = findViewById(R.id.mis_opencv_camera_view);
            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            mOpenCvCameraView.setCvCameraViewListener(this);
            //Create Object in here intead of onFrame function makes the app don't crush
        }else{
            Toast toast = Toast.makeText(this, "No Camera Found", Toast.LENGTH_SHORT);
            toast.show();
        }

        //add some extra functionality to show the nose or the rectangular trackers
        //It was fun to play with those
        final Button circleBtn = findViewById(R.id.circleOpenCv);
        circleBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               if(paintNose){
                   paintNose=false;
               }else{
                   paintNose=true;
               }
            }
        });

        final Button rectBtn = findViewById(R.id.rectOpenCv);
        rectBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(paintFace){
                    paintFace=false;
                }else{
                    paintFace=true;
                }
            }
        });
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
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

        //I have problem with memory use, create this ones outside the OnCamera frame function
        //But still having some problem with memory use
        //https://answers.opencv.org/question/61516/android-app-force-closed-by-memory-leak/
        matOfRect= new MatOfRect();
        gray= new Mat();
        result = new Mat();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        //Rotate the video in 90 degress
        mRgba =rotateMatCW(mRgba);

        //Try using the RGB video but goes faster and better with a gray one
        Imgproc.cvtColor(mRgba,gray,Imgproc.COLOR_BGR2GRAY);

        //Useful explanation of the parameters that I use
        //https://stackoverflow.com/questions/20801015/recommended-values-for-opencv-detectmultiscale-parameters
        cascadeClassifier.detectMultiScale(gray ,matOfRect,1.3,1,1,new Size(100,100),new Size(600,600));

        System.out.println("Print info "+matOfRect.size());

        for (int i=0;i<matOfRect.toArray().length;i++) {
            Rect rect=matOfRect.toArray()[i];
            if(paintFace) {
                Imgproc.rectangle(mRgba, new Point(rect.x, rect.y), new Point(rect.width + rect.x, rect.height + rect.y), new Scalar(76, 255, 0), 1, 8, 0);
            }
            if(paintNose) {
                //https://docs.opencv.org/2.4/modules/core/doc/drawing_functions.html
                Imgproc.circle(mRgba, new Point(rect.x + (rect.width / 2), rect.y + (rect.height * 0.6)), (int) (rect.width * 0.15f), new Scalar(255, 0, 0), -1, 8, 0);
            }
        }

        //The app was crushing after 10 second I found a code, still having the problem
        //https://stackoverflow.com/questions/23718912/opencv-android-crashing-after-10-sec
        //I found some interesting comments about using the garbage collector and why probably is not a
        //good idea to use it but
        // //https://stackoverflow.com/questions/2414105/why-is-it-bad-practice-to-call-system-gc
        // because I'm using OpenCV and don't understand very well the memory ussage but testes for
        //5 min and no crush so will comment on it and save it fore later.
        //System.gc();

        return mRgba;
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    //OpenCv only accept path for the xml, the asset manager don't use File object, so this function
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


    //I was having some assertion error it was stupid but this helps me to firgur it out
    //https://stackoverflow.com/questions/13772704/opencv-nmattobitmap-assertion-failed
    //Rotation was achieve thanks to this
    //https://www.tutorialspoint.com/javaexamples/rotate_image.htm
    Mat rotateMatCW(Mat  src ) {
        Mat rotationMatrix = Imgproc.getRotationMatrix2D(new Point(src.cols()/2,src.rows()/2),270,1);

        //Rotating the given image
        Imgproc.warpAffine(src, result,rotationMatrix, new Size(src.cols(), src.rows()));
        return result;
    }

}