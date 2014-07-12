package br.com.uget.listeners;

import java.net.URL;

import br.com.uget.info.VideoInfo;

public interface AsyncExtractListener {
	public void onExtracting(URL url);
	public void onError(Throwable t);
	public void onComplete(VideoInfo info);
}
