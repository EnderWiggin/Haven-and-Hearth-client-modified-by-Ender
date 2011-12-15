package ender;

import haven.Fightview;
import haven.Fightview.Relation;

public class SkillAvailability {
    
    public boolean isActive(){
	return false;
    }
    
    public boolean isAvailable(){
	return false;
    }
    
    
    public static class Combat extends SkillAvailability{
	public static final int NOP = 12345;
	private int reqIP = NOP;
	private int minINT = NOP;
	private int maxINT = NOP;
	private int minBAL = NOP;
	private int maxBAL = NOP;
	private int minATK = NOP;
	private int maxATK = NOP;
	private int minDEF = NOP;
	private int maxDEF = NOP;
	
	public Combat(int IP){
	    super();
	    reqIP = IP;
	}
	
	public Combat() {
	    super();
	}

	@Override
	public boolean isActive() {
	    return (Fightview.instance != null)&&(Fightview.instance.current != null);
	}

	@Override
	public boolean isAvailable() {
	    Fightview fv;
	    Relation rel;
	    
	    if((fv = Fightview.instance) == null){return false;}
	    if((rel = fv.current) == null){return false;}
	    
	    if((reqIP != NOP)&&(rel.ip < reqIP)){
		return false;
	    }
	    
	    if((minINT != NOP)&&(rel.intns < minINT)){
		return false;
	    }
	    if((maxINT != NOP)&&(rel.intns > maxINT)){
		return false;
	    }

	    if((minBAL != NOP)&&(rel.bal < minBAL)){
		return false;
	    }
	    if((maxBAL != NOP)&&(rel.bal > maxBAL)){
		return false;
	    }
	    
	    if((minATK != NOP)&&(fv.off < minATK)){
		return false;
	    }
	    if((maxATK != NOP)&&(fv.off > maxATK)){
		return false;
	    }
	    
	    if((minDEF != NOP)&&(fv.def < minDEF)){
		return false;
	    }
	    if((maxDEF != NOP)&&(fv.def > maxDEF)){
		return false;
	    }
	    
	    return true;
	}

	public synchronized Combat minIP(int reqIP) {
	    this.reqIP = reqIP;
	    return this;
	}

	public synchronized Combat minINT(int minINT) {
	    this.minINT = minINT;
	    return this;
	}
	
	public synchronized Combat maxINT(int maxINT) {
	    this.maxINT = maxINT;
	    return this;
	}

	public synchronized Combat minBAL(int reqBAL) {
	    this.minBAL = reqBAL;
	    return this;
	}

	public synchronized Combat minATK(int minATK) {
	    this.minATK = 100*minATK;
	    return this;
	}

	public synchronized Combat maxATK(int maxATK) {
	    this.maxATK = 100*maxATK;
	    return this;
	}

	public synchronized Combat minDEF(int minDEF) {
	    this.minDEF = 100*minDEF;
	    return this;
	}

	public synchronized Combat maxDEF(int maxDEF) {
	    this.maxDEF = 100*maxDEF;
	    return this;
	}
	
    }
}
