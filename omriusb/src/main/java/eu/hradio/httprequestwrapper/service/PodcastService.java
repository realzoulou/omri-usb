package eu.hradio.httprequestwrapper.service;

import eu.hradio.httprequestwrapper.dtos.programme.*;
import eu.hradio.httprequestwrapper.dtos.podcast.*;
import eu.hradio.httprequestwrapper.listener.*;

public interface PodcastService
{
    void parsePodcasts(final StandaloneProgramme p0, final OnSearchResultListener<Podcast> p1, final OnErrorListener p2);
    
    void parsePodcasts(final String p0, final OnSearchResultListener<Podcast> p1, final OnErrorListener p2);
    
    void parsePodcasts(final String p0, final OnSearchResultListener<Podcast> p1, final OnErrorListener p2, final int p3);
}
