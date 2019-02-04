package ar.wv;

import org.opencv.core.Point;

import java.util.ArrayList;

public class OCircle {
    private Boolean confirmed = Boolean.FALSE;
    private Boolean flagbit;
    private ArrayList<Integer> datalist = new ArrayList<>();
    private OPoint center;
    private Double radius;

    public OCircle(Double x, Double y, Double z){
        center =  new OPoint(x,y);
        radius  = z;
    }

    public OCircle(OPoint x, double z){
        center = x;
        radius = z;
    }
    public Point getCenter()    {
        return new Point(center.getxCoord(),center.getyCoord());
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

    public Boolean getFlagbit() {
        return flagbit;
    }

    public void setFlagbit(Boolean flagbit) {
        this.flagbit = flagbit;
    }
    public ArrayList<Integer> getDataList() {
        return datalist;
    }

    public void addDataList( int toAdd){
        this.datalist.add(toAdd);
    }

    public boolean getConfirmed() {
        return this.confirmed;
    }
    public void setConfirmed(Boolean toSet)  {
        this.confirmed = toSet;
    }
}