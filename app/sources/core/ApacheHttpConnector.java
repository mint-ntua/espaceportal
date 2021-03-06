package sources.core;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import play.Logger;
import play.Logger.ALogger;

public class ApacheHttpConnector extends HttpConnector {
	public static final ALogger log = Logger.of( ApacheHttpConnector.class );
	
	private static ApacheHttpConnector instance;
	
	public static HttpConnector getApacheHttpConnector(){
		if (instance==null){
			instance = new ApacheHttpConnector();
		}
		return instance;
	}

	private HttpResponse getContentResponse(HttpUriRequest method) throws IOException, ClientProtocolException {
		URI url = method.getURI();
		try {
			log.debug("calling: " + url);
			long time = System.currentTimeMillis();
			HttpClient client = HttpClientBuilder.create().build();
			HttpResponse response = client.execute(method);
			long ftime = (System.currentTimeMillis() - time) / 1000;
			log.debug("waited " + ftime + " sec for: " + url);
			return response;
		} catch (Exception e) {
			log.error("calling: " + url);
			log.error("msg: " + e.getMessage());
			throw e;
		}
	}

	private JsonNode getJsonContentResponse(HttpUriRequest method) throws IOException, ClientProtocolException {
		HttpResponse response = getContentResponse(method);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
		ObjectMapper mapper = new ObjectMapper();
		JsonNode readValue = mapper.readValue(rd, JsonNode.class);
//		log.debug("Response: " + readValue);
		
		return readValue;
	}

	public <T> T getContent(String url) throws Exception {
		log.debug("1 calling: " + url);
		JsonNode response = getJsonContentResponse(buildGet(url));
		return (T) response;
	}

	private HttpGet buildGet(String url) throws URISyntaxException {
		return new HttpGet(url);
	}

//	public File getContentAsFile(String url) throws Exception {
//		HttpResponse response = getContentResponse(buildGet(url));
//		File tmp = new File("temp");
//		response.getEntity().writeTo(new FileOutputStream(tmp));
//		return tmp;
//	}
//
//	public <T> T postFileContent(String url, File file, String paramName, String paramValue) throws Exception {
//		String url1 = Utils.replaceQuotes(url);
//		HttpClient client = HttpClientBuilder.create().build();
//		HttpPost method = new HttpPost(url1);
//		method.setHeader(paramName, paramValue);
//
//		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//		builder.addTextBody(paramName, paramValue);
//		builder.addBinaryBody("file", file, ContentType.APPLICATION_OCTET_STREAM, "file.ext");
//
//		method.setEntity(builder.build());
//		JsonNode res = getJsonContentResponse(method);
//		return (T) res;
//	}

}
