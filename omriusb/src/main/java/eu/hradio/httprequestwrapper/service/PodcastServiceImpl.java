package eu.hradio.httprequestwrapper.service;

import eu.hradio.httprequestwrapper.dtos.podcast.*;
import eu.hradio.httprequestwrapper.listener.*;
import eu.hradio.httprequestwrapper.dtos.programme.*;
import android.os.*;
import eu.hradio.httprequestwrapper.parser.*;
import eu.hradio.httprequestwrapper.exception.*;

public class PodcastServiceImpl implements PodcastService
{
    @Override
    public void parsePodcasts(final String link, final OnSearchResultListener<Podcast> podcastListener, final OnErrorListener errorListener, final int limit) {
        new PodcastDownloadTask(podcastListener, errorListener, limit).execute(new String[] { link });
    }
    
    @Override
    public void parsePodcasts(final StandaloneProgramme programme, final OnSearchResultListener<Podcast> podcastListener, final OnErrorListener errorListener) {
        for (final WebContent link : programme.getWebContents()) {
            if (link.getMimeType().equals("application/rss+xml")) {
                this.parsePodcasts(link.getUrl(), podcastListener, errorListener, 20);
            }
        }
    }
    
    @Override
    public void parsePodcasts(final String link, final OnSearchResultListener<Podcast> podcastListener, final OnErrorListener errorListener) {
        this.parsePodcasts(link, podcastListener, errorListener, 20);
    }
    
    static class PodcastDownloadTask extends AsyncTask<String, Integer, Podcast>
    {
        private OnSearchResultListener<Podcast> podcastListener;
        private OnErrorListener errorListener;
        private int limit;
        
        PodcastDownloadTask(final OnSearchResultListener<Podcast> podcastListener, final OnErrorListener errorListener, final int limit) {
            this.podcastListener = podcastListener;
            this.errorListener = errorListener;
            this.limit = limit;
        }
        
        protected Podcast doInBackground(final String... strings) {
            final int length = strings.length;
            int i = 0;
            while (i < length) {
                final String url = strings[i];
                try {
                    return new XMLParser().parsePodcast(url, this.limit);
                }
                catch (NetworkException e) {
                    this.errorListener.onError(e);
                    ++i;
                }
            }
            return null;
        }
        
        protected void onPostExecute(final Podcast podcast) {
            super.onPostExecute(podcast);
            this.podcastListener.onResult(podcast);
        }
    }
}
