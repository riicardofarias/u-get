package br.com.uget;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.com.uget.exceptions.ExtractException;
import br.com.uget.info.VideoInfo;
import br.com.uget.info.VideoInfo.Quality;
import br.com.uget.listeners.AsyncDownloadListener;
import br.com.uget.listeners.AsyncExtractListener;
import br.com.uget.tasks.VideoDownload;


public class YouGet {
	private File fileDir;
	private VideoInfo videoInfo;
	private List<URL> links;
	
	private AsyncExtractListener extractListener;
	private AsyncDownloadListener downloadListener;
	
	public YouGet() {
		links = new ArrayList<>();
		this.fileDir = new File("D:\\YouGet");
	}

	public YouGet(File fileDir) {
		this();
		this.fileDir = fileDir;
	}

	public void download(){
		ExecutorService pool = Executors.newFixedThreadPool(3);
		VideoInfo info;
		VideoDownload dTask = null;
		
		for (URL link : links) {
			try{
				info = new VideoInfo(link);
				info.setQuality(Quality.p360);
				if(extractListener != null)
					info.addExtractListener(extractListener);
				info.extract();

				dTask = new VideoDownload(info, fileDir);
				if(downloadListener != null)
					dTask.addDownloadListener(downloadListener); //download listener
				
				pool.submit(dTask);
			}catch (ExtractException e) {
				System.err.println(e);
				continue;
			}
		}
		
		pool.shutdown();
	}

	/* Getters e Setters */
	public void addDownloadListener(AsyncDownloadListener listener){
		this.downloadListener = listener;
	}
	
	public void addExtractListener(AsyncExtractListener listener){
		this.extractListener = listener;
	}
	
	public List<URL> getURLs() {
		return this.links;
	}

	public void addURL(String url) throws MalformedURLException {
		links.add(new URL(url));
	}

	public VideoInfo getVideoInfo() {
		return videoInfo;
	}
}
