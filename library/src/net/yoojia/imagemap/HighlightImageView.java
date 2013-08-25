package net.yoojia.imagemap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.util.AttributeSet;

import net.yoojia.imagemap.core.CircleShape;
import net.yoojia.imagemap.core.Shape;
import net.yoojia.imagemap.core.ShapeExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HighlightImageView基于TouchImageView的功能，在ImageView的Canvas上绘制一些形状。
 * Based on TouchImageView class, Design for draw shapes on canvas of ImageView
 */
public class HighlightImageView extends TouchImageView implements ShapeExtension {

	public HighlightImageView(Context context) {
		this(context,null);
	}

	public HighlightImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	private Map<Object,Shape> shapesCache = new HashMap<Object, Shape>();
    private OnShapeActionListener onShapeClickListener;

    public void setOnShapeClickListener(OnShapeActionListener onShapeClickListener){
        this.onShapeClickListener = onShapeClickListener;
    }

    @Override
	public void addShape(Shape shape){

		shapesCache.put(shape.tag, shape);
		postInvalidate();
	}

    @Override
	public void removeShape(Object tag){
		if(shapesCache.containsKey(tag)){
			shapesCache.remove(tag);
			postInvalidate();
		}
	}

    @Override
    public void clearShapes() {
        shapesCache.clear();
    }

    public List<Shape> getShapes(){
        return new ArrayList<Shape>(shapesCache.values());
    }

    public Shape getShape(Object tag){
        if (!shapesCache.containsKey(tag)){
            return null;
        }

        return shapesCache.get(tag);
    }

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
        for(Shape shape : shapesCache.values()){
            shape.onDraw(canvas);
        }
        canvas.save();
        onDrawWithCanvas(canvas);
	}

    //TODO now this function is just for circle
    public void postInvalidate(Object tag){
        CircleShape shape = (CircleShape) shapesCache.get(tag);
        float radius = shape.getRadius();
        PointF pointF = shape.getCenterPoint();
        int left = (int) (pointF.x - radius) + 1;
        int top = (int) (pointF.y - radius) + 1;
        int right = (int) (pointF.x + radius) + 1;
        int bottom = (int) (pointF.y + radius) + 1;

        postInvalidate(left, top, right, bottom);
        //postInvalidateOnAnimation();
        //postInvalidate((int)(pointF.x - radius), pointF.y - radius, pointF.x + radius, pointF.y +radius);
    }

    public void setShapeColor(Object tag, int coverColor){
        if (!shapesCache.containsKey(tag)){
            return;
        }
        shapesCache.get(tag).setColor(coverColor);
    }
    //TODO now this function is just for circle
    public void highLightShape(Object tag){
        if (!shapesCache.containsKey(tag)){
           return;
        }
        Shape shape = shapesCache.get(tag);
        if (!(shape instanceof CircleShape)){
            return;
        }
        final CircleShape circleShape = (CircleShape) shape;
        circleShape.setColor(0xFF00FF);
        circleShape.setLightingStatus(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int MAX_RADIUS = 20;

                while (circleShape.isLighting()){
                    try {
                        PointF pointF = circleShape.getCenterPoint();
                        int left = (int) (pointF.x - MAX_RADIUS);
                        int top = (int) (pointF.y - MAX_RADIUS);
                        int right = (int) (pointF.x + MAX_RADIUS);
                        int bottom = (int) (pointF.y + MAX_RADIUS);

                        circleShape.setRadius(5);
                        postInvalidate(left, top, right, bottom);
                        Thread.sleep(1000);

                        circleShape.setRadius(10);
                        postInvalidate(left, top, right, bottom);
                        Thread.sleep(1000);

//                        circleShape.setRadius(15);
//                        postInvalidate(left, top, right, bottom);
//                        Thread.sleep(1000);

                        circleShape.setRadius(20);
                        postInvalidate(left, top, right, bottom);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                }
            }
        }).start();
    }

    //TODO now this function is just for circle shape
    public void stopHighLighting(Object tag){
        if (!shapesCache.containsKey(tag)){
            return;
        }
        Shape shape = shapesCache.get(tag);
        if (!(shape instanceof CircleShape)){
            return;
        }
        shape.setColor(Color.GRAY);
        ((CircleShape) shape).setLightingStatus(false);
    }

    /**
     * 如果继承HighlightImageView，并需要在Canvas上绘制，可以Override这个方法来实现。
	 * - Override this method for draw something on canvas when YourClass extends HighlightImageView.
     * @param canvas 画布
     */
    protected void onDrawWithCanvas(Canvas canvas){}

    @Override
    protected void onViewClick (float xOnView, float yOnView) {
        if(onShapeClickListener == null) return;
        for(Shape shape : shapesCache.values()){
            if(shape.inArea(xOnView,yOnView)){
                // 如果一个形状被点击，通过监听接口回调给点击事件的关注者。
				// Callback by listener when a shape has been clicked
                onShapeClickListener.onShapeClick(shape, xOnView, yOnView);
                break; // 只有一个形状可以被点击 - Only one shape can be click
            }
        }
    }

    @Override
	protected void postScale(float scaleFactor, float scaleCenterX,float scaleCenterY) {
		super.postScale(scaleFactor, scaleCenterX, scaleCenterY);
		if(scaleFactor != 0){
            for(Shape shape : shapesCache.values()){
                if(scaleFactor != 0){
                    shape.onScale(scaleFactor, scaleCenterX, scaleCenterY);
                }
            }
		}
	}

    @Override
    protected void postTranslate(float deltaX, float deltaY) {
		super.postTranslate(deltaX, deltaY);
        if( !(deltaX == 0 && deltaY == 0)){
            for(Shape shape : shapesCache.values()){
                shape.onTranslate(deltaX, deltaY);
            }
        }
    }

}
