package com.xaridar.notable.events;

import androidx.annotation.NonNull;

public interface QueryListener<T> {
    void onQueryResult(@NonNull T result);
    void onQueryError(@NonNull String error);
}
