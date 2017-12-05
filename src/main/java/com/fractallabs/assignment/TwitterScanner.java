package com.fractallabs.assignment;

import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesSampleEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.BasicClient;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

public class TwitterScanner {
	
	// Date Format
	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/YY hh:mm");
		
	// MultiThreading access
	private Timer mTimer = new Timer();
	private volatile int mMentions = 0;
		
	private final String mCompanyName;
	private final Authentication mAuth;
	
	public static enum TimerUnit{
    	H1(60*60*1000),
    	M5(5*60*1000),
    	M1(60*1000);
    	
    	private long mValue;
    	TimerUnit(long value){
    		mValue = value;
    	}
    	
    	public long getValue() {
    		return mValue;
    	}
    }
	
	public static class TSValue {
		private final long timestamp;
		private final double val;

		public TSValue(long timestamp, double val) {
			this.timestamp = timestamp;
			this.val = val;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public double getVal() {
			return val;
		}
	}

	public TwitterScanner(String companyName, Authentication auth) {
		if (companyName == null) {
			throw new IllegalArgumentException("Company name null");
		} else if (auth == null) {
			throw new IllegalArgumentException("Authentication null");
		} else if (companyName.isEmpty()) {
			throw new IllegalArgumentException("No company name");
		} else {
			mCompanyName = companyName;
			mAuth = auth;
		}
	}

	public void run() {
		// Begin aggregating mentions. Every hour, "store" the relative change
		TimerTask  task = new TimerTask() {
			private int prvMentions = 0;
			
			public void run() {
				TSValue value = new TSValue(System.currentTimeMillis(), (prvMentions == 0) ? 100.0 : 100.0 * (mMentions - prvMentions) / prvMentions);
		        storeValue(value);
		        
		        // Reset the mention counters
		        prvMentions = mMentions;
		        mMentions = 0;
			}
		};
		
		// Start Scheduling
		mTimer.scheduleAtFixedRate(task, 0, TimerUnit.H1.getValue());

		// Setup loop for scanning twitter msgs
		try {
			// Set up your blocking queues: Be sure to size these properly based on expected
			// TPS of your stream
			BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(20000);

            // Declare the the endpoint
            StatusesSampleEndpoint hbEndpoint = new StatusesSampleEndpoint();
            hbEndpoint.stallWarnings(false);

            // Creating a client:
            ClientBuilder builder = new ClientBuilder()
                    .name("TwitterAPP")
                    .hosts(Constants.STREAM_HOST)
                    .endpoint(hbEndpoint)
                    .authentication(mAuth)
                    .processor(new StringDelimitedProcessor(msgQueue));
            // No client events

            BasicClient hbClient = builder.build();
    		
            // Attempts to establish a connection.
            hbClient.connect();

            // Read from these queues
            while (!hbClient.isDone()) {
                String msg = msgQueue.poll(50, TimeUnit.SECONDS); 
                if (msg == null) continue;
                JSONTokener jsTokener = new JSONTokener(msg);
                JSONObject payload = (JSONObject) jsTokener.nextValue();
                try {
                    String text = payload.getString("text");
                    if (text.contains(mCompanyName)) {
                    	mMentions++; 
                    }
                } catch (JSONException e) {
                    continue;
                }
            }
            
            // Close the connection
        	hbClient.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
	}

	private void storeValue(TSValue value) {
		System.out.println(dateFormat.format(value.getTimestamp()) + ", " + String.format("%.2f", value.getVal()) + '%');
	}

	public static void main(String... args) {
		// Keyword
		String companyName = "Facebook";

		// Credentials
		String consumerKey = "ju0sJx5aSTBdwLYj8ESiBOmvN";
		String consumerSecret = "yLBx15INmkB0yxpN22GBHJs5dZFyJ8WHmegtGdMqzhbFFivhon";
		String token = "817683156228784128-3fY42Dh0JsWZ7W6V17zMLEuGUYelDPH";
		String secret = "OncIF3NtOFTyRP0j5X8iI1Z10IMbG4VF1vLU6VycNn7qO";
		Authentication auth = new OAuth1(consumerKey, consumerSecret, token, secret);

		TwitterScanner scanner = new TwitterScanner(companyName, auth);
		scanner.run();
	}
}
