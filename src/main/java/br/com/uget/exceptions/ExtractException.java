package br.com.uget.exceptions;

public class ExtractException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public ExtractException(Throwable t) {
		super(t);
	}
	
	public ExtractException(String msg) {
		super(msg);
	}
}
