package haven;

import java.util.*;
import java.awt.image.BufferedImage;

public class Anim {
	List<BufferedImage> frames;
	List<Integer> prio;
	List<Integer> dur;
	Coord cc;
	Coord sz;
	boolean isgay;
	
	public Anim(List<BufferedImage> frames, List<Integer> prio, List<Integer> dur, Coord cc, Coord sz) {
		this.frames = frames;
		this.prio = prio;
		this.dur = dur;
		this.cc = cc;
		this.sz = sz;
		isgay = Resource.detectgay(frames.get(0));
	}
}
