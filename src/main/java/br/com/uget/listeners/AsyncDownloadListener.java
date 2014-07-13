package br.com.uget.listeners;

import br.com.uget.info.DownloadInfo;
import br.com.uget.info.VideoInfo;

public interface AsyncDownloadListener {
	public void onDownloading(VideoInfo info, float progress);
	public void onComplete(DownloadInfo downloadInfo);
	public void onError(Throwable t);
}
