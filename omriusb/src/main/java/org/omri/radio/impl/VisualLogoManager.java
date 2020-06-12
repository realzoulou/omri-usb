package org.omri.radio.impl;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.omri.radio.Radio;
import org.omri.radioservice.RadioService;
import org.omri.radioservice.RadioServiceDab;
import org.omri.radioservice.RadioServiceType;
import org.omri.radioservice.metadata.Visual;
import org.omri.radioservice.metadata.VisualMimeType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.omri.BuildConfig.DEBUG;

class VisualLogoManager {

	private final static String TAG = "VisualLogoManager";
	private final static String VIS_CACHE_DIR = "logo_cache";
    private final static String LOGOS_FILENAME = "logos.json";

	private static VisualLogoManager mManagerInstance = null;

	private CopyOnWriteArrayList<VisualLogoImpl> mLogoList = new CopyOnWriteArrayList<>();
	private AtomicBoolean mSerializingInProgress = new AtomicBoolean();
	private AtomicBoolean mDeserializingInProgress = new AtomicBoolean();

	private Thread mDeSerThread = null;

	private VisualLogoManager() {
		final Context context = ((RadioImpl) Radio.getInstance()).mContext;
		if (context != null) {
			File logoDir = new File(context.getCacheDir(), VIS_CACHE_DIR);
			if (DEBUG) Log.d(TAG, "LogoCacheDir: " + logoDir.getAbsolutePath());

			if (!logoDir.exists()) {
				boolean logoCacheCreated = logoDir.mkdir();
				if (logoCacheCreated) {
					if (DEBUG) Log.d(TAG, "Created successfully LogoCacheDir");
				} else {
					Log.w(TAG, "Creating LogoCacheDir failed");
				}
			} else {
				mDeSerThread = new Thread(new Runnable() {
					@Override
					public void run() {
						Thread.currentThread().setName("DeserLogos");
						deserializeLogos();
					}
				});
				mDeSerThread.start();
			}
		}
	}

	static VisualLogoManager getInstance() {
		if(mManagerInstance == null) {
			mManagerInstance = new VisualLogoManager();
		}

		return mManagerInstance;
	}

	void destroyInstance() {
		if (mDeSerThread != null && mDeSerThread.isAlive()) {
			try {
				mDeSerThread.join();
			} catch (Exception e) {
				if (DEBUG) e.printStackTrace();
			} finally {
				mDeSerThread = null;
				mDeserializingInProgress.set(false);
			}
		}
		if (mLogoList != null) {
			mLogoList.clear();
		}
		mManagerInstance = null;
	}

	boolean isReady() {
		return !mDeserializingInProgress.get();
	}

	void addLogoVisual(VisualLogoImpl logoVisual) {
		if(!mLogoList.contains(logoVisual)) {
			if(DEBUG)Log.d(TAG, "Adding Logo " + logoVisual.getLogoUrl());
			mLogoList.add(logoVisual);
		}
	}

	List<Visual> getLogoVisuals(RadioService service) {
		ArrayList<Visual> retArr = new ArrayList<>();
		if(mLogoList == null) {
			return retArr;
		}

		if(service.getRadioServiceType() == RadioServiceType.RADIOSERVICE_TYPE_DAB || service.getRadioServiceType() == RadioServiceType.RADIOSERVICE_TYPE_EDI) {
			for(VisualLogoImpl vis : mLogoList) {
				for(RadioDnsEpgBearer bearer : vis.getBearers()) {
					if(bearer.getBearerType() == RadioDnsEpgBearerType.DAB) {
						if(((RadioDnsEpgBearerDab)bearer).getEnsembleId() == ((RadioServiceDab)service).getEnsembleId() &&
								((RadioDnsEpgBearerDab)bearer).getServiceId() == ((RadioServiceDab)service).getServiceId() &&
								((RadioDnsEpgBearerDab)bearer).getEnsembleEcc() == ((RadioServiceDab)service).getEnsembleEcc() ) {
							retArr.add(vis);
							break;
						}
					}
				}
			}
		} else if(service.getRadioServiceType() == RadioServiceType.RADIOSERVICE_TYPE_IP) {
			for(VisualLogoImpl vis : mLogoList) {
				boolean logoFound = false;
				for(RadioDnsEpgBearer bearer : vis.getBearers()) {
					for(RadioDnsEpgBearer srvBearer : ((RadioServiceIpImpl)service).getBearers()) {
						if(bearer.equals(srvBearer)) {
							retArr.add(vis);
							logoFound = true;
							break;
						}
					}

					if(logoFound) {
						break;
					}
				}
			}
		}

		return retArr;
	}

	void serializeLogos() {
		if(!mSerializingInProgress.get()) {
			mSerializingInProgress.set(true);

			if (DEBUG) Log.d(TAG, "LogoJson Serializing " + mLogoList.size() + " LogoVisuals");

			if (((RadioImpl) Radio.getInstance()).mContext != null) {
				File visCacheDir = new File(((RadioImpl) Radio.getInstance()).mContext.getCacheDir(), VIS_CACHE_DIR);
				if (!visCacheDir.exists()) {
					boolean cacheDirCreated = visCacheDir.mkdir();
					if (DEBUG) {
						Log.d(TAG, "VisCache dir created: " + cacheDirCreated);
					}
				}

				if (visCacheDir.exists()) {
					try {
						JSONArray visArr = new JSONArray();
						for (VisualLogoImpl logo : mLogoList) {
							JSONObject visObj = new JSONObject();

							visObj.put("logoUrl", logo.getLogoUrl());
							visObj.put("visType", logo.getVisualType().toString());
							visObj.put("filePath", logo.getFilePath());
							visObj.put("width", logo.getVisualWidth());
							visObj.put("height", logo.getVisualHeight());
							visObj.put("mimeType", logo.getVisualMimeType().toString());

							JSONArray bearersArr = new JSONArray();
							for (RadioDnsEpgBearer bearer : logo.getBearers()) {
								JSONObject bearerObj = new JSONObject();

								bearerObj.put("mimeValue", bearer.getMimeValue());
								bearerObj.put("bearerId", bearer.getBearerId());
								bearerObj.put("bearerType", bearer.getBearerType().toString());
								bearerObj.put("bitrate", bearer.getBitrate());
								bearerObj.put("cost", bearer.getCost());

								bearersArr.put(bearerObj);
							}
							visObj.put("bearers", bearersArr);

							visArr.put(visObj);
						}

						BufferedWriter logoListWriter = null;
						try {
							//save to file
							if (DEBUG) Log.d(TAG, "Serializing LogoJson writing to file...");

							File srvListFile = new File(visCacheDir, LOGOS_FILENAME);
							logoListWriter = new BufferedWriter(new FileWriter(srvListFile));
							logoListWriter.write(visArr.toString(2));
							logoListWriter.close();
						} catch (JSONException jsonExc) {
							if (DEBUG) jsonExc.printStackTrace();
						} catch (IOException ioExc) {
							if (DEBUG) ioExc.printStackTrace();
						} finally {
							mSerializingInProgress.set(false);

							if (logoListWriter != null) {
								try {
									logoListWriter.close();
								} catch (IOException ioExc) {
									if (DEBUG) ioExc.printStackTrace();
								}
							}
						}
					} catch (JSONException jsonExc) {
						if (DEBUG) jsonExc.printStackTrace();
					}
				} else {
					mSerializingInProgress.set(false);
				}
			}

			if (DEBUG) Log.d(TAG, "Serializing LogoJson done!");
		} else {
			if(DEBUG)Log.d(TAG, "Serializing already in progress");
		}
	}

	private void deserializeLogos() {
		if (!mDeserializingInProgress.get()) {
			mDeserializingInProgress.set(true);

			if (DEBUG) Log.d(TAG, "Restoring LogoJson");
			if (((RadioImpl) Radio.getInstance()).mContext == null) {
				Log.w(TAG, "deserializeLogos: Radio context null");
				mDeserializingInProgress.set(false);
				return;
			}
			File visCacheFile = new File(((RadioImpl) Radio.getInstance()).mContext.getCacheDir().getAbsolutePath()
					+ File.separatorChar + VIS_CACHE_DIR + File.separatorChar + LOGOS_FILENAME);
			if (visCacheFile.exists()) {
				if (mLogoList != null) {
					mLogoList.clear();
				} else {
					mLogoList = new CopyOnWriteArrayList<VisualLogoImpl>();
				}

				FileInputStream logoJsonInputStream = null;
				BufferedReader logoJsonReader = null;
				try {
					logoJsonInputStream = new FileInputStream(visCacheFile);
					logoJsonReader = new BufferedReader(new InputStreamReader(logoJsonInputStream));
					StringBuilder logoJsonBuilder = new StringBuilder();

					char[] readBuf = new char[4096];
					int bytesRead = 0;
					while ((bytesRead = logoJsonReader.read(readBuf)) != -1) {
						logoJsonBuilder.append(readBuf, 0, bytesRead);
					}

					if (DEBUG)
						Log.d(TAG, "Read LogoJson filedata done: " + logoJsonBuilder.length());

					JSONArray logoListArr = new JSONArray(logoJsonBuilder.toString());
					if (DEBUG) Log.d(TAG, "Read LogoJson length: " + logoListArr.length());
					ArrayList<VisualLogoImpl> deserList = new ArrayList<>();
					for (int i = 0; i < logoListArr.length(); i++) {
						JSONObject logoObj = logoListArr.getJSONObject(i);

						VisualLogoImpl logo = new VisualLogoImpl();

						logo.setLogoUrl(logoObj.getString("logoUrl"));
						logo.setVisualMimeType(VisualMimeType.valueOf(logoObj.getString("mimeType")));
						logo.setFilePath(logoObj.getString("filePath"));
						logo.setWidth(logoObj.getInt("width"));
						logo.setHeight(logoObj.getInt("height"));

						JSONArray bearersArr = logoObj.getJSONArray("bearers");
						for (int j = 0; j < bearersArr.length(); j++) {
							JSONObject bearerObj = bearersArr.getJSONObject(j);

							RadioDnsEpgBearerType bearerType = RadioDnsEpgBearerType.valueOf(bearerObj.getString("bearerType"));
							String bearerId = bearerObj.getString("bearerId");
							String mimeValue = bearerObj.getString("mimeValue");
							int bitrate = bearerObj.getInt("bitrate");
							int cost = bearerObj.getInt("cost");
							switch (bearerType) {
								case DAB:
									logo.addBearer(new RadioDnsEpgBearerDab(bearerId, cost, mimeValue, bitrate));
									break;
								case IP_HTTP:
									logo.addBearer(new RadioDnsEpgBearerIpHttp(bearerId, cost, mimeValue, bitrate));
									break;
								default:
									break;
							}
						}

						// sanity check, only in DEBUG build to not slow down the app startup
						if (DEBUG) {
							if (!logo.isAvailable()) {
								Log.w(TAG, "not avail:" + logo.getFilePath());
							}
						}
						deserList.add(logo);
					}

					mLogoList.addAll(deserList);

					if (DEBUG) Log.d(TAG, "Restoring LogoJson done");
				} catch (JSONException jsonExc) {
					if (DEBUG) jsonExc.printStackTrace();
				} catch (FileNotFoundException fnfExc) {
					if (DEBUG) fnfExc.printStackTrace();
				} catch (IOException ioExc) {
					if (DEBUG) ioExc.printStackTrace();
				} finally {
					mDeserializingInProgress.set(false);

					try {
						if (logoJsonInputStream != null) {
							logoJsonInputStream.close();
						}
						if (logoJsonReader != null) {
							logoJsonReader.close();
						}
					} catch (IOException ioExc) {
						if (DEBUG) ioExc.printStackTrace();
					}
				}
			} else {
				mDeserializingInProgress.set(false);
				if (DEBUG) Log.d(TAG, "Restoring LogoJson does not exist");
			}
		}
	}
}
