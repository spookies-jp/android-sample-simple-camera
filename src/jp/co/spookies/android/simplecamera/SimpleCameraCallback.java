package jp.co.spookies.android.simplecamera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.view.SurfaceHolder;

public class SimpleCameraCallback implements SurfaceHolder.Callback {
	protected Camera camera = null;
	private Camera.Parameters params = null;
	private boolean timerLock = false;
	private Context context = null;
	private final File path = new File(
			Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
			"SimpleCamera");
	private final SimpleDateFormat format = new SimpleDateFormat(
			"yyyyMMddHHmmssSSS'.jpg'");
	private final Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			if (jpegCallback != null) {
				jpegCallback.onPictureTaken(data, camera);
			}
			timerLock = false;
		}
	};
	private Camera.PictureCallback jpegCallback;
	private final Camera.PictureCallback defaultJpegCallback = new Camera.PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			File file = new File(path, format.format(new Date(System
					.currentTimeMillis())));
			try {
				if (!path.exists()) {
					// フォルダがなければ生成
					path.mkdirs();
				}
				// 書き込み
				FileOutputStream out = new FileOutputStream(file);
				out.write(data);
				out.close();
				// ギャラリー登録
				MediaScannerConnection.scanFile(context,
						new String[] { file.toString() }, null, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (camera != null) {
				// プレビュー再開
				camera.startPreview();
			}
			timerLock = false;
		}
	};

	public SimpleCameraCallback(Context context) {
		this.context = context;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		params = camera.getParameters();

		// プレビューサイズ設定
		List<Camera.Size> supportedPreviewSizes = params
				.getSupportedPreviewSizes();
		Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {
			@Override
			public int compare(Camera.Size s1, Camera.Size s2) {
				return s1.width - s2.width;
			}
		});
		// 画面サイズにもっとも近いサイズを使用
		Camera.Size previewSize = supportedPreviewSizes.get(0);
		for (Camera.Size size : supportedPreviewSizes) {
			if (size.width > width) {
				break;
			}
			previewSize = size;
		}
		params.setPreviewSize(previewSize.width, previewSize.height);
		camera.setParameters(params);

		// プレビュー開始
		camera.startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (camera == null) {
			// カメラ取得
			camera = Camera.open();
		}
		try {
			// ビューとの接続
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			camera.release();
			camera = null;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// カメラの解放
		camera.stopPreview();
		camera.release();
		camera = null;
	}

	/**
	 * シンプルに撮影
	 */
	public void takePicture() {
		takePicture(null, null, defaultJpegCallback, 0, null);
	}

	/**
	 * コールバックを指定して撮影
	 * 
	 * @param shutter
	 * @param raw
	 * @param jpeg
	 */
	public void takePicture(Camera.ShutterCallback shutter,
			Camera.PictureCallback raw, Camera.PictureCallback jpeg) {
		takePicture(shutter, raw, jpeg, 0, null);
	}

	/**
	 * セルフタイマー指定して撮影
	 * 
	 * @param time
	 * @param callback
	 */
	public void takePicture(int time, ISelfTimerCallback callback) {
		takePicture(null, null, defaultJpegCallback, time, callback);
	}

	/**
	 * 撮影
	 * 
	 * @param shutter
	 * @param raw
	 * @param jpeg
	 * @param time
	 * @param callback
	 */
	public void takePicture(Camera.ShutterCallback shutter,
			Camera.PictureCallback raw, Camera.PictureCallback jpeg, int time,
			ISelfTimerCallback callback) {
		// 連続で呼ばれても撮影しない
		if (timerLock) {
			return;
		}
		timerLock = true;

		// カウントダウン
		for (int i = 0; i < time; i++) {
			if (callback != null) {
				callback.countDown(time - i);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (callback != null) {
			callback.countDown(0);
		}
		if (camera != null) {
			jpegCallback = jpeg;
			// 撮影
			camera.takePicture(shutter, raw, pictureCallback);
		}
	}

	/**
	 * 指定できるカラーエフェクト一覧
	 * 
	 * @return
	 */
	public List<String> getSupportedColorEffects() {
		return params.getSupportedColorEffects();
	}

	/**
	 * カラーエフェクト設定
	 * 
	 * @param value
	 */
	public void setColorEffect(String value) {
		if (getSupportedColorEffects().contains(value)) {
			params.setColorEffect(value);
			camera.setParameters(params);
		}
	}

	/**
	 * 指定できるフラッシュモード一覧
	 * 
	 * @return
	 */
	public List<String> getSupportedFlashModes() {
		return params.getSupportedFlashModes();
	}

	/**
	 * フラッシュモード設定
	 * 
	 * @param value
	 */
	public void setFlashMode(String value) {
		if (getSupportedFlashModes().contains(value)) {
			params.setFlashMode(value);
			camera.setParameters(params);
		}
	}

	/**
	 * 指定できるフォーカス一覧取得
	 * 
	 * @return
	 */
	public List<String> getSupportedFocusModes() {
		return params.getSupportedFocusModes();
	}

	/**
	 * フォーカスモード設定
	 * 
	 * @param value
	 */
	public void setFocusMode(String value) {
		if (getSupportedFocusModes().contains(value)) {
			params.setFocusMode(value);
			camera.setParameters(params);
		}
	}

	/**
	 * 指定できるシーンモード一覧取得
	 * 
	 * @return
	 */
	public List<String> getSupportedSceneModes() {
		return params.getSupportedSceneModes();
	}

	/**
	 * シーンモード設定
	 * 
	 * @param value
	 */
	public void setSceneMode(String value) {
		if (getSupportedSceneModes().contains(value)) {
			params.setSceneMode(value);
			camera.setParameters(params);
		}
	}

	/**
	 * 指定できるホワイトバランス一覧取得
	 * 
	 * @return
	 */
	public List<String> getSupportedWhiteBalance() {
		return params.getSupportedWhiteBalance();
	}

	/**
	 * ホワイトバランス設定
	 * 
	 * @param value
	 */
	public void setWhiteBalance(String value) {
		if (getSupportedWhiteBalance().contains(value)) {
			params.setWhiteBalance(value);
			camera.setParameters(params);
		}
	}

	/**
	 * 水平視野取得
	 * 
	 * @return
	 */
	public float getHorizontalViewAngle() {
		return params.getHorizontalViewAngle();
	}

	/*
	 * セルフタイマーのカウントダウン用コールバックインターフェース
	 */
	public interface ISelfTimerCallback {
		public void countDown(int time);
	}
}
