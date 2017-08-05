package indosis.atcommand;

import gnu.io.*;
import indosis.atcommand.server.HttpAtCommandServerHandler;
import indosis.atcommand.util.GetProperties;
import indosis.atcommand.util.WebUtil;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import java.lang.*;
import java.nio.charset.Charset;

public class SerialWrite implements Runnable, SerialPortEventListener {

	static String output = "";
	Logger log = Logger.getLogger(SerialWrite.class);
	public void run() {
	}

	static Enumeration portList;
	static CommPortIdentifier portId;
	static InputStream inputStream;
	static SerialPort serialPort;
	static OutputStream outputStream;

	public void serialEvent(SerialPortEvent event) {
		// System.out.println("Eventnya :"+event.getEventType());
		switch (event.getEventType()) {
		case SerialPortEvent.BI:
		case SerialPortEvent.OE:
		case SerialPortEvent.FE:
		case SerialPortEvent.PE:
		case SerialPortEvent.CD:
		case SerialPortEvent.CTS:
		case SerialPortEvent.DSR:
		case SerialPortEvent.RI:
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			log.error("Error");
			break;
		case SerialPortEvent.DATA_AVAILABLE: {
	
			int available;
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try {
				available = inputStream.available();
				byte[] chunk = new byte[available];
				inputStream.read(chunk, 0, available);
				String st = new String(chunk);
				HttpAtCommandServerHandler.stsUssd=1;
				HttpAtCommandServerHandler.responseUssd=HttpAtCommandServerHandler.responseUssd+st;
				log.info("Got Something From Modem : "+st);
				//log.info("Got Sommething From Modem :"+st);
				if(st.contains("+CMTI:"))
				{
					log.info("Got SMS");
					String indexSMS = st.substring(st.indexOf(",")+1);
					log.info(indexSMS);
					HttpAtCommandServerHandler.sendCommand("at+CMGR="+indexSMS);
				}
				if(st.contains("+CMGR"))
				{
					String concat=st.substring(st.indexOf(":")+1);
					log.info(concat);
					String [] concatMsg = concat.split(",",3);
					for (String string : concatMsg) {
						log.info("arrayConcatMSG :"+string);
					}
					String sms = concat.substring(concat.indexOf("\n")+1);
					sms=sms.replaceAll("OK", "");
					sms=sms.replaceAll("\n", "");
					String sender=concatMsg[1].replaceAll("\"", "");
					log.info("From : "+sender+" SMS : "+sms);
					String param = WebUtil.createURLSend(sms, sender);
					log.info("Param : "+param);
					GetProperties getProp= new GetProperties();
					String responseFromServer=WebUtil.getDataFromWeb(getProp.getUrl()+param);
					log.info("Response From url"+responseFromServer);
//					String paramSendSMS = WebUtil.createURLSendSMS("Ini SMS yang dari maneh "+sms,sender);
//					String ResponseFromSMSGtw = WebUtil.getDataFromWeb("http://localhost/smsTest/sendSMS.php?"+param);
//					log.info("Param SMS Gtw + URL : http://localhost/smsTest/sendSMS.php?"+paramSendSMS);
//					log.info("Response From SMSGTW : "+ResponseFromSMSGtw);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;
		}
		} // switch
	}

	public SerialWrite(SerialPort serial) {
		try {
			inputStream = serial.getInputStream();
			try {
				serial.addEventListener(this);
			} catch (TooManyListenersException e) {
				System.out.println("Exception in Adding Listener" + e);
			}
			serial.notifyOnDataAvailable(true);
		} catch (Exception ex) {
			System.out.println("Exception in getting InputStream" + ex);
		}
	}

	public static void sendCommand(String command) {
		System.out.println("Command Inserted : " + command);
		try {
			outputStream.write(command.getBytes());
			outputStream.write('\r');
			outputStream.flush();
			Thread.sleep(1000);

			byte buffer[] = new byte[1000];			
			inputStream.read(buffer);
			String s = new String(buffer);
//			System.out.println("Text Decryted : " + s);
//			String text = s.replace("\n", "").replace("\r", "");
//
//			System.out.println(text);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws Exception {
		int i = 0;
		String line1 = "AT+CSQ\r\n";
		String line2 = "at+cusd=1, *888#\r\n";
		String line3 = "at+cusd=1, *555#\r\n";
		String line4 = "at+cusd=1, *363#\r\n";
		String line5 = "at+cusd=1,*123#\r\n";
		String line6 = "at+cusd=1,*889#\r\n";
		String line7 = "at+cmti:\"sm\"";
		portList = CommPortIdentifier.getPortIdentifiers();
		while (portList.hasMoreElements()) {
			portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {

				if (portId.getName().equals("COM5")) {
					System.out.println("At Sending....Port Found");
					try {
						serialPort = (SerialPort) portId.open("SerialTestApp", 2000);

						SerialWrite wr = new SerialWrite(serialPort);
					} catch (PortInUseException e) {
						System.out.println("Port In Use " + e);
					}
					try {
						outputStream = serialPort.getOutputStream();
					} catch (IOException e) {
						System.out.println("Error writing to output stream " + e);
						e.printStackTrace();
					}
					try {
						serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
								SerialPort.PARITY_NONE);
						serialPort.setFlowControlMode(
								SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
						serialPort.setRTS(true);
					} catch (UnsupportedCommOperationException e) {
						System.out.println("Error");
					}
					try {
						System.out.println("1st Command");
						// outputStream.write(line5.getBytes());
						// outputStream.write('\r');
						// outputStream.flush();
						// Thread.sleep(1000);
						//
						// byte buffer[] = new byte[1000];
						// // read the response from mobile phone
						// System.out.println("inputStream Available :
						// "+inputStream.available());
						// System.out.println(inputStream.read());
						// inputStream.read(buffer);
						//
						//
						// System.out.println("AT Comand response:
						// "+buffer.toString());
						//
						// System.out.println("Text [Byte Format] : " + buffer);
						// System.out.println("Text [Byte Format] : " +
						// buffer.toString());
						//
						// String s = new String(buffer);
						//
						// System.out.println("Text Decryted : " + s);
						// String text = s.replace("\n", "").replace("\r", "");
						//
						// System.out.println(text );

						BufferedReader readerInput = new BufferedReader(new InputStreamReader(System.in));
						while (true) {
							System.out.println("Please Insert At-Command : type quit to quit");
							String atCommand = readerInput.readLine();
							if (atCommand.equals("quit")) {
								System.out.println("Quiting Apps");
								System.exit(-1);
							} else {
								sendCommand(atCommand);
							}
						}

					} catch (IOException e) {
						System.out.println("Error writing message " + e);
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static String slurp(final InputStream is, final int bufferSize) {
		final char[] buffer = new char[bufferSize];
		final StringBuilder out = new StringBuilder();
		try (Reader in = new InputStreamReader(is, "UTF-8")) {
			for (;;) {
				int rsz = in.read(buffer, 0, buffer.length);
				if (rsz < 0)
					break;
				out.append(buffer, 0, rsz);
			}
		} catch (UnsupportedEncodingException ex) {
			/* ... */
		} catch (IOException ex) {
			/* ... */
		}
		return out.toString();
	}

	public static void showText(String Text) {
		System.out.println("TEXT " + Text);
	}
}