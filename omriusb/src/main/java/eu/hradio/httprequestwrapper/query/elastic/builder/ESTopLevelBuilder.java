package eu.hradio.httprequestwrapper.query.elastic.builder;

import eu.hradio.httprequestwrapper.query.elastic.*;

public interface ESTopLevelBuilder extends ESQueryBuilder
{
    ESTopLevelBuilder setLocalQuery(final ESQuery p0);
    
    ESTopLevelBuilder appendTopParam(final String p0, final Object p1);
}
