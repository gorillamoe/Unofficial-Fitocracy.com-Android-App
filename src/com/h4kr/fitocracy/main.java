package com.h4kr.fitocracy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

public class main extends Activity {
	FitocracyWebsite fitocracyWebsite = new FitocracyWebsite();
	private ProgressDialog progressDialog;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // attach onclick listener to logWorkout Button
        Button closeButton = (Button)this.findViewById(R.id.main_logWorkout);
        closeButton.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
          	fitocracyWebsite.logWorkout(31);
          }
        });
        
        // Set content of the workout set spinner
        Spinner spinnerWorkoutSets = (Spinner) findViewById(R.id.main_workoutSets);
        ArrayAdapter<CharSequence> adapterWorkoutSets = ArrayAdapter.createFromResource(
                this, R.array.main_spinner_workoutSets, android.R.layout.simple_spinner_item);
        adapterWorkoutSets.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWorkoutSets.setAdapter(adapterWorkoutSets);
        
        // Set Content of the workout spinner
        Spinner spinnerWorkout = (Spinner) findViewById(R.id.main_spinner_workout);
        ArrayAdapter<CharSequence> adapterWorkout = ArrayAdapter.createFromResource(
                this, R.array.main_spinner_workout, android.R.layout.simple_spinner_item);
        adapterWorkout.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWorkout.setAdapter(adapterWorkout);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.main_menu_account_login:
        	progressDialog = ProgressDialog.show(main.this, "Logging you in", "Please wait...", true);
            new Thread() {
            	public void run() {
                    try{
                    	if(fitocracyWebsite.login() == true){
                    		progressDialog.dismiss();
                    	}
                   } catch (Exception e) {  }
                    	progressDialog.dismiss();                                   }
			   }.start();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    public void app_editAccount() {
		//Intent in = new Intent(main.this,ImhoPostsActivity_PostAdd.class);
		//startActivity(in);
    }    
    public void exitApp() { 
         this.finish();
    }
    public class FitocracyWebsite {
    	private static final String LOGIN_URL = "http://www.fitocracy.com/accounts/login/";
    	private static final String PLAY_URL = "http://www.fitocracy.com/m/play/";
    	private static final String SUBMIT_WORKOUT_URL = "http://www.fitocracy.com/submit_workout/";
    	// http://www.fitocracy.com/get_workout_from_date/12/22/2011/
    	private static final String GET_WORKOUT_FROM_DATE_URL = "http://www.fitocracy.com/get_workout_from_date/";
    	private static final String COOKIE_NAME_CSRF_MIDDLEWARE_TOKEN = "csrftoken";
    	private static final String BROWSER_USER_AGENT = "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0 Mobile/7A341 Safari/528.16";
    	private static final String FIELDNAME_USERNAME = "username";
    	private static final String FIELDNAME_PASSWORD = "password";
    	private static final String FIELDNAME_CSRF = "csrfmiddlewaretoken";
    	private String CSRF_MIDDLEWARE_TOKEN = "";
    	private Boolean LOGGED_IN_STATUS = false;
    	private HashMap<String, Integer> workoutName2WorkoutId = new HashMap<String, Integer>();
    	private DefaultHttpClient httpclient = new DefaultHttpClient();
    	
    	private String getCsrfMiddlewareToken(){
    		try {
    			if(this.CSRF_MIDDLEWARE_TOKEN == ""){
    				HttpGet httpget = new HttpGet(LOGIN_URL);
	        		httpget.setHeader("User-Agent", BROWSER_USER_AGENT);
	
	        		HttpResponse response = httpclient.execute(httpget);
	        		HttpEntity entity = response.getEntity();
	
	        		if (entity != null) {
	        		  //entity.consumeContent();
	        		}
	
	        		List<Cookie> cookies = httpclient.getCookieStore().getCookies();
	        		for (Cookie cookie : cookies) {
						if(cookie.getName().equals(COOKIE_NAME_CSRF_MIDDLEWARE_TOKEN)){
							CSRF_MIDDLEWARE_TOKEN = cookie.getValue();
							return cookie.getValue();
						}
					}
    			}
    			else{
    				return this.CSRF_MIDDLEWARE_TOKEN;
    			}
    		}
    		catch (Exception e) {
				// TODO: handle exception
			}
    		return "";
    	}
    	public boolean login(){
    		new Thread(new Runnable() {
    		    public void run() {
		    		try {
		    			// call login page
		        		HttpPost httpost = new HttpPost(LOGIN_URL);
		        		httpost.setHeader("User-Agent", BROWSER_USER_AGENT);
		        		
		        		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		        		nvps.add(new BasicNameValuePair(FIELDNAME_USERNAME, ""));
		        		nvps.add(new BasicNameValuePair(FIELDNAME_PASSWORD, ""));
		        		nvps.add(new BasicNameValuePair(FIELDNAME_CSRF, getCsrfMiddlewareToken()));
		
		        		httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		
		        		HttpResponse response = httpclient.execute(httpost);
		        		HttpEntity entity = response.getEntity();
		
		        		// check if login was successful
		        		HttpGet httpget = new HttpGet(PLAY_URL);
		        		httpget.setHeader("User-Agent", BROWSER_USER_AGENT);
		
		        		response = httpclient.execute(httpget);
		        		entity = response.getEntity();
		        		
		                String responseText = EntityUtils.toString(response.getEntity(), "UTF-8");
		
		        		// TODO: check if logged in
		                // maybe check for <title>.. play ...</title>
		                LOGGED_IN_STATUS = true;
		        		if (entity != null) {
		        		  //entity.consumeContent();
		        		}
					} catch (Exception e) {
						// TODO: handle exception
					}
    		    }}).run();
    		return true;
    	}
    	public String logWorkout(Integer WorkoutId){
    		AlertDialog.Builder builder=new AlertDialog.Builder(main.this);
    		
    		if(LOGGED_IN_STATUS == false){
    			builder.setTitle("You are not logged in");
    			builder.setMessage("Please login first");
    			builder.setIcon(android.R.drawable.ic_dialog_alert);
    			builder.setPositiveButton("OK", null);
    			builder.show();
    			return "";
    		}
    		try {
    			
    			// create actual date
    			/*Calendar calendar = Calendar.getInstance(); // today
    			String dateString = calendar.get(calendar.MONTH)+1 + "/" + calendar.get(calendar.DAY_OF_MONTH) + "/" + calendar.get(calendar.YEAR);*/
    			DatePicker datePicker = (DatePicker)findViewById(R.id.main_datePicker);
    			String dateString = datePicker.getMonth()+1 + "/" + datePicker.getDayOfMonth() + "/" + datePicker.getYear();
    			Log.v("Date String", dateString);
    			
    			// fetch actual workouts
    			String url = GET_WORKOUT_FROM_DATE_URL + dateString + "/";
    			Log.v("DEBUG", "URL: "+url);
				HttpGet httpget = new HttpGet(url);
        		httpget.setHeader("User-Agent", BROWSER_USER_AGENT);

        		HttpResponse response = httpclient.execute(httpget);
        		HttpEntity entity = response.getEntity();
        		String responseText = EntityUtils.toString(entity, "UTF-8");
        		Log.v("DEBUG", "Response from server: "+responseText);
        		
        		JSONArray jsonArray = new JSONArray(responseText);
        		
        		for(int i=0;i<jsonArray.length();i++){
        			Log.v("Effort", jsonArray.getJSONObject(i).toString());
        			JSONObject efforts = jsonArray.getJSONObject(i);
        			Integer lengthEfforts = efforts.length();
        			Log.v("subgroup_details",efforts.getJSONArray("subgroup_details").toString());
        			for(int ii=0;i<lengthEfforts;i++){
        				// amount of reps per effort
        				String amountRepsPerEffort = efforts.getJSONArray("subgroup_details").getJSONObject(ii).getString("effort1_conv_metric");
        				Log.v("subgroup_details array",amountRepsPerEffort);
        			}
        		}  		
        		Integer AmountOfWorkoutsSoFar = jsonArray.length();
        		if(AmountOfWorkoutsSoFar == -1){
        			AmountOfWorkoutsSoFar = 0;
        		}
        		Log.v("DEBUG", "AmountOfWorkoutsSoFar: "+AmountOfWorkoutsSoFar);
        		
    			
    			// fetch workout id from spinner 
    			Spinner spinnerWorkout = (Spinner)findViewById(R.id.main_spinner_workout);
    			String spinnerWorkoutValue = spinnerWorkout.getSelectedItem().toString();
    	    	if(workoutName2WorkoutId.isEmpty()){
        	    	String[] workoutNames = getResources().getStringArray(R.array.main_spinner_workout);
	    	    	int[] workoutIds = getResources().getIntArray(R.array.main_spinner_workout_ids);
	    	    	for(int i = 0; i < workoutNames.length; i++) {
	    	    		workoutName2WorkoutId.put(workoutNames[i], workoutIds[i]);
	    	    	}
    	    	}
    	    	Integer spinnerWorkoutId = (Integer)workoutName2WorkoutId.get(spinnerWorkoutValue.toString());
    	    	
    	    	Log.v("Workout Selection String", spinnerWorkoutValue);
    			Log.v("Workout Selection Id", spinnerWorkoutId.toString());
    			
    			// fetch reps from editbox
    			EditText editBoxReps = (EditText)findViewById(R.id.main_reps);
    			String editBoxRepsValue = editBoxReps.getText().toString();
    			
    			if(editBoxRepsValue == ""){
        			builder.setTitle("You need to supply reps..");
        			builder.setMessage("You need to enter the amount of reps before submitting a workout.");
        			builder.setIcon(android.R.drawable.ic_dialog_alert);
        			builder.setPositiveButton("OK", null);
        			builder.show();
        			return "";
    			}
    			
    			
    			// call submit workout page
        		HttpPost httpost = new HttpPost(SUBMIT_WORKOUT_URL);
        		httpost.setHeader("User-Agent", BROWSER_USER_AGENT);
        		
        		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        		nvps.add(new BasicNameValuePair("activity_order["+AmountOfWorkoutsSoFar+"]", spinnerWorkoutId.toString()));
        		nvps.add(new BasicNameValuePair("api_source["+spinnerWorkoutId+"]["+AmountOfWorkoutsSoFar+"]", "mobile_web"));
        		nvps.add(new BasicNameValuePair(FIELDNAME_CSRF, fitocracyWebsite.getCsrfMiddlewareToken()));
        		nvps.add(new BasicNameValuePair("effort1["+spinnerWorkoutId+"]["+AmountOfWorkoutsSoFar+"]", editBoxRepsValue)); // The amount of reps
        		nvps.add(new BasicNameValuePair("effort1_unit["+spinnerWorkoutId+"]["+AmountOfWorkoutsSoFar+"]", "31")); // The Workout Id
        		nvps.add(new BasicNameValuePair("log_option", "log"));
        		nvps.add(new BasicNameValuePair("notes["+spinnerWorkoutId+"]", ""));
        		nvps.add(new BasicNameValuePair("workout_time_hidden", dateString));
        		
        		
        		/*httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

        		response = httpclient.execute(httpost);
        		responseText = EntityUtils.toString(response.getEntity(), "UTF-8");
        		jObject = new JSONObject(responseText);
        		if(jObject.getString("success") == "true"){
        			builder.setTitle("Success");
        			builder.setMessage("Successfully logged your workout");
        			builder.setIcon(android.R.drawable.ic_dialog_alert);
        			builder.setPositiveButton("OK", null);
        		}
        		else{
        			builder.setTitle("Error");
        			builder.setMessage("Could not log your workout");
        			builder.setIcon(android.R.drawable.ic_dialog_alert);
        			builder.setPositiveButton("OK", null);
        		}
        		builder.show();*/
        		//Log.v("Response Logging Workout", responseText);
        		
			} catch (Exception e) {
				// TODO: handle exception
				Log.v("Exception", e.toString());
			}
    		return "";
    	}
    }
}