package org.socionity.gps.marker;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class PhotoPreview extends Activity{
	//variable for selection intent
	private final int PICKER = 1;
	//variable to store the currently selected image
	private int currentPic = 0;
	//gallery object
	private Gallery picGallery;
	//image view for larger display
	private ImageView picView;
	//adapter for gallery view
	private PicAdapter imgAdapt;
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_select);
        //get the large image view
        picView = (ImageView) findViewById(R.id.picture);
        //get the gallery view
        picGallery = (Gallery) findViewById(R.id.gallery);
      //create a new adapter
        imgAdapt = new PicAdapter(this);
        //set the gallery adapter
        picGallery.setAdapter(imgAdapt);
      
      //set the click listener for each item in the thumbnail gallery
        picGallery.setOnItemClickListener(new OnItemClickListener() {
            //handle clicks
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //set the larger image view to display the chosen bitmap calling method of adapter class
                picView.setImageBitmap(imgAdapt.getPic(position));
            }
        });
        

    }
    
    public class PicAdapter extends BaseAdapter {
    	//use the default gallery background image
    	int defaultItemBackground;
    	//gallery context
    	private Context galleryContext;
    	private Bitmap[] imageBitmaps ;
    	//array to store bitmaps to display 
    	//placeholder bitmap for empty spaces in gallery
    //	galleryContext = c;
    	Bitmap placeholder;
    	public PicAdapter(Context c) {
    	    //instantiate context
    		galleryContext = c;
    	    //create bitmap array
    	    imageBitmaps  = new Bitmap[10];
    	    //decode the placeholder image
    	    placeholder = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
    	    //more processing
    	    //set placeholder as all thumbnail images in the gallery initially
    	    for(int i=0; i<imageBitmaps.length; i++)
    	        imageBitmaps[i]=placeholder;
    	  //get the styling attributes - use default Andorid system resources
    	    TypedArray styleAttrs = galleryContext.obtainStyledAttributes(R.styleable.PicGallery);
    	    //get the background resource
    	    defaultItemBackground = styleAttrs.getResourceId(
    	        R.styleable.PicGallery_android_galleryItemBackground, 0);
    	    //recycle attributes
    	    styleAttrs.recycle();
    	}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return imageBitmaps.length;
		}
		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}
		@Override
		//get view specifies layout and display options for each thumbnail in the gallery
		public View getView(int position, View convertView, ViewGroup parent) {
		    //create the view
		    ImageView imageView = new ImageView(galleryContext);
		    //specify the bitmap at this position in the array
		    imageView.setImageBitmap(imageBitmaps[position]);
		    //set layout options
		    imageView.setLayoutParams(new Gallery.LayoutParams(300, 200));
		    //scale type within view area
		    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		    //set default gallery item background
		    imageView.setBackgroundResource(defaultItemBackground);
		    //return the view
		    return imageView;
		}
		//helper method to add a bitmap to the gallery when the user chooses one
		public void addPic(Bitmap newPic)
		{
		    //set at currently selected index
		    imageBitmaps[currentPic] = newPic;
		}
		//return bitmap at specified position for larger display
		public Bitmap getPic(int posn)
		{
		    //return bitmap at posn index
		    return imageBitmaps[posn];
		}
	}

}
