package haven;

import java.util.*;
import java.awt.image.BufferedImage;

public class Anim {
	List<BufferedImage> frames;
	List<Integer> dur;
	Coord cc;
	Coord sz;
	
	public Anim(List<BufferedImage> frames, List<Integer> dur, Coord cc, Coord sz) {
		this.frames = frames;
		this.dur = dur;
		this.cc = cc;
		this.sz = sz;
	}
}
