// MainActivity.java
package com.example.javamapbox;



import com.mapbox.maps.extension.style.layers.LayerUtils;
import com.mapbox.maps.extension.style.sources.SourceUtils;
import com.mapbox.maps.extension.style.sources.generated.*;
import com.mapbox.maps.extension.style.layers.generated.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.javamapbox.databinding.ActivityMainBinding;
import com.mapbox.bindgen.Value;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.style.layers.generated.LineLayer;
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap;
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin;
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource;
import com.mapbox.maps.plugin.animation.CameraAnimationsPlugin;
import com.mapbox.maps.plugin.animation.CameraAnimationsUtils;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.animation.MapAnimationOptions.Builder;
import com.mapbox.maps.plugin.delegates.MapPluginProviderDelegate;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils;
import com.mapbox.maps.plugin.locationcomponent.LocationProvider;
import com.mapbox.maps.viewannotation.ViewAnnotationManager;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.MapboxNavigationProvider;
import com.mapbox.navigation.core.trip.session.LocationMatcherResult;
import com.mapbox.navigation.core.trip.session.LocationObserver;
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import kotlin.Metadata;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref.ObjectRef;
import kotlin.text.Charsets;
import retrofit2.http.Url;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MainActivity extends AppCompatActivity implements SensorEventListener {
    double plong;
    double plat;
    private double distance=0.0;
    public MapView mapView = null;
    public Sensor stepSensor;
    private SensorManager sensorManager;
    private boolean running;
    private float totalSteps;
    private float previousTotalSteps;
    private final String filename = "SampleFile.geojason";
    private final String filepath = "MyFileStorage";
    private double myprevlinelong;
    private double myprevlinelat;
    private Integer sourcecounter=0;
    @Nullable
    private File myExternalFile;
    private double dis;
    @NotNull
    public String myline = "{\n  \"type\": \"FeatureCollection\",\n  \"features\": [\n    {\n      \"type\": \"Feature\",\n      \"properties\": {\n        \"name\": \"Crema to Council Crest\"\n      },\n      \"geometry\": {\n        \"type\": \"LineString\",\n        \"coordinates\": [";
    private boolean firstpoint = true;
    public boolean flag;
    public LocationPermissionHelper locationPermissionHelper;
    public ActivityMainBinding binding;
    private ViewAnnotationManager viewAnnotationManager;
//    public final WaypointsSet addedWaypoints = new WaypointsSet();
    public MapboxMap mapboxMap;
    public final NavigationLocationProvider navigationLocationProvider = new NavigationLocationProvider();
    public MapboxNavigation mapboxNavigation;

    LocationObserver locationObserver = new LocationObserver() {
        boolean firstLocationUpdateReceived;

        public void onNewRawLocation(@NotNull Location rawLocation) {
            Intrinsics.checkNotNullParameter(rawLocation, "rawLocation");
        }

        public void onNewLocationMatcherResult(@NotNull LocationMatcherResult locationMatcherResult) {
            Intrinsics.checkNotNullParameter(locationMatcherResult, "locationMatcherResult");
            Location enhancedLocation = locationMatcherResult.getEnhancedLocation();
            navigationLocationProvider.changePosition(
                    enhancedLocation,
                    locationMatcherResult.getKeyPoints(),  (kotlin.jvm.functions.Function1<? super android.animation.ValueAnimator, Unit>)null, (kotlin.jvm.functions.Function1<? super android.animation.ValueAnimator, Unit>)null);
            if (flag) {
                if (firstpoint) {
                    myline += "[" +
                            (Math.round((enhancedLocation.getLongitude() * 100000.0)) / 100000.0) +
                            "," + " " +
                            (Math.round((enhancedLocation.getLatitude() * 100000.0)) / 100000.0) + "]    ";
                    plong = enhancedLocation.getLongitude();
                    plat = enhancedLocation.getLatitude();
                    firstpoint = false;
                } else {
                    if (myprevlinelong == enhancedLocation.getLongitude() && myprevlinelat ==
                            enhancedLocation.getLatitude()
                    ) {
                        System.out.println("user just stayed in one palce for now ;)");
                    } else {
                        if (myline.substring(myline.length() - 5).equals("]}}]}")) {
                            myline = myline.substring(0, myline.length() - 6);
                        }
                        myline += ",[" + (Math.round((enhancedLocation.getLongitude() * 100000.0)) / 100000.0) +
                                "," + " " +
                                (Math.round((enhancedLocation.getLatitude() * 100000.0)) / 100000.0) + "]  ";

                        myprevlinelong = plong;
                        myprevlinelat = plat;
                        plong = enhancedLocation.getLongitude();
                        plat = enhancedLocation.getLatitude();

                        provide();
                        liner();
                        System.out.println(Point.fromLngLat(myprevlinelong, myprevlinelat)+" %%% "+
                                Point.fromLngLat(enhancedLocation.getLongitude(),
                                        enhancedLocation.getLatitude()));
                        dis += TurfMeasurement.distance(
                                Point.fromLngLat(myprevlinelong, myprevlinelat),
                                Point.fromLngLat(enhancedLocation.getLongitude(), enhancedLocation.getLatitude()),
                                TurfConstants.UNIT_KILOMETERS
                        );

                        distance = (Math.round((dis * 100.0)) / 100.0);
                        System.out.println(distance+" ** "+dis);
                        distancecounter();
                    }
                }
            }
            if (!this.firstLocationUpdateReceived) {
                this.firstLocationUpdateReceived = true;
                this.moveCameraTo(enhancedLocation);
            }
        }
        public final boolean getFirstLocationUpdateReceived() {
            return this.firstLocationUpdateReceived;
        }

        public final void setFirstLocationUpdateReceived(boolean var1) {
            this.firstLocationUpdateReceived = var1;
        }

        private final void moveCameraTo(Location location) {
            MapAnimationOptions mapAnimationOptions = (new Builder()).duration(0L).build();
            MapView var10000 = MainActivity.access$getBinding$p(MainActivity.this).mapView;
            Intrinsics.checkNotNullExpressionValue(var10000, "binding.mapView");
            CameraAnimationsPlugin var3 = CameraAnimationsUtils.getCamera((MapPluginProviderDelegate) var10000);
            CameraOptions var10001 = (new com.mapbox.maps.CameraOptions.Builder()).center(Point.fromLngLat(location.getLongitude(), location.getLatitude())).zoom(16.0D).build();
            Intrinsics.checkNotNullExpressionValue(var10001, "CameraOptions.Builder()\n…                 .build()");
            var3.easeTo(var10001, mapAnimationOptions);
        }
    };

    @NotNull
    public final Sensor getStepSensor() {
        Sensor var10000 = this.stepSensor;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("stepSensor");
        }
        return var10000;
    }

    public final void setStepSensor(@NotNull Sensor var1) {
        Intrinsics.checkNotNullParameter(var1, "<set-?>");
        this.stepSensor = var1;
    }

    public final double getMyprevlinelong() {
        return this.myprevlinelong;
    }

    public final void setMyprevlinelong(double var1) {
        this.myprevlinelong = var1;
    }

    public final double getMyprevlinelat() {
        return this.myprevlinelat;
    }

    public final void setMyprevlinelat(double var1) {
        this.myprevlinelat = var1;
    }

    @Nullable
    public final File getMyExternalFile() {
        return this.myExternalFile;
    }

    public final void setMyExternalFile(@Nullable File var1) {
        this.myExternalFile = var1;
    }

    @NotNull
    public final String getMyline() {
        return this.myline;
    }

    public final void setMyline(@NotNull String var1) {
        Intrinsics.checkNotNullParameter(var1, "<set-?>");
        this.myline = var1;
    }

    public final boolean getFirstpoint() {
        return this.firstpoint;
    }

    public final void setFirstpoint(boolean var1) {
        this.firstpoint = var1;
    }

    public final boolean getFlag() {
        return this.flag;
    }

    public final void setFlag(boolean var1) {
        this.flag = var1;
    }

    @SuppressLint("MissingPermission")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding var10001 = ActivityMainBinding.inflate(this.getLayoutInflater());
        Intrinsics.checkNotNullExpressionValue(var10001, "ActivityMainBinding.inflate(layoutInflater)");
        this.binding = var10001;
        var10001 = this.binding;
        if (var10001 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("binding");
        }

        this.setContentView((View) var10001.getRoot());
        this.loadData();
        this.resetSteps();
        Object var9 = this.getSystemService(Context.SENSOR_SERVICE);
        if (var9 == null) {
            throw new NullPointerException("null cannot be cast to non-null type android.hardware.SensorManager");
        } else {
            this.sensorManager = (SensorManager) var9;
            var10001 = this.binding;
            if (var10001 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("binding");
            }

            this.viewAnnotationManager = var10001.mapView.getViewAnnotationManager();
            var10001 = this.binding;
            if (var10001 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("binding");
            }

            mapboxMap = var10001.mapView.getMapboxMap();

            ActivityMainBinding var10000 = this.binding;
            if (var10000 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("binding");
            }

            MapView var6 = var10000.mapView;
            Intrinsics.checkNotNullExpressionValue(var6, "binding.mapView");
            LocationComponentPlugin var2 = LocationComponentUtils.getLocationComponent((MapPluginProviderDelegate) var6);
            var2.setLocationProvider((LocationProvider) this.navigationLocationProvider);
            var2.setEnabled(true);
            Context var10003 = this.getApplicationContext();
            Intrinsics.checkNotNullExpressionValue(var10003, "this.applicationContext");
            this.mapboxNavigation = MapboxNavigationProvider.create(
                    (new com.mapbox.navigation.base.options.NavigationOptions.Builder(var10003))
                            .accessToken(this.getString(R.string.mapbox_access_token)).build());


            this.locationPermissionHelper = new LocationPermissionHelper(new WeakReference<>(this));

            this.locationPermissionHelper.checkPermissions((Function0)(new Function0() {
                // $FF: synthetic method
                // $FF: bridge method

                public final Object invoke() {
                    MainActivity.access$getMapboxNavigation$p(MainActivity.this).startTripSession(false);
//                    this.invoke();
                    return Unit.INSTANCE;
                }
            }));
            mapboxMap.loadStyleUri(
                    Style.DARK);

            binding.start.setVisibility(View.VISIBLE);

            binding.start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    flag = true;
                    binding.start.setVisibility(View.INVISIBLE);
                    binding.stop.setVisibility(View.VISIBLE);
                    binding.stop.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            flag = false;
                            binding.stop.setVisibility(View.INVISIBLE);
                            binding.window.setVisibility(View.VISIBLE);
                            binding.multipleWaypointResetRouteButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            binding.window.setVisibility(View.INVISIBLE);
                                            binding.start.setVisibility(View.VISIBLE);
                                        }
                            });
                        }
                    });
                }
            });
        }
    }
    private final void liner() {

        mapboxMap.getStyle(style -> {

            String s1 = "trace-source" + sourcecounter.toString();

            GeoJsonSource traceSource = new GeoJsonSource.Builder(s1)
                    .url("file:///data/user/0/com.example.javamapbox/files/xxyyxx.geojson")
                    .build();

            SourceUtils.addSource(style, traceSource);

            String s2 = "trace-layer" + sourcecounter.toString();

            LineLayer traceLayer = new LineLayer(s2, s1)
                    .lineWidth(6.f)
                    .lineColor(Color.parseColor("#31661d"))
                    .lineOpacity(0.5f);

            LayerUtils.addLayer(style, traceLayer);
            sourcecounter++;

        });
    }
    public final void provide() {
        String var10001 = this.myline;
        this.myline = var10001 + "]}}]}";
        String content = this.myline;
        File file = new File(this.getFilesDir(), "xxyyxx.geojson");
        if (this.writeFile(file, content)) {
            Uri uri2= Uri.fromFile(file);
            String a = readFromFile(this);
//            System.out.println(a+"****!!!!!!!!!!!!!!!****");
        }
    }

    private final boolean writeFile(File file, String content) {
        FileOutputStream stream = (FileOutputStream)null;
        boolean var13 = false;
        label124: {
            boolean var20;
            label125: {
                boolean created;
                try {
                    var13 = true;
                    if (!file.exists()) {
                        created = file.createNewFile();
                        if (!created) {
                            var20 = false;
                            var13 = false;
                            break label125;
                        }
                    }
                    stream = new FileOutputStream(file);
                    Charset var5 = Charsets.UTF_8;
                    byte[] var10001 = content.getBytes(var5);
                    Intrinsics.checkNotNullExpressionValue(var10001, "this as java.lang.String).getBytes(charset)");
                    stream.write(var10001);
                    stream.flush();
                    stream.close();
                    created = true;
                    var13 = false;
                } catch (IOException var18) {
                    Log.e("provider", "IOException writing file: ", (Throwable)var18);
                    var13 = false;
                    break label124;
                } finally {
                    if (var13) {
                        try {
                            if (stream != null) {
                                stream.close();
                            }
                        } catch (IOException var14) {
                            Log.e("provider", "IOException closing stream: ", (Throwable)var14);
                        }
                    }
                }
                try {
                    stream.close();
                } catch (IOException var17) {
                    Log.e("provider", "IOException closing stream: ", (Throwable)var17);
                }
                return created;
            }
            return var20;
        }
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException var15) {
            Log.e("provider", "IOException closing stream: ", (Throwable)var15);
        }
        return false;
    }
    private final String readFromFile(Context context) {
        String ret = "";

        try {
            InputStream inputStream = (InputStream)context.openFileInput("xxyyxx.geojson");
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader((Reader)inputStreamReader);
                Object receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while(true) {
                    String var8 = bufferedReader.readLine();

                    if (var8 == null) {
                        inputStream.close();
                        String var10000 = stringBuilder.toString();
                        Intrinsics.checkNotNullExpressionValue(var10000, "stringBuilder.toString()");
                        ret = var10000;
                        break;
                    }

                    stringBuilder.append("\n").append(var8);
                }
            }
        } catch (FileNotFoundException var11) {
            Log.e("login activity", "File not found: " + var11.toString());
        } catch (IOException var12) {
            Log.e("login activity", "Can not read file: " + var12);
        }

        return ret;
    }

    protected void onStart() {
        super.onStart();
        MapboxNavigation var10000 = this.mapboxNavigation;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("mapboxNavigation");
        }
        var10000.registerLocationObserver((LocationObserver)this.locationObserver);
    }

    protected void onStop() {
        super.onStop();
        MapboxNavigation var10000 = this.mapboxNavigation;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("mapboxNavigation");
        }

        var10000.unregisterLocationObserver((LocationObserver)this.locationObserver);
        SensorManager var1 = this.sensorManager;
        if (var1 != null) {
            var1.unregisterListener((SensorEventListener)this);
        }

    }

    protected void onResume() {
        super.onResume();
        this.running = true;
        int sensorstepcounter = 19;
        SensorManager var10001 = this.sensorManager;
        Sensor var3 = var10001 != null ? var10001.getDefaultSensor(sensorstepcounter) : null;
        Intrinsics.checkNotNull(var3);
        this.stepSensor = var3;
        Sensor var10000 = this.stepSensor;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("stepSensor");
        }

        if (var10000 == null) {
            Toast.makeText((Context)this, (CharSequence)"No sensor detected on this device", Toast.LENGTH_SHORT).show();
        } else {
            SensorManager var2 = this.sensorManager;
            if (var2 != null) {
                SensorEventListener var4 = (SensorEventListener)this;
                Sensor var10002 = this.stepSensor;
                if (var10002 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("stepSensor");
                }

                var2.registerListener(var4, var10002, 2);
            }
        }

    }

    protected void onDestroy() {
        super.onDestroy();
        SensorManager var10000 = this.sensorManager;
        if (var10000 != null) {
            var10000.unregisterListener((SensorEventListener)this);
        }

    }
    public void distancecounter(){
        TextView tv_stepsTaken1 = (TextView)this.findViewById(R.id.dis);
        tv_stepsTaken1.setText((CharSequence)String.valueOf(distance));
    }

    public void onSensorChanged(@Nullable SensorEvent event) {
        TextView tv_stepsTaken = (TextView)this.findViewById(R.id.stepsnumber);

        if (this.running) {
            Intrinsics.checkNotNull(event);
            this.totalSteps = event.values[0];
            int currentSteps = (int)this.totalSteps - (int)this.previousTotalSteps;
            Intrinsics.checkNotNullExpressionValue(tv_stepsTaken, "tv_stepsTaken");
            tv_stepsTaken.setText((CharSequence)String.valueOf(currentSteps));

        }
    }

    public final void resetSteps() {
        final ObjectRef<TextView> tv_stepsTaken = new ObjectRef<TextView>();
        tv_stepsTaken.element = (TextView)this.findViewById(R.id.stepsnumber);
        final ObjectRef<TextView> distance = new ObjectRef<TextView>();
        distance.element = (TextView)this.findViewById(R.id.dis);
        TextView resetbut = (TextView)this.findViewById(R.id.resetbut);
        resetbut.setOnClickListener((OnClickListener)(new OnClickListener() {
            public final void onClick(View it) {
                Toast.makeText((Context)MainActivity.this, (CharSequence)"Long tap to reset", Toast.LENGTH_SHORT).show();
            }
        }));
        resetbut.setOnLongClickListener((OnLongClickListener)(new OnLongClickListener() {
            public final boolean onLongClick(View it) {
                MainActivity.this.previousTotalSteps = MainActivity.this.totalSteps;
                TextView var10000 = tv_stepsTaken.element;
                Intrinsics.checkNotNullExpressionValue(var10000, "tv_stepsTaken");
                var10000.setText((CharSequence)String.valueOf(0));
                MainActivity.this.saveData();
                var10000 = distance.element;
                Intrinsics.checkNotNullExpressionValue(var10000, "distance");
                var10000.setText((CharSequence)String.valueOf(0));
                MainActivity.this.distance = 0.0D;
                return true;
            }
        }));
    }

    private final void saveData() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("myPrefs", 0);
        Editor editor = sharedPreferences.edit();
        editor.putFloat("key1", this.previousTotalSteps);
        editor.apply();
    }

    private final void loadData() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("myPrefs", 0);
        float savedNumber = sharedPreferences.getFloat("key1", 0.0F);
        Log.d("MainActivity", String.valueOf(savedNumber));
        this.previousTotalSteps = savedNumber;
    }

    public void onAccuracyChanged(@Nullable Sensor sensor, int accuracy) {
    }

    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        Intrinsics.checkNotNullParameter(permissions, "permissions");
        Intrinsics.checkNotNullParameter(grantResults, "grantResults");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LocationPermissionHelper var10000 = this.locationPermissionHelper;
        if (var10000 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("locationPermissionHelper");
        }

        var10000.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

   // $FF: synthetic method
   public static final MapboxNavigation access$getMapboxNavigation$p(MainActivity $this) {
       MapboxNavigation var10000 = $this.mapboxNavigation;
       if (var10000 == null) {
           Intrinsics.throwUninitializedPropertyAccessException("mapboxNavigation");
       }

       return var10000;
   }

   // $FF: synthetic method
   public static final void access$setMapboxNavigation$p(MainActivity $this, MapboxNavigation var1) {
       $this.mapboxNavigation = var1;
   }

   // $FF: synthetic method
   public static final ActivityMainBinding access$getBinding$p(MainActivity $this) {
       ActivityMainBinding var10000 = $this.binding;
       if (var10000 == null) {
           Intrinsics.throwUninitializedPropertyAccessException("binding");
       }

       return var10000;
   }

   // $FF: synthetic method
   public static final void access$setBinding$p(MainActivity $this, ActivityMainBinding var1) {
       $this.binding = var1;
   }

   // $FF: synthetic method
   public static final float access$getPreviousTotalSteps$p(MainActivity $this) {
       return $this.previousTotalSteps;
   }

   // $FF: synthetic method
   public static final void access$setTotalSteps$p(MainActivity $this, float var1) {
       $this.totalSteps = var1;
   }

   // $FF: synthetic method
   public static final double access$getDistance$p(MainActivity $this) {
       return $this.distance;
   }

   public static final class Companion {
       public final boolean isExternalStorageReadOnly() {
           String extStorageState = Environment.getExternalStorageState();
           return Intrinsics.areEqual("mounted_ro", extStorageState);
       }

       public final boolean isExternalStorageAvailable() {
           String extStorageState = Environment.getExternalStorageState();
           return Intrinsics.areEqual("mounted", extStorageState);
       }

       private Companion() {
       }

       // $FF: synthetic method
       public Companion(DefaultConstructorMarker $constructor_marker) {
           this();
       }
   }
}