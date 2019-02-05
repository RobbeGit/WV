package ar.wv;

import org.opencv.core.Point;

public class FinishedEl {
    private String data;
    private Integer x;
    private Integer y;

    public FinishedEl(String string,OCircle c) {
        this.data = string;
        this.x = (int)c.getCenterX();
        this.y = (int)c.getCenterY();
    }

    public Point getPoint() {
        return new Point(this.x,this.y);
    }



    public String getData() {
        return data;
    }


}
