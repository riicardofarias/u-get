package br.com.uget.listeners;

import java.io.File;

public interface AsyncConvertListener {
	public void onConverting(File video, float progress);
	public void onComplete(File mp3);
	public void onError(Throwable t);
}
