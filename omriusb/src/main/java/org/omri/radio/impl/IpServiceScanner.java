package org.omri.radio.impl;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import org.omri.BuildConfig;
import org.omri.radio.Radio;
import org.omri.radioservice.RadioService;
import org.omri.radioservice.RadioServiceDab;
import org.omri.radioservice.RadioServiceIp;
import org.omri.radioservice.RadioServiceIpStream;
import org.omri.radioservice.RadioServiceMimeType;
import org.omri.radioservice.RadioServiceType;
import org.omri.radioservice.metadata.Visual;
import org.omri.radioservice.metadata.VisualMimeType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import eu.hradio.core.radiodns.RadioDnsCore;
import eu.hradio.core.radiodns.RadioDnsCoreLookupCallback;
import eu.hradio.core.radiodns.RadioDnsFactory;
import eu.hradio.core.radiodns.RadioDnsServiceEpg;
import eu.hradio.core.radiodns.RadioDnsServiceEpgSiCallback;
import eu.hradio.core.radiodns.RadioEpgServiceInformation;
import eu.hradio.core.radiodns.radioepg.bearer.Bearer;
import eu.hradio.core.radiodns.radioepg.bearer.BearerType;
import eu.hradio.core.radiodns.radioepg.description.Description;
import eu.hradio.core.radiodns.radioepg.genre.Genre;
import eu.hradio.core.radiodns.radioepg.link.Link;
import eu.hradio.core.radiodns.radioepg.mediadescription.MediaDescription;
import eu.hradio.core.radiodns.radioepg.multimedia.Multimedia;
import eu.hradio.core.radiodns.radioepg.multimedia.MultimediaType;
import eu.hradio.core.radiodns.radioepg.name.Name;
import eu.hradio.core.radiodns.radioepg.serviceinformation.Service;
import eu.hradio.httprequestwrapper.dtos.service_search.RankedStandaloneService;
import eu.hradio.httprequestwrapper.dtos.service_search.ServiceList;
import eu.hradio.httprequestwrapper.exception.HRadioSearchClientException;
import eu.hradio.httprequestwrapper.listener.OnErrorListener;
import eu.hradio.httprequestwrapper.listener.OnSearchResultListener;
import eu.hradio.httprequestwrapper.service.ServiceSearchClient;
import eu.hradio.httprequestwrapper.service.ServiceSearchClientImpl;

import static org.omri.BuildConfig.DEBUG;

/**
 * Copyright (C) 2018 IRT GmbH
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
 * @author Fabian Sattler, IRT GmbH
 */

class IpServiceScanner {

	private final static String TAG = "IpServiceScanner";

	private static IpServiceScanner mScannerInstance = new IpServiceScanner();

	private List<IpScannerListener> mScannerListeners = new ArrayList<>();

	private boolean mIsScanning = false;
	private AtomicInteger mSiCallbacksPendings = new AtomicInteger();
	private int mCoreCallbacksTotal = -1;

	private ConcurrentHashMap<RadioService, RadioDnsCore> mLookups = new ConcurrentHashMap<>();
	private ConcurrentHashMap<RadioService, eu.hradio.core.radiodns.RadioDnsService> mAvailableServices = new ConcurrentHashMap<>();

	private File mLogoCacheDir;

	private IpServiceScanner() {
		createLogoFilesCacheDir();
	}

	static IpServiceScanner getInstance() {
		return mScannerInstance;
	}

	void addScanListener(IpScannerListener listener) {
		if(!mScannerListeners.contains(listener)) {
			mScannerListeners.add(listener);
		}
	}

	void removeScanListener(IpScannerListener listener) {
		mScannerListeners.remove(listener);
	}

	boolean isScanning() {
		return mIsScanning;
	}

	private RadioDnsCoreLookupCallback mCoreCallback = new RadioDnsCoreLookupCallback() {
		@Override
		public void coreLookupFinished(RadioService radioService, List<eu.hradio.core.radiodns.RadioDnsService> availableServices) {
			if(!mIsScanning) {
				return;
			}

			mLookups.remove(radioService);

			if(DEBUG)Log.d(TAG, "Corelookup for: '" + radioService.getServiceLabel() + "' finished, " + mLookups.size() + " lookups remaining");

			for(eu.hradio.core.radiodns.RadioDnsService rdnsSrv : availableServices) {
				if(rdnsSrv.getServiceType() == eu.hradio.core.radiodns.RadioDnsServiceType.RADIO_EPG) {
					if(!mAvailableServices.containsValue(rdnsSrv)) {
						if(DEBUG)Log.d(TAG, "Adding " + rdnsSrv.getTarget() + " to SI scan list");
						mAvailableServices.put(radioService, rdnsSrv);
					} else {
						if(DEBUG)Log.d(TAG, "AvailServicesMap already contains this service");
					}

					break;
				}
			}

			if(mLookups.isEmpty()) {
				if(DEBUG)Log.d(TAG, "Corelookup scan finished " + mAvailableServices.size() + " EPG services available");

				if(!mAvailableServices.isEmpty()) {
					mSiCallbacksPendings.set(mAvailableServices.values().size());

					notifyListeners(50, false);

					for (eu.hradio.core.radiodns.RadioDnsService rdnsSrv : mAvailableServices.values()) {
						((RadioDnsServiceEpg) rdnsSrv).getServiceInformation(mSiCallback);
					}
				} else {
					notifyListeners(100, true);
				}
			} else {
				double scanProgress = (((double)mCoreCallbacksTotal - (double)mLookups.size()) / (double)mCoreCallbacksTotal) * 45.d;
				notifyListeners((int)(5.d+scanProgress), false);
			}
		}
	};

	private OnSearchResultListener<ServiceList> mOnSearchResultListener = new OnSearchResultListener<ServiceList>() {
		@Override
		public void onResult(ServiceList serviceList) {

			notifyListeners(50, false);

			List<RadioService> foundIpServices = new ArrayList<>();
			double perSrv = 50.d / serviceList.getContent().length;
			int srvsDone = 0;
			for(RankedStandaloneService rankedSrv : serviceList.getContent()) {
				if(!mIsScanning) {
					return;
				}

				RadioServiceIpImpl ipSrv = new RadioServiceIpImpl();
				ipSrv.setHradioSearchSource(rankedSrv.getSource());

				notifyListeners((int)(50 + (perSrv*srvsDone++)), false);

				if(rankedSrv.getContent().getName().isEmpty()) {
					if(DEBUG)Log.w(TAG, "HRadioHttpClient Servicename is empty");
					continue;
				}

				ipSrv.setServiceLabel(rankedSrv.getContent().getName());

				if(BuildConfig.DEBUG) Log.d(TAG, "HRadioHttpClient Service: " + rankedSrv.getContent().getName() + ", " + rankedSrv.getContent().getId() + ", Source: " + rankedSrv.getSource() + ", Score: " + rankedSrv.getScore());
				for(eu.hradio.httprequestwrapper.dtos.service_search.Bearer bearer : rankedSrv.getContent().getBearers()) {
					if(BuildConfig.DEBUG) Log.d(TAG, "HRadioHttpClient Bearer Type: " + bearer.getType().toString() + ", Address: " + bearer.getAddress() + ", MIME: " + bearer.getMimeType() + ", Bitrate: " + bearer.getBitrate());

					switch (bearer.getType()) {
						case DAB:
							ipSrv.addBearer(new RadioDnsEpgBearerDab(bearer.getAddress(), 100, bearer.getMimeType(), bearer.getBitrate()));
							break;
						case HTTP: {
							ipSrv.addBearer(new RadioDnsEpgBearerIpHttp(bearer.getAddress(), 10, bearer.getMimeType(), bearer.getBitrate()));

							RadioServiceIpStreamImpl stream = new RadioServiceIpStreamImpl();
							stream.setBitrate(bearer.getBitrate());
							stream.setOffset(-1);
							stream.setStreamUrl(bearer.getAddress());

							switch (bearer.getMimeType()) {
								case "audio/aac":
								case "audio/aacp":
								case "audio/x-scpls":
									stream.setMimeType(RadioServiceMimeType.AUDIO_AAC);
									stream.setCost(80);
									break;
								case "audio/mpeg":
								case "audio/x-mpegurl":
									stream.setMimeType(RadioServiceMimeType.AUDIO_MPEG);
									stream.setCost(100);
									break;
								case "audio/edi":
									if(DEBUG)Log.d(TAG, "HRadioHttpClientEDI found EDI stream");
									stream.setMimeType(RadioServiceMimeType.AUDIO_EDI);
									stream.setCost(100);
									break;
								default:
									stream.setMimeType(RadioServiceMimeType.UNKNOWN);
									stream.setCost(-1);
									break;
							}

							//don't add unknown streams
							if(stream.getMimeType() != RadioServiceMimeType.UNKNOWN) {
								if(DEBUG) {
									if(stream.getMimeType() == RadioServiceMimeType.AUDIO_EDI) {
										Log.d(TAG, "HRadioHttpClientEDI adding EDI stream for: " + ipSrv.getServiceLabel());
									}
								}
								ipSrv.addStream(stream);
							}

							break;
						}
					}
				}

				boolean srvOkay = false;
				if(ipSrv.getIpStreams().isEmpty()) {
					if(DEBUG)Log.w(TAG, "HRadioHttpClient IpStreams empty, searching available DAB service");
					for(RadioService searchSrv : Radio.getInstance().getRadioServices()) {
						if(searchSrv.getRadioServiceType() == RadioServiceType.RADIOSERVICE_TYPE_DAB || searchSrv.getRadioServiceType() == RadioServiceType.RADIOSERVICE_TYPE_EDI) {
							RadioServiceDab dabSrv = (RadioServiceDab)searchSrv;
							for (RadioDnsEpgBearer dnsBear : ipSrv.getBearers()) {
								if (dnsBear.getBearerType() == RadioDnsEpgBearerType.DAB) {
									RadioDnsEpgBearerDab dabBearer = (RadioDnsEpgBearerDab)dnsBear;
									if(dabSrv.getEnsembleId() == dabBearer.getEnsembleId() && dabSrv.getEnsembleEcc() == dabBearer.getEnsembleEcc() && dabSrv.getServiceId() == dabBearer.getServiceId()) {
										if(DEBUG)Log.d(TAG, "HRadioHttpClient Found DAB service '" + dabSrv.getServiceLabel() + "' for IPService without IpStreams");
										srvOkay = true;
										break;
									}
								}
							}
						}
					}
				} else {
					srvOkay = true;
				}

				if(srvOkay) {
					//TODO other properties like location, keywords, genres

					//mediadescriptions
					for (eu.hradio.httprequestwrapper.dtos.service_search.MediaDescription mediaDesc : rankedSrv.getContent().getMediaDescriptions()) {
						if (mediaDesc.getType() != null) {
							if (mediaDesc.getType().equals("SHORT_DESCRIPTION")) {
								if(DEBUG)Log.d(TAG, "HRadioHttpClient Setting short description");
								if(mediaDesc.getShortDescription() != null) {
									if (!mediaDesc.getShortDescription().isEmpty()) {
										ipSrv.setShortDescription(mediaDesc.getShortDescription());
									}
								}
							}

							if (mediaDesc.getType().equals("LONG_DESCRIPTION")) {
								if(DEBUG)Log.d(TAG, "HRadioHttpClient Setting long description");
								if(mediaDesc.getLongDescription() != null) {
									if (!mediaDesc.getLongDescription().isEmpty()) {
										ipSrv.setLongDescription(mediaDesc.getLongDescription());
									}
								}
							}

							if(mediaDesc.getType().equals("MULTIMEDIA")) {
								if(DEBUG)Log.d(TAG, "HRadioHttpClient Setting mediadescription multimedia");

								if(mediaDesc.getMultimediaType() != null) {
									MultimediaType mmType = null;
									VisualMimeType logoMime = VisualMimeType.METADATA_VISUAL_MIMETYPE_UNKNOWN;
									int logoWidth = 0;
									int logoHeight = 0;

									switch (mediaDesc.getMultimediaType()) {
										case "logo_colour_square": {
											mmType = MultimediaType.MULTIMEDIA_LOGO_SQUARE;
											logoMime = VisualMimeType.METADATA_VISUAL_MIMETYPE_PNG;
											logoWidth = 32;
											logoHeight = 32;
											break;
										}
										case "logo_colour_rectangle": {
											mmType = MultimediaType.MULTIMEDIA_LOGO_RECTANGLE;
											logoMime = VisualMimeType.METADATA_VISUAL_MIMETYPE_PNG;
											logoWidth = 112;
											logoHeight = 32;
											break;
										}
										case "":
										case "logo_unrestricted": {
											mmType = MultimediaType.MULTIMEDIA_LOGO_UNRESTRICTED;
											for(VisualMimeType mimeType : VisualMimeType.values()) {
												if(mimeType.getMimeTypeString().equalsIgnoreCase(mediaDesc.getMimeValue())) {
													logoMime = mimeType;
													break;
												}
											}
											if(mediaDesc.getHeight() <= 0 || mediaDesc.getWidth() <= 0) {
												if(DEBUG)Log.w(TAG, "HRadioHttpClient " + MultimediaType.MULTIMEDIA_LOGO_UNRESTRICTED.toString() + " has invalid dimensions of: " + mediaDesc.getWidth() + "x" + mediaDesc.getHeight());
												continue;
											}

											logoWidth = mediaDesc.getWidth();
											logoHeight = mediaDesc.getHeight();

											break;
										}
									}

									if(logoMime != VisualMimeType.METADATA_VISUAL_MIMETYPE_UNKNOWN) {
										Multimedia logoMm = new Multimedia(mmType, "en", mediaDesc.getUrl(), mediaDesc.getMimeValue(), logoWidth, logoHeight);

										VisualLogoImpl stationLogo = new VisualLogoImpl();
										stationLogo.setHeight(logoHeight);
										stationLogo.setWidth(logoWidth);

										stationLogo.setVisualMimeType(logoMime);
										stationLogo.setLogoUrl(mediaDesc.getUrl());
										stationLogo.addBearer(ipSrv.getBearers());

										Collections.sort(stationLogo.getBearers());

										boolean downloadLogo = true;
										List<Visual> logoList = VisualLogoManager.getInstance().getLogoVisuals(ipSrv);
										for(Visual compVis : logoList) {
											if(compVis.equals(stationLogo)) {
												if(DEBUG)Log.d(TAG, "HRadioHttpClient Logo " + ((VisualLogoImpl)compVis).getLogoUrl() + " already exists");
												if(DEBUG)Log.d(TAG, "HRadioHttpClient Logo for " + ipSrv.getServiceLabel() + " : " + compVis.getVisualWidth() + "x" + compVis.getVisualHeight() + " : " + compVis.getVisualMimeType() + " already exists");
												downloadLogo = false;
												break;
											}
										}

										if(downloadLogo) {
											String logoPath = downloadHttpLogoFile(logoMm.getUrl(), logoMm, stationLogo);
											if (logoPath != null) {
												stationLogo.setFilePath(logoPath);
												VisualLogoManager.getInstance().addLogoVisual(stationLogo);
											}
										} else {
											if(DEBUG)Log.d(TAG, "HRadioHttpClient Logos for " + ipSrv.getServiceLabel() + " existing, not downloading");
										}
									}
								}
							}
						}
					}

					foundIpServices.add(ipSrv);
				} else {
					if(DEBUG)Log.d(TAG, "HRadioHttpClient No IP streams and no matching DAB service found");
				}
			}

			for(RadioService foundSrv : foundIpServices) {
				if(foundSrv.getRadioServiceType() == RadioServiceType.RADIOSERVICE_TYPE_IP) {
					RadioServiceIp ipSrv = (RadioServiceIp)foundSrv;
					for(RadioServiceIpStream stream : ipSrv.getIpStreams()) {
						if(stream.getMimeType() == RadioServiceMimeType.AUDIO_EDI) {
							if(DEBUG)Log.d(TAG, "HRadioHttpClient Found EdiStream Service at: " + stream.getUrl());
						}
					}
				}
			}

			for(IpScannerListener listener : mScannerListeners) {
				listener.foundStreamingServices(new ArrayList<>(foundIpServices));
			}

			VisualLogoManager.getInstance().serializeLogos();

			notifyListeners(100, true);

			mIsScanning = false;
		}
	};

	private OnErrorListener mOnErrorListener = new OnErrorListener() {
		@Override
		public void onError(HRadioSearchClientException e) {
			if (BuildConfig.DEBUG) Log.e(TAG, "HRadioHttpClient onError: " + e.getMessage());
			if (BuildConfig.DEBUG) e.printStackTrace();

			notifyListeners(100, true);
			mIsScanning = false;
		}
	};

	private volatile boolean mEnrichInProgress = false;
	void enrichServices(List<RadioService> services) {
		if(((RadioImpl)Radio.getInstance()).mContext != null && !mEnrichInProgress) {
			mEnrichInProgress = true;

			for(RadioService enrichSrv : services) {
				if(enrichSrv.getRadioServiceType() == RadioServiceType.RADIOSERVICE_TYPE_DAB) {
					RadioServiceDabImpl dabSrv = (RadioServiceDabImpl) enrichSrv;
					if(dabSrv.isProgrammeService()) {
						String bearer = "dab:" + Integer.toHexString(dabSrv.getServiceId()).substring(0, 1) + Integer.toHexString(dabSrv.getEnsembleEcc()) + "." +
								Integer.toHexString(dabSrv.getEnsembleId()) + "." +
								Integer.toHexString(dabSrv.getServiceId()) + "." +
								"0";

						if(DEBUG)Log.d(TAG, "Enrich BearerId for Service " + dabSrv.getServiceLabel() + " : " + bearer);
						Map<String, String> builder = new HashMap<>();
						builder.put("bearers.address", bearer);

						ServiceSearchClient impl = new ServiceSearchClientImpl();

						impl.asyncServiceSearch(builder, new OnSearchResultListener<ServiceList>() {
							@Override
							public void onResult(ServiceList serviceList) {
								if(DEBUG)Log.d(TAG, "Enrich found " + serviceList.getSize() + " services for bearer: " + bearer);
								for(RankedStandaloneService rankedSrv : serviceList.getContent()) {
									if(DEBUG)Log.d(TAG, "Enrich rankedSrv: " + rankedSrv.getContent().getName());
									for(eu.hradio.httprequestwrapper.dtos.service_search.MediaDescription mDesc : rankedSrv.getContent().getMediaDescriptions()) {
										if(mDesc.getType().equals("MULTIMEDIA")) {
											if(DEBUG)Log.d(TAG, "Enrich LogoUrl: " + mDesc.getUrl());
										}
									}

									//mediadescriptions
									for (eu.hradio.httprequestwrapper.dtos.service_search.MediaDescription mediaDesc : rankedSrv.getContent().getMediaDescriptions()) {
										if (mediaDesc.getType() != null) {

											if (mediaDesc.getType().equals("SHORT_DESCRIPTION")) {
												if(DEBUG)Log.d(TAG, "HRadioHttpClient Setting short description");
												if(mediaDesc.getShortDescription() != null) {
													if (!mediaDesc.getShortDescription().isEmpty()) {
														dabSrv.setShortDescription(mediaDesc.getShortDescription());
													}
												}
											}

											if (mediaDesc.getType().equals("LONG_DESCRIPTION")) {
												if(DEBUG)Log.d(TAG, "HRadioHttpClient Setting long description");
												if(mediaDesc.getLongDescription() != null) {
													if (!mediaDesc.getLongDescription().isEmpty()) {
														dabSrv.setLongDescription(mediaDesc.getLongDescription());
													}
												}
											}

											if(mediaDesc.getType().equals("MULTIMEDIA")) {
												if(DEBUG)Log.d(TAG, "HRadioHttpClient Setting mediadescription multimedia");

												if(mediaDesc.getMultimediaType() != null) {
													MultimediaType mmType = null;
													VisualMimeType logoMime = VisualMimeType.METADATA_VISUAL_MIMETYPE_UNKNOWN;
													int logoWidth = 0;
													int logoHeight = 0;

													switch (mediaDesc.getMultimediaType()) {
														case "logo_colour_square": {
															mmType = MultimediaType.MULTIMEDIA_LOGO_SQUARE;
															logoMime = VisualMimeType.METADATA_VISUAL_MIMETYPE_PNG;
															logoWidth = 32;
															logoHeight = 32;
															break;
														}
														case "logo_colour_rectangle": {
															mmType = MultimediaType.MULTIMEDIA_LOGO_RECTANGLE;
															logoMime = VisualMimeType.METADATA_VISUAL_MIMETYPE_PNG;
															logoWidth = 112;
															logoHeight = 32;
															break;
														}
														case "":
														case "logo_unrestricted": {
															mmType = MultimediaType.MULTIMEDIA_LOGO_UNRESTRICTED;
															for(VisualMimeType mimeType : VisualMimeType.values()) {
																if(mimeType.getMimeTypeString().equalsIgnoreCase(mediaDesc.getMimeValue())) {
																	logoMime = mimeType;
																	break;
																}
															}
															if(mediaDesc.getHeight() <= 0 || mediaDesc.getWidth() <= 0) {
																if(DEBUG)Log.w(TAG, "HRadioHttpClient " + MultimediaType.MULTIMEDIA_LOGO_UNRESTRICTED.toString() + " has invalid dimensions of: " + mediaDesc.getWidth() + "x" + mediaDesc.getHeight());
																continue;
															}

															logoWidth = mediaDesc.getWidth();
															logoHeight = mediaDesc.getHeight();

															break;
														}
													}

													if(logoMime != VisualMimeType.METADATA_VISUAL_MIMETYPE_UNKNOWN) {
														Multimedia logoMm = new Multimedia(mmType, "en", mediaDesc.getUrl(), mediaDesc.getMimeValue(), logoWidth, logoHeight);

														VisualLogoImpl stationLogo = new VisualLogoImpl();
														stationLogo.setHeight(logoHeight);
														stationLogo.setWidth(logoWidth);

														stationLogo.setVisualMimeType(logoMime);
														stationLogo.setLogoUrl(mediaDesc.getUrl());
														RadioDnsEpgBearerDab dabBearer = new RadioDnsEpgBearerDab(bearer, 20, "audio/aac", dabSrv.getServiceComponents().size() > 0 ? dabSrv.getServiceComponents().get(0).getBitrate() : 0);
														stationLogo.addBearer(dabBearer);

														Collections.sort(stationLogo.getBearers());

														boolean downloadLogo = true;
														List<Visual> logoList = VisualLogoManager.getInstance().getLogoVisuals(dabSrv);
														for(Visual compVis : logoList) {
															if(compVis.equals(stationLogo)) {
																if(DEBUG)Log.d(TAG, "HRadioHttpClient Logo " + ((VisualLogoImpl)compVis).getLogoUrl() + " already exists");
																if(DEBUG)Log.d(TAG, "HRadioHttpClient Logo for " + dabSrv.getServiceLabel() + " : " + compVis.getVisualWidth() + "x" + compVis.getVisualHeight() + " : " + compVis.getVisualMimeType() + " already exists");
																downloadLogo = false;
																break;
															}
														}

														if(downloadLogo) {
															String logoPath = downloadHttpLogoFile(logoMm.getUrl(), logoMm, stationLogo);
															if (logoPath != null) {
																stationLogo.setFilePath(logoPath);
																VisualLogoManager.getInstance().addLogoVisual(stationLogo);
															}
														} else {
															if(DEBUG)Log.d(TAG, "HRadioHttpClient Logos for " + dabSrv.getServiceLabel() + " existing, not downloading");
														}
													}
												}
											}
										}
									}

									VisualLogoManager.getInstance().serializeLogos();
								}
							}
						}, new OnErrorListener() {
							@Override
							public void onError(HRadioSearchClientException e) {
								if(DEBUG)Log.e(TAG, "Enrich onError: " + e.getMessage());
								if(DEBUG)e.printStackTrace();
							}
						});
					}
				}

				mEnrichInProgress = false;
				if(DEBUG)Log.d(TAG, "Enrich finished");
			}
		}
	}

	private void scanHradioServices(Bundle optBundle) {
		if(DEBUG)Log.d(TAG, "Starting HRadio ServiceScan: " + mIsScanning);
		if(((RadioImpl)Radio.getInstance()).mContext != null) {
			if (!mIsScanning) {
				mIsScanning = true;

				for (IpScannerListener listener : mScannerListeners) {
					listener.scanStarted();
					listener.scanProgress(0);
				}

				ServiceSearchClient srchClient = new ServiceSearchClientImpl();

				Map<String, String> scanOptMap = new HashMap<>();
				for(String scanOptKey : optBundle.keySet()) {
					Object scanOptVal = optBundle.get(scanOptKey);
					if(scanOptVal instanceof String) {
						if(DEBUG)Log.d(TAG, "ScanOption: " + scanOptKey + " : " + scanOptVal);
						scanOptMap.put(scanOptKey, (String)scanOptVal);
					}
				}

				if(!scanOptMap.isEmpty()) {
					if (BuildConfig.DEBUG) Log.d(TAG, "HRadioHttpClient starting scan with options");
					srchClient.asyncServiceSearch(scanOptMap, mOnSearchResultListener, mOnErrorListener, true);
				} else {
					if (BuildConfig.DEBUG) Log.d(TAG, "HRadioHttpClient starting scan without options");
					srchClient.asyncGetAllServices(mOnSearchResultListener, mOnErrorListener);
				}
			}
		}
	}

	private boolean mSorted  = false;
	private RadioDnsServiceEpgSiCallback mSiCallback = new RadioDnsServiceEpgSiCallback() {
		@Override
		public void serviceInformationRetrieved(RadioEpgServiceInformation radioEpgServiceInformation, RadioDnsServiceEpg service) {
			if(!mIsScanning) {
				return;
			}

			if(radioEpgServiceInformation != null) {
				List<RadioService> foundIpServices = new ArrayList<>();
				for (Service siSrv : radioEpgServiceInformation.getServices()) {
					if(DEBUG)Log.d(TAG, "SI ServiceName: " + (siSrv.getNames().size() > 0 ? siSrv.getNames().get(0).getName() : ""));
					RadioServiceIpImpl ipSrv = new RadioServiceIpImpl();

					for (Bearer bearer : siSrv.getBearers()) {
						if(DEBUG)Log.d(TAG, "SI Bearer: " + bearer.getBearerIdString() + ", MIME: " + bearer.getMimeType());
						ipSrv.addBearer(bearer);

						if(bearer.getBearerType() == BearerType.BEARER_TYPE_HTTP || bearer.getBearerType() == BearerType.BEARER_TYPE_HTTPS) {
							RadioServiceIpStreamImpl stream = new RadioServiceIpStreamImpl();
							stream.setBitrate(bearer.getBitrate());
							stream.setCost(bearer.getCost());
							stream.setOffset(bearer.getOffset());
							stream.setStreamUrl(bearer.getBearerIdString());
							switch (bearer.getMimeType()) {
								case "audio/aac":
								case "audio/aacp":
								case "audio/x-scpls":
									stream.setMimeType(RadioServiceMimeType.AUDIO_AAC);
									break;
								case "audio/mpeg":
								case "audio/x-mpegurl":
									stream.setMimeType(RadioServiceMimeType.AUDIO_MPEG);
									break;
								case "audio/edi":
									stream.setMimeType(RadioServiceMimeType.AUDIO_EDI);
									break;
								default:
									stream.setMimeType(RadioServiceMimeType.UNKNOWN);
									break;
							}

							if(stream.getMimeType() != RadioServiceMimeType.UNKNOWN) {
								ipSrv.addStream(stream);
							}
						}
					}

					//TODO no IP bearers
					boolean srvOkay = false;
					if(ipSrv.getIpStreams().isEmpty()) {
						if(DEBUG)Log.w(TAG, "IpStreams empty, searching available DAB service");
						for(RadioService searchSrv : Radio.getInstance().getRadioServices()) {
							if(searchSrv.getRadioServiceType() == RadioServiceType.RADIOSERVICE_TYPE_DAB || searchSrv.getRadioServiceType() == RadioServiceType.RADIOSERVICE_TYPE_EDI) {
								RadioServiceDab dabSrv = (RadioServiceDab)searchSrv;
								for (RadioDnsEpgBearer dnsBear : ipSrv.getBearers()) {
									if (dnsBear.getBearerType() == RadioDnsEpgBearerType.DAB) {
										RadioDnsEpgBearerDab dabBearer = (RadioDnsEpgBearerDab)dnsBear;
										if(dabSrv.getEnsembleId() == dabBearer.getEnsembleId() && dabSrv.getEnsembleEcc() == dabBearer.getEnsembleEcc() && dabSrv.getServiceId() == dabBearer.getServiceId()) {
											if(DEBUG)Log.d(TAG, "Found DAB service '" + dabSrv.getServiceLabel() + "' for IPService without IpStreams");
											srvOkay = true;
											break;
										}
									}
								}
							}
						}
					} else {
						srvOkay = true;
					}

					if(srvOkay) {
						for(Name srvName : siSrv.getNames()) {
							switch (srvName.getType()) {
								case NAME_LONG:
									ipSrv.setServiceLabel(srvName.getName());
									break;
								case NAME_MEDIUM:
									if(ipSrv.getServiceLabel().isEmpty()) {
										ipSrv.setServiceLabel(srvName.getName());
									}
									break;
								case NAME_SHORT:
									if(ipSrv.getServiceLabel().isEmpty()) {
										ipSrv.setServiceLabel(srvName.getName());
									}
									break;
							}
						}

						for(Genre genre : siSrv.getGenres()) {
							TermIdImpl termId = new TermIdImpl();
							termId.setGenreHref(genre.getGenreHref());
							termId.setTermId(genre.getTvaCs().getScheme());
							termId.setGenreText(genre.getGenre());
							ipSrv.addGenre(termId);
						}

						if(siSrv.getKeywords() != null) {
							for(String keyWord : siSrv.getKeywords().getKeywords()) {
								ipSrv.addKeyword(keyWord);
							}
						}

						if(siSrv.getLinks() != null && !siSrv.getLinks().isEmpty()) {
							for(Link link : siSrv.getLinks()) {
								ipSrv.addLink(link.getUri());
							}
						}

						if(siSrv.getRadioDns() != null) {
							ipSrv.setRadioDns(siSrv.getRadioDns());
						}

						for(MediaDescription mediaDesc : siSrv.getMediaDescriptions()) {
							if(!mIsScanning) {
								if(DEBUG)Log.d(TAG, "Stopping multimedia processing because scan was stopped");
								return;
							}

							for(Description desc : mediaDesc.getDescriptions()) {
								switch (desc.getType()) {
									case DESCRIPTION_SHORT: {
										ipSrv.setShortDescription(desc.getDescription());
										break;
									}
									case DESCRIPTION_LONG: {
										ipSrv.setLongDescription(desc.getDescription());
										break;
									}
								}
							}

							Multimedia multiMedia = mediaDesc.getMultimedia();
							if(multiMedia != null) {
								if(DEBUG)Log.d(TAG, "SI Multimedia Type: " + multiMedia.getType().toString() + ", Width: " + multiMedia.getWidth() + ", Height: " + multiMedia.getHeight());

								//check for dimensions if type is MultimediaType.MULTIMEDIA_LOGO_UNRESTRICTED
								if(multiMedia.getType() == MultimediaType.MULTIMEDIA_LOGO_UNRESTRICTED) {
									if(multiMedia.getHeight() <= 0 || multiMedia.getWidth() <= 0) {
										if(DEBUG)Log.w(TAG, MultimediaType.MULTIMEDIA_LOGO_UNRESTRICTED.toString() + " has invalid dimensions of: " + multiMedia.getWidth() + "x" + multiMedia.getHeight());
										continue;
									}
								}

								VisualLogoImpl stationLogo = new VisualLogoImpl();
								stationLogo.setHeight(multiMedia.getHeight());
								stationLogo.setWidth(multiMedia.getWidth());

								//TODO logo size restrictions
								if(false) {
								//if(multiMedia.getHeight() < 128 || multiMedia.getHeight() > 128) {
									if(DEBUG)Log.d(TAG, "Not downloading logo because of size restriction");
									continue;
								}

								VisualMimeType logoMime = VisualMimeType.METADATA_VISUAL_MIMETYPE_UNKNOWN;
								for(VisualMimeType mimeType : VisualMimeType.values()) {
									if(mimeType.getMimeTypeString().equalsIgnoreCase(multiMedia.getMime())) {
										logoMime = mimeType;
										break;
									}
								}

								if(logoMime != VisualMimeType.METADATA_VISUAL_MIMETYPE_UNKNOWN) {
									stationLogo.setVisualMimeType(logoMime);
									stationLogo.setLogoUrl(multiMedia.getUrl());
									stationLogo.addBearer(ipSrv.getBearers());

									Collections.sort(stationLogo.getBearers());

									String logoPath = downloadHttpLogoFile(multiMedia.getUrl(), multiMedia, stationLogo);
									if (logoPath != null) {
										stationLogo.setFilePath(logoPath);
										VisualLogoManager.getInstance().addLogoVisual(stationLogo);
									}
								}
							}
						}


						if(ipSrv.getIpStreams().isEmpty()) {
							if(DEBUG)Log.w(TAG, "Empty IpStreams for: " + ipSrv.getServiceLabel() + " with " + ipSrv.getLogos().size() + " logos");
						}

						foundIpServices.add(ipSrv);
					} else {
						if(DEBUG)Log.d(TAG, "IpStreams empty and no DAB service found");
					}
				}

				for(IpScannerListener listener : mScannerListeners) {
					listener.foundStreamingServices(new ArrayList<>(foundIpServices));
				}

				if(DEBUG) {
					Log.d(TAG, "Found " + foundIpServices.size() + " IpServices");
					for (RadioService foundSrv : foundIpServices) {
						Log.d(TAG, "Found " + foundSrv.getServiceLabel());
					}
				}
			}

			mSiCallbacksPendings.decrementAndGet();

			if(mSiCallbacksPendings.get() == 0) {
				notifyListeners(100, true);

				VisualLogoManager.getInstance().serializeLogos();
			} else {
				double scanProgress  = (((double)mAvailableServices.size() - (double)mSiCallbacksPendings.get()) / (double)mAvailableServices.size()) * 50.d;
				notifyListeners((int)scanProgress + 50, false);
			}
		}
	};

	private void notifyListeners(int scanProgress, boolean finished) {
		for (IpScannerListener listener : mScannerListeners) {
			listener.scanProgress(scanProgress);
			if(finished) {
				listener.scanFinished();
				mIsScanning = false;
			}
		}
	}

	void scanServices(Bundle searchOptions) {
		if(!mIsScanning) {
			if (searchOptions != null) {
				scanHradioServices(searchOptions);
			} else {
				scanRdnsServices();
			}
		}
	}

	private void scanRdnsServices() {
		if(DEBUG)Log.d(TAG, "Starting ServiceScan: " + mIsScanning);
		if(((RadioImpl)Radio.getInstance()).mContext != null) {
			if (!mIsScanning) {
				mIsScanning = true;

				mLookups.clear();
				mAvailableServices.clear();
				mSiCallbacksPendings.set(0);

				for (IpScannerListener listener : mScannerListeners) {
					listener.scanStarted();
					listener.scanProgress(0);
				}

				ArrayList<RadioService> srvList = new ArrayList<>(Radio.getInstance().getRadioServices());
				if (DEBUG) Log.d(TAG, "Scanning " + srvList.size() + " services");
				if (!srvList.isEmpty()) {
					for (RadioService srv : srvList) {
						if (srv.getRadioServiceType() == RadioServiceType.RADIOSERVICE_TYPE_DAB || srv.getRadioServiceType() == RadioServiceType.RADIOSERVICE_TYPE_EDI) {
							if (((RadioServiceDab) srv).isProgrammeService()) {

								RadioDnsCore coreLookup = RadioDnsFactory.createCoreLookup(srv, true);
								mLookups.put(srv, coreLookup);
								if (DEBUG) Log.d(TAG, "Running lookuptasks: " + mLookups.size());
							}
						}
					}
				} else {
					notifyListeners(100, true);
				}

				mCoreCallbacksTotal = mLookups.size();

				for (RadioDnsCore core : mLookups.values()) {
					core.coreLookup(mCoreCallback, ((RadioImpl) Radio.getInstance()).mContext);
				}

				notifyListeners(5, false);
			}
		} else {
			notifyListeners(100, true);
		}
	}

	void stopScan() {
		if(mIsScanning) {
			mIsScanning = false;
			mSiCallbacksPendings.set(0);
			notifyListeners(100, true);
		}
	}

	@Nullable File getLogoFilesCacheDir() {
	    if (mLogoCacheDir == null) {
			mLogoCacheDir = createLogoFilesCacheDir();
        }
		return mLogoCacheDir;
	}

	@Nullable private File createLogoFilesCacheDir() {
		File dir = null;
		if(((RadioImpl)Radio.getInstance()).mContext != null) {
			dir = new File(((RadioImpl)Radio.getInstance()).mContext.getCacheDir(), "logofiles_cache");
			if(DEBUG)Log.d(TAG, "LogoFilesCacheDir: " + dir.getAbsolutePath());
			if(!dir.exists()) {
				boolean logoCacheCreated = dir.mkdirs();
				if(logoCacheCreated) {
					if(DEBUG)Log.d(TAG, "Created successfully LogoFilesCacheDir");
				} else {
					Log.w(TAG, "Creating LogoFilesCacheDir failed");
				}
			}
		} else {
			Log.w(TAG, "Radio context null");
		}
		return dir;
	}

	/* ETSI TS 102 818
	D.3.3 Caching
		It is recommended that the device use standard HTTP methods for checking whether a resource has changed since last
		acquisition, e.g. by using the If-Modified-Since parameter in the HTTP request for the resource. Similarly, it is
		recommended that the service provider respond to such requests in the expected way with the appropriate HTTP status
		code if the resource has not changed.
	*/
	private String downloadHttpLogoFile(String logoUrl, Multimedia mm, VisualLogoImpl logo) {
		if (DEBUG) Log.d(TAG, "LogoDownload URL: " + (logoUrl != null ? logoUrl : "null"));
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		InputStream inputStream = null;

		FileOutputStream fileOutputStream = null;
		HttpURLConnection httpUrlConnection = null;
		File logofile = null;
		boolean downloadNeeded = true;

		logofile = createLogoFileName(mm, logo);
		if (logofile == null) {
			return null;
		}
		final long logoFileLastModified = logofile.lastModified();
		final long logoFileAgeMillis = System.currentTimeMillis() - logoFileLastModified;

		if (logoFileAgeMillis > TimeUnit.DAYS.toMillis(1)) {
			// if the file from createLogoFile has not just been created, but already existed for > 1 day,
			// then use the file modification date and ask server if it has been modified since that time

			/* Variant 1: GET THE LAST MODIFIED TIME */
			try {
				httpUrlConnection = (HttpURLConnection) new URL(logoUrl).openConnection();
				long remoteLastModified = httpUrlConnection.getLastModified();
				if (remoteLastModified > 0L) {
					if (logoFileLastModified > remoteLastModified) {
						if (DEBUG) Log.d(TAG, logoUrl + " no download, remote last-modified " + dateFormat.format(new Date(remoteLastModified)));
						downloadNeeded = false;
					}
				} else {
					if (DEBUG) Log.d(TAG, logoUrl + " remote last-modified unknown");
				}
			} catch (Exception e) {
				if(DEBUG)e.printStackTrace();
			} finally {
				if (httpUrlConnection != null) httpUrlConnection.disconnect();
			}

			/* Variant 2: Using HTTP_NOT_MODIFIED */
			try {
				httpUrlConnection = (HttpURLConnection) new URL(logoUrl).openConnection();
				httpUrlConnection.setRequestMethod("HEAD");
				final int httpResponseCode = httpUrlConnection.getResponseCode();
				if (httpResponseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
					if (DEBUG)Log.d(TAG, logoUrl + " no download, not modified, code " + httpResponseCode);
					downloadNeeded = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (httpUrlConnection != null) httpUrlConnection.disconnect();
			}
		}
		else /* logoFileAgeMillis < 1 day */ {
            if (logoFileAgeMillis < TimeUnit.MINUTES.toMillis(30)) {
                // downloaded in the last 30 minutes, no need to download again
                downloadNeeded = false;
            }
        }

		if (downloadNeeded) {
			try {
				httpUrlConnection = getConnection(logoUrl);

				if (httpUrlConnection != null) {
					inputStream = httpUrlConnection.getInputStream();
					if (DEBUG)Log.d(TAG, "Downloading LogoFile: " + logofile.getAbsolutePath());
					fileOutputStream = new FileOutputStream(logofile);

					byte[] downBuff = new byte[16*1024];

					int len;
					while ((len = inputStream.read(downBuff)) != -1) {
						fileOutputStream.write(downBuff, 0, len);
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
				logofile = null;
			} finally {
				try {
					if (inputStream != null) {
						inputStream.close();
					}
					if (fileOutputStream != null) {
						fileOutputStream.close();
					}
					if (httpUrlConnection != null) {
						httpUrlConnection.disconnect();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		if (logofile != null) {
			if (!logofile.exists()) {
				// should have been downloaded but finally did not create a file
				logofile = null;

			} else if (logofile.length() == 0L) {
				// an empty file was created, delete it
				//noinspection ResultOfMethodCallIgnored
				logofile.delete();
				logofile = null;
			}


		}
		return logofile != null ? logofile.getName() : null;
	}

	private @Nullable HttpURLConnection getConnection(String connUrl) throws IOException {
		URL url = new URL(connUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(2000);
		conn.setConnectTimeout(2000);
		conn.setRequestMethod("GET");
		conn.setDoInput(true);
		conn.connect();

		int httpResponseCode = conn.getResponseCode();
		if(DEBUG)Log.d(TAG, "GetConnection HTTP responseCode: " + httpResponseCode);
		switch (httpResponseCode) {
			case HttpURLConnection.HTTP_OK:
				// all fine
			break;
			case HttpURLConnection.HTTP_NOT_FOUND: {
				// avoid running into an exception later
				conn.disconnect();
				conn = null;
			}
			break;
			case HttpURLConnection.HTTP_MOVED_PERM:
			case HttpURLConnection.HTTP_MOVED_TEMP: {
				String redirectUrl = conn.getHeaderField("Location");
				if (redirectUrl != null && !redirectUrl.isEmpty()) {
					if (DEBUG) Log.d(TAG, "GetConnection Following redirect to: " + redirectUrl);
					conn.disconnect();

					return getConnection(redirectUrl);
				}
			}
			break;
			default:
				// all other response codes:
				// let's see what happens when trying to read from the URL...
				break;
		}

		return conn;
	}

	private File createLogoFileName(Multimedia mm, VisualLogoImpl logo) {
		File logoFile = null;
		File logoCacheDir = getLogoFilesCacheDir();
		if(logoCacheDir != null) {
			logoFile = new File(mLogoCacheDir.getAbsolutePath() + "/" + logo.hashCode() + "_" + mm.getWidth() + "_" + mm.getHeight());
			if(DEBUG) {
				if (logoFile.exists()) {
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
					Log.d(TAG, "Logofile: '" + logoFile.getAbsolutePath() + "' already exists, last modified "
							+ dateFormat.format(new Date(logoFile.lastModified())));
				} else {
					Log.d(TAG, "Logofile: '" + logoFile.getAbsolutePath() + "' doesn't exist");
				}
			}
		}

		return logoFile;
	}

	/* Callback Interface */
	interface IpScannerListener {

		void scanStarted();

		void scanProgress(int percent);

		void foundStreamingServices(List<RadioService> ipStreamservices);

		void scanFinished();
	}
}
