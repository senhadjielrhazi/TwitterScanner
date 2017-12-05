package com.fractallabs.assignment;

import java.lang.reflect.Method;

import com.fractallabs.assignment.TwitterScanner.TSValue;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

public class TwitterScannerTest {

	private static Method getStoreValueMethod() throws NoSuchMethodException {
        Method sValueMethod = TwitterScanner.class.getDeclaredMethod("storeValue", TSValue.class);
        sValueMethod.setAccessible(true);
        return sValueMethod;
    }
	
	/**
     * Tests the method StoreValue
     */
    public void testStoreValueZeroZero() throws Exception {
    	TSValue value = new TSValue(System.currentTimeMillis(), 0.5);
        getStoreValueMethod().invoke(value);
    }

    /**
     * Tests the null Auth
     */
    public void testTwitterAuthNull() throws Exception {
        new TwitterScanner("Facebook", null);
    }

    /**
     * Tests the null Company Name
     */
    public void testTwitterCPNull() throws Exception {
        new TwitterScanner(null, new OAuth1("", "", "", ""));
    }
    
    /**
     * Tests the empty Company Name
     */
    public void testTwitterCPEmpty() throws Exception {
        new TwitterScanner("", new OAuth1("", "", "", ""));
    }
    
    /**
     * Tests a valid inputs
     */
    public void testTwitterValidInput() throws Exception {
        String companyName = "Facebook";
        String consumerKey = "ju0sJx5aSTBdwLYj8ESiBOmvN";
		String consumerSecret = "yLBx15INmkB0yxpN22GBHJs5dZFyJ8WHmegtGdMqzhbFFivhon";
		String token = "817683156228784128-3fY42Dh0JsWZ7W6V17zMLEuGUYelDPH";
		String secret = "OncIF3NtOFTyRP0j5X8iI1Z10IMbG4VF1vLU6VycNn7qO";
		Authentication auth = new OAuth1(consumerKey, consumerSecret, token, secret);
		
		@SuppressWarnings("unused")
		TwitterScanner scanner = new TwitterScanner(companyName, auth);
    }
}
