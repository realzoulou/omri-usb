package eu.hradio.httprequestwrapper.service;

import eu.hradio.httprequestwrapper.dtos.user_report.*;
import eu.hradio.httprequestwrapper.listener.*;

public interface UserReportClient extends HRadioHttpClient
{
    void asyncUserReportRequest(final UserReport p0, final OnSearchResultListener<UserReportResult> p1, final OnErrorListener p2);
}
