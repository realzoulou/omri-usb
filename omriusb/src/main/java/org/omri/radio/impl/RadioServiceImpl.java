package org.omri.radio.impl;

import android.util.Log;

import androidx.annotation.NonNull;

import org.omri.radio.Radio;
import org.omri.radioservice.RadioService;
import org.omri.radioservice.RadioServiceAudiodataListener;
import org.omri.radioservice.RadioServiceDab;
import org.omri.radioservice.RadioServiceFollowingListener;
import org.omri.radioservice.RadioServiceListener;
import org.omri.radioservice.RadioServiceMimeType;
import org.omri.radioservice.RadioServiceRawAudiodataListener;
import org.omri.radioservice.metadata.Group;
import org.omri.radioservice.metadata.Location;
import org.omri.radioservice.metadata.ProgrammeServiceMetadataListener;
import org.omri.radioservice.metadata.TermId;
import org.omri.radioservice.metadata.Textual;
import org.omri.radioservice.metadata.TextualMetadataListener;
import org.omri.radioservice.metadata.Visual;
import org.omri.radioservice.metadata.VisualDabSlideShow;
import org.omri.radioservice.metadata.VisualMetadataListener;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

public abstract class RadioServiceImpl implements RadioService, Serializable {

	private static final long serialVersionUID = 1823267026268112744L;

	private final String TAG = "RadioServiceImpl";

	private String mShortDescription = "";
	private String mLongDescription = "";
	private List<Visual> mLogoVisuals = new ArrayList<Visual>();
	private List<TermId> mGenreList = new ArrayList<TermId>();
	private List<String> mLinksList = new ArrayList<String>();
	private List<Location> mLocationList = new ArrayList<Location>();
	private List<String> mKeywordsList = new ArrayList<String>();
	private List<Group> mGroupsList = new ArrayList<Group>();
	private final List<RadioService> mSfServices = Collections.synchronizedList(new ArrayList<>());

	final transient List<VisualMetadataListener> mSlideshowListeners = Collections.synchronizedList(new ArrayList<>());
	final transient List<TextualMetadataListener> mLabelListeners = Collections.synchronizedList(new ArrayList<>());
	final transient List<ProgrammeServiceMetadataListener> mSpiListeners = Collections.synchronizedList(new ArrayList<>());
	final transient List<RadioServiceAudiodataListener> mAudiodataListeners = Collections.synchronizedList(new ArrayList<>());
	final transient List<RadioServiceRawAudiodataListener> mRawAudiodataListeners = Collections.synchronizedList(new ArrayList<>());
	final transient List<RadioServiceFollowingListener> mSfListeners = Collections.synchronizedList(new ArrayList<>());

	boolean mDecodeAudio = false;

	private int mAscty = -1;
	private boolean mSbrUsed = false;
	private boolean mPsUsed = false;

	private int mChannelConfig = 0;
	private int mSamplingRate = 0;

	private int mConfigChans = 0;
	private int mConfigSampling = 0;

	private String mHradioSearchSource = "";

	void setHradioSearchSource(String source) {
		if(source != null) {
			mHradioSearchSource = source;
		}
	}

	public String getHradioSearchSource() {
		return mHradioSearchSource;
	}

	//Serialization
	private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
		stream.writeObject(mShortDescription);
		stream.writeObject(mLongDescription);
		stream.writeObject(mGenreList);
		stream.writeObject(mLinksList);
		stream.writeObject(mLocationList);
		stream.writeObject(mKeywordsList);
		stream.writeObject(mGroupsList);
	}

	private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
		mShortDescription = (String)stream.readObject();
		mLongDescription  = (String)stream.readObject();
		mLogoVisuals = new ArrayList<>();

		mGenreList = (ArrayList<TermId>)stream.readObject();
		mLinksList = (ArrayList<String>)stream.readObject();
		mLocationList = (ArrayList<Location>)stream.readObject();
		mKeywordsList = (ArrayList<String>)stream.readObject();
		mGroupsList = (ArrayList<Group>)stream.readObject();

		mSlideshowListeners.clear();
		mLabelListeners.clear();
		mSpiListeners.clear();
		mAudiodataListeners.clear();
		mRawAudiodataListeners.clear();

		mDecodeAudio = false;

		mAscty = -1;
		mSbrUsed = false;
		mPsUsed = false;

		mChannelConfig = 0;
		mSamplingRate = 0;

		mConfigChans = 0;
		mConfigSampling = 0;
	}

	@Override
	public String getShortDescription() {
		return mShortDescription;
	}

	void setShortDescription(String shortDesc) {
		this.mShortDescription = shortDesc;
	}

	@Override
	public String getLongDescription() {
		return mLongDescription;
	}

	void setLongDescription(String longDesc) {
		this.mLongDescription = longDesc;
	}
	
	@Override
	public List<Visual> getLogos() {
		//return mLogoVisuals;
		return VisualLogoManager.getInstance().getLogoVisuals(this);
	}

	public void addLogo(Visual logoVisual) {
		this.mLogoVisuals.add(logoVisual);
	}

	void addLogo(List<Visual> logoVisual) {
		this.mLogoVisuals.addAll(logoVisual);
	}

	@Override
	public List<TermId> getGenres() {
		return mGenreList;
	}

	void addGenre(TermId genreId) {
		this.mGenreList.add(genreId);
	}

	void addGenre(List<TermId> genreId) {
		this.mGenreList.addAll(genreId);
	}

	@Override
	public List<String> getLinks() {
		return mLinksList;
	}

	void addLink(String linkString) {
		this.mLinksList.add(linkString);
	}

	void addLink(List<String> linkString) {
		this.mLinksList.addAll(linkString);
	}

	@Override
	public List<Location> getLocations() {
		return mLocationList;
	}

	public void addLocation(Location location) {
		this.mLocationList.add(location);
	}

	void addLocation(List<Location> location) {
		this.mLocationList.addAll(location);
	}

	@Override
	public List<String> getKeywords() {
		return mKeywordsList;
	}

	void addKeyword(String keyWord) {
		this.mKeywordsList.add(keyWord);
	}

	void addKeyword(List<String> keyWord) {
		this.mKeywordsList.addAll(keyWord);
	}

	@Override
	public List<Group> getMemberships() {
		return mGroupsList;
	}

	public void addMembership(Group group) {
		this.mGroupsList.add(group);
	}

	public void addMembership(List<Group> group) {
		this.mGroupsList.addAll(group);
	}

	@Override
	public void subscribe(RadioServiceListener radioServiceListener) {
		if(radioServiceListener != null) {
			if(radioServiceListener instanceof TextualMetadataListener) {
				synchronized (mLabelListeners) {
					if (!mLabelListeners.contains(radioServiceListener)) {
						if (DEBUG)
							Log.d(TAG, "Subscribing TextualMetadataListener: " + radioServiceListener);
						this.mLabelListeners.add((TextualMetadataListener) radioServiceListener);
					}
				}
			}
			if(radioServiceListener instanceof VisualMetadataListener) {
				synchronized (mSlideshowListeners) {
					if (!mSlideshowListeners.contains(radioServiceListener)) {
						if (DEBUG)
							Log.d(TAG, "Subscribing VisualMetadataListener: " + radioServiceListener);
						this.mSlideshowListeners.add((VisualMetadataListener) radioServiceListener);
					}
				}
			}
			if(radioServiceListener instanceof RadioServiceAudiodataListener) {
				synchronized (mAudiodataListeners) {
					if (!mAudiodataListeners.contains(radioServiceListener)) {
						if (DEBUG)
							Log.d(TAG, "Subscribing RadioServiceAudiodataListener: " + radioServiceListener);
						mDecodeAudio = true;
						this.mAudiodataListeners.add((RadioServiceAudiodataListener) radioServiceListener);
					}
				}
			}
			if(radioServiceListener instanceof RadioServiceRawAudiodataListener) {
				synchronized (mRawAudiodataListeners) {
					if (!mRawAudiodataListeners.contains(radioServiceListener)) {
						if (DEBUG)
							Log.d(TAG, "Subscribing RadioServiceRawAudiodataListener: " + radioServiceListener);
						this.mRawAudiodataListeners.add((RadioServiceRawAudiodataListener) radioServiceListener);
					}
				}
			}
			if(radioServiceListener instanceof ProgrammeServiceMetadataListener) {
				synchronized (mSpiListeners) {
					if (!mSpiListeners.contains(radioServiceListener)) {
						if (DEBUG)
							Log.d(TAG, "Subscribing ProgrammeServiceMetadataListener: " + radioServiceListener);
						this.mSpiListeners.add((ProgrammeServiceMetadataListener) radioServiceListener);
					}
				}
			}
			if (radioServiceListener instanceof RadioServiceFollowingListener) {
				synchronized (mSfListeners) {
					if (!mSfListeners.contains(radioServiceListener)) {
						if (DEBUG)
							Log.d(TAG, "Subscribing RadioServiceFollowingListener: " + radioServiceListener);
						this.mSfListeners.add((RadioServiceFollowingListener) radioServiceListener);
					}
				}
			}
		} else {
			if(DEBUG)Log.d(TAG, "Subscribing RadioServiceListener is null");
		}
	}

	@Override
	public void unsubscribe(RadioServiceListener radioServiceListener) {
		if(radioServiceListener != null) {
			if(radioServiceListener instanceof TextualMetadataListener) {
				synchronized (this.mLabelListeners) {
					if (this.mLabelListeners.contains(radioServiceListener)) {
						if (DEBUG)
							Log.d(TAG, "UnSubscribing TextualMetadataListener: " + radioServiceListener);
						this.mLabelListeners.remove(radioServiceListener);
					}
				}
			}
			if(radioServiceListener instanceof VisualMetadataListener) {
				synchronized (this.mSlideshowListeners) {
					if (this.mSlideshowListeners.contains(radioServiceListener)) {
						if (DEBUG)
							Log.d(TAG, "UnSubscribing VisualMetadataListener: " + radioServiceListener);
						this.mSlideshowListeners.remove(radioServiceListener);
					}
				}
			}
			if(radioServiceListener instanceof RadioServiceAudiodataListener) {
				synchronized (this.mAudiodataListeners) {
					if (this.mAudiodataListeners.contains(radioServiceListener)) {
						if (DEBUG)
							Log.d(TAG, "UnSubscribing RadioServiceAudiodataListener: " + radioServiceListener);
						this.mAudiodataListeners.remove(radioServiceListener);
					}
					if (mAudiodataListeners.isEmpty()) {
						mDecodeAudio = false;
					}
				}
			}
			if(radioServiceListener instanceof RadioServiceRawAudiodataListener) {
				synchronized (this.mRawAudiodataListeners) {
					if (this.mRawAudiodataListeners.contains(radioServiceListener)) {
						if (DEBUG)
							Log.d(TAG, "UnSubscribing RadioServiceRawAudiodataListener: " + radioServiceListener);
						this.mRawAudiodataListeners.remove(radioServiceListener);
					}
				}
			}
			if(radioServiceListener instanceof ProgrammeServiceMetadataListener) {
				synchronized (this.mSpiListeners) {
					if (this.mSpiListeners.contains(radioServiceListener)) {
						if (DEBUG)
							Log.d(TAG, "UnSubscribing ProgrammeServiceMetadataListener: " + radioServiceListener);
						this.mSpiListeners.remove(radioServiceListener);
					}
				}
			}
			if (radioServiceListener instanceof RadioServiceFollowingListener) {
				synchronized (this.mSfListeners) {
					if (this.mSfListeners.contains(radioServiceListener)) {
						if (DEBUG)
							Log.d(TAG, "UnSubscribing RadioServiceFollowingListener: " + radioServiceListener);
						this.mSfListeners.remove(radioServiceListener);
					}
				}
			}
		} else {
			if(DEBUG)Log.d(TAG, "UnSubscribing RadioServiceListener is null");
		}
	}

	@Override
	public ArrayList<RadioService> getFollowingServices() {
		ArrayList<RadioService> ret = new ArrayList<>(mSfServices.size());
		synchronized (mSfServices) {
			ret.addAll(mSfServices);
		}
		return ret;
	}

	//callbacks from the tuner
	void slideshowReceived(VisualDabSlideShow slideShow) {
		for(VisualMetadataListener slsListener : mSlideshowListeners) {
			slsListener.newVisualMetadata(slideShow);
		}
	}

	void labelReceived(Textual label) {
		for(TextualMetadataListener dlsListener : mLabelListeners) {
			dlsListener.newTextualMetadata(label);
		}
	}

	void spiReceived(String spiPath) {
		if(DEBUG)Log.d(TAG, "RadioDns Spi Path: " + spiPath);

		SpiProgrammeInformationImpl spiInfo = new SpiProgrammeInformationImpl(spiPath);

		if(DEBUG)Log.d(TAG, "RadioDns DocsSize: " + spiInfo.getSpiDocument().getElementsByTagName("schedule").getLength());
		for(ProgrammeServiceMetadataListener metaListener : mSpiListeners) {
			metaListener.newProgrammeInformation(spiInfo);
		}
	}

	void audioData(final byte[] pcmData, final int channelCount, final int samplingRate) {
		if(mDecodeAudio && mAudioDec != null) {
			mAudioDec.feedData(pcmData);
		}

		synchronized (mRawAudiodataListeners) {
			for (RadioServiceRawAudiodataListener rawListener : mRawAudiodataListeners) {
				rawListener.rawAudioData(pcmData, mSbrUsed, mPsUsed, mMimeType, channelCount, samplingRate);
			}
		}
	}

	@NonNull ArrayList<RadioService> replaceLinkedRadioServicesWithKnown(@NonNull ArrayList<RadioService> linkedServices) {
		ArrayList<RadioService> retLinkedServices = new ArrayList<>();

		// retrieve list of known services
		final List<RadioService> radioServices = Radio.getInstance().getRadioServices();
		for (final RadioService linkedService : linkedServices) {
			boolean foundRadioServiceInCurrentList = false;
			for (final RadioService radioService : radioServices) {
				if (radioService instanceof RadioServiceDab && linkedService instanceof RadioServiceDab) {
					// if linked service is equal in ECC, EId, SId compared to a known service,
					// then take the known service, otherwise the new linked DAB service
					final RadioServiceDab radioServiceDab = (RadioServiceDab) radioService;
					final RadioServiceDab linkedServiceDab = (RadioServiceDab) linkedService;
					// strict check of ECC, SId, EId, Frequency
					if (radioServiceDab.equals(linkedServiceDab)) {
						// add the already known RadioServiceDab at the front
						retLinkedServices.add(0, radioServiceDab);
						foundRadioServiceInCurrentList = true;
						break;
					}
				}
			}
			if (!foundRadioServiceInCurrentList) {
				// not found in current service list, add the new service
				retLinkedServices.add(linkedService);
			}
		}
		return retLinkedServices;
	}

	void setFollowingServices(@NonNull ArrayList<RadioService> sfServices) {
		synchronized (mSfServices) {
			mSfServices.clear();
			mSfServices.addAll(sfServices);
		}
	}

	void serviceFollowingReceived(ArrayList<RadioService> sfServices) {
		if (sfServices != null) {
			if(DEBUG) Log.d(TAG, "serviceFollowingReceived " + getServiceLabel() + " sz=" + sfServices.size());
			ArrayList<RadioService> sfRadioServices = new ArrayList<>(sfServices.size());
			sfRadioServices.addAll(replaceLinkedRadioServicesWithKnown(sfServices));

			ArrayList<RadioService> prevList = getFollowingServices();
			if (!prevList.equals(sfRadioServices)) {
				// only real changes, no repetition of the same information
				setFollowingServices(sfRadioServices);
		synchronized (mSfListeners) {
			for (RadioServiceFollowingListener sfListener : mSfListeners) {
				sfListener.newServiceFollowingRadioServices(sfRadioServices);
			}
		}
				RadioServiceManager.getInstance().scheduleSaveServices(getRadioServiceType());
			}
		}
	}

	private transient DabAudioDecoder mAudioDec = null;
	private RadioServiceMimeType mMimeType = RadioServiceMimeType.UNKNOWN;
	void audioFormatChanged(final int ascty, final int channelCount, final int samplingRate, final boolean sbrUsed, final boolean psUsed) {
		if(DEBUG)Log.d(TAG, "audioFormatChanged: ASCTY:" + ascty +", SBR: " + sbrUsed + ", PS: " + psUsed);
		mMimeType = RadioServiceMimeType.UNKNOWN;
		mAscty = ascty;
		mSbrUsed = sbrUsed;
		mPsUsed = psUsed;
		mConfigChans = channelCount;
		mConfigSampling = samplingRate;

		if(mAscty == 0) {
			mMimeType = RadioServiceMimeType.AUDIO_MPEG;
		}
		if(mAscty == 63) {
			mMimeType = RadioServiceMimeType.AUDIO_AAC_DAB_AU;
		}

		if(mAudioDec == null) {
			mAudioDec = DabAudioDecoderFactory.getInstance().getDecoder(ascty, samplingRate, channelCount, sbrUsed, psUsed);
		} else {
			if(mAudioDec.getConfCodec() != ascty ||
			    mAudioDec.getConfSampling() != samplingRate ||
			    mAudioDec.getConfChans() != channelCount ||
				mAudioDec.getConfSbr() != sbrUsed ||
				mAudioDec.getConfPs() != psUsed) {
				if(DEBUG)Log.d(TAG, "Reconfiguring codec");

				mAudioDec.stopCodec();

				mAudioDec = DabAudioDecoderFactory.getInstance().getDecoder(ascty, samplingRate, channelCount, sbrUsed, psUsed);
			}
		}

		if(mAudioDec != null) {
			mAudioDec.setCodecCallback(new DabAudioDecoder.DabDecoderCallback() {
				@Override
				public void decodedAudioData(byte[] pcmData, final int samplerate, final int channels) {
					for (final RadioServiceAudiodataListener audiolistener : mAudiodataListeners) {
						audiolistener.pcmAudioData(pcmData, channels, samplingRate);
					}
				}

				@Override
				public void outputFormatChanged(int sampleRate, int chanCnt) {
					Log.d(TAG, "outputFormatChanged: " + sampleRate + " : " + chanCnt);
					mSamplingRate = sampleRate;
					mChannelConfig = chanCnt;
				}
			});
		} else {
			Radio.getInstance().stopRadioService(this);
		}
	}

	void serviceStarted() {
		if(DEBUG)Log.d(TAG, "Service '" + getServiceLabel() + "' started");
	}

	void serviceStopped() {
		if(DEBUG)Log.d(TAG, "Service '" + getServiceLabel() + "' stopped");

		if(mAudioDec != null) {
			if(DEBUG)Log.d(TAG, "Stopping DabAudioDecoder...");
			mAudioDec.stopCodec();
		}

		mAudioDec = null;
	}

	/* PCM data from a Shoutcast IP service */
	void decodedAudioData(final byte[] pcmAudioData, final int channelCount, final int samplingRate) {
		for (final RadioServiceAudiodataListener audiolistener : mAudiodataListeners) {
			audiolistener.pcmAudioData(pcmAudioData, channelCount, samplingRate);
		}
	}

}
