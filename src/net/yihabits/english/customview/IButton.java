package net.yihabits.english.customview;

import android.content.Context;  
import android.graphics.Canvas;  
import android.graphics.Color;  
import android.graphics.LinearGradient;  
import android.graphics.Paint;  
import android.graphics.Shader;  
import android.graphics.Paint.Align;  
import android.graphics.Paint.FontMetrics;  
import android.graphics.drawable.ShapeDrawable;  
import android.graphics.drawable.shapes.RoundRectShape;  
import android.util.AttributeSet;  
import android.view.MotionEvent;  
import android.view.View;  
import android.view.View.OnTouchListener;  
import android.widget.Button;  
  
/** 
 */  
public class IButton extends Button implements OnTouchListener {  
      
    private int buttonID;  
      
    private ShapeDrawable mDrawable;  
      
    private boolean isPressed = false;  
    private int radian;  
      
    float[] DEFAULT_OUTRADII;  
    float[] TAB_OUTRADII;  
    float[] LEFT_OUTRADII;  
    float[] RIGHT_OUTRADII;  
    float[] CENTER_OUTRADII;  
    private static int DEFAULT_RADIAN = 8;  
      
    //#B7B7B7  
    private  int DEFAULT_START_COLOR =  -4737097;  
    //8F8F8F  
    private int DEFAULT_END_COLOR = -7368817;  
  
    //#9F9F9F  
    private int PRESSED_START_COLOR = -6316129;  
    //#767676  
    private int PRESSED_END_COLOR = -9013642;  
      
    public static int TAB = 1;  
    public static int SEGMENT_LEFT = 2;  
    public static int SEGMENT_CENTER = 3;  
    public static int SEGMENT_RIGHT = 4;  
    public static int DEFAULT = 0;  
    public static int PICTURE = 5;  
      
    private int style;  
      
    private OnChooseListener mOnChooseListener;  
      
    /** 
     * 默认图片 
     */  
    private int defaultDrawableId;  
      
    /** 
     * 按下图片 
     */  
    private int pressedDrawableId;  
      
    public boolean hasDefaultDrawable(){  
        if(defaultDrawableId != 0){  
            return true;  
        }else{  
            return false;  
        }  
    }  
      
    public boolean hasPressedDrawable(){  
        if(pressedDrawableId != 0){  
            return true;  
        }else{  
            return false;  
        }  
    }  
      
    public void setDefaultDrawableId(int defaultDrawableId){  
        this.defaultDrawableId = defaultDrawableId;  
    }  
      
    public void setDefaultDrawable(int defaultDrawableId){  
        setDefaultDrawableId(defaultDrawableId);  
        setDefaultDrawable();  
    }  
      
    public void setDefaultDrawable(){  
        setBackgroundResource(defaultDrawableId);  
    }  
      
    public void setPressedDrawable(int pressedDrawableId){  
        setPressedDrawableId(pressedDrawableId);  
        setPressedDrawable();  
    }  
      
    public void setPressedDrawable(){  
        setBackgroundResource(pressedDrawableId);  
    }  
      
    public void setPressedDrawableId(int pressedDrawableId){  
        this.pressedDrawableId = pressedDrawableId;  
    }  
    public void setOnChooseListener(IButton.OnChooseListener l){  
        this.mOnChooseListener = l;  
    }  
    public boolean isNormal(){  
        return !isPressed;  
    }  
      
    public boolean isPressed(){  
        return isPressed;  
    }  
      
      
    public void setRadian(int radian){  
        this.radian = radian;  
        initRadian();  
        changeButtonStyle(style);  
    }  
      
      
    private void initRadian(){  
        DEFAULT_OUTRADII = new float[] { radian, radian, radian, radian, radian, radian, radian, radian };  
        TAB_OUTRADII = new float[] { radian, radian, radian, radian, 0, 0, 0, 0 };  
        LEFT_OUTRADII = new float[] { radian, radian, 0, 0, 0, 0, radian, radian };  
        RIGHT_OUTRADII = new float[] { 0, 0, radian, radian, radian, radian, 0, 0 };  
        CENTER_OUTRADII = new float[] { 0, 0, 0, 0, 0, 0, 0, 0 };  
    }  
    /** 
     *  
     * @param startColor 
     * @param endColor 
     */  
    public IButton setNormalColor(int startColor, int endColor){  
        this.DEFAULT_START_COLOR = startColor;  
        this.DEFAULT_END_COLOR = endColor;  
        invalidate();  
        return this;  
    }  
      
    /** 
     *  
     * @param startColor 
     * @param endColor 
     */  
    public IButton setPressedColor(int startColor, int endColor){  
        this.PRESSED_START_COLOR = startColor;  
        this.PRESSED_END_COLOR = endColor;  
        invalidate();  
        return this;  
    }  
      
    private Shader getNormalColor(int width, int height){  
        return new LinearGradient(width/2,0,width/2,height,DEFAULT_START_COLOR,DEFAULT_END_COLOR,Shader.TileMode.MIRROR);    
    }  
      
    private Shader getPressedColor(int width, int height){  
        return new LinearGradient(width/2,0,width/2,height,PRESSED_START_COLOR,  PRESSED_END_COLOR,Shader.TileMode.MIRROR);    
    }  
      
      
    public IButton(Context context, int id, int style) {  
        super(context,null);  
          
        this.buttonID = id;  
        init(style);  
    }  
      
    public IButton(Context context, int id){  
        super(context,null);  
          
        this.buttonID = id;  
        init(DEFAULT);  
    }  
      
    private void init(int style){  
        radian = DEFAULT_RADIAN;  
        initRadian();  
        if(PICTURE != style){  
            if(mDrawable == null){  
                mDrawable = getShapeDrawable(style);  
            }  
            this.getBackground().setAlpha(0);  
            this.setTextColor(Color.WHITE);  
        }  
        this.setOnTouchListener(this);  
    }  
      
    public IButton(Context context, int id, AttributeSet attrs) {  
        super(context,attrs);  
          
        this.buttonID = id;  
        init(DEFAULT);  
    }  
      
    public IButton(Context context, int id, ShapeDrawable mDrawable){  
        super(context);  
          
        this.buttonID = id;  
        this.mDrawable = mDrawable;  
    }  
      
    public int getCmdId() {  
        return buttonID;  
    }  
      
    @Override  
    protected void onDraw(Canvas canvas) {  
        super.onDraw(canvas);  
        if(mDrawable != null){  
            mDrawable.setBounds(0, 0, this.getWidth(), this.getHeight());  
            if(!isPressed){  
                mDrawable.getPaint().setShader(getNormalColor(this.getWidth(), this.getHeight()));  
            }else{  
                mDrawable.getPaint().setShader(getPressedColor(this.getWidth(), this.getHeight()));  
            }  
              
            //mDrawable.getPaint().setColor(Color.BLUE);  
            //mDrawable.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);  
            mDrawable.draw(canvas);  
        }  
  
        Paint paint = new Paint();  
        paint.setAntiAlias(true);  
        paint.setStyle(Paint.Style.STROKE);  
        paint.setTextAlign(Align.CENTER);  
        paint.setTextSize(getTextSize());  
        paint.setColor(Color.WHITE);  
        FontMetrics fm = paint.getFontMetrics();  
        int y = getTop() + (int)(getHeight() - fm.ascent)/2;  
        canvas.drawText((String)getText(), getWidth()/2, y, paint);  
          
    }  
      
    public void onDown() {  
        onDefaultDown();  
        if(mOnChooseListener != null){  
            mOnChooseListener.onDown();  
        }  
    }  
  
    public void onUp() {  
        onDefaultUp();  
        if(mOnChooseListener != null){  
            mOnChooseListener.onUp();  
        }  
    }  
      
    public void onDefaultUp(){  
        isPressed = false;  
        invalidate();  
    }  
      
    public void onDefaultDown(){  
        isPressed = true;  
        invalidate();  
    }  
  
    public void changeButtonStyle(int style){  
        getShapeDrawable(style);  
        invalidate();  
    }  
      
    private ShapeDrawable getShapeDrawable(int style){  
        this.style = style;  
        if(style == TAB){  
            mDrawable = new ShapeDrawable(new RoundRectShape(TAB_OUTRADII, null,  
                    null));  
        }else if(style == SEGMENT_LEFT){  
            mDrawable = new ShapeDrawable(new RoundRectShape(LEFT_OUTRADII, null,  
                    null));  
        }else if(style == SEGMENT_CENTER){  
            mDrawable = new ShapeDrawable(new RoundRectShape(CENTER_OUTRADII, null,  
                    null));  
        }else if(style == SEGMENT_RIGHT){  
            mDrawable = new ShapeDrawable(new RoundRectShape(RIGHT_OUTRADII, null,  
                    null));  
        }else{  
            mDrawable = new ShapeDrawable(new RoundRectShape(DEFAULT_OUTRADII, null,  
                    null));  
        }  
        return mDrawable;  
    }  
    @Override  
    public boolean onTouch(View v, MotionEvent event) {  
        if (event.getAction() == MotionEvent.ACTION_DOWN) {  
            if (!isPressed) {  
                if(hasPressedDrawable()){  
                    setBackgroundResource(pressedDrawableId);  
                }  
                // 更改为按下时的背景图  
                onDown();  
            }  
        } else if (event.getAction() == MotionEvent.ACTION_UP) {  
            if (isPressed) {  
                if(hasDefaultDrawable()){  
                    setBackgroundResource(defaultDrawableId);  
                }  
                // 改为抬起时的图片  
                onUp();  
            }  
        }  
        // TODO Auto-generated method stub  
        return false;  
    }  
      
    public interface OnChooseListener{  
        public void onDown();  
        public void onUp();  
    }  
}  