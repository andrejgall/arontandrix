package com.example.arontandrix;

import java.util.ArrayList;
import java.util.List;
import org.andrix.motors.Motor;
import org.andrix.motors.Servo;
import org.andrix.sensors.Analog;
import org.andrix.sensors.Digital;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.metaio.sdk.jni.Vector2d;

/**
 * Draws a transparent overlay with values of the objects.
 * 
 * @author Andrej
 *
 */
public class OverlayView extends View {

	/**
	 * Text to be displayed.
	 */
	private String text;
	/**
	 * X coordinate of the text field.
	 */
	private int textx;
	/**
	 * Y coordinate of the text field.
	 */
	private int texty;
	/**
	 * Visibility on screen.
	 */
	public boolean visible;

	/**
	 * List of objects to display.
	 */
	private List<ObjValVec> list;

	/**
	 * Size of the font.
	 */
	private static final int FONT_SIZE = 25;
	/**
	 * Field margin.
	 */
	private static final int TITLE_MARGIN = 10;

	private ArrayList<Vector2d> pointlist;

	private Vector2d dp;

	Paint framepaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	Paint linepaint = new Paint();
	Paint textpaint = new Paint();
	Paint pathpaint = new Paint() {
		{
			setStyle(Paint.Style.STROKE);
			setStrokeCap(Paint.Cap.ROUND);
			setStrokeWidth(10.0f);
			setAntiAlias(true);
		}
	};
	Shader shader;

	/**
	 * Default constructor.
	 * 
	 * @param context
	 * @param attrs
	 */
	public OverlayView(Context context, AttributeSet attrs) {
		super(context, attrs);
		visible = false;
		text = null;
		textx = 0;
		texty = 0;
		framepaint.setStrokeWidth(5);
		linepaint.setColor(Color.WHITE);
		textpaint.setColor(Color.WHITE);
		pointlist = new ArrayList<Vector2d>();
		dp = new Vector2d(0);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		dp.setX(event.getX());
		dp.setY(event.getY());
		return true;
	};

	public Vector2d dp() {
		return dp;
	}

	/**
	 * Updates the list.
	 * 
	 * @param list
	 *            List with actual values.
	 */
	public void updateMap(List<ObjValVec> list) {
		this.list = list;
	}

	public void updatePointList(ArrayList<Vector2d> pl) {
		this.pointlist = pl;
	}

	/**
	 * Goes through the list and displays all the values of the object in the
	 * given place.
	 */
	@Override
	public void onDraw(Canvas c) {
		if (visible) {
			TextPaint paintText = new TextPaint();
			Paint paintRect = new Paint();
			ArrayList<Vector2d> verts = new ArrayList<Vector2d>();
			Object o;
			for (ObjValVec ovv : list) {
				Vector2d v = ovv.getVec();
				textx = (int) v.getX();
				texty = (int) v.getY();

				try {
					o = ovv.getObj();
					if (o instanceof Analog) {
						text = "a" + ((Analog) o).getPort() + ":" + ovv.getVal();
						c.drawRect(textx - 5, texty - 5, textx + 5, texty + 5, textpaint);
					} else if (o instanceof Digital) {
						text = "d" + ((Digital) o).getPort() + ":" + ovv.getVal();
					} else if (o instanceof Motor) {
						text = "m" + ((Motor) o).getPort() + ":" + ovv.getVal();
						c.drawCircle(textx, texty, 8, textpaint);
					} else if (o instanceof Servo) {
						text = "s" + ((Servo) o).getPort() + ":" + ovv.getVal();
						verts.add(v);
						c.drawCircle(textx, texty, 8, textpaint);
					} else if (o instanceof String) {
						text = (String) o;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				Rect rect = new Rect();
				paintText.setTextSize(FONT_SIZE);
				paintText.getTextBounds(text, 0, text.length(), rect);

				rect.inset(-TITLE_MARGIN, -TITLE_MARGIN);

				if (textx < c.getWidth() / 2) {
					rect.offsetTo(0, texty - FONT_SIZE - rect.height());
					// textx - rect.width() / 2
				} else {
					rect.offsetTo(c.getWidth() - rect.width(), texty - FONT_SIZE - rect.height());
				}

				paintText.setTextAlign(Paint.Align.CENTER);
				paintText.setTextSize(FONT_SIZE);
				paintText.setARGB(255, 255, 255, 255);
				paintRect.setARGB(130, 0, 0, 0);

				c.drawLine(rect.exactCenterX(), rect.exactCenterY(), textx, texty, linepaint);
				c.drawRoundRect(new RectF(rect), 4, 4, paintRect);
				c.drawText(text, rect.left + rect.width() / 2, rect.bottom - TITLE_MARGIN, paintText);
			}

			for (int i = 0; i < verts.size() - 1; i++) {
				Vector2d v0 = verts.get(i);
				Vector2d v1 = verts.get(i + 1);
				c.drawLine(v0.getX(), v0.getY(), v1.getX(), v1.getY(), framepaint);
			}

			if (pointlist.size() > 0) {
				Vector2d start = pointlist.get(0);
				Vector2d end = pointlist.get(pointlist.size() - 1);
				shader = new LinearGradient(start.getX(), start.getY(), end.getX(), end.getY(), Color.WHITE, Color.RED, Shader.TileMode.CLAMP);
				pathpaint.setShader(shader);
				final Path path = new Path();
				path.moveTo(start.getX(), start.getY());
				for (Vector2d v : pointlist) {
					path.lineTo(v.getX(), v.getY());
				}
				c.drawPath(path, pathpaint);
			}
		}
	}

	public void moveAlongPath(View view) {
		Paint paint = new Paint() {
			{
				setStyle(Paint.Style.STROKE);
				setStrokeCap(Paint.Cap.ROUND);
				setStrokeWidth(3.0f);
				setAntiAlias(true);
			}
		};
		int x1 = 10;
		int y1 = 10;
		int x2 = 550;
		int y2 = 100;
		int x3 = 800;
		int y3 = 800;
		int baseR = 0xFF;
		int baseG = 0x00;
		int baseB = 0x00;
		shader = new LinearGradient(x1, y1, x3, y3, Color.rgb(baseR, baseG, baseB), Color.BLACK, Shader.TileMode.CLAMP);
		paint.setShader(shader);
		final Path path = new Path();
		path.moveTo(x1, y1);
		path.quadTo(x2, y2, x3, y3);
		// c.drawPath(path, paint);

		view.setVisibility(VISIBLE);
		ObjectAnimator anim = ObjectAnimator.ofFloat(view, "x", "y", path);
		anim.setDuration(5000);
		anim.setRepeatCount(-1);
		anim.setRepeatMode(1);
		anim.start();
	}
}
