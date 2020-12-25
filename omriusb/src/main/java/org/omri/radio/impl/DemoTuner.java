package org.omri.radio.impl;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import org.omri.radioservice.RadioService;
import org.omri.radioservice.RadioServiceType;
import org.omri.tuner.Tuner;
import org.omri.tuner.TunerListener;
import org.omri.tuner.TunerStatus;
import org.omri.tuner.TunerType;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.omri.BuildConfig.DEBUG;

/**
 * Copyright (C) 2020 realzoulou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author realzoulou
 */

public class DemoTuner implements Tuner {

	private final String TAG = "DemoTuner";
	private final TunerType mTunertype = TunerType.TUNER_TYPE_DAB;

	private TunerStatus mTunerStatus = TunerStatus.TUNER_STATUS_NOT_INITIALIZED;
	private final List<RadioService> mServices = new ArrayList<>();
	private final List<TunerListener> mTunerlisteners = Collections.synchronizedList(new ArrayList<>());
	private RadioService mCurrentlyRunningService = null;
	private final String mInputFilesPath;
	private boolean mCollectRecordingsAfterInit = false;


	DemoTuner(String inputFilesPath) {
		mInputFilesPath = inputFilesPath;
	}

	@Override
	public void initializeTuner() {
		if(mTunerStatus == TunerStatus.TUNER_STATUS_NOT_INITIALIZED) {
			if (DEBUG) Log.d(TAG, "Initializing Tuner");
			mCollectRecordingsAfterInit = true;
			UsbHelper.getInstance().attachDemoTuner(this);
			new CollectRecordingsTask(this).execute();
		}
	}

	@Override
	public void suspendTuner() {
		switch (mTunerStatus) {
			case TUNER_STATUS_SUSPENDED:
			case TUNER_STATUS_NOT_INITIALIZED:
				break;
			case TUNER_STATUS_SCANNING:
				stopRadioServiceScan();
			case TUNER_STATUS_INITIALIZED:
			case TUNER_STATUS_ERROR: {
				if(DEBUG)Log.d(TAG, "Suspending Tuner: " + this);
				mTunerStatus = TunerStatus.TUNER_STATUS_SUSPENDED;

				for (TunerListener listener : mTunerlisteners) {
					listener.tunerStatusChanged(this, mTunerStatus);
				}
				break;
			}
		}
	}

	@Override
	public void resumeTuner() {
		if(mTunerStatus == TunerStatus.TUNER_STATUS_SUSPENDED) {
			mTunerStatus = TunerStatus.TUNER_STATUS_INITIALIZED;

			for (TunerListener listener : mTunerlisteners) {
				listener.tunerStatusChanged(this, mTunerStatus);
			}
		}
	}

	@Override
	public void deInitializeTuner() {
		switch (mTunerStatus) {
			case TUNER_STATUS_NOT_INITIALIZED:
				break;
			case TUNER_STATUS_SCANNING:
				stopRadioServiceScan();
			case TUNER_STATUS_SUSPENDED:
			case TUNER_STATUS_ERROR:
			case TUNER_STATUS_INITIALIZED: {
				mTunerStatus = TunerStatus.TUNER_STATUS_NOT_INITIALIZED;
				for (TunerListener listener : mTunerlisteners) {
					listener.tunerStatusChanged(this, mTunerStatus);
				}
				mTunerlisteners.clear();
				mServices.clear();
				mCurrentlyRunningService = null;
				UsbHelper.getInstance().detachDemoTuner(this);
			}
		}
	}

	@Override
	public TunerType getTunerType() {
		return mTunertype;
	}

	@Override
	public TunerStatus getTunerStatus() {
		return mTunerStatus;
	}

	@Override
	public List<RadioService> getRadioServices() {
		if(mTunerStatus == TunerStatus.TUNER_STATUS_INITIALIZED) {
			return mServices;
		} else {
			return new ArrayList<>();
		}
	}

	@NonNull
	@Override
	public ArrayList<RadioService> getLinkedRadioServices(@NonNull RadioService service) {
		// dummy implementation, not required to be implemented
		return new ArrayList<>();
	}

	@Override
	public void startRadioServiceScan() {
		// Not supported
	}

	@Override
	public void startRadioServiceScan(Object scanOptions) {
		// Not supported
	}

	@Override
	public void stopRadioServiceScan() {
		// Not supported
	}

	@Override
	public void startRadioService(RadioService radioService) {
        if (DEBUG) Log.d(TAG, "Starting Service: " + radioService.getServiceLabel());
        mCurrentlyRunningService = radioService;
        UsbHelper.getInstance().startDemoService(radioService);
    }

	@Override
	public void stopRadioService() {
		if (mCurrentlyRunningService != null) {
			if (DEBUG)Log.d(TAG, "Stopping Service: " + mCurrentlyRunningService.getServiceLabel());
			mCurrentlyRunningService = null;
			UsbHelper.getInstance().stopDemoService();
		}
	}

	@Override
	public RadioService getCurrentRunningRadioService() {
		return mCurrentlyRunningService;
	}

	@Override
	public @NonNull String getHardwareVersion() {
		return "demoHw";
	}

	@Override
	public @NonNull String getSoftwareVersion() {
		return "demoSw";
	}

	@Override
	public void subscribe(TunerListener tunerListener) {
		if(!mTunerlisteners.contains(tunerListener)) {
			mTunerlisteners.add(tunerListener);
		}
	}

	@Override
	public void unsubscribe(TunerListener tunerListener) {
		mTunerlisteners.remove(tunerListener);
	}

	/* callbacks from native code --------- */
	public void serviceStarted(RadioService radioService) {
		for (TunerListener listener : mTunerlisteners) {
			listener.radioServiceStarted(this, radioService);
		}
	}

	public void serviceStopped(RadioService radioService) {
		for (TunerListener listener : mTunerlisteners) {
			listener.radioServiceStopped(this, radioService);
			((RadioServiceImpl) radioService).serviceStopped();
		}
	}
	/*  --------- callbacks from native code END */

	private class CollectRecordingsTask extends AsyncTask<Void, Void, Void> {
		private final String TAG = "CollectRecordingsTask";
		private final Tuner mInstance;

		CollectRecordingsTask(Tuner instance) {
			mInstance = instance;
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... voids) {
			File inputDir = new File(mInputFilesPath);
			if (inputDir.isDirectory()) {
				String[] filenames = inputDir.list(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name != null
								// see RaonTunerInput::rawRecordOpen
								&& name.startsWith("dab_")
								&& name.endsWith(".raw");
					}
				});
				if (filenames != null) {
					int counter = 1;
					for (String filename : filenames) {
						File file = new File(inputDir, filename);
						if (file.exists()) {
							if (DEBUG) Log.d(TAG, "recording found:'" + file.getAbsolutePath() + "'");
							filename = filename.replace("dab_", "");
							filename = filename.replace(".raw", "");
							String[] splits = filename.split("_");
							String label = "";
							int eid=0, sid=0;
							if (splits.length == 5) {
								// Y-m-d_H-M-S_LABEL_EID_SID
								try {
									label = splits[2].trim();
									eid = Integer.parseInt(splits[3], 16);
									sid = Integer.parseInt(splits[4], 16);
								} catch (Exception e) {
									e.printStackTrace();
								}
							} else if (splits.length == 4) {
								// Y-m-d_H-M-S_scan_FreqMHz
								label = splits[2] + " " + splits[3]; // e.g. "scan 178352"
								eid = sid = counter; // a dummy EId/SId
								counter++;
							}
							else {
								Log.w(TAG, "split filename: " + Arrays.toString(splits));
							}
							RadioServiceDabImpl foundService = new RadioServiceDabImpl();
							foundService.setServiceLabel(label);
							foundService.setLongDescription(file.getAbsolutePath());
							foundService.setEnsembleId(eid);
							foundService.setServiceId(sid);
							foundService.setIsProgrammeService(true);
							foundService.setEnsembleFrequency(174928000 /* fake 5A */);
							mServices.add(foundService);
						}
					}
				}
			} else {
				Log.w(TAG, "not a dir:'" + mInputFilesPath + "'");
			}

			while (!RadioServiceManager.getInstance().isServiceListReady(RadioServiceType.RADIOSERVICE_TYPE_DAB) || !VisualLogoManager.getInstance().isReady()) {
				try {
					Thread.sleep(10);
					if(DEBUG)Log.d(TAG, "Waiting for servicelist or VisualLogoManager to be ready");
				} catch(Exception e) {
					if(DEBUG)e.printStackTrace();
				}
			}

			RadioServiceManager rsm = RadioServiceManager.getInstance();
			rsm.clearServiceList(RadioServiceType.RADIOSERVICE_TYPE_DAB);
			for (RadioService service : mServices) {
				rsm.addRadioService(service);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			if (DEBUG) Log.d(TAG, "CollectRecordingsTask finished with " + mServices.size()
					+ " services");
			if(mInstance != null && mCollectRecordingsAfterInit) {
				mCollectRecordingsAfterInit = false;
				for (TunerListener listener : mTunerlisteners) {
					mTunerStatus = TunerStatus.TUNER_STATUS_INITIALIZED;
					listener.tunerStatusChanged(mInstance, mTunerStatus);
				}
			}
		}
	}

}
