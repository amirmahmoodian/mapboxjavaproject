package com.example.javamapbox;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.List;

import kotlin.Metadata;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;

final class LocationPermissionHelper {
    private PermissionsManager permissionsManager;
    @NotNull
    private final WeakReference activity;

    public final void checkPermissions(@NotNull final Function0 onMapReady) {
        Intrinsics.checkNotNullParameter(onMapReady, "onMapReady");
        if (PermissionsManager.areLocationPermissionsGranted((Context)this.activity.get())) {
            onMapReady.invoke();
        } else {
            this.permissionsManager = new PermissionsManager((PermissionsListener)(new PermissionsListener() {
                public void onExplanationNeeded(@NotNull List permissionsToExplain) {
                    Intrinsics.checkNotNullParameter(permissionsToExplain, "permissionsToExplain");
                    Toast.makeText((Context)LocationPermissionHelper.this.getActivity().get(),
                            (CharSequence)"You need to accept location permissions.",
                            Toast.LENGTH_SHORT).show();
                }

                public void onPermissionResult(boolean granted) {
                    if (granted) {
                        onMapReady.invoke();
                    } else {
                        Activity var10000 = (Activity)LocationPermissionHelper.this.getActivity().get();
                        if (var10000 != null) {
                            var10000.finish();
                        }
                    }

                }
            }));
            PermissionsManager var10000 = this.permissionsManager;
            if (var10000 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("permissionsManager");
            }

            var10000.requestLocationPermissions((Activity)this.activity.get());
        }

    }

    public final void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        Intrinsics.checkNotNullParameter(permissions, "permissions");
        Intrinsics.checkNotNullParameter(grantResults, "grantResults");
        PermissionsManager var10000 = this.permissionsManager;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("permissionsManager");
        }

        var10000.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @NotNull
    public final WeakReference getActivity() {
        return this.activity;
    }

    public LocationPermissionHelper(@NotNull WeakReference activity) {
        super();
        Intrinsics.checkNotNullParameter(activity, "activity");
        this.activity = activity;
    }
}
