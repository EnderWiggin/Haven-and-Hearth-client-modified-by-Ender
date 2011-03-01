package wikireader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.regex.*;

public class wikiReader extends Thread{
   private URL site;
   private String search = "";
        private String results = "";
        private List<header> searchResults = null;
        private boolean ready = false;
        private int currentOffset = 0;
        private int currentLimit = 20;
        private boolean grabPage = false;
        //restrict limit to proper one
        private int[] limits = {20,50,100,250,500};
        //pre compile regex pattren's that we are using for convience for performance.
        //little memory overhead but faster results
        private Pattern removeHtml = Pattern.compile("\\<.*?\\>");
        private Pattern findKeywords = Pattern.compile("\\[\\[([\\sa-zA-Z0-9]+)(\\|\\s*([\\sa-zA-Z0-9]+))?\\]\\]");
        private Pattern findSpaces = Pattern.compile("\\s");

        //Constructors
   public wikiReader(String search){ this.search = search; }
        public wikiReader(){ }

        //Thread main process
        @Override
   public void run(){
            String lastsearchs = "";
            while(true){
                if(!this.search.equals(lastsearchs)){
                    lastsearchs = this.search;
                    this.ready = false;
                    this.searchPage(lastsearchs,this.currentLimit,this.currentOffset);
                }else if(this.grabPage){
                    this.ready = false;
                    this.grabPage = false;
                    this.getNextPage();
                }
            }
        }
        //-----------------------
        //Public methods with javadoc documentation
        //-----------------------
        /**
        *Returns the current state of data requested
        *@return      True if data is ready to be accessed false if not.
        */
        public synchronized boolean isReady(){ return this.ready; }
        /**
        *Returns the contents of a page on wiki
        *@return      String of the contents or null if no data.
        */
        public synchronized String getResults(){ this.ready = false; return this.results; }
        /**
        *Returns the contents of a search
        *@return      ArrayList of headers or null if no search data
        */
        public synchronized List<header> getSearchResults(){ this.ready = false; return this.searchResults; }
        /**
        *Requests the next page of search results using previous settings.
        */
        public synchronized void nextPage(){ this.ready = false; this.grabPage = true; };
        /**
        *Returns the last String used for a search
        *@return      String of the contents or empty string if no search.
        */
        public synchronized String getSearch(){ return this.search; }
        /**
        *Requests a search using default of 20 results per page, of the first page.
        *@param search The text to search for
        */
        public synchronized void setSearch(String search){ this.setSearch(search,20,1); }
        /**
        *Requests a search using supplied number of results per page.
        *@param search The text to search for
        *@param limit The number of results per page
        */
        public synchronized void setSearch(String search,int limit){ this.setSearch(search,limit,1); }
        /**
        *Requests a search using supplied number of results per page and supplied page number.
        *@param search The text to search for
        *@param limit The number of results per page
        *@param page The page to start search on
        */
        public synchronized void setSearch(String search,int limit, int page){
            boolean valid = (Arrays.binarySearch(limits,this.currentLimit) > -1) ? true : false;
            this.ready = false; this.search = search;
            this.currentLimit = (valid) ? limit : 20;
            this.currentOffset = (page - 1) * this.currentLimit;
            
        }
        
        //-----------------------
        //Private helping methods.
        //-----------------------
        private void getNextPage(){
            searchPage(this.search,this.currentLimit, (this.currentOffset += this.currentLimit));
        }
        private void searchPage(String search, int limit , int offset){
       try{
               URL addr = new URL("http://ringofbrodgar.com/w/index.php?limit="+limit+"&offset="+offset+"&search="+search);
               BufferedReader in = new BufferedReader(new InputStreamReader(addr.openStream()));
               String inputLine,content = "";
               while ((inputLine = in.readLine()) != null) content += inputLine;
               in.close();
               if(content.indexOf("<div class='searchresults'>") != -1){
                    this.results = null;
                    //clean up the contents so less content to go through (faster)
                    content = content.substring(content.indexOf("<div class='searchresults'>"),content.indexOf("<div class=\"printfooter\">"));
                    this.searchResults = this.getHeaders(content);
                    this.ready = true;
               }else{
                   this.results = this.showPage(content);
                   this.searchResults = null;
               }
            }catch (Exception ex) { }
            
        }
        private String formatKeywords(String text){
            Matcher ma =  this.findKeywords.matcher(text);
            StringBuffer buffer = new StringBuffer(text.length());
            while(ma.find()){
                String link = "/wiki/"+ this.findSpaces.matcher(ma.group(1)).replaceAll("_");
                String title = (ma.group(3) == null)? ma.group(1): ma.group(3);
                //link = $u{$col[70,70,200]{$a[link]{title}}};
                ma.appendReplacement(buffer, Matcher.quoteReplacement(link));
            }
              ma.appendTail(buffer);
              return buffer.toString();
        }
        private String showPage(String content){
            // TODO add code for decyphering wiki code language for single pages
            return null;
        }
        private List<header> getHeaders(String content){


            List<header> headers = new ArrayList<header>();
            //Create each search section
            String[] h2 = new String[50];
            int idx = 0,cnt = 0;
            while(content.indexOf("<h2>",idx) != -1){
                int h2Start = content.indexOf("<h2>",idx);
                int h2End = content.indexOf("</h2>",h2Start);
                h2[cnt] = this.removeHtml.matcher(content.substring(h2Start,h2End)).replaceAll("");
                idx = h2End;
                cnt++;
            }
            //Get content for each section
            int idxHeader = 0;
            cnt = 0;
            while(content.indexOf("<ul class='mw-search-results'>",idxHeader) != -1){
                //Alot of abusing of indexOf cause faster than compiled regex
                header cheader = new header();
                cheader.title = h2[cnt];
                int ulStart = content.indexOf("<ul class='mw-search-results'>",idxHeader);
                idxHeader = ulStart+1;
                String header = content.substring(ulStart, content.indexOf("</ul>",ulStart));

                //for each search section create search items
                int idxItem = header.indexOf("<li>");
                while(header.indexOf("<li>",idxItem) != -1){
                    searchItem item = new searchItem();
                    int liStart = header.indexOf("<li>",idxItem);
                    int liEnd = header.indexOf("</li>",liStart);
                    
                    String itemcnt = header.substring(liStart, liEnd);
                    int descStart = itemcnt.indexOf("<div");
                    int descEnd = itemcnt.indexOf("</div");

                    item.desc = itemcnt.substring(descStart,descEnd);
                    item.desc = this.removeHtml.matcher(item.desc).replaceAll("");
                    item.desc = formatKeywords(item.desc);
                    String link = itemcnt.substring(itemcnt.indexOf("<a"),itemcnt.indexOf("</a>"));
                    
                    int linkidx = link.indexOf("href=\"");
                    item.link = link.substring(linkidx,link.indexOf("\"",linkidx+6)).replace("href=\"","");

                    item.title = this.removeHtml.matcher(link).replaceAll("");
                    cheader.results.add(item);
                    idxItem = liEnd+1;
                }
                cnt++;
                headers.add(cheader);
            }
            return headers;
        }
}
//helper classes for returning data
class header{
    String title;
    List<searchItem> results = new ArrayList<searchItem>();
}
class searchItem{
    String link;
    String title;
    String desc;
}