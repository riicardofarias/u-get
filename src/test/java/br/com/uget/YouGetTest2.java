package br.com.uget;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import br.com.uget.info.DownloadInfo;
import br.com.uget.info.VideoInfo;
import br.com.uget.listeners.AsyncConvertListener;
import br.com.uget.listeners.AsyncDownloadListener;
import br.com.uget.listeners.AsyncExtractListener;
import br.com.uget.tasks.Converter;
import br.com.uget.utils.YouGetUtils;


public class YouGetTest2 {
	private static String source;
	public static void main(String[] args) throws MalformedURLException {
		/*
		List<String> videos = getPlaylist("https://www.youtube.com/playlist?list=PLu3m-CUa7JQyjVA791h9L-totDbYKENQM");
		
		YouGet yGet = new YouGet(new File("D:\\YouGetMusic\\" + source));
		
		for (String url : videos) {
			yGet.addURL(url);
		}
		*/
		
		YouGet yGet = new YouGet(new File("D:\\"));
		yGet.addURL("https://www.youtube.com/watch?v=bK74s9B3aBc");
		
		yGet.addExtractListener(new ExtractListener());
		yGet.addDownloadListener(new DownloadListener());
		
		yGet.download();
	}
	
	static class ConvertListener implements AsyncConvertListener{

		@Override
		public void onConverting(File video, float progress) {
			System.out.printf("Convertendo: %s - %.2f%%\n", video.getName(), progress);
		}

		@Override
		public void onComplete(File mp3) {
			System.out.println("Conversão completa: " + mp3);
		}

		@Override
		public void onError(Throwable t) {
			System.err.println("Erro: " + t);
		}
	}
	
	static class DownloadListener implements AsyncDownloadListener{

		@Override
		public void onDownloading(VideoInfo info, float progress) {
			System.out.printf("Baixando: %s - %.2f%%\n", info.getTitle(), progress);
		}

		@Override
		public void onComplete(DownloadInfo downloadInfo) {
			System.out.println("Baixou: " + downloadInfo.getVideoFile());
			Converter c = new Converter(downloadInfo);
			c.addConvertListener(new ConvertListener());
			c.convert();
		}

		@Override
		public void onError(Throwable t) {
			System.err.println("Erro: " + t);
		}
	}
	
	static class ExtractListener implements AsyncExtractListener{

		@Override
		public void onExtracting(URL url) {
			System.out.println("Extraindo: " + url);
		}

		@Override
		public void onError(Throwable t) {
			System.err.println("Erro: " + t);
		}

		@Override
		public void onComplete(VideoInfo info) {
			System.out.println("Extração completa: " + info.getTitle());
		}
	}
	
	public static List<String> getPlaylist(String url){
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
			
			source = (String) jData.get("title");
			
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
}
