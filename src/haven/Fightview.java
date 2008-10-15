package haven;

import java.util.*;

public class Fightview extends Widget {
    static Tex bg = Resource.loadtex("gfx/hud/bosq");
    static int height = 5;
    static int ymarg = 5;
    static Coord avasz = new Coord(27, 27);
    LinkedList<Relation> lsrel = new LinkedList<Relation>();
    int curign = -1;
    
    public class Relation {
        int gobid;
        int bal, intns;
        Avaview ava;
        
        public Relation(int gobid) {
            this.gobid = gobid;
            this.ava = new Avaview(Coord.z, Fightview.this, gobid, avasz);
        }
    }
    
    static {
        Widget.addtype("frv", new WidgetFactory() {
            public Widget create(Coord c, Widget parent, Object[] args) {
                return(new Fightview(c, parent));
            }
        });
    }
    
    public Fightview(Coord c, Widget parent) {
        super(c.add(-bg.sz().x, 0), new Coord(bg.sz().x, (bg.sz().y + ymarg) * height), parent);
    }
    
    public void draw(GOut g) {
        int y = 0;
        for(Relation rel : lsrel) {
            /*
            if(rel.gobid == curign)
                continue;
            */
            g.image(bg, new Coord(0, y));
            rel.ava.c = new Coord(10, ((bg.sz().y - rel.ava.sz.y) / 2) + y);
            g.text(String.format("%d %d", rel.bal, rel.intns), new Coord(50, y + 10));
            y += bg.sz().y + ymarg;
        }
        super.draw(g);
    }
    
    public static class Notfound extends RuntimeException {
        public final int id;
        
        public Notfound(int id) {
            super("No relation for Gob ID " + id + " found");
            this.id = id;
        }
    }
    
    private Relation getrel(int gobid) {
        for(Relation rel : lsrel) {
            if(rel.gobid == gobid)
                return(rel);
        }
        throw(new Notfound(gobid));
    }
    
    public void wdgmsg(Widget sender, String msg, Object... args) {
        if(sender instanceof Avaview) {
            for(Relation rel : lsrel) {
                if(rel.ava == sender)
                    wdgmsg("click", rel.gobid, args[0]);
            }
            return;
        }
        super.wdgmsg(sender, msg, args);
    }
    
    public void uimsg(String msg, Object... args) {
        if(msg == "new") {
            Relation rel = new Relation((Integer)args[0]);
            rel.bal = (Integer)args[1];
            rel.intns = (Integer)args[2];
            lsrel.addFirst(rel);
            return;
        } else if(msg == "del") {
            Relation rel = getrel((Integer)args[0]);
            lsrel.remove(rel);
            return;
        } else if(msg == "upd") {
            Relation rel = getrel((Integer)args[0]);
            rel.bal = (Integer)args[1];
            rel.intns = (Integer)args[2];
            return;
        } else if(msg == "bump") {
            try {
                Relation rel = getrel((Integer)args[0]);
                lsrel.remove(rel);
                lsrel.addFirst(rel);
            } catch(Notfound e) {}
            return;
        } else if(msg == "ign") {
            curign = (Integer)args[0];
            return;
        }
        super.uimsg(msg, args);
    }
}
