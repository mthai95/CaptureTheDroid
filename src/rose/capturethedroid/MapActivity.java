package rose.capturethedroid;

import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Activity that opens up the map.
 * 
 * @author thaimp. Created Apr 12, 2014.
 */
public class MapActivity extends Activity {

	private Timer autoUpdate;
	GoogleMap googleMap;
	Coordinate playerCoords;
	Coordinate flagZeroCoords;
	Coordinate flagOneCoords;
	MarkerOptions playerMarker;
	MarkerOptions flagZeroMarker;
	MarkerOptions flagOneMarker;

	@Override
	protected void onCreate(Bundle savedInstanceBundle) {
		super.onCreate(savedInstanceBundle);
		setContentView(R.layout.map_fragment);
		
		playerCoords = new Coordinate(0, 0);		
		flagZeroCoords = new Coordinate(30, 30);
		flagOneCoords = new Coordinate(60, 60);
		
		loadMap();

	}

	/**
	 * function to load map. If map is not created it will create it for you
	 * */
	private void loadMap() {
		
		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();
		} else {
			googleMap.clear();
		}

		// create new player and flag markers
		playerMarker = new MarkerOptions().position(
				new LatLng(playerCoords.latitude, playerCoords.longitude)).title("You");
		playerMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
		flagZeroMarker = new MarkerOptions().position(
				new LatLng(flagZeroCoords.latitude, flagZeroCoords.longitude)).title("Flag 0");
		flagZeroMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
		flagOneMarker = new MarkerOptions().position(
				new LatLng(flagOneCoords.latitude, flagOneCoords.longitude)).title("Flag 1");
		flagZeroMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

		// adding markers
		googleMap.addMarker(playerMarker);
		googleMap.addMarker(flagZeroMarker);
		googleMap.addMarker(flagOneMarker);

		// check if map is created successfully or not
		if (googleMap == null) {
			Toast.makeText(getApplicationContext(),
					"Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		autoUpdate = new Timer();
		autoUpdate.schedule(new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
//						Log.e("Marker","Marker executing");
						updateMarkers();
					}
				});
			}
		}, 0, 2000); // update every 2 seconds
	}

	public void updateMarkers() {
		
//		Bundle extras = getIntent().getExtras();
//		if(extras!=null){
//			Log.e("MARKER","Extras not null");
		Extras app=(Extras)getApplication();
		playerCoords = app.getPlayerCoords();//new Coordinate(extras.getDouble("KEY_PLAYERLOC_LAT"), extras.getDouble("KEY_PLAYERLOC_LON"));
		flagZeroCoords = app.getFlagpos()[0];//new Coordinate(extras.getDouble("KEY_FLAGLOC_LAT_0"), extras.getDouble("KEY_FLAGLOC_LON_0"));
		flagOneCoords = app.getFlagpos()[1];//new Coordinate(extras.getDouble("KEY_FLAGLOC_LAT_1"), extras.getDouble("KEY_FLAGLOC_LON_1"));
		
		// Loading map
		loadMap();
//		}else{
//			Log.e("MARKER","extras null :(");
//		}
		// check if map is created successfully or not
		if (googleMap == null) {
			Toast.makeText(getApplicationContext(),
					"Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
		}
	}
}
