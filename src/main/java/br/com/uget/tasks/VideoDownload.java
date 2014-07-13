package br.com.uget.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.uget.handlers.EventDownloadHandler;
import br.com.uget.info.DownloadInfo;
import br.com.uget.info.VideoInfo;
import br.com.uget.listeners.AsyncDownloadListener;
import br.com.uget.utils.YouGetUtils;

public class VideoDownload implements Runnable, EventDownloadHandler{
	private ConcurrentLinkedQueue<AsyncDownloadListener> listeners = new ConcurrentLinkedQueue<>();
	private Logger log = LoggerFactory.getLogger(VideoDownload.class);
	private VideoInfo videoInfo;
	private File fileDir;

	private final int MAXSIZEBUFFER   = 8 * 1024;
	private final int READ            = 0;
	private final int DOWNLOADING     = 1;
	private final int ERROR           = 2;
	private final int COMPLETE        = 3;

	private volatile int status;
	private int size;
	private int downloaded;

	public VideoDownload() {
		size       = -1;
		downloaded = 0;
		status     = READ;
	}

	public VideoDownload(VideoInfo videoInfo, File fileDir) {
		this();
		this.videoInfo = videoInfo;
		this.fileDir   = fileDir;

		if(!fileDir.exists())
			fileDir.mkdir();
	}

	public VideoDownload(VideoInfo videoInfo, File fileDir, AsyncDownloadListener listener) {
		this(videoInfo, fileDir);
		addDownloadListener(listener);
	}

	@Override
	public void run() {
		try {
			downloadVideo(videoInfo, fileDir);
		} catch (IOException e) {
			log.debug(e.getMessage());
			error(e.getCause());
		}
	}

	public void downloadVideo(VideoInfo vInfo, File local) throws IOException{
		OutputStream output = null;
		InputStream input = null;
		File videoFile = null;
		setEstado(DOWNLOADING); //Downloading

		try {
			URL url = videoInfo.getVideoDownload().url;
			URLConnection connection = (HttpURLConnection) url.openConnection(); 
			int contentSize = connection.getContentLength();

			//Marca o download com erro
			if(contentSize < 1){
				error(new Throwable("download error")); // Evento de erro
			}

			if(size == -1){
				size = contentSize;
			}

			input = connection.getInputStream();

			String fileName = YouGetUtils.sanitizeNome(vInfo.getVideoId());
			fileName += ".mp4";
			
			videoFile = new File(local + File.separator +  fileName);      											 // Arquivo MP4 

			removeIfExists(videoFile); //Remove o v�deo se existir

			output = new FileOutputStream(videoFile);

			int bytesRead;
			while(getStatus() == DOWNLOADING){
				byte buffer[];
				buffer = new byte[MAXSIZEBUFFER];

				bytesRead = input.read(buffer);	
				if(bytesRead == -1) break;

				output.write(buffer, 0, bytesRead);
				downloaded += bytesRead;

				downloading(vInfo, getProgress()); //Baixando arquivo
			}

		} catch (Exception e) {
			error(e.getCause());
		}finally{
			if(output != null){ output.close();} //close
			if(input != null){ input.close(); } //close

			// download complete
			if(getStatus() == DOWNLOADING){
				complete(new DownloadInfo(vInfo, videoFile));
			}
		}
	}

	private void removeIfExists(File file){
		if(file.exists())
			file.delete();
	}

	//Progresso do download
	public float getProgress(){
		return ((float) downloaded / size) * 100;
	}

	//Erro durante o download
	private void error(Throwable t){
		setEstado(ERROR);
		fireDownloadError(t);
	}

	//Download completo
	private void complete(DownloadInfo downloadInfo){
		setEstado(COMPLETE);
		fireDownloadComplete(downloadInfo);
	}

	//Baixando arquivo
	private void downloading(VideoInfo info, float progress){
		fireDownloading(info, progress);
	}
	
	//Status do download
	public int getStatus() {
		return status;
	}

	private void setEstado(int status) {
		this.status = status;
	}

	@Override
	public void addDownloadListener(AsyncDownloadListener event) {
		listeners.offer(event);
	}

	@Override
	public void removeDownloadListener(AsyncDownloadListener event) {
		listeners.remove(event);
	}

	@Override
	public void fireDownloadComplete(DownloadInfo downloadInfo) {
		for (AsyncDownloadListener event : listeners) {
			event.onComplete(downloadInfo);
		}
	}

	@Override
	public void fireDownloadError(Throwable t) {
		for (AsyncDownloadListener event : listeners) {
			event.onError(t);
		}
	}

	@Override
	public void fireDownloading(VideoInfo info, float progress) {
		for (AsyncDownloadListener event : listeners) {
			event.onDownloading(info, progress);
		}
	}

	public VideoInfo getVideoInfo() {
		return videoInfo;
	}

	public void setVideoInfo(VideoInfo videoInfo) {
		this.videoInfo = videoInfo;
	}

	public File getLocalPath() {
		return fileDir;
	}

	public void setLocalPath(File localPath) {
		this.fileDir = localPath;
	}
}
