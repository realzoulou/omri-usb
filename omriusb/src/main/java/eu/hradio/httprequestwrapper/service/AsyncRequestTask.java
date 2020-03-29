package eu.hradio.httprequestwrapper.service;

import android.os.*;
import eu.hradio.httprequestwrapper.listener.*;
import eu.hradio.httprequestwrapper.exception.*;

class AsyncRequestTask<V, T> extends AsyncTask<V, Integer, T>
{
    private OnSearchResultListener<T> listener;
    private OnErrorListener errorListener;
    private SearchDelegate<V, T> searchDelegate;
    private Class<T> expectedResultType;
    
    AsyncRequestTask(final OnSearchResultListener<T> listener, final OnErrorListener errorListener, final SearchDelegate<V, T> searchDelegate, final Class<T> expectedResultType) {
        this.listener = listener;
        this.errorListener = errorListener;
        this.searchDelegate = searchDelegate;
        this.expectedResultType = expectedResultType;
    }
    
    protected T doInBackground(final V... params) {
        try {
            final int length = params.length;
            final int n = 0;
            if (n < length) {
                final V query = params[n];
                final T result = this.searchDelegate.delegate(query, this.expectedResultType);
                this.listener.onResult(result);
                return result;
            }
        }
        catch (HRadioSearchClientException e) {
            this.errorListener.onError(e);
        }
        return null;
    }
}
