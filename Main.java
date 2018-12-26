package com.example.robbesteynen.wv;

import org.opencv.*;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;
import org.opencv.android.JavaCameraView;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;


public class Main extends AppCompatActivity implements CvCameraViewListener2 {

    // Used for logging success or failure messages
    private static final String TAG = "OCVSample::Activity";

    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
    private CameraBridgeViewBase mOpenCvCameraView;

    // Used in Camera selection from menu (when implemented)
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;

    // These variables are used (at the moment) to fix camera orientation from 270degree to 0degree
    Mat input;
    Mat mRgbaF;
    Mat mRgbaT;
    Mat gray;
    Mat blurred;
    Mat labels;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
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


    public Main() {

      /* I hope this works for the permission stuff.
      Na een keer denyen kunnen users aangeven dat het nooit meer moet gevraagd worden,
      daarmee de tweede if statement, zodat ze een melding krijgen dat je wel degelijk
      permissie moet geven voordat de app iets kan doen.
      */

      // TODO testen

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                  showMessageOKCancel("You need to allow access to Camera", new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialog, int which) {
                              requestPermissions(new String[] {Manifest.permission.WRITE_CONTACTS},MY_CAMERA_REQUEST);
                          }
                      });
              return;
            }

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST);
            return;
        }
        else {

          /* IN CASE DAT DIT MAIN DEEL OOIT WORDT AANGEPAST
          - PAS HET OOK AAN IN onRequestPermissionsResult*/
            Log.i(TAG, "Instantiated new " + this.getClass());
        }

    }

/* Geeft Message indien de gebruiker heeft aangegeven nooit meer om
    perissie te vragen */

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

/* Nog een extra functie nodig voor permissies aanvragen
    */

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_CAMERA_REQUEST:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    // NAMELIK HIER - Zie public main //

                    Log.i(TAG, "Instantiated new " + this.getClass());

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    Toast.makeText(this, "CAMERA permission Denied.", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.show_camera);

        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.show_camera_activity_java_surface_view);

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

    public void onCameraViewStarted(int width, int height) {

        input = new Mat(height, width, CvType.CV_8UC4);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped() {
        input.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        // TODO Auto-generated method stub
        input = inputFrame.gray();
        // Rotate mRgba 90 degrees
        Size test = new Size(11,11);
        Imgproc.blur(input,input,test);
        Imgproc.threshold(input,input,200,255,Imgproc.THRESH_BINARY);
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,test);
        Imgproc.erode(input,input,element,new org.opencv.core.Point(-1,-1) ,2);//Seems to slow the app down
        Imgproc.dilate(input,input,element,new org.opencv.core.Point(-1,-1),4);//Seems to slow the app down
        //Mat test2 = new Mat();
        //Imgproc.connectedComponents(input,test2 , 8);

        Core.transpose(input, mRgbaT);
        Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0,0, 0);
        Core.flip(mRgbaF, input, 1 );

        return input;



    }


    public ArrayList<Integer> startsequence = new ArrayList<>();
    public Integer startSeqLength = startsequence.size();
    public Integer frameLength = startSeqLength+10;
    private static final int MY_CAMERA_REQUEST = 100;

    public Circle findIntersect(Circle a, Circle[] list){
        for(Circle circle : list){
            if(intersect(a,circle))
                return circle;
        }
        return null;
    }
    public Boolean intersect(Circle a, Circle b){
        return Math.abs(Math.hypot(a.getCenterX()-b.getCenterX(),a.getCenterY()-b.getCenterY()))<a.getRadius()+b.getRadius();
    }

/*
rom imutils import contours
from skimage import measure
import numpy as np
import argparse
import imutils
import cv2
import math
import time

def intersect(circle, lst):
        for c in lst:
        if c.intersect(circle):
        return c
        return None

class Point:
        def __init__(self, x, y):
        self.x = x
        self.y = y

class Circle:
        """
        Holds data on a circle in the plane
        """
        def __init__(self, a, b, c=None):
        self.confirmed = False
        self.flagbit = True
        self.datalist = []
        if c is None:
        # Circle(Point, scalar)
        self.center = a
        self.r = b
        else:
        # Circle(scalar, scalar, scalar)
        self.center = Point(a, b)
        self.r = c

        def intersect(self, c):
        return abs(math.hypot(self.center.x - c.center.x, self.center.y - c.center.y)) <  self.r + c.r
        def isConfirmedSource(self, startSequence):
        if self.confirmed:
        return True
        if len(self.datalist) < len(startSequence):
        return False
        else:
        for i in range(0,len(startSequence)): # Optional optimalization: start with second element, first one should always be 1
        if (self.datalist[i] != startSequence[i]):
        return False
        return True
        def getDataString(self,start,end):
        return "".join(list(map(str,self.datalist[start:end])))


        ### GLOBAL VARIABLES ####
        startSequence = []
        startSeqLength = len(startSequence)
        frameLength = startSeqLength + 10
        #########################


        # load the video feed
        #cap = cv2.VideoCapture('kek.mkv')
        cap = cv2.VideoCapture(0)
        # load the image, convert it to grayscale, and blur it

        # create empty list and initialize index to check for stale PIS
        lst = []
        confirmedList = []
        finishedList = []
        staleCheckIdx = 0
        while(True):
        start = time.time()
        ret,image = cap.read()

        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        blurred = cv2.GaussianBlur(gray, (11, 11), 0)

        # threshold the image to reveal light regions in the
        # blurred image
        thresh = cv2.threshold(blurred, 200, 255, cv2.THRESH_BINARY)[1]
        #thresh = cv2.adaptiveThreshold(blurred,255,cv2.ADAPTIVE_THRESH_MEAN_C,\
        #          cv2.THRESH_BINARY,151,-70)

        # perform a series of erosions and dilations to remove
        # any small blobs of noise from the thresholded image
        thresh = cv2.erode(thresh, None, iterations=2)
        thresh = cv2.dilate(thresh, None, iterations=4)

        # perform a connected component analysis on the thresholded
        # image, then initialize a mask to store only the "large"
        # components
        labels = measure.label(thresh, neighbors=8, background=0)
        mask = np.zeros(thresh.shape, dtype="uint8")

        # loop over the unique components
        for label in np.unique(labels):
        # if this is the background label, ignore it
        if label == 0:
        continue

        # otherwise, construct the label mask and count the
        # number of pixels
        labelMask = np.zeros(thresh.shape, dtype="uint8")
        labelMask[labels == label] = 255
        numPixels = cv2.countNonZero(labelMask)

        # if the number of pixels in the component is sufficiently
        # large, then add it to our mask of "large blobs"
        if numPixels > 130 and numPixels < 2500:
        mask = cv2.add(mask, labelMask)

        # find the contours in the mask, then sort them from left to
        mask = cv2.add(mask, labelMask)

        # find the contours in the mask, then sort them from left to
        # right
        cnts = cv2.findContours(mask.copy(), cv2.RETR_EXTERNAL,
        cv2.CHAIN_APPROX_SIMPLE)
        cnts = cnts[0] if imutils.is_cv2() else cnts[1]
        if len(cnts) >2 :
        cnts = contours.sort_contours(cnts)[0]

        # loop over the contours
        for (i, c) in enumerate(cnts):
        # draw the bright spot on the image
        (x, y, w, h) = cv2.boundingRect(c)
        ((cX, cY), radius) = cv2.minEnclosingCircle(c)
        circle = Circle(int(cX), int(cY), int(radius))

        # Check if the circle exists in the list. (By checking for intersections)
        a = intersect(circle, lst)
        # if this is a new circle, add it to the list and flag the bit;
        if (a == None):
        lst.append(circle)
        circle.flagbit = True
        # if it's not a new circle, signal to the bit flag that we read a 1.
        else:
        a.flagbit = True
        # Add correct bit to each datalist, depending on their flag.
        for a in lst:
        if a.flagbit == True:
        a.datalist.append(1)
        # Reset flag for next iteration, default should be false
        a.flagbit = False
        else:
        a.datalist.append(0)

        ### LIST MAINTENANCE ###
        lstLength = len(lst)
        #print("lstLength = " + str(lstLength))
        #print("ConfirmedList = " + str(len(confirmedList)))
        if (lstLength > 0):
        if (staleCheckIdx > lstLength-1):
        staleCheckIdx = 0
        c = lst[staleCheckIdx]
        dataLength = len(c.datalist)
        #print("DataLength = " + str(dataLength))
        if (dataLength > startSeqLength):
        if (c.isConfirmedSource(startSequence)):
        if (dataLength < frameLength):
        if not (c.confirmed):
        confirmedList.append(c)
        c.confirmed = True
        else:
        finishedList.append([c.getDataString(startSeqLength,frameLength),(int(c.center.x), int(c.center.y))])
        del lst[staleCheckIdx]
        if(c in confirmedList):
        confirmedList.remove(c)
        else:
        del lst[staleCheckIdx]
        staleCheckIdx += 1
        ########################

        ### DRAW CONFIRMED CIRCLES ###
        for c in confirmedList:
        cv2.circle(image, (int(c.center.x), int(c.center.y)), int(c.r), (0, 0, 255), 3)
        #   cv2.putText(image, "#{}".format(i + 1), (x, y - 15),
        #   cv2.FONT_HERSHEY_SIMPLEX, 0.45, (0, 0, 255), 2)
        for i in finishedList:
        cv2.putText(image, i[0], i[1], cv2.FONT_HERSHEY_SIMPLEX, 0.45, (0, 0, 255), 2)
        ##############################

        # Display the resulting frame
        cv2.imshow('image',image)

        ### DELAY PER FRAME #######
        if cv2.waitKey(1) & 0xFF == ord('q'):
        break
        time.sleep(max(0,0.05 - (time.time() - start)))
        #print(time.time() - start)
        ###########################

        # When everything done, release the capture
        cap.release()
        cv2.destroyAllWindows()
*/
}
