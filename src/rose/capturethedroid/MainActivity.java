package rose.capturethedroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.gson.Gson;

public class MainActivity extends Activity {

	private GoogleMap map;

	private static final String KEY_PLAYERLOC_LAT = "KEY_PLAYERLOC_LAT";
	private static final String KEY_PLAYERLOC_LON = "KEY_PLAYERLOC_LON";
	private static final String KEY_FLAGLOC_LON_0 = "KEY_FLAGLOC_LON_0";
	private static final String KEY_FLAGLOC_LON_1 = "KEY_FLAGLOC_LON_1";
	private static final String KEY_FLAGLOC_LAT_0 = "KEY_FLAGLOC_LAT_0";
	private static final String KEY_FLAGLOC_LAT_1 = "KEY_FLAGLOC_LAT_1";
	private static final String KEY_SOCKET = "KEY_SOCKET";

	private static final int RED_TEAM = 0;
	private static final int BLU_TEAM = 1;

	private boolean hasFlag;
	private boolean connected;
	private Coordinate playerloc;

	private int team;

	private Socket socket = null;
	private final int port = 1432;
	private final String serverIP = "172.22.119.68";// "172.22.119.69";

	private String playerName;
	private String inputText;

	Gson gson = new Gson();
	PrintWriter out;
	BufferedReader in;
	
	Intent intent;
	private boolean mapdisplayed;

	protected Coordinate[] flagpos;

	protected int gameCondition;// -1:game still running,0:team 0 wins,1:team 1
								// wins

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Extras app=(Extras)getApplication();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// map = ((MapFragment)
		// getFragmentManager().findFragmentById(R.id.map)).getMap();
		this.hasFlag = false;
		this.connected = false;
		this.playerloc = new Coordinate(0, 0);
		app.playerCoords=this.playerloc;
		app.flagpos=new Coordinate[]{new Coordinate(0,0),new Coordinate(30,30)};		
		this.team = 0;

		// System.out.println("I'm alive");
		Log.e("stuff", "We got here!");
		// Toast.makeText(getApplicationContext(), "YAYYYYYYYY",
		// Toast.LENGTH_SHORT).show();

		new OpenConnection().execute();

		final Handler handler = new Handler();
		handler.post(new Runnable() {

			

			@Override
			public void run() {
				boolean processing = true;

				if (MainActivity.this.connected) {

					if (MainActivity.this.in == null || MainActivity.this.out == null) {
						TextView tv = (TextView) findViewById(R.id.text1);
						tv.setText("Failed to Connect");
						// Toast.makeText(getApplicationContext(),
						// "Failed to connect", Toast.LENGTH_SHORT).show();

					} else {
						TextView tv = (TextView) findViewById(R.id.text1);
						tv.setText("Connected");
						// Toast.makeText(getApplicationContext(), "YAYYYYYYYY",
						// Toast.LENGTH_SHORT).show();
					}
					// Send information to the server
					// create new JsonObject

					MainActivity.this.playerName = ((EditText) findViewById(R.id.editText2))
							.getText().toString();

					JsonHolder jsonData = new JsonHolder(MainActivity.this.team, MainActivity.this.playerName,
							MainActivity.this.playerloc);
					String json = MainActivity.this.gson.toJson(jsonData);
					MainActivity.this.out.println(json);

					// Get information from the server
//					String inputLine = null;
					try {
						// if(in==null||out==null) {
						// Toast.makeText(getApplicationContext(),
						// "still null :(",
						// Toast.LENGTH_LONG).show();
						// }
						// inputLine = in.readLine();
						// if (inputLine == null) {// if the input is null, wait
						// a
						// // bit, then try again
						// for (int i = 0; i <= 16; i = i * 2) {
						// // inputLine = in.readLine();
						// if (inputLine == null)
						// try {
						// Thread.sleep(i * 1000);
						// } catch (InterruptedException exception) {
						// // TODO Auto-generated catch-block stub.
						// exception.printStackTrace();
						// }
						// else {
						// break;
						// }
						// }
						// if (inputLine == null)
						// processing = false;
						// }
						// if (processing == false)
						// return;// lost connection with server TODO decide
						// // what to do here
					} catch (Exception e) {
						e.printStackTrace();
					}

					// Parse information from the server

					// Calls up a worker thread to recieve data from the server
//					inputText = inputLine;
					new ReadInfoFromServer().execute();

					// update hasFlag box here
					TextView tv1 = (TextView) findViewById(R.id.textView1);
					if (MainActivity.this.hasFlag) {
						tv1.setText("Flag: You have the enemy flag!");
					} else {
						tv1.setText("Flag: Go get the flag!");
					}

					// update gameCondition
					TextView tv2 = (TextView) findViewById(R.id.textView2);
					switch (MainActivity.this.gameCondition) {
					case RED_TEAM:
						if (MainActivity.this.team == RED_TEAM) {
							tv2.setText("You are VICTORIOUS!");
						} else {
							tv2.setText("You have LOST");
						}
						break;
					case BLU_TEAM:
						if (MainActivity.this.team == BLU_TEAM) {
							tv2.setText("You are VICTORIOUS!");
						} else {
							tv2.setText("You have LOST");
						}
						break;
					default:
						tv2.setText("The game is IN PROGRESS");
					}

					// Test toast
					// Toast.makeText(getApplicationContext(), "Toast",
					// Toast.LENGTH_SHORT).show();
				if(MainActivity.this.mapdisplayed)
					new UpdateMap(MainActivity.this.intent).execute();
				}
				// Delay
				handler.postDelayed(this, 1000); // Refresh time

			}

		});

		// if (savedInstanceState == null) {
		// getSupportFragmentManager().beginTransaction()
		// .add(R.id.container, new PlaceholderFragment()).commit();
		// }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void showLocation(View view) {

		this.connected = !this.connected;

		GPSTracker gps = new GPSTracker(MainActivity.this);

		// check if GPS enabled
		if (gps.canGetLocation()) {
			double latitude = gps.getLatitude();
			double longitude = gps.getLongitude();

			this.playerloc.longitude = longitude;
			this.playerloc.latitude = latitude;

			// Toast.makeText(
			// getApplicationContext(),
			// "Your location is - \nLat: " + latitude + "\nLong: "
			// + longitude, Toast.LENGTH_SHORT).show();
		} else {
			// can't get location
			// GPS or Network isn't enabled
			// ask user to enable GPS/network in settings
			gps.showSettingsAlert();
		}

	}

	public void displayMap(View view) {

		// Uri location = Uri.parse("geo:" + this.playerloc.latitude + ","
		// + this.playerloc.longitude + "z=14");
		// Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);
		// startActivity(mapIntent);

		this.intent = new Intent(MainActivity.this, MapActivity.class);
		this.mapdisplayed=true;
		// async task goes here
		
		
		
		startActivity(this.intent);

	}

	public void chooseRed(View view) {
		this.team = RED_TEAM;
	}

	public void chooseBlu(View view) {
		this.team = BLU_TEAM;
	}

	// /**
	// * A placeholder fragment containing a simple view.
	// */
	// public static class PlaceholderFragment extends Fragment {
	//
	// public PlaceholderFragment() {
	// }
	//
	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
	// Bundle savedInstanceState) {
	// View rootView = inflater.inflate(R.layout.activity_main, container,
	// false);
	// return rootView;
	// }
	// }
@Override
protected void onDestroy() {
	// TODO Auto-generated method stub.
	super.onDestroy();
}
	private class OpenConnection extends AsyncTask {

		@Override
		protected Object doInBackground(Object... params) {
			try {
				// Inet4Address inet4 = (Inet4Address) InetAddress
				// .getByName("Eddie");
				MainActivity.this.socket = new Socket(MainActivity.this.serverIP, MainActivity.this.port);
				MainActivity.this.out = new PrintWriter(MainActivity.this.socket.getOutputStream(), true);
				MainActivity.this.in = new BufferedReader(new InputStreamReader(
						MainActivity.this.socket.getInputStream()));
				// Log.e("Connection Debug message", inet4.toString());
				// Toast.makeText(getApplicationContext(), inet4.toString(),
				// Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				Log.e("Connection Debug message", "Your network failed");
				// Toast.makeText(getApplicationContext(), e.toString(),
				// Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			return null;
		}
	}

	private class ReadInfoFromServer extends AsyncTask {

		@Override
		protected Object doInBackground(Object... params) {
			Log.e("WHAT","readinfofromserver is working");
			try {
				MainActivity.this.inputText=MainActivity.this.in.readLine();
			} catch (IOException exception) {
				// TODO Auto-generated catch-block stub.
				exception.printStackTrace();
			}
			GameInfo gameinfo = MainActivity.this.gson.fromJson(MainActivity.this.inputText, GameInfo.class);
			 Log.e("OOPS", "Part 1");
			if (gameinfo != null) {
				Log.e("Input","parsing gameinfo");
				MainActivity.this.hasFlag = gameinfo.flagholder;
				MainActivity.this.flagpos = gameinfo.flagpos;
				MainActivity.this.gameCondition = gameinfo.win;
			}else {
				Log.e("Input","Gameinfo was null :(");
			}
			// Log.e("OOPS", "Part 2");
			return null;
		}
		

	}
	
	private class UpdateMap extends AsyncTask {
		
		Intent intent;
		
		public UpdateMap(Intent intent) {
			this.intent = intent;
		}
		
		@Override
		protected Object doInBackground(Object... params) {
			this.intent.putExtra(KEY_PLAYERLOC_LON, MainActivity.this.playerloc.longitude);
			Log.e("stuff", "" + MainActivity.this.playerloc.latitude);
			Extras app=(Extras)getApplication();
			app.flagCarrier=MainActivity.this.hasFlag;
			app.flagpos=MainActivity.this.flagpos;
			app.gameCondition=MainActivity.this.gameCondition;
			app.playerCoords=MainActivity.this.playerloc;
//			if (MainActivity.this.flagpos != null) {
//				intent.putExtra(KEY_FLAGLOC_LAT_0, MainActivity.this.flagpos[0].latitude);
//				intent.putExtra(KEY_FLAGLOC_LON_0, MainActivity.this.flagpos[0].longitude);
//				intent.putExtra(KEY_FLAGLOC_LAT_1, MainActivity.this.flagpos[1].latitude);
//				intent.putExtra(KEY_FLAGLOC_LON_1, MainActivity.this.flagpos[1].longitude);
//				Log.e("stuffis", "" + flagpos[0].latitude);
//			}
			
			return null;
		}
		
	}

}
