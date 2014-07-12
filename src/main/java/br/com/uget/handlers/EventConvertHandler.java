package br.com.uget.handlers;

import java.io.File;

import br.com.uget.listeners.AsyncConvertListener;

public interface EventConvertHandler {
	public void addConvertListener(AsyncConvertListener listener);
	
	public void fireOnConverting(File video, float progress);
	public void fireOnConverted(File mp3);
	public void fireOnError(Throwable t);
}
