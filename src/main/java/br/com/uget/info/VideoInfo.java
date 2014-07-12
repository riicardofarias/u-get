package br.com.uget.info;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.uget.exceptions.DownloadRetryException;
import br.com.uget.exceptions.ExtractException;
import br.com.uget.handlers.EventExtractHandler;
import br.com.uget.listeners.AsyncExtractListener;
import br.com.uget.parser.Parser;
import br.com.uget.parser.Parser.VideoDownload;
import br.com.uget.parser.Parser.VideoOrderComparator;
import br.com.uget.parser.YoutubeParser;

public class VideoInfo implements EventExtractHandler{
	private ConcurrentLinkedQueue<AsyncExtractListener> listeners = new ConcurrentLinkedQueue<>(); // list of listeners
	private Logger log = LoggerFactory.getLogger(VideoInfo.class);                                 // logger

	private String videoId    = null;  								// Video id
	private String title      = null;  								// Video title
	private URL urlVideo;
	private List<VideoDownload> videoDownloads = new ArrayList<>(); // url videos
	private Quality quality;                                        // quality of video
	private VideoDownload videoDownload;                            // video download


	public VideoInfo(URL urlVideo) {
		this.urlVideo = urlVideo;
	}

	//Quality of video
	public enum Quality{p144, p240, p270, p360, p480, p720, p1080, p3072}

	/**
	 * Extrai as informações do vídeo
	 * @param url - Url do vídeo
	 */
	public void extract(){
		fireOnExtracting(urlVideo); 								    // extracting event
		
		Parser parser = new YoutubeParser(urlVideo);                    // get youtube extractor
		
		try{
			videoDownloads = parser.extract(this);                      // extracting format links
			getVideoToDownload(this, videoDownloads);                   // get video url
		}catch(DownloadRetryException e){
			log.debug(e.getMessage() + ": extracting again");           // log message
			fireOnError(e); 											// event error
			extract(); 													// try extract again
		}catch (ExtractException e) {
			fireOnError(e); 											// event error
			log.debug(e.getMessage());                                  // log message
		}
	}

	public void getVideoToDownload(VideoInfo info, List<VideoDownload> videoDownloads){
		if(videoDownloads.size() == 0){
			throw new DownloadRetryException("error on extract info, wait process again.");
		}

		Collections.sort(videoDownloads, new VideoOrderComparator());
		for (VideoDownload v : videoDownloads) {
			if(v.quality.equals(info.getQuality())){
				videoDownload = new VideoDownload(v.quality, v.url);
				fireOnComplete(this);                                    // extract complete
				return;
			}	
		}

		throw new ExtractException("format video does not found");
	}

	//Getters e Setters
	public String getTitle() {
		return title;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public VideoDownload getVideoDownload() {
		return videoDownload;
	}

	public List<VideoDownload> getVideoDownloads() {
		return videoDownloads;
	}

	public Quality getQuality() {
		return quality;
	}

	public void setQuality(Quality quality) {
		this.quality = quality;
	}

	public void setVideoId(String id) {
		this.videoId = id;
	}

	public String getVideoId(){
		return this.videoId;
	}

	//Listeners methods

	@Override
	public void addExtractListener(AsyncExtractListener extractListener) {		
		listeners.add(extractListener);
	}

	@Override
	public void fireOnExtracting(URL url) {
		for(AsyncExtractListener listener : listeners){
			listener.onExtracting(url);
		}
	}

	@Override
	public void fireOnError(Throwable t) {
		for(AsyncExtractListener listener : listeners){
			listener.onError(t);
		}
	}

	@Override
	public void fireOnComplete(VideoInfo info) {
		for(AsyncExtractListener listener : listeners){
			listener.onComplete(info);
		}
	}
}