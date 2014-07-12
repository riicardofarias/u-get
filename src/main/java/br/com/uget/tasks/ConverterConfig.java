package br.com.uget.tasks;


public class ConverterConfig {
	private static String DEFAULT_HOME_CONV = "C:\\ffmpeg\\bin\\";
	
	private String pathConv;
	
	public ConverterConfig() {
		this(DEFAULT_HOME_CONV);
	}

	public ConverterConfig(String pathConv) {
		this.pathConv = pathConv;
	}
	
	public String getPathConverter() {
		return pathConv;
	}

	public void setPathConverter(String pathConverter) {
		this.pathConv = pathConverter;
	}
}
