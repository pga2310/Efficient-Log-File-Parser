package com.ef;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap; 
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
 

public class Parser {

 
	private Date date1;
	private Date date2;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	   
	static List<String> list;
	static List<String> blocked = new ArrayList<>();
	static Map<String, Integer> map = new HashMap<>();

	// METHOD TO FIND THE IP ADDRESS THAT CAN BE BLOCKED	
	public void CalculateFirst(String startDateTime, String endDateTime, String duration,int threshold){ 
		
			// ITERATE THROUGH THE LIST (LOG FILE)
			list.forEach(list->{				
				try {
					
					// GET THE START DATE, END DATE AND THE DATE AT THE CURRENT POINTER
					this.date1 = sdf.parse(startDateTime);					
					this.date2 = sdf.parse(endDateTime);
					String[] log = list.toString().split("\\|"); 
					Date date3 = sdf.parse(log[0]); 
					
					// IF DATE AT CURRENT POINTER IS BETWEEN START DATE AND END DATE
						if(date3.compareTo(date1)>=0 && date3.compareTo(date2) <=0){
							
							// ADD THE IP ADDRESS TO MAP
							// IF IP ADDRESS ALREADY EXIST IN MAP
							// INCREAMENT THE VALUE COUNT
							Integer i = map.get(log[1]);
						       if (i ==  null) {
						           i = 0;
						       }
						       map.put(log[1], i + 1);
						}
						
				} catch (Exception e) { 
					e.printStackTrace();
				}	
					
			});
			
			// ASSUME THERE IS NO IP ADDRESS ABOVE THE THRESHOLD
			int i = 0;
				
				// ITERATE THROUGH THE MAP AND CHECK IF THE VALUE OF MAP ENTRY IS ABOVE THRESHOLD
				for (Entry<String, Integer> entry : map.entrySet()){
		            if (entry.getValue() >= threshold){
		            	
		            	// PRINT THE IP ADDRESS TO THE CONSOLE AND ADD TO THE BLOCKED IP ADDRESS LIST
		                System.out.println(entry.getKey()+" has made "+entry.getValue()+" requests "+
		                			"between "+sdf.format(date1)+" and "+sdf.format(date2));
		                blocked.add(entry.getKey()+" | Made more than "+threshold+" requests between "
		                		+sdf.format(date1)+" and "+sdf.format(date2));
		                i++;
		            }
				}
			if(i < 1)System.out.println("No ip count above Threshold...");
	}
    
	
	
	/**
	 * =============
	 * PARSER START EXECUTION FROM HERE
	 * =============
	 */
    public static void main(String[] args) throws IOException, InterruptedException { 
    	
    	//GET PATH, START DATE, DURATION, AND THRESHOLD FROM ARGUMENT
   
    	String path = args[0].matches("--accesslog=.*") ? args[0].split("=")[1] : null;
    	String startDateTime = args[1].matches("--startDate=.*") ? args[1].split("=")[1].replace(".", " ").concat(".000") : null;
    	String duration = args[2].matches("--duration=.*") ? args[2].split("=")[1] : null;
    	int threshold = Integer.parseInt(
    			args[3].matches("--threshold=.*") ? args[3].split("=")[1] : "-1"
    			);
    	String endDateTime = null;
    	
    	
    	//CHECK IF USER MADE A VALID ARGUMENT OR NOT
    	if(path==null || startDateTime == null || duration == null || threshold == -1){
    		System.out.println("INVALID ARGUMENT\nValid Argument: --accesslog=/path/to/file"+
    							" --startDate=2017-01-01.13:00:00 --duration=hourly --threshold=100");
    	}else{ 
    		
    		//SET THE END DATE IF THE ARGUMENTS ARE VALID
			if(duration.equals("hourly")){
				endDateTime = startDateTime + ((60*60*1000)-1);
			}if(duration.equals("daily")){
				endDateTime = startDateTime + ((24*60*60*1000)-1);
			}
			
			// USING JAVA 8 
			// FASTEST WAY TO READ A LARGE FILE
			// ADD LINES TO A LIST OF TYPE STRING
			list= (List<String>) Files.readAllLines(Paths.get(path));
					
			// USE MAP THROUGH THE LIST TO FIND THE IP THAT CAN BE BLOCKED
			new Parser().CalculateFirst(startDateTime, endDateTime, duration, threshold);
			

			// LOAD THE LOG FILE TO MySQL DATABASE USING A THREAD
			Thread dbThread = new Thread(){
				public void run(){
					try {
						new Backend().getDbOperation(list, path);
						
					} catch (FileNotFoundException e) { 
						e.printStackTrace();
					} catch (IOException e) { 
						e.printStackTrace();
					} catch (SQLException e) { 
						e.printStackTrace();
					}
				}
			};
				
			
			// ONCE WE HAVE THE LIST OF IPs TO BLOCK
			// RUN A THREAD TO ADD IPs IN BLOCK_IP TABLE
			Thread blockThread = new Thread(){
				public void run(){
					try {
						new Backend().getDbBlocked(blocked);
						
					} catch (FileNotFoundException e) { 
						e.printStackTrace();
					} catch (IOException e) { 
						e.printStackTrace();
					} catch (SQLException e) { 
						e.printStackTrace();
					}
				}
			};
			
			dbThread.start();
			blockThread.start();
			
			// PRINT RESULTS ON CONSOLE
			System.out.println("Database operation running...");
			dbThread.join();
			blockThread.join();
			System.out.println("--Complete");
    	}
    }
	
}
