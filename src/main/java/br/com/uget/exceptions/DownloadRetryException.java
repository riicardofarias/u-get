package br.com.uget.exceptions;

public class DownloadRetryException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public DownloadRetryException() {

	}

	public DownloadRetryException(Throwable e) {
		super(e);
	}

	public DownloadRetryException(String str) {
		super(str);
	}
}
