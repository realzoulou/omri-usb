package eu.hradio.core.radiodns;

public interface RadioDnsServiceEpgPiCallback extends RadioDnsCallback
{
    void programmeInformationRetrieved(final RadioEpgProgrammeInformation p0, final RadioDnsServiceEpg p1);
}
