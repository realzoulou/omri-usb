package eu.hradio.httprequestwrapper.service;

import java.util.*;
import eu.hradio.httprequestwrapper.dtos.programme.*;
import eu.hradio.httprequestwrapper.listener.*;

public interface MetaDataSearchClient extends HRadioHttpClient
{
    void asyncProgrammeSearch(final Map<String, String> p0, final OnSearchResultListener<ProgrammeList> p1, final OnErrorListener p2, final boolean p3);
    
    void asyncProgrammeSearchForServiceHash(final String p0, final OnSearchResultListener<ProgrammeList> p1, final OnErrorListener p2, final boolean p3);
}
