package br.com.uget.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class YouGetUtils {
	public static final String FFMPEG_CONFIG = "ffmpeg";
	//public static final String FFMPEG_CONFIG = "C:\\ffmpeg\\bin\\ffmpeg.exe";
	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36";
	public static final String ENCODING = "UTF-8";
	public static final int    STATUS_OK = 200; // Requisi��o correta
	public static final int    STATUS_FORBIDDEN = 403;
	/**
	 * Retorna o ID do v�deo
	 * @param url - Url do v�deo
	 * @return ID - mdB3Oyd5HtU
	 */
	public static String getVideoID(String url){
		return url.substring(url.indexOf("?v=") + 3);
	}
	
	public static String getVideoUrl(String url){
		return "http://www.youtube.com/get_video_info?adformat=1_2_1&splay=1&asv=3&el=adunit&video_id="+getVideoID(url)+"&ps=trueview-instream";
	}
	
	public static String getUrlFromID(String ID){
		return "http://www.youtube.com?v="+ID;
	}
	
	public static String getPlayListFromURL(String url){
		return "http://gdata.youtube.com/feeds/api/playlists/"+getPlaylistID(url)+"?v=2&start-index=1&alt=jsonc";
	}
	
	public static boolean isPlaylist(String url){
		return (url.contains("list=")) ? true : false;
	}
	
	public static String getPlaylistID(String url){
		return url.substring(url.indexOf("list=") + 5);
	}
	
	public static String sanitizeNome(String nomeArquivo){
		char[] ILLEGAL_FILENAME_CHARACTERS = {'/', '`', '?', '*', '\\', '<', '>', '|', '"', ':'};
		for (char c : ILLEGAL_FILENAME_CHARACTERS) {
			nomeArquivo = nomeArquivo.replace(c, '_');
		}
		return nomeArquivo;
	}
	
	public static String getString(String encoding, InputStream instream) throws UnsupportedEncodingException, IOException {
		Writer writer = new StringWriter();

		char[] buffer = new char[1024];
		try {
			Reader reader = new BufferedReader(new InputStreamReader(instream, encoding));
			int n;
			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}
		} finally {
			instream.close();
		}
		String result = writer.toString();
		return result;
	}
	
	public static String getFormato(int formato){
		return "mp4";
	}
}
