package com.tomaflow.app.utils;

import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.ListenerRegistration;

/**
 * LiveData base that ties a Firestore {@code SnapshotListener} to the LiveData
 * lifecycle: the listener is registered in {@link #onActive()} (first observer)
 * and removed in {@link #onInactive()} (no observers). Subclasses implement
 * {@link #listen()} to attach the actual listener and return its registration so
 * it can be torn down automatically — preventing the listener leaks that arise
 * from calling {@code addSnapshotListener} directly and never capturing/removing
 * the {@link ListenerRegistration}.
 *
 * <p>Note: a LiveData kept alive with {@code observeForever} (e.g. by a
 * long-lived singleton) will keep its Firestore listener permanently active —
 * the caller is responsible for {@code removeObserver} to release it.</p>
 */
public abstract class FirestoreLiveData<T> extends LiveData<T> {

    private ListenerRegistration registration;

    @Override
    protected void onActive() {
        super.onActive();
        if (registration == null) {
            registration = listen();
        }
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }

    /** Attach the snapshot listener and return its registration for later removal. */
    protected abstract ListenerRegistration listen();
}
