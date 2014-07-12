package br.com.uget.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import br.com.uget.exceptions.DownloadErrorException;
import br.com.uget.exceptions.ExtractException;
import br.com.uget.info.VideoInfo;
import br.com.uget.info.VideoInfo.Quality;
import br.com.uget.utils.YouGetUtils;

public class YoutubeParser extends Parser{
	private URL source;
	private List<VideoDownload> videoDownloads;

	public YoutubeParser() {
		videoDownloads = new ArrayList<>();
	}

	public YoutubeParser(URL source) {
		this();
		this.source = source;
	}

	@Override
	public List<VideoDownload> extract(VideoInfo info) {
		try {
			return extractInfo(info);
		} catch (ExtractException e) {
			throw new ExtractException(e);
		}catch (ClientProtocolException e) {
			throw new DownloadErrorException(e);
		} catch (IOException e) {
			throw new DownloadErrorException(e);
		}catch (RuntimeException e) {
			throw new RuntimeException(e);
		}
	}

	private List<VideoDownload> extractInfo(VideoInfo info) throws ClientProtocolException, IOException{
		HttpClient cliente = HttpClientBuilder.create().build();
		String id = extractId(source);

		if(id == null)
			throw new RuntimeException("unknown url");

		String get = "http://www.youtube.com/get_video_info?el=adunit&asv=3&ad_format=1_2_1&video_id=" + id;
		
		
		info.setTitle(source.toString()); // video title
		info.setVideoId(id);              // video id
		
		HttpGet httpGet = new HttpGet(get);
		httpGet.addHeader("User-Agent", YouGetUtils.USER_AGENT);
		
		HttpResponse resp = cliente.execute(httpGet);
		HttpEntity entity = resp.getEntity();

		int statusCode = resp.getStatusLine().getStatusCode();

		if(entity != null && statusCode == YouGetUtils.STATUS_OK){
			InputStream input = entity.getContent();
			String data = YouGetUtils.getString(YouGetUtils.ENCODING, input);

			Map<String, String> map = getQueryMap(data);
			
			if(map.get("status").equals("fail")){
				String reason = URLDecoder.decode(map.get("reason"), YouGetUtils.ENCODING);
				throw new ExtractException("fail on extract info: " + reason + " Video: " + id);
			}
			
			info.setTitle(URLDecoder.decode(map.get("title"), YouGetUtils.ENCODING));

			String url_encoded_fmt_stream_map = URLDecoder.decode(map.get("url_encoded_fmt_stream_map"), YouGetUtils.ENCODING);	
			
			extractFormEncoded(url_encoded_fmt_stream_map);
			
			
		}
		return videoDownloads;
	}

	/**
	 * Extrai as informa��es codificadas da URL
	 * @param encoded
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException 
	 */
	public void extractFormEncoded(String encoded) throws UnsupportedEncodingException{
		String[] urlStrings = encoded.split("url=");

		for (String s : urlStrings) {
			String urlString = s;
			String urlFull = URLDecoder.decode(urlString, YouGetUtils.ENCODING);

			String url = null;
			
			{
				url = getUrl(urlString);
			}

			String itag = null;
			{
				itag = getItag(urlFull);
			}

			if(url != null && itag != null){
				try{
					int tag = Integer.parseInt(itag); 				// Parse iTag to int	
					addVideo(tag, new URL(url));                    // Add video to download
					continue;
				}catch(MalformedURLException e){}
			}
		}
	}

	public HashMap<Integer, Quality> iTagMap = new HashMap<Integer, Quality>(){
		private static final long serialVersionUID = 1L;
		{
			put(17,  Quality.p144);
			put(36,  Quality.p240);
			put(5,   Quality.p240);
			put(83,  Quality.p240);
			put(6,   Quality.p270);
			put(18,  Quality.p360);
			put(34,  Quality.p360);
			put(43,  Quality.p360);
            put(101, Quality.p360);
            put(100, Quality.p360);
            put(82,  Quality.p360);
            put(35,  Quality.p480);
			put(44,  Quality.p480);
			put(120, Quality.p720);
			put(22,  Quality.p720);
			put(45,  Quality.p720);
            put(102, Quality.p720);
            put(84,  Quality.p720);
			put(37,  Quality.p1080);
			put(46,  Quality.p1080);
			put(38,  Quality.p3072);
		}
	};

	public static Map<String, String> getQueryMap(String qs) {
		try {
			qs = qs.trim();
			List<NameValuePair> list;
			list = URLEncodedUtils.parse(new URI(null, null, null, -1, null, qs, null), YouGetUtils.ENCODING);
			HashMap<String, String> map = new HashMap<String, String>();
			for (NameValuePair p : list) {
				map.put(p.getName(), p.getValue());
			}
			return map;
		} catch (URISyntaxException e) {
			throw new RuntimeException(qs, e);
		}
	}

	public List<String> getPlaylist(String url){
		List<String> _mLinks = new ArrayList<String>();

		try {
			HttpGet httpGet = new HttpGet(YouGetUtils.getPlayListFromURL(url));
			httpGet.addHeader("User-Agent", YouGetUtils.USER_AGENT);

			HttpClient cliente = HttpClientBuilder.create().build();
			HttpResponse resp = cliente.execute(httpGet);
			HttpEntity entity = resp.getEntity();
			InputStream input = entity.getContent();

			String data = YouGetUtils.getString("UTF-8", input);
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(data);

			JSONObject jObj = (JSONObject) obj;
			JSONObject jData = (JSONObject) jObj.get("data");
			JSONArray items = (JSONArray) jData.get("items");

			for (Object ob : items) {
				JSONObject obItem = (JSONObject) ob;
				JSONObject item = (JSONObject) obItem.get("video");
				String _url = YouGetUtils.getUrlFromID(item.get("id").toString());
				_mLinks.add(_url);
			}
		} catch (Exception e) {
			System.err.println("Erro: " + e.getMessage());
		}

		return _mLinks;
	}

	private String extractId(URL url){

		Pattern p = Pattern.compile("v=([^&]*)");
		Matcher m = p.matcher(url.toString());

		if(m.find()){
			return m.group(1);
		}

		return null;
	}

	private String getUrl(String urlString) throws UnsupportedEncodingException{
		String url = null;
		Pattern link = Pattern.compile("([^&,]*)[&,]");
		Matcher linkMatch = link.matcher(urlString);
		if (linkMatch.find()) {
			url = linkMatch.group(1);
			url = URLDecoder.decode(url, YouGetUtils.ENCODING);
		}

		return url;
	}

	private String getItag(String urlFull){
		String itag = null;
		Pattern link = Pattern.compile("itag=(\\d+)");
		Matcher linkMatch = link.matcher(urlFull);
		if (linkMatch.find()) {
			itag = linkMatch.group(1);
		}
		return itag;
	}
	
	private void addVideo(int tag, URL url) throws MalformedURLException{
		Quality q = iTagMap.get(tag);
		
		if(q != null)
			videoDownloads.add(new VideoDownload(q, url));
	}
}
