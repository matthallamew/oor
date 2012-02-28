package bvu.edu.mattw.oor;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import java.util.*;

public class OorActivity extends Activity implements RadioGroup.OnCheckedChangeListener{
	Button radio1,radio2;
	TextView outputText;
	RadioGroup radgroup;
	//int radCheckId = -1;
	String defaultMsg = "I am currently unavailable.  I will call you back shortly.";
	String msge = defaultMsg;
	ArrayList<String> msgdUsr = new ArrayList<String>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
	    radio1 = (Button) findViewById(R.id.radioButton1);
	    //radio1.setOnClickListener(this);
	    radio2 = (Button) findViewById(R.id.radioButton2);
	    //radio2.setOnClickListener(this);
        radgroup = (RadioGroup) findViewById(R.id.radgroup1);
        radgroup.setOnCheckedChangeListener(this);
        outputText = (TextView) findViewById(R.id.output_text);
        outputText.setText("Default message if none selected: \n"+"\""+defaultMsg+"\"");

        //startService(new Intent(TweetCollectorService.class.getName()));
        if(myBcast != null){
        	IntentFilter filter = new IntentFilter();
        	filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        	filter.addAction("android.intent.action.PHONE_STATE");
        	registerReceiver(myBcast,filter);
        	System.out.println("Registered");
        }
    }
	@Override
	public void onCheckedChanged(RadioGroup radGroup, int checkedID) {
		switch(checkedID){
		case R.id.radioButton1:
			msge = radio1.getText().toString();
			break;
		case R.id.radioButton2:
			msge = radio2.getText().toString();
			break;
		case -1:
			msge = defaultMsg;
			break;
		default :
			msge = defaultMsg;
			break;
		}
		//radCheckId = checkedID;
	}
/*	@Override
	public void onClick(View v) {
		switch(radCheckId){
		case R.id.radioButton1:
			msge = radio1.getText().toString();
			break;
		case R.id.radioButton2:
			break;
		case -1:
			break;
		}
	}*/
    public void onPause(){
    	super.onPause();
    	//Don't unregister so it continues working when paused
    	if(myBcast != null){
        	//this.unregisterReceiver(myBcast);
        	//myBcast = null;
        	//System.out.println("Pausing");
    	}
    }
    public void onResume(){
    	super.onResume();
    	if(myBcast == null){
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.provider.Telephony.SMS_RECEIVED");
            filter.addAction("android.intent.action.PHONE_STATE");
        	registerReceiver(myBcast, filter); 
        	//System.out.println("Resuming");
    	}
    }
    public void onDestroy(){
		//System.out.println("Destroying");
    	if(myBcast != null){
        	unregisterReceiver(myBcast);
        	myBcast = null;
    	}
    	super.onDestroy();
    	//SUPPOSEDLY NOT BEST PRACTICE
    	//However, I need to do this in order to truly stop the process
    	//otherwise two or more Broadcast receivers get registered
    	//and you get weird things going on with phone calls and auto
    	//reply text messages -aka more than one message per phone call
    	//android.os.Process.killProcess(android.os.Process.myPid());
    }
    private BroadcastReceiver myBcast = new BroadcastReceiver() {
    	String action = null;
		//boolean sentMsg = false;
		//String message = msge;

		@Override
		public void onReceive(Context cntx, Intent intent) {
	         action = intent.getAction();
	        
	        if(action.equals("android.intent.action.PHONE_STATE")){
	        	getCall(cntx);
	        }
	        if(action.equals("android.provider.Telephony.SMS_RECEIVED")){        	
	    		getSMS(cntx,intent);
	        }
	    }
		private void getSMS(Context cntxt, Intent intent){
			String phoneNumber = "0";
			String message = msge;//"I am busy sorry bro";
			
			//---get the SMS message passed in---
	        Bundle bndl = intent.getExtras();        
	        SmsMessage[] msg = null;
	        String str = "";            
	        if (bndl != null){
	            //---retrieve the SMS message received---
	            Object[] pdus = (Object[]) bndl.get("pdus");
	            msg = new SmsMessage[pdus.length];            
	            for (int i=0; i<msg.length; i++){
	                msg[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
	                phoneNumber = msg[i].getOriginatingAddress();
	                str += "SMS: ";
	                str += msg[i].getMessageBody().toString();
	            }
	            //---display the new SMS message---
	            //displays sms overtop of anything on the screen
	            Toast.makeText(cntxt, str, Toast.LENGTH_LONG).show();
		    	//Have we sent a message already?  If not, add to msgdUsr array and send message
		    	//This gets reset everytime the app stops and restarts so no need for user to clear out
		    	//This does only allow one message to be sent to each person who calls or sends a text
		    	//example If user calls, then texts, auto msg gets sent on call and none on text.  Vice versa for text first, then call
		    	if(!msgdUsr.contains(phoneNumber)){
		    		msgdUsr.add(phoneNumber);
		            sendSMS(phoneNumber,message);		            
		    	}
	        }
		}
	    private void sendSMS(String phoneNumber, String _message){        
	        //PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, smsreceiver.class), 0);
	        SmsManager sms = SmsManager.getDefault();
	        sms.sendTextMessage(phoneNumber, null, _message, null, null);
	       //supposedly not a good idea to use this.
	        ContentValues values = new ContentValues();
	        values.put("address", phoneNumber);
	        values.put("body", _message);
	        // Note: This uses an Android internal API to save to Sent-folder
	        getContentResolver().insert(Uri.parse("content://sms/sent"), values); 

	    }
		private void getCall(Context contxt){
		    MyPhoneStateListener phoneListener=new MyPhoneStateListener();
		    TelephonyManager telephony = (TelephonyManager) contxt.getSystemService(Context.TELEPHONY_SERVICE);
		    telephony.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);
		}
		
		/*
		 * This is used to check the state of the phone.
		 * Is it ringing?  Is it Idle?
		 */
		class MyPhoneStateListener extends PhoneStateListener {
			public void onCallStateChanged(int state,String incomingNumber){
				String message = msge;//"I am busy sorry bro";

				switch(state){
				    case TelephonyManager.CALL_STATE_RINGING:
				      //Log.d("DEBUG", "RINGING");
				    	  //sendSMS(incomingNumber,message);
				    	  //sentMsg=false;
				    break;
				    case TelephonyManager.CALL_STATE_IDLE:
				    	//Log.d("DEBUG","IDLE");
					    /*if(sentMsg == false){
					    	sendSMS(incomingNumber,message);
					    	sentMsg = true;
					    }*/
				    	//Have we sent a message already?  If not, add to msgdUsr array and send message
				    	//This gets reset everytime the app stops and restarts so no need for user to clear out
				    	//This does only allow one message to be sent to each person who calls or sends a text
				    	//example If user calls, then texts, auto msg gets sent on call and none on text.  Vice versa for text first, then call
				    	if(!msgdUsr.contains(incomingNumber)){
				    		msgdUsr.add(incomingNumber);
					    	sendSMS(incomingNumber,message);
				    	}
				    break;
			    }
			}
		}
    };
}