package br.com.uget.exceptions;

public class DownloadErrorException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public DownloadErrorException() {

	}

	public DownloadErrorException(Throwable e) {
		super(e);
	}

	public DownloadErrorException(String str) {
		super(str);
	}
}
