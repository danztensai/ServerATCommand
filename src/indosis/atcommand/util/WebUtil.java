package indosis.atcommand.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;


public class WebUtil
{
    private static Logger log;
    
    static {
        WebUtil.log = Logger.getLogger((Class)WebUtil.class);
    }
    
    public static String createURLSend(String sms,String sender) {
        String urlReturn = "";
        try {
             StringBuilder data = new StringBuilder(String.valueOf(URLEncoder.encode("text", "UTF-8")) + "=" + URLEncoder.encode(sms, "UTF-8"));
            data.append("&" + URLEncoder.encode("sender", "UTF-8") + "=" + URLEncoder.encode(sender, "UTF-8"));
          
            urlReturn = data.toString();
        }
        catch (UnsupportedEncodingException e) {
            WebUtil.log.error((Object)"error", (Throwable)e);
        }
        catch (NullPointerException e2) {
            WebUtil.log.error((Object)"error", (Throwable)e2);
        }
        WebUtil.log.info((Object)urlReturn);
        return urlReturn;
    }

    public static String createURLSendSMS(String sms,String sender) {
        String urlReturn = "";
        try {
             StringBuilder data = new StringBuilder(String.valueOf(URLEncoder.encode("sms", "UTF-8")) + "=" + URLEncoder.encode(sms, "UTF-8"));
            data.append("&" + URLEncoder.encode("destNo", "UTF-8") + "=" + URLEncoder.encode(sender, "UTF-8"));
          
            urlReturn = data.toString();
        }
        catch (UnsupportedEncodingException e) {
            WebUtil.log.error((Object)"error", (Throwable)e);
        }
        catch (NullPointerException e2) {
            WebUtil.log.error((Object)"error", (Throwable)e2);
        }
        WebUtil.log.info((Object)urlReturn);
        return urlReturn;
    }
    public static String getDataFromWeb( String urlDT) {
        try {
            WebUtil.log.info((Object)urlDT);
             URL url = new URL(urlDT);
             URLConnection uconn = url.openConnection();
            if (!(uconn instanceof HttpURLConnection)) {
                throw new IllegalArgumentException("URL protocol must be HTTP.");
            }
             HttpURLConnection conn = (HttpURLConnection)uconn;
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-agent", "CRMZilla");
             BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
             StringBuilder strRet = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                strRet.append(inputLine);
            }
            in.close();
            conn.disconnect();
            return strRet.toString();
        }
        catch (IOException e) {
            e.printStackTrace();
            WebUtil.log.error((Object)e, (Throwable)e);
            return null;
        }
    }
    public static void main(String []args)
	{
		//httpsCon("https://www.tanyadok.com/mhealth/shorten.php?url=https://www.tanyadok.com/artikel-kesehatan/exercise-for-better-heart/2m=6281319292741%26o=");
    	System.out.println(getDataFromWeb("http://localhost:8080/reqat?sms=Ini+SMS+yang+dari+maneh+BMT+081394098892+SN5+7777&destNo=%2B6289655394054"));
	}
    public static void httpsCon(String url)
    {
    	try{
    		
//    		String certified="-----BEGIN CERTIFICATE-----\n"+
//"MIICiDCCAi6gAwIBAgIUXZP3MWb8MKwBE1Qbawsp1sfA/Y4wCgYIKoZIzj0EAwIw"+
//"gY8xCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1T"+
//"YW4gRnJhbmNpc2NvMRkwFwYDVQQKExBDbG91ZEZsYXJlLCBJbmMuMTgwNgYDVQQL"+
//"Ey9DbG91ZEZsYXJlIE9yaWdpbiBTU0wgRUNDIENlcnRpZmljYXRlIEF1dGhvcml0"+
//"eTAeFw0xNjAyMjIxODI0MDBaFw0yMTAyMjIwMDI0MDBaMIGPMQswCQYDVQQGEwJV"+
//"UzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNU2FuIEZyYW5jaXNjbzEZ"+
//"MBcGA1UEChMQQ2xvdWRGbGFyZSwgSW5jLjE4MDYGA1UECxMvQ2xvdWRGbGFyZSBP"+
//"cmlnaW4gU1NMIEVDQyBDZXJ0aWZpY2F0ZSBBdXRob3JpdHkwWTATBgcqhkjOPQIB"+
//"BggqhkjOPQMBBwNCAASR+sGALuaGshnUbcxKry+0LEXZ4NY6JUAtSeA6g87K3jaA"+
//"xpIg9G50PokpfWkhbarLfpcZu0UAoYy2su0EhN7wo2YwZDAOBgNVHQ8BAf8EBAMC"+
//"AQYwEgYDVR0TAQH/BAgwBgEB/wIBAjAdBgNVHQ4EFgQUhTBdOypw1O3VkmcH/es5"+
//"tBoOOKcwHwYDVR0jBBgwFoAUhTBdOypw1O3VkmcH/es5tBoOOKcwCgYIKoZIzj0E"+
//"AwIDSAAwRQIgEiIEHQr5UKma50D1WRMJBUSgjg24U8n8E2mfw/8UPz0CIQCr5V/e"+
//"mcifak4CQsr+DH4pn5SJD7JxtCG3YGswW8QZsw== \n"+
//"-----END CERTIFICATE-----";
//    		ByteArrayInputStream derInputStream = new ByteArrayInputStream(certified.getBytes());
//            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
//            X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(derInputStream);
//            String alias = "alias";//cert.getSubjectX500Principal().getName();
//
//            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
//            trustStore.load(null);
//            trustStore.setCertificateEntry(alias, cert);
//            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
//            kmf.init(trustStore, null);
//            KeyManager[] keyManagers = kmf.getKeyManagers();
//
//            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
//            tmf.init(trustStore);
//            TrustManager[] trustManagers = tmf.getTrustManagers();
//
//            SSLContext sslContext = SSLContext.getInstance("TLS");
//            sslContext.init(keyManagers, trustManagers, null);
    		//System.setProperty("https.protocols", "SSLv3");
    		//System.setProperty("javax.net.ssl.keyStorePassword", "C:/Users/Danz/Desktop/tanyadok.cer");
    		
    		
    		String httpsURL = url;
    	    URL myurl = new URL(httpsURL);
    	    HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
    	    
    	    InputStream ins = con.getInputStream();
    	    InputStreamReader isr = new InputStreamReader(ins);
    	    BufferedReader in = new BufferedReader(isr);

    	    String inputLine;

    	    while ((inputLine = in.readLine()) != null)
    	    {
    	      System.out.println(inputLine);
    	    }

    	    in.close();
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }
    
    public static void  testIt(String urlSend){

        String https_url = urlSend;
        URL url;
        try {

  	     url = new URL(https_url);
  	     HttpsURLConnection con = (HttpsURLConnection)url.openConnection();

  	     //dumpl all cert info
  	     print_https_cert(con);

  	     //dump all the content
  	     print_content(con);

        } catch (MalformedURLException e) {
  	     e.printStackTrace();
        } catch (IOException e) {
  	     e.printStackTrace();
        }

     }
    private static void print_https_cert(HttpsURLConnection con){

        if(con!=null){

          try {

    	System.out.println("Response Code : " + con.getResponseCode());
    	System.out.println("Cipher Suite : " + con.getCipherSuite());
    	System.out.println("\n");

    	Certificate[] certs = con.getServerCertificates();
    	for(Certificate cert : certs){
    	   System.out.println("Cert Type : " + cert.getType());
    	   System.out.println("Cert Hash Code : " + cert.hashCode());
    	   System.out.println("Cert Public Key Algorithm : "
                                        + cert.getPublicKey().getAlgorithm());
    	   System.out.println("Cert Public Key Format : "
                                        + cert.getPublicKey().getFormat());
    	   System.out.println("\n");
    	}

    	} catch (SSLPeerUnverifiedException e) {
    		e.printStackTrace();
    	} catch (IOException e){
    		e.printStackTrace();
    	}

         }

       }

       private static void print_content(HttpsURLConnection con){
    	if(con!=null){

    	try {

    	   System.out.println("****** Content of the URL ********");
    	   BufferedReader br =
    		new BufferedReader(
    			new InputStreamReader(con.getInputStream()));

    	   String input;

    	   while ((input = br.readLine()) != null){
    	      System.out.println(input);
    	   }
    	   br.close();

    	} catch (IOException e) {
    	   e.printStackTrace();
    	}

           }

       }
}
