package br.com.uget.listeners;

import java.io.File;

import br.com.uget.info.VideoInfo;

public interface AsyncDownloadListener {
	public void onDownloading(VideoInfo info, float progress);
	public void onComplete(File file);
	public void onError(Throwable t);
}
