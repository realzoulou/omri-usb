package eu.hradio.httprequestwrapper.listener;

@FunctionalInterface
public interface OnSearchResultListener<T>
{
    void onResult(final T p0);
}
