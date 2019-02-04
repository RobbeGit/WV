
package ar.wv;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

import static org.opencv.core.Core.FONT_HERSHEY_PLAIN;
import static org.opencv.core.Core.add;
import static org.opencv.core.Core.countNonZero;
import static org.opencv.imgproc.Imgproc.minEnclosingCircle;

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
        Mat mask;
        Mat mRgbaT;
        Mat gray;
        Mat blurred;
        Mat labelled;

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
        Log.i(TAG, "Instantiated new " + this.getClass());
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
            mRgbaT = new Mat(width, width, CvType.CV_8UC4);
            labelled = new Mat(height,width,CvType.CV_8UC4);
        }

        public void onCameraViewStopped() {
            input.release();
        }

        public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

            // TODO variant op while true uit originele code; anders gaat het blijven crashen,
            input = inputFrame.gray();
            // Rotate mRgba 90 degrees
            Size test = new Size(11,11);
            Imgproc.blur(input,input,test);
            Imgproc.threshold(input,input,200,255,Imgproc.THRESH_BINARY);
            Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,test);
            Imgproc.erode(input,input,element,new org.opencv.core.Point(-1,-1) ,2);//Seems to slow the app down
            Imgproc.dilate(input,input,element,new org.opencv.core.Point(-1,-1),4);//Seems to slow the app down


            Imgproc.connectedComponents(input,labelled,8);
            mask = Mat.zeros(input.size(),CvType.CV_8UC4);
            int iter;
            int height = labelled.height();
            int width = labelled.width();
            int size = height*width;

            for( iter = 0;iter < size;iter++){ //TODO find a way to only handle unique labels
                double[] a = new double[0];
                double [] label = labelled.get(iter % width, iter - (iter % width));
                if(label ==a){
                    continue;
                }

                Mat iterMask = Mat.zeros(input.size(), CvType.CV_8UC4);
                //iterMask[labelled == label] = 255;//TODO find java equivalent
                int numPixels = countNonZero(iterMask);

               if(numPixels>130 &&  numPixels < 2500){
                   add(mask,iterMask,mask);
               }
            }

            ArrayList<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(input,contours,new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_NONE); //creates a list of vectors of points which are the contours of each blob
            ArrayList<OCircle> lst = new ArrayList<>();
            for(MatOfPoint cont:contours) {
                Rect bound = Imgproc.boundingRect(cont);//TODO no idea if this should be used somewhere
                Point cent = null;
                float[] rad = null;
                minEnclosingCircle(new MatOfPoint2f(cont.toArray()), cent, rad);
                OPoint point = new OPoint(cent.x, cent.y);
                OCircle circle = new OCircle(point, (double) rad[0]);
                OCircle a = findIntersect(circle, lst);
                if (a == null) {
                    lst.add(circle);
                    circle.setFlagbit(Boolean.TRUE);
                } else {
                    a.setFlagbit(Boolean.TRUE);
                }
            }

            for(OCircle iterator:lst){
                if (iterator.getFlagbit()==Boolean.TRUE){
                    iterator.addDataList(1);
                    iterator.setFlagbit(Boolean.FALSE);
                }else{
                    iterator.addDataList(0);
                }
            }
            int staleCheckIdx = 0;
            int lstLength = lst.size();
            ArrayList<OCircle> confirmedList = new ArrayList<>();
            ArrayList<FinishedEl> finishedList = new ArrayList<>();
            if (lstLength > 0){
                if(staleCheckIdx > lstLength-1){
                    staleCheckIdx = 0;
                }
                OCircle c = lst.get(staleCheckIdx);
                int dataLength = c.getDataList().size();
                if(dataLength>startSeqLength){
                    if(c.isConfirmedSource(startsequence)){
                        if(!c.getConfirmed()){
                            confirmedList.add(c);
                            c.setConfirmed(Boolean.TRUE);
                        }
                    }else{
                        finishedList.add(new FinishedEl(c.getDataString(startSeqLength,frameLength),c));
                        lst.remove(staleCheckIdx);
                        if(confirmedList.contains(c)) {
                        confirmedList.remove(c);
                        }
                    }
                }else {
                    lst.remove(staleCheckIdx);
                }
            }
            staleCheckIdx+=1; //TODO is useless zolang er geen variant op while True is
            Mat image=null;
            Scalar color = new Scalar(0,0,255);
            for(OCircle c:confirmedList){
                Imgproc.circle(image,c.getCenter(),(int)c.getRadius(),color);
            }
            for(FinishedEl i:finishedList){
                Imgproc.putText(image,i.getData(),i.getPoint(),FONT_HERSHEY_PLAIN,0.5,color);
            }

            /* flips the image */ //TODO is alleen nodig als we het in portrait ipv landscape willen
           // Core.transpose(input, mRgbaT);
           // Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0,0, 0);
           // Core.flip(mRgbaF, input, 1 );
                return image; 
        }

        public ArrayList<Integer> startsequence = new ArrayList<>();
        public Integer startSeqLength = startsequence.size();
        public Integer frameLength = startSeqLength+10;
        private static final int MY_CAMERA_REQUEST = 100;

        public OCircle findIntersect(OCircle a, ArrayList<OCircle> list){
            for(OCircle circle : list){
                if(intersect(a,circle))
                    return circle;
            }
            return null;
        }
        public Boolean intersect(OCircle a, OCircle b){
            return Math.abs(Math.hypot(a.getCenterX()-b.getCenterX(),a.getCenterY()-b.getCenterY()))<a.getRadius()+b.getRadius();
        }


    }


