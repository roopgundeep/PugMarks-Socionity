package org.socionity.gps.marker;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.MapViewPosition;
import org.mapsforge.android.maps.overlay.ArrayItemizedOverlay;
import org.mapsforge.android.maps.overlay.Overlay;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.mapsforge.core.GeoPoint;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IMapView;
import org.osmdroid.views.overlay.ItemizedOverlay;
//import org.osmdroid.views.overlay.OverlayItem;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class VectorActivity extends MapActivity {
	//Declarations of variables
	private static final String marker = null;
	ArrayList<File> mNames = new ArrayList<File>();
	MapView mapView;
	File file;
	String file_name;
	MyItem itemizedOverlay;
	Drawable drawable;// abstraction for something that can be drawn. It draws from the xml file
	ListView lv;
	Location location;
	DbManager db_man;
	SocionityGPSApplication app;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_advanced_map); //referring to the layout for this activity
		mapView = (MapView) findViewById(R.id.mapView); //Creating a MapView
		mapView.setBuiltInZoomControls(true); //Enabling Zoom
		mapView.setClickable(true); //TO make the map click-able
		drawable = this.getResources().getDrawable(R.drawable.marker); //A draw-able instance for the marker		
		loadMap();//THis function will load Map on the page and make it visible
		
		//Putting Markers over the Map
		double lat= 17.445406;
		double lng= 78.349183;
		GeoPoint point = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));//1E6 is floating point number
		putMarker(point);//This function overlays the marker on Map
		
	}

	public void loadMap() {
		mapView.setBuiltInZoomControls(true);
		mapView.setClickable(true); //Click-able true
		file_name = "/sdcard/VectorMaps/Belgaum.map"; //fileName of your Map
		file = new File(file_name);
		mapView.setMapFile(file);// set which .map file to include
		drawable.setAlpha(100);//set opaque value for a draw-able
		itemizedOverlay = new MyItem(drawable, file); 
		mapView.getOverlays().add(itemizedOverlay); // Overlaying of the Map so it becomes visible
	}

	public void putMarker(GeoPoint geopoint) {
		
		OverlayItem item = new OverlayItem(geopoint, "hi",
				"this is the location");
		itemizedOverlay.addItem(item); //adding the item in overlay
		mapView.getOverlays().add(itemizedOverlay); //adding the map over the layout
		mapView.invalidate(); //to redraw the view
	}

	public class MyItem extends ArrayItemizedOverlay {

		public MyItem(Drawable defaultMarker) {
			super(defaultMarker);
		}
		public MyItem(Drawable defaultMarker, File file) {
			super(defaultMarker);
		}
	}
}
