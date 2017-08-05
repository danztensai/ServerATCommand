package indosis.atcommand.util;

import java.util.*;
import java.io.*;

public class GetProperties
{
	String port;
	String com;
	String url;
	
	
    public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getCom() {
		return com;
	}

	public void setCom(String com) {
		this.com = com;
	}

	public GetProperties() {
         Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("conf/config.cfg"));
            this.setCom(prop.getProperty("com"));
            this.setPort(prop.getProperty("port"));
            this.setUrl(prop.getProperty("urlSend"));
            
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e2) {
            e2.printStackTrace();
        }
    }
    

}
