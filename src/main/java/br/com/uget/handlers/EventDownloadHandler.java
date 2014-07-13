package br.com.uget.handlers;

import br.com.uget.info.DownloadInfo;
import br.com.uget.info.VideoInfo;
import br.com.uget.listeners.AsyncDownloadListener;

public interface EventDownloadHandler {
	public void addDownloadListener(AsyncDownloadListener event);
	public void removeDownloadListener(AsyncDownloadListener event);
	
	public void fireDownloadComplete(DownloadInfo downloadInfo);
	public void fireDownloadError(Throwable t);
	public void fireDownloading(VideoInfo info, float progress);
}
