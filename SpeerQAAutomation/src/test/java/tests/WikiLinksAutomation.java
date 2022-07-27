package tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.opencsv.CSVWriter;

public class WikiLinksAutomation {
	WebDriver driver;
	Properties prop;
	String baseurl;
	int n;
	Set<String> visitedURL;
	String outputfile;
	String outputCSVfilepath;

	//Function to read configurations file 
	public void readConfigFile() throws IOException {

		// Read configuration file for Login
		prop = new Properties();
		FileInputStream stream = new FileInputStream("config.properties");
		prop.load(stream);
		baseurl = prop.getProperty("url");
		n = Integer.parseInt(prop.getProperty("n"));
		outputfile = prop.getProperty("outfilename");

	}

	@BeforeTest
	public void setupTest() throws IOException {
		System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "//Drivers//chromedriver.exe");
		readConfigFile();
		visitedURL = new HashSet<String>();
		//Get timestamp and use for creation of csv filename
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		outputCSVfilepath = System.getProperty("user.dir") + "//"+timestamp.getTime()+outputfile;

	}

	@BeforeMethod
	public void invokeBrowser() {
		driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.manage().deleteAllCookies();
		driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	}

	@Test
	public void validateWikiLinks() throws Exception {
		// Check for valid link
		try {
			// Open URL
			driver.get(baseurl);
			System.out.println("Base URL:" + baseurl);
		} catch (Exception e) {
			System.out.println("URL not valid - Exception thrown");
			throw e;
		}

		//Get all links with anchor tag and store in List of WebElements
		List<WebElement> links = driver.findElements(By.tagName("a"));
		System.out.println("Total links on the Wb Page: " + links.size());
		//Iterator to travserse the links List
		Iterator<WebElement> iterator = links.iterator();
		// Use Set to get unique links
		Set<String> urls = new HashSet<String>();
		while (iterator.hasNext()) {
			String url = iterator.next().getAttribute("href");
			//Add urls in the Set
			urls.add(url);
		}
		
		//Convert Set to List to get ordered elements
		List <String> data = convertSetToList(urls);
		
		//Add total number of links found in the opened url
		data.add(String.valueOf(links.size()));
		
		//Add total number of unique links in the opened url
		data.add(String.valueOf(urls.size()));
		
		//Write to csv file
		writeCSVfile(data);
		
		//Use operations n times 
		for (int i = 1; i < n; i++) {
			// Traverse through collected urls
			Iterator<String> newData = urls.iterator();
			System.out.println("i=" + i);
			if (newData.hasNext()) {
				// Get 1st URL from the set
				String url = newData.next();
				System.out.println("New URL to open: " + url);
				// Open new url
				driver.get(url);
				// Add into the VisitedURLs list
				 visitedURL.add(url);
				// Empty set to reuse for another new url embedded links
				urls.removeAll(urls);
				// find links in new page
				List<WebElement> newlinks = driver.findElements(By.tagName("a"));
				System.out.println("Total links on the Wb Page: " + newlinks.size());
				
				//Traverse through collected links in List
				Iterator<WebElement> newiterator = newlinks.iterator();
				
				while (newiterator.hasNext()) {
					String newurl = newiterator.next().getAttribute("href");
					//Check for url value as not null , not empty and not visited before same URL
					if (newurl != null && (!newurl.isEmpty()) && !checkVisitedLinks(newurl)) {
						//Add url to the set
						urls.add(newurl);
					}
				}
				
				//Convert Set to List to get ordered elements
				List <String> data1 = convertSetToList(urls);
				
				//Add total count of links found in the opened url
				data1.add(String.valueOf(newlinks.size()));
				
				//Add total unique number of links found in the opened url
				data1.add(String.valueOf(urls.size()));
				
				//Write to csv file
				writeCSVfile(data1);

			} else {
				//Condition if any link is not containing embedded links
				System.out.println("URL doesn't contain any embedded links ");
				break;
			}

		}

	}
	
	
    // function to convert set to list
    public <String> List<String> convertSetToList(Set<String> set)
    {
        // create a list from Set
        List<String> list = new ArrayList<String>(set);
  
        // return the list
        return list;
    }
  
    // Write CSV file
	public void writeCSVfile(List <String> listdata)
	{

		// Create File type object
		File file = new File(outputCSVfilepath);
		try {
			// create FileWriter object with file as parameter
			FileWriter outputfile = new FileWriter(file,true);

			// create CSVWriter object filewriter object as parameter
			CSVWriter writer = new CSVWriter(outputfile);

			// Transform List to String Array
			String[] data = new String[listdata.size()];
			 
		        for (int i = 0; i < listdata.size(); i++) {
		        	data[i] = listdata.get(i);
		        }
		    //Write Data
			writer.writeNext(data);

			// closing writer connection
			writer.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	// Function to check for visited links 
	public boolean checkVisitedLinks(String link) {
		boolean status = visitedURL.contains(link);
		if (status == true) {
		System.out.println("Site already visited URL:"+link);
		}
		return status;
	}

	@AfterMethod
	public void TearDown() {
		driver.quit();
	}

}
