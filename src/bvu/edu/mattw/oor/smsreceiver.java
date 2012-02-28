package bvu.edu.mattw.oor;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class smsreceiver extends BroadcastReceiver {
	String action = null;
	
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
	public void getSMS(Context cntxt, Intent intent){
		String phoneNumber = "0";
		String message = "I am busy sorry bro";
		
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
            Toast.makeText(cntxt, str, Toast.LENGTH_LONG).show();
            sendSMS(phoneNumber,message);
        }
	}
    private void sendSMS(String phoneNumber, String message){        
        //PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, smsreceiver.class), 0);                
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }
	public void getCall(Context contxt){
	    MyPhoneStateListener phoneListener=new MyPhoneStateListener();
	    TelephonyManager telephony = (TelephonyManager) 
	    contxt.getSystemService(Context.TELEPHONY_SERVICE);
	    telephony.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);
	}
	public class MyPhoneStateListener extends PhoneStateListener {
		public void onCallStateChanged(int state,String incomingNumber){
			  switch(state){
			    case TelephonyManager.CALL_STATE_RINGING:
			      Log.d("DEBUG", "RINGING");
			    break;
		    }
		}
	}
}