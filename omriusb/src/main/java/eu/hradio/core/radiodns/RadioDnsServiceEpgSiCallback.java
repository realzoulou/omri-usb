package eu.hradio.core.radiodns;

public interface RadioDnsServiceEpgSiCallback extends RadioDnsCallback
{
    void serviceInformationRetrieved(final RadioEpgServiceInformation p0, final RadioDnsServiceEpg p1);
}
