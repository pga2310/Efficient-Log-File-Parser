# Efficient-Log-File-Parser
 - A Java parser that parses web server access log file, loads the log to MySQL and checks if a given IP makes more than a certain number of requests for the given duration.

	The program takes about 2 Seconds  to complete the process.
 
 ## Java program can be run from command line
	
    java -cp "parser.jar" com.ef.Parser --accesslog=/path/to/file --startDate=2017-01-01.13:00:00 --duration=hourly --threshold=100 
    
    I have used:
    java -cp "parser.jar" com.ef.Parser --accesslog=C:/users/pushpak/downloads/java_mysql_test/access.log --startDate=2017-01-01.13:00:00 --duration=hourly --threshold=100
    
## Source Code for the Java program

  src/com/ef/Parser.java
  src/com/ef/Backend.java
  
## MySQL schema 

### blocked_ip
	ip
	comments
	id(PK)
### log_data
	date
	ip
	request
	status
	user_agent
                    
                     
## SQL queries for SQL test

(1) Write MySQL query to find IPs that mode more than a certain number of requests for a given time period.

Ex: Write SQL to find IPs that made more than 100 requests starting from 2017-01-01.13:00:00 to 2017-01-01.14:00:00.

-------------------------------------------

	SELECT  d.ip
	FROM log_data d
	WHERE d.date 
	BETWEEN '2017-01-01 13:00:00' AND '2017-01-01 14:00:00'
	GROUP BY ip
	HAVING COUNT(ip) > 100;

-----------------------------------------


(2) Write MySQL query to find requests made by a given IP.

-----------------------------------------

	SELECT d.request AS Requests
	FROM log_data d
	WHERE d.ip = '192.168.77.101';

----------------------------------------
