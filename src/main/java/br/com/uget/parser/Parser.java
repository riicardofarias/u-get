package br.com.uget.parser;

import java.net.URL;
import java.util.Comparator;
import java.util.List;

import br.com.uget.info.VideoInfo;
import br.com.uget.info.VideoInfo.Quality;

public abstract class Parser {
	static public class VideoDownload{
		public URL url;
		public Quality quality;
		
		public VideoDownload(Quality quality, URL url) {
			this.quality = quality;
			this.url = url;
		}
	}
	
	static public class VideoOrderComparator implements Comparator<VideoDownload>{

		@Override
		public int compare(VideoDownload o1, VideoDownload o2) {
			Integer v1 = o1.quality.ordinal();
			Integer v2 = o2.quality.ordinal();
			Integer c = v1.compareTo(v2);
			return c;
		}
		
	}
	
	public abstract List<VideoDownload> extract(VideoInfo info);
}
