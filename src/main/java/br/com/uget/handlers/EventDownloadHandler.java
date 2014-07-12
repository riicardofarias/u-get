package br.com.uget.handlers;

import java.io.File;

import br.com.uget.info.VideoInfo;
import br.com.uget.listeners.AsyncDownloadListener;

public interface EventDownloadHandler {
	public void addDownloadListener(AsyncDownloadListener event);
	public void removeDownloadListener(AsyncDownloadListener event);
	
	public void fireDownloadComplete(File file);
	public void fireDownloadError(Throwable t);
	public void fireDownloading(VideoInfo info, float progress);
}
