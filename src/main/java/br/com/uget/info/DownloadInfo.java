package br.com.uget.info;

import java.io.File;

public class DownloadInfo {
	private VideoInfo info;
	private File videoFile;
	
	public DownloadInfo() {
	}
	
	public DownloadInfo(VideoInfo info, File videoFile) {
		this.info = info;
		this.videoFile = videoFile;
	}

	public VideoInfo getInfo() {
		return info;
	}

	public void setInfo(VideoInfo info) {
		this.info = info;
	}

	public File getVideoFile() {
		return videoFile;
	}

	public void setVideoFile(File videoFile) {
		this.videoFile = videoFile;
	}
}
