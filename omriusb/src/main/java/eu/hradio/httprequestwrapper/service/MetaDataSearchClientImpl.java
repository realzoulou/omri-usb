package eu.hradio.httprequestwrapper.service;

import eu.hradio.httprequestwrapper.listener.*;
import eu.hradio.httprequestwrapper.query.elastic.*;
import eu.hradio.httprequestwrapper.query.*;
import eu.hradio.httprequestwrapper.dtos.programme.*;
import eu.hradio.httprequestwrapper.exception.*;
import java.util.*;

public class MetaDataSearchClientImpl extends HRadioHttpClientImpl implements MetaDataSearchClient
{
    @Override
    public void asyncProgrammeSearch(final Map<String, String> params, final OnSearchResultListener<ProgrammeList> listener, final OnErrorListener errorListener, final boolean returnAll) {
        ESQuery bodyQuery;
        if (returnAll) {
            bodyQuery = ESQuery.fromPropertyMap(params);
        }
        else {
            bodyQuery = ESQuery.fromProgrammePropertyMap(params);
        }
        final HRadioQuery query = new HRadioQueryImpl();
        query.addEndPoint("programmes");
        query.append("size", "50");
        query.setBody(bodyQuery.toString());
        query.setPort(8080);
        query.setRequestMethod("POST");
        if (returnAll) {
            this.crawlAllProgrammePages(query, listener, errorListener, new ArrayList<ProgrammeList>());
        }
        else {
            this.asyncRequest(query, listener, errorListener, ProgrammeList.class);
        }
    }
    
    @Override
    public void asyncProgrammeSearchForServiceHash(final String hash, final OnSearchResultListener<ProgrammeList> listener, final OnErrorListener errorListener, final boolean returnAll) {
        final HRadioQuery query = new HRadioQueryImpl();
        query.addEndPoint("programmes");
        query.setPort(8080);
        query.append("q", "serviceHash:" + hash);
        query.setRequestMethod("GET");
        if (returnAll) {
            this.crawlAllProgrammePages(query, listener, errorListener, new ArrayList<ProgrammeList>());
        }
        else {
            this.asyncRequest(query, listener, errorListener, ProgrammeList.class);
        }
    }
    
    private void crawlAllProgrammePages(final HRadioQuery query, final OnSearchResultListener<ProgrammeList> listener, final OnErrorListener errorListener, final List<ProgrammeList> pageList) {
        query.append("page", pageList.size() + "");
        this.asyncRequest(query, list -> {
            pageList.add(list);
            if (list.getNumberOfElements() == list.getSize()) {
                this.crawlAllProgrammePages(query, listener, errorListener, pageList);
            }
            else {
                List<RankedStandaloneProgramme> mergedPages = new ArrayList<RankedStandaloneProgramme>();
                Iterator<ProgrammeList> iterator = pageList.iterator();
                ProgrammeList programmeList;
                while (iterator.hasNext()) {
                    programmeList = iterator.next();
                    mergedPages.addAll(Arrays.asList(programmeList.getContent()));
                }
                list.setContent(mergedPages.toArray(new RankedStandaloneProgramme[pageList.size()]));
                listener.onResult(list);
                if (list.getNumberOfElements() == 0) {
                    errorListener.onError(new NoResultFoundException(query.getBody()));
                }
            }
        }, errorListener, ProgrammeList.class);
    }
}
