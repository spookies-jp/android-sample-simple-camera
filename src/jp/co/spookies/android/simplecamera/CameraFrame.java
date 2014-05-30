package jp.co.spookies.android.simplecamera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.View;

/**
 * セルフタイマーのカウントダウン描画View
 * 
 */
public class CameraFrame extends View implements
		SimpleCameraCallback.ISelfTimerCallback {
	private int width;
	private int height;
	private int time = 0;
	private Paint paint = new Paint();

	public CameraFrame(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		width = w;
		height = h;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		drawCountDown(canvas);
	}

	/**
	 * カウントダウンの数字描画
	 * 
	 * @param canvas
	 */
	private void drawCountDown(Canvas canvas) {
		paint.setAntiAlias(true);
		paint.setColor(0x88FFFFFF);
		paint.setTextSize(200f);
		paint.setTextAlign(Align.RIGHT);
		if (time > 0) {
			canvas.drawText(Integer.toString(time), width - 20, height - 20,
					paint);
		}
	}

	@Override
	public void countDown(int time) {
		// 残りカウントを取得して画面更新
		this.time = time;
		postInvalidate();
	}
}
