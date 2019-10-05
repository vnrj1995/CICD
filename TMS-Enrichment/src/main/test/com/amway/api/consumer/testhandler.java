package com.amway.api.consumer;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;

public class testhandler {
	
	File file = new File("C:\\Users\\675381\\Desktop\\InputStream-ANZ\\getinputstream.txt");

	 FileInputStream fis = null;
	 
	    Context ctx = createContext();
	  private Context createContext() {
	        TestContext ctx = new TestContext();

	        // customize your context here if needed.
	        ctx.setFunctionName("ApacIdnDevHybrisOrderConsumer");

	        return ctx;
	    }
	@Test
	public void testHandleRequest() {
		   Scanner scanner = null;
		try {
			scanner = new Scanner( new File("C:\\Users\\675381\\Desktop\\InputStream-ANZ\\postoutputstream.txt") );
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		    String text = scanner.useDelimiter("\\A").next();
		    scanner.close(); 
		    
//		    String str = "";
		    //ReturnHistorySearch handler=new ReturnHistorySearch();
//			int content;
//			while ((content = fis.read()) != -1) {
//	            // convert to char and display it
//	            str += (char) content;
//	        }
		    String retVal;
		    String Str1 = new String("This is really not immutable!!");
		    String Str4 = new String("91");
		    retVal = Str4.toLowerCase();
		    System.out.println("Returned Value = " + retVal );
		    
			 InputStream input = new ByteArrayInputStream(text.getBytes());
		     OutputStream output = new ByteArrayOutputStream();
//		     try {
//				//handler.handleRequest(input, output, ctx);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
	}

}
