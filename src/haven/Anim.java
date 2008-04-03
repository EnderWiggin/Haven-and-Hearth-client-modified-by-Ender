package haven;

import java.util.*;
import java.awt.image.BufferedImage;

public class Anim {
	List<BufferedImage> frames;
	Tex[] texes;
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
		texes = new Tex[frames.size()];
		isgay = Resource.detectgay(frames.get(0));
	}
	
	public Tex tex(int f) {
		if(texes[f] == null)
			texes[f] = new TexI(frames.get(f));
		return(texes[f]);
	}
}
