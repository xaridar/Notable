package com.xaridar.notable.app;

import java.util.function.Function;

public interface SettingsFragment {
    default void scheduleFinish(Function<Void, Void> whenCanFinish) {
        whenCanFinish.apply(null);
    }
}
