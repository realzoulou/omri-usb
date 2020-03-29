package eu.hradio.core.radiodns;

import org.omri.radioservice.*;
import java.util.*;

public interface RadioDnsCoreLookupCallback extends RadioDnsCallback
{
    void coreLookupFinished(final RadioService p0, final List<RadioDnsService> p1);
}
