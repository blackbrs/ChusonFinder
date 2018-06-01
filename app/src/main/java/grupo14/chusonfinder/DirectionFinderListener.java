package grupo14.chusonfinder;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import grupo14.chusonfinder.Route;


public  interface DirectionFinderListener {
     void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
}
