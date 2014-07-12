package br.com.uget.handlers;

import java.net.URL;

import br.com.uget.info.VideoInfo;
import br.com.uget.listeners.AsyncExtractListener;

public interface EventExtractHandler {
	public void addExtractListener(AsyncExtractListener listener);
	
	public void fireOnExtracting(URL url);
	public void fireOnError(Throwable t);
	public void fireOnComplete(VideoInfo info);
}
