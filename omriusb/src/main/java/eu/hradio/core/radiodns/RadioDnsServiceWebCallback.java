package eu.hradio.core.radiodns;

import org.omri.radioservice.*;

public interface RadioDnsServiceWebCallback extends RadioDnsCallback
{
    void applicationInformationListRetrieved(final RadioWebApplicationInformationList p0, final RadioService p1);
}
