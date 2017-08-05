/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package indosis.atcommand.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.CharsetUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.util.Set;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;
import indosis.atcommand.SerialWrite;
import indosis.atcommand.util.GetProperties;

public class HttpAtCommandServerHandler extends SimpleChannelInboundHandler<Object> {

	private HttpRequest request;
	static Logger log = Logger.getLogger(HttpAtCommandServerHandler.class);
	/** Buffer that stores the response content */
	public final StringBuilder buf = new StringBuilder();
	static Enumeration portList;
	static CommPortIdentifier portId;
	static InputStream inputStream;
	static SerialPort serialPort;
	static OutputStream outputStream;
	public static int stsUssd = 0;
	public static String responseUssd = "";
	boolean stsStart = false;

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
		String sms="";
		String destNo="";
		if (msg instanceof HttpRequest) {
			HttpRequest request = this.request = (HttpRequest) msg;

			if (HttpUtil.is100ContinueExpected(request)) {
				send100Continue(ctx);
			}

			buf.setLength(0);
			// buf.append("WELCOME TO THE WILD WILD WEB SERVER\r\n");
			// buf.append("===================================\r\n");
			//
			// buf.append("VERSION:
			// ").append(request.protocolVersion()).append("\r\n");
			// buf.append("HOSTNAME:
			// ").append(request.headers().get(HttpHeaderNames.HOST,
			// "unknown")).append("\r\n");
			// buf.append("REQUEST_URI:
			// ").append(request.uri()).append("\r\n\r\n");

			// HttpHeaders headers = request.headers();
			// if (!headers.isEmpty()) {
			// for (Map.Entry<String, String> h : headers) {
			// CharSequence key = h.getKey();
			// CharSequence value = h.getValue();
			// buf.append("HEADER: ").append(key).append(" =
			// ").append(value).append("\r\n");
			// }
			// buf.append("\r\n");
			// }

			QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
			Map<String, List<String>> params = queryStringDecoder.parameters();
			if (!params.isEmpty() && request.getUri().length() > 5) {
				String req = request.getUri().substring(1, 6);
				if (req.equalsIgnoreCase("reqat")) {
					log.info("Inside path reqat");
					for (Map.Entry<String, List<String>> p : params.entrySet()) {
						String key = p.getKey();
						List<String> vals = p.getValue();
						for (String val : vals) {
							if (key.equalsIgnoreCase("command")) {
								// buf.append(val);
								if (val.equalsIgnoreCase("start")) {
									if (!stsStart) {
										GetProperties prop = new GetProperties();
										String commport=prop.getCom();
										portList = CommPortIdentifier.getPortIdentifiers();
										while (portList.hasMoreElements()) {
											portId = (CommPortIdentifier) portList.nextElement();
											if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {

												if (portId.getName().equals(commport)) {
													log.info("Port "+commport+" Found,Starting Comm Connection");
													try {
														serialPort = (SerialPort) portId.open("SerialTestApp", 2000);

														SerialWrite wr = new SerialWrite(serialPort);
														outputStream = serialPort.getOutputStream();
														serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8,
																SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
														serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN
																| SerialPort.FLOWCONTROL_RTSCTS_OUT);
														serialPort.setRTS(true);
														log.info("Comm Connection Started");
													}

													catch (UnsupportedCommOperationException | IOException e) {
														buf.append("Something Wrong, Please Ask Developer");
														buf.append(e.getMessage());
														e.printStackTrace();

													} catch (PortInUseException e) {
														buf.append(" Error : Port already In used");
													}

												}
											}

										}
										stsStart = true;
										JSONObject obj = new JSONObject();

										obj.put("status", "0");
										obj.put("description", "Success");
										obj.put("response", "Starting Comm Connection Success");
										obj.put("yourRequest", val);
										buf.append(obj);
									} else {
										JSONObject obj = new JSONObject();

										obj.put("status", "1");
										obj.put("description", "Error");
										obj.put("response", "Com Connection Already Start");
										obj.put("yourRequest", val);
										buf.append(obj);

									}

								} else if (val.equalsIgnoreCase("stop")) {
									if (stsStart) {
										serialPort.close();
										stsStart=false;
										JSONObject obj = new JSONObject();

										obj.put("status", "1");
										obj.put("description", "sukses");
										obj.put("response", "Com Connection Stopped");
										obj.put("yourRequest", val);
										buf.append(obj);
									} else {
										JSONObject obj = new JSONObject();

										obj.put("status", "1");
										obj.put("description", "Error");
										obj.put("response", "Com Connection Already Stopped	");
										obj.put("yourRequest", val);
										buf.append(obj);
									}
								} else {
									log.info(val.substring(0, 2));
									String atCommand="";
									if(val.substring(0, 2).equalsIgnoreCase("at"))
									{
										atCommand=val;
									}else
									{
									atCommand = "at+cusd=1," + val;
									}
									responseUssd = "";
									sendCommand(atCommand);
									try {
										Thread.sleep(5000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									JSONObject obj = new JSONObject();

									String resp = responseUssd.substring(responseUssd.indexOf(":") + 1);
									log.info("Concat Resp"+resp);
									if (responseUssd.contains("+CUSD: 4")||responseUssd.contains("ERROR")) {
										obj.put("status", "1");
										obj.put("description", "Error");

										obj.put("response", "Please Try Again Later,");
										obj.put("yourRequest", val);

									} else {
										obj.put("status", "0");
										obj.put("description", "Success");

										obj.put("response", resp);
										obj.put("yourRequest", val);

									}

									buf.append(obj);

								}
							}else if(key.equalsIgnoreCase("sms")||key.equalsIgnoreCase("destNo"))
							{
								long startTime = System.currentTimeMillis();
								log.info(startTime);
								if(key.equalsIgnoreCase("sms"))
										{
											sms=val;	
										}
								if(key.equalsIgnoreCase("destNo"))
								{
									destNo=val;
								}
								if(!sms.equalsIgnoreCase("")&&!destNo.equalsIgnoreCase("")){
								String cmd = "at+cmgs="+"\""+destNo+"\"\r"+sms+"\u001A";
								
								sendCommand(cmd);
								
								JSONObject obj = new JSONObject();

								String resp = responseUssd.substring(responseUssd.indexOf(":") + 1);
								obj.put("status", "0");
								obj.put("description", "Success");

								obj.put("response", resp);
								obj.put("yourRequest", val);
								buf.append(obj);
								long endTime   = System.currentTimeMillis();
								long totalTime = endTime - startTime;
								log.info("Total Time Execution : "+totalTime);
								}
							}

						}
					}
				} else {
					buf.append("Invalid key");

				}
			}
			appendDecoderResult(buf, request);
		}

		if (msg instanceof HttpContent) {
			HttpContent httpContent = (HttpContent) msg;

			ByteBuf content = httpContent.content();
			if (content.isReadable()) {
				buf.append("CONTENT: ");
				buf.append(content.toString(CharsetUtil.UTF_8));
				buf.append("\r\n");
				appendDecoderResult(buf, request);
			}

			if (msg instanceof LastHttpContent) {
				// buf.append("END OF CONTENT\r\n");

				LastHttpContent trailer = (LastHttpContent) msg;
				if (!trailer.trailingHeaders().isEmpty()) {
					buf.append("\r\n");
					for (CharSequence name : trailer.trailingHeaders().names()) {
						for (CharSequence value : trailer.trailingHeaders().getAll(name)) {
							buf.append("TRAILING HEADER: ");
							buf.append(name).append(" = ").append(value).append("\r\n");
						}
					}
					buf.append("\r\n");
				}

				if (!writeResponse(trailer, ctx)) {
					// If keep-alive is off, close the connection once the
					// content is fully written.
					ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
				}
			}
		}
	}

	public static void sendCommand(String command) {
		log.info("Command Inserted : " + command);
		try {
			outputStream.write(command.getBytes());
			outputStream.write('\r');
			outputStream.flush();
			Thread.sleep(1000);

			// byte buffer[] = new byte[1000];
			// inputStream.read(buffer);
			// String s = new String(buffer);
			// System.out.println("Text Decryted : " + s);
			// String text = s.replace("\n", "").replace("\r", "");
			//
			// System.out.println(text);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void appendDecoderResult(StringBuilder buf, HttpObject o) {
		DecoderResult result = o.decoderResult();
		if (result.isSuccess()) {
			return;
		}

		buf.append(".. WITH DECODER FAILURE: ");
		buf.append(result.cause());
		buf.append("\r\n");
	}

	private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
		// Decide whether to close the connection or not.
		boolean keepAlive = HttpUtil.isKeepAlive(request);
		// Build the response object.
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
				currentObj.decoderResult().isSuccess() ? OK : BAD_REQUEST,
				Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));

		response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

		if (keepAlive) {
			// Add 'Content-Length' header only for a keep-alive connection.
			response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
			// Add keep alive header as per:
			// -
			// http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
			response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
		}

		// Encode the cookie.
		String cookieString = request.headers().get(HttpHeaderNames.COOKIE);
		if (cookieString != null) {
			Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieString);
			if (!cookies.isEmpty()) {
				// Reset the cookies if necessary.
				for (Cookie cookie : cookies) {
					response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
				}
			}
		} else {
			// Browser sent no cookie. Add some.
			response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode("key1", "value1"));
			response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode("key2", "value2"));
		}

		// Write the response.
		ctx.write(response);

		return keepAlive;
	}

	private static void send100Continue(ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
		ctx.write(response);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
