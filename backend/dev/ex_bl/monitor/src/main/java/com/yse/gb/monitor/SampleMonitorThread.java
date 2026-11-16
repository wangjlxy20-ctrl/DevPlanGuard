package com.yse.gb.monitor;

import java.io.IOException;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;

import javax.management.Notification;

import com.yse.gb.util.SystemInformationConnection;
import com.yse.gb.util.monitor.AbstractMonitorThread;

/**
 * AbstractMonitorThreadを継承したMXBeanのサンプルです。
 *
 * @author 30029733/Kazuya.Tada
 */
/*
 * ========================== MODIFICATION HISTORY ==========================
 * Release  Date       ID/Name                   Comment
 * --------------------------------------------------------------------------
 * R1.00.00 2016/06/31 30029733/Kazuya.Tada      初版
 * [END OF MODIFICATION HISTORY]
 * ==========================================================================
 */
public class SampleMonitorThread extends AbstractMonitorThread {

	// メモリーMXBean
	private volatile MemoryMXBean mxBean;

	// 一回前の使用メモリと時刻
	private volatile long preTimeMillis;
	private volatile long preHeapMemoryUsed;

	// 今の使用メモリと時刻
	private volatile long nowTimeMillis;
	private volatile long nowHeapMemoryUsed;

	// 使用メモリ増加速度
	private volatile double heapIncreaseSpeed;

	// 通知関連
	private volatile long sequenceNumber = 0;
	private static final String NOTIFICATION_SOURCE = "com.yse.gb.util.sample:type=monitor,name=SampleMonitor";
	private static final String NOTIFICATION_TYPE_NOW_USAGE = "monitor.heap.nowusage";
	private static final String NOTIFICATION_TYPE_TOO_FAST = "monitor.heap.toofast";
	private static final String FORMAT_MESSAGE_NOW_USAGE = "heap eden used : %d bytes.";
	private static final String FORMAT_MESSAGE_TOO_FAST = "heap eden speed : %.2f MB/sec.";

	// 閾値とする速度(MB/sec)：システムに合わせて変更して使うこと
	private static final int HEAP_INCREASE_SPEED_THRESHOLD = 3;

	// スレッドの処理中で実行間隔ごとに現在のヒープメモリーの使用量を通知するかどうかのフラグ
	private static final boolean SEND_NOTIFICATION_NOW_USAGE = true;

	/**
	 * コンストラクタ。
	 */
	public SampleMonitorThread() {
		super();
	}

	/* (非 Javadoc)
	 * @see com.yse.gb.util.monitor.AbstractMonitorThread#startThreadAutomatically()
	 */
	@Override
	protected synchronized boolean startThreadAutomatically() {
		// スレッドの自動実行間隔を１０秒に設定
		setThreadRunInterval(10000);

		// スレッドを自動起動しない
		return false;
	}

	/* (非 Javadoc)
	 * @see com.yse.gb.util.monitor.AbstractMonitorThread#doThreadTask()
	 */
	@Override
	protected synchronized List<Notification> doThreadTask() {
		List<Notification> notifications = new ArrayList<>();

		// MemoryMXBean取得
		if (this.mxBean == null) {
			try (SystemInformationConnection connection = new SystemInformationConnection()) {
				this.mxBean = connection.getMemoryMXBean();

				// 現在のヒープメモリの使用量を取得
				getNowHeapMemoryUsed(this.mxBean);

			} catch (IOException e) {
				System.out.println(this.getClass().getName() + ":MemoryMXBeanの取得でエラーが発生しました。");
				e.printStackTrace(System.out);
			}

		} else {
			// 前回値の保持
			this.preHeapMemoryUsed = this.nowHeapMemoryUsed;
			this.preTimeMillis = this.nowTimeMillis;

			// 現在のヒープメモリの使用量を取得
			getNowHeapMemoryUsed(this.mxBean);

			if (SEND_NOTIFICATION_NOW_USAGE) {
				// 現在のヒープメモリの使用量を通知する内容を作成
				String message = String.format(FORMAT_MESSAGE_NOW_USAGE, this.nowHeapMemoryUsed);
				Notification notification = new Notification(NOTIFICATION_TYPE_NOW_USAGE, NOTIFICATION_SOURCE,
						incrementSequenceNumber(), message);
				notifications.add(notification);
			}

			// ヒープメモリの使用量の増加速度(MB/sec)を算出
			culcHeapIncreaseSpeed();

			// 増加速度が閾値よりも速い場合は通知
			if (this.heapIncreaseSpeed > HEAP_INCREASE_SPEED_THRESHOLD) {
				String message = String.format(FORMAT_MESSAGE_TOO_FAST, this.heapIncreaseSpeed);
				Notification notification = new Notification(NOTIFICATION_TYPE_TOO_FAST, NOTIFICATION_SOURCE,
						incrementSequenceNumber(), message);
				notification.setUserData(this.heapIncreaseSpeed);
				notifications.add(notification);
			}
		}

		return notifications;
	}

	/**
	 * 現在のヒープメモリの使用量を取得
	 *
	 * @param mxBean メモリーMXBean
	 */
	private synchronized void getNowHeapMemoryUsed(MemoryMXBean mxBean) {
		MemoryUsage heapMemoryUsage = mxBean.getHeapMemoryUsage();
		this.nowHeapMemoryUsed = heapMemoryUsage.getUsed();
		this.nowTimeMillis = System.currentTimeMillis();
	}

	/**
	 * ヒープメモリの使用量の増加速度(MB/sec)を算出
	 */
	private synchronized void culcHeapIncreaseSpeed() {
		double usedDiff = this.nowHeapMemoryUsed - this.preHeapMemoryUsed;
		double timeDiff = this.nowTimeMillis - this.preTimeMillis;
		this.heapIncreaseSpeed = (usedDiff / 1024 / 1024) / (timeDiff / 1000);
		System.out.println(this.getClass().getName() + ":preHeapMemoryUsed=" + this.preHeapMemoryUsed + ", preTimeMillis=" + this.preTimeMillis);
		System.out.println(this.getClass().getName() + ":nowHeapMemoryUsed=" + this.nowHeapMemoryUsed + ", nowTimeMillis=" + this.nowTimeMillis);
		System.out.println(this.getClass().getName() + ":usedDiff=" + usedDiff + ", timeDiff=" + timeDiff);
		System.out.println(this.getClass().getName() + ":heapIncreaseSpeed=" + heapIncreaseSpeed);
	}

	/**
	 * {@link this.sequenceNumber}をインクリメントする。
	 *
	 * @return インクリメント後の値
	 */
	private synchronized long incrementSequenceNumber() {
		return this.sequenceNumber++;
	}

}
