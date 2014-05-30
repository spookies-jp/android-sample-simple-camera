package jp.co.spookies.android.simplecamera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class SimpleCameraActivity extends Activity {
	private CameraFrame frame;
	private SimpleCameraCallback camera = null;
	private int selfTimer = 0;

	private static final int MENU_COLOR_EFFECT = 0;
	private static final int MENU_FLASH_MODE = 1;
	private static final int MENU_FOCUS_MODE = 2;
	private static final int MENU_SCENE_MODE = 3;
	private static final int MENU_WHITE_BALANCE = 4;
	private static final int MENU_SELF_TIMER = 100;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.simple_camera);

		SurfaceView view = (SurfaceView) findViewById(R.id.camera_view);
		frame = (CameraFrame) findViewById(R.id.camera_frame);
		camera = new SimpleCameraCallback(getApplicationContext());
		view.getHolder().addCallback(camera);
		view.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			new Thread(new Runnable() {
				public void run() {
					camera.takePicture(selfTimer, frame);
				}
			}).start();
			return true;
		}
		return super.onTouchEvent(event);
	}

	/**
	 * メニュー設定
	 * 端末でサポートしている機能のみメニューに含める
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (camera.getSupportedColorEffects() != null)
			menu.add(Menu.NONE, MENU_COLOR_EFFECT, Menu.NONE, "COLOR EFFECT");
		if (camera.getSupportedFlashModes() != null)
			menu.add(Menu.NONE, MENU_FLASH_MODE, Menu.NONE, "FLASH");
		if (camera.getSupportedFocusModes() != null)
			menu.add(Menu.NONE, MENU_FOCUS_MODE, Menu.NONE, "FOCUS");
		if (camera.getSupportedSceneModes() != null)
			menu.add(Menu.NONE, MENU_SCENE_MODE, Menu.NONE, "SCENE");
		if (camera.getSupportedWhiteBalance() != null)
			menu.add(Menu.NONE, MENU_WHITE_BALANCE, Menu.NONE, "WHITE BALANCE");
		menu.add(Menu.NONE, MENU_SELF_TIMER, Menu.NONE, "SELF TIMER");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_COLOR_EFFECT:
			final String[] itemColorEffects = camera.getSupportedColorEffects()
					.toArray(new String[0]);
			new AlertDialog.Builder(this)
					.setTitle("list")
					.setItems(itemColorEffects,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									camera.setColorEffect(itemColorEffects[which]);
								}
							}).create().show();
			break;
		case MENU_FLASH_MODE:
			final String[] itemFlashModes = camera.getSupportedFlashModes()
					.toArray(new String[0]);
			new AlertDialog.Builder(this)
					.setTitle("list")
					.setItems(itemFlashModes,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									camera.setFlashMode(itemFlashModes[which]);
								}
							}).create().show();
			break;
		case MENU_FOCUS_MODE:
			final String[] itemFocusModes = camera.getSupportedFocusModes()
					.toArray(new String[0]);
			new AlertDialog.Builder(this)
					.setTitle("list")
					.setItems(itemFocusModes,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									camera.setFocusMode(itemFocusModes[which]);
								}
							}).create().show();
			break;
		case MENU_SCENE_MODE:
			final String[] itemSceneModes = camera.getSupportedSceneModes()
					.toArray(new String[0]);
			new AlertDialog.Builder(this)
					.setTitle("list")
					.setItems(itemSceneModes,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									camera.setSceneMode(itemSceneModes[which]);
								}
							}).create().show();
			break;
		case MENU_WHITE_BALANCE:
			final String[] itemWhiteBalance = camera.getSupportedWhiteBalance()
					.toArray(new String[0]);
			new AlertDialog.Builder(this)
					.setTitle("list")
					.setItems(itemWhiteBalance,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									camera.setWhiteBalance(itemWhiteBalance[which]);
								}
							}).create().show();
			break;
		case MENU_SELF_TIMER:
			final String[] itemSelfTimer = SelfTimers.getSelfTimerNames();
			new AlertDialog.Builder(this)
					.setTitle("list")
					.setItems(itemSelfTimer,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									SelfTimers s = SelfTimers
											.getByName(itemSelfTimer[which]);
									if (s != null) {
										selfTimer = s.getTime();
									}
								}
							}).create().show();
		}
		return true;
	}

	/**
	 * セルフタイマーの列挙型
	 */
	enum SelfTimers {
		// なし、3秒、10秒から選択
		NONE(0, "NONE"), SHORT(3, "3 sec"), LONG(10, "10 sec");

		private int time;
		private String name;

		SelfTimers(int time, String name) {
			this.time = time;
			this.name = name;
		}

		public int getTime() {
			return this.time;
		}

		public String getName() {
			return this.name;
		}

		public static SelfTimers getByName(String name) {
			for (SelfTimers selfTimer : SelfTimers.values()) {
				if (selfTimer.getName().equals(name)) {
					return selfTimer;
				}
			}
			return null;
		}

		public static String[] getSelfTimerNames() {
			SelfTimers[] selfTimers = SelfTimers.values();
			int length = selfTimers.length;
			String[] names = new String[length];
			for (int i = 0; i < length; i++) {
				names[i] = selfTimers[i].getName();
			}
			return names;
		}
	}
}