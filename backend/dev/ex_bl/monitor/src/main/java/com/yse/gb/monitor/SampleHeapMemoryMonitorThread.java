package com.yse.gb.monitor;

import com.yse.gb.util.monitor.AbstractHeapMemoryMonitorThread;

/**
 * AbstractHeapMemoryMonitorThreadを継承したMXBeanのサンプルです。
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
public class SampleHeapMemoryMonitorThread extends AbstractHeapMemoryMonitorThread {

	/**
	 * コンストラクタ。
	 */
	public SampleHeapMemoryMonitorThread() {
		super();
	}

	/* (非 Javadoc)
	 * @see com.yse.gb.util.monitor.AbstractMonitorThread#startThreadAutomatically()
	 */
	@Override
	protected synchronized boolean startThreadAutomatically() {
		// スレッドの自動実行間隔を10秒に設定
		setThreadRunInterval(10000);

		// 通知を行う閾値(%)を設定
		setUsageUpperThreshold(90);
		setUsageLowerThreshold(30);

		// スレッドを自動起動しない
		return false;
	}

}
