package br.com.uget.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.uget.handlers.EventConvertHandler;
import br.com.uget.listeners.AsyncConvertListener;
import br.com.uget.utils.ConversorUtils;

public class Converter implements EventConvertHandler{
	private Logger log = LoggerFactory.getLogger(Converter.class);
	private ConcurrentLinkedQueue<AsyncConvertListener> listeners = new ConcurrentLinkedQueue<>();
	
	private File videoFile;
	private File musicFile;
	
	private Pattern patternTotalSeconds = Pattern.compile("Duration: ([^,.]*)");
	private Pattern patternProgressTime = Pattern.compile("time=([^,.]*)");
	
	/**
	 * Construtor padr�o
	 */
	public Converter() {}
	
	public Converter(File videoFile){
		this();
		this.videoFile = videoFile;
	}
	
	public void convert(){
		if(musicFile == null)
			musicFile = new File(videoFile.getPath().replace("mp4", "mp3")); // replace extension
		
		if(musicFile.exists())
			musicFile = rename(musicFile);                                   // rename file if exists
		try {
			if(convertMP3(videoFile, musicFile))                             // init convertion
				fireOnConverted(musicFile);                                  // convertion complete
		} catch (IOException e) {
			log.debug(e.getMessage());                                      // log error
			fireOnError(e);                                                 // error
		} catch (InterruptedException e) {
			log.debug(e.getMessage());                                      // log error
			fireOnError(e);                                                 // error
		}finally{
			if(videoFile != null && videoFile.exists()) videoFile.delete();
		}
	}
	
	/**
	 * Extrair o �udio do v�deo e converte para o formato mp3
	 * @param videoFile Arquivo de v�deo
	 * @param musicFile Arquivo de �udio
	 * @return Resultado da convers�o
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private boolean convertMP3(File videoFile, File musicFile) throws IOException, InterruptedException{
		ProcessBuilder builder = new ProcessBuilder(ConversorUtils.BASE_PATH, "-i", videoFile.getAbsolutePath(), musicFile.getAbsolutePath());
		builder.redirectErrorStream(true);
		final Process processo = builder.start();
		
		InputStream is = processo.getInputStream();
	    InputStreamReader isr = new InputStreamReader(is);
	    BufferedReader br = new BufferedReader(isr);
	    String line;
	    
	    float totalSeconds = 0;
	    while ((line = br.readLine()) != null) {
	    	Matcher mDuration = patternTotalSeconds.matcher(line);
	    	
	    	if(mDuration.find())
	    		totalSeconds = getSeconds(mDuration.group(1));
	    	
	    	Matcher mProgress = patternProgressTime.matcher(line);
	    	if(mProgress.find()){
	    		float progress = (getSeconds(mProgress.group(1)) / totalSeconds) * 100; // progress
	    		fireOnConverting(videoFile, progress); 									// converting file
	    	}
	    }
	    
		return (processo.waitFor() == 0) ? true : false;
	}
	
	private File rename(File music){
		String name = music.getPath().substring(0, music.getPath().length() - 4);
		String extension = music.getName().substring(music.getName().length() - 4);
		int count = 1;
		
		for (String m : music.getParentFile().list()) {
			if(music.getName().equals(m)){
				System.out.println(name);
				count++;
			}
		}
		
		String newFile = String.format("%s (%d)%s", name, count, extension);
		return new File(newFile);
	}
	
	private float getSeconds(String time){
		String[] times = time.split(":");
		float totalSeconds = (Integer.parseInt(times[0]) * 3600) 
			+ (Integer.parseInt(times[1]) * 60)
			+ (Integer.parseInt(times[2]));
		
		return totalSeconds;
	}
	
	//Listeners
	@Override
	public void addConvertListener(AsyncConvertListener listener) {
		listeners.add(listener);
	}

	@Override
	public void fireOnConverting(File video, float progress) {
		for (AsyncConvertListener listener : listeners) {
			listener.onConverting(video, progress);
		}
	}

	@Override
	public void fireOnConverted(File mp3) {
		for (AsyncConvertListener listener : listeners) {
			listener.onComplete(mp3);
		}
	}
	
	@Override
	public void fireOnError(Throwable t) {
		for (AsyncConvertListener listener : listeners) {
			listener.onError(t);
		}
	}
	
	// Getters and Setters
	public File getvideoFile() {
		return videoFile;
	}

	public void setvideoFile(File videoFile) {
		this.videoFile = videoFile;
	}

	public File getMusicFile() {
		return musicFile;
	}

	public void setMusicFile(File musicFile) {
		this.musicFile = musicFile;
	}

}
