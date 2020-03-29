package eu.hradio.httprequestwrapper.service;

import eu.hradio.httprequestwrapper.exception.*;

@FunctionalInterface
interface SearchDelegate<V, T>
{
    T delegate(final V p0, final Class<T> p1) throws HRadioSearchClientException;
}
