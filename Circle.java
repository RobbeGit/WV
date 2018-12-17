package menbcom.wv.wvopencv;
import java.util.*;

public class Circle {
    private Boolean confirmed;
    private Boolean flagbit;
    private ArrayList<Integer> datalist = new ArrayList<>();
    private Point center;
    private Double radius;

    public Circle(Double x, Double y, Double z){
        center =  new Point(x,y);
        radius  = z;
    }

    public Circle(Point x, Double z){
        center = x;
        radius = z;
    }


    public double getRadius() {
        return radius;
    }

    public double getCenterX(){
        return center.getxCoord();
    }

    public double getCenterY(){
        return center.getyCoord();
    }

    public Boolean isConfirmedSource(ArrayList<Integer> list){
        if (confirmed == Boolean.TRUE)
            return Boolean.TRUE;
        else if(datalist.size()<list.size())
            return Boolean.FALSE;
        else{
            for(Integer i = 0; i<list.size();i++){
                if(datalist.get(i) != list.get(i))
                    return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    }
    public String getDataString(Integer start,Integer end){
        String str = datalist.subList(start,end).toString();
        return str.replaceAll("\\s","");
    }
}
