package eu.hradio.httprequestwrapper.service;

import eu.hradio.httprequestwrapper.dtos.user_report.*;
import eu.hradio.httprequestwrapper.listener.*;
import eu.hradio.httprequestwrapper.parser.*;
import eu.hradio.httprequestwrapper.exception.*;
import eu.hradio.httprequestwrapper.query.*;

public class UserReportClientImpl extends HRadioHttpClientImpl implements UserReportClient
{
    @Override
    public void asyncUserReportRequest(final UserReport userReport, final OnSearchResultListener<UserReportResult> listener, final OnErrorListener errorListener) {
        final HRadioQuery userReportQuery = new HRadioQueryImpl();
        userReportQuery.setPort(8110);
        userReportQuery.addEndPoint("user_reports");
        userReportQuery.setRequestMethod("POST");
        final JsonParser parser = new JsonParser();
        try {
            final String userReportBody = parser.toJSON(userReport).toString();
            userReportQuery.setBody(userReportBody);
            this.asyncRequest(userReportQuery, listener, errorListener, UserReportResult.class);
        }
        catch (JsonEncoderTypeMismatch jsonEncoderTypeMismatch) {
            errorListener.onError(jsonEncoderTypeMismatch);
        }
    }
}
