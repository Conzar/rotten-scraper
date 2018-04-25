/**
 * License GPL v3.0+
 */
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;
import java.io.File;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import org.jsoup.parser.Parser;
import org.apache.commons.io.FileUtils;

class RottenScraper {

    private int reviews = 0;
    private int stars_0 = 0;
    private int stars_h = 0;
    private int stars_1 = 0;
    private int stars_1h = 0;
    private int stars_2 = 0;
    private int stars_2h = 0;
    private int stars_3 = 0;
    private int stars_3h = 0;
    private int stars_4 = 0;
    private int stars_4h = 0;
    private int stars_5 = 0;
    private int total_pages = 0;
    private float total_stars = 0;
    private int pages_with_no_results = 0;

  public RottenScraper(){
  }

  public void formatDocument(Document doc){
      Document.OutputSettings settings = doc.outputSettings();
      settings.prettyPrint(false);
      settings.escapeMode(Entities.EscapeMode.extended);
      settings.charset("ASCII");
  }

  public void downloadPage(String url, String outfile) throws Exception {
        final Connection.Response response = Jsoup.connect(url).execute();
        final Document doc = response.parse();
        formatDocument(doc);

        final File f = new File(outfile);
        FileUtils.writeStringToFile(f, doc.outerHtml(), "UTF-8");
  }


  public void parseBody(Element body){
    total_pages++;
    Elements rows = body.getElementsByClass("row review_table_row");
    if(rows.size() == 0){
      pages_with_no_results++;
    }else{
      for(Element row : rows){
          float result = countStars(row);
          if(result == 0){
            stars_0++;
          }else if(result == 0.5f){
            stars_h++;
          }else if(result == 1f){
            stars_1++;
          }else if(result == 1.5f){
            stars_1h++;
          }else if(result == 2f){
            stars_2++;
          }else if(result == 2.5f){
            stars_2h++;
          }else if(result == 3f){
            stars_3++;
          }else if(result == 3.5f){
            stars_3h++;
          }else if(result == 4f){
            stars_4++;
          }else if(result == 4.5f){
            stars_4h++;
          }else if(result == 5f){
            stars_5++;
          }
          reviews++;
          total_stars += result;
      }
    }
  }
  public void printStats(){
    System.out.println("Pages: "+total_pages);
    System.out.println("Pages With No Results: "+pages_with_no_results);
    System.out.println("Reviews: "+reviews);
    System.out.println("total stars : "+total_stars);
    float ave = total_stars/(5f*reviews);
    float avestars = total_stars/(reviews);
    System.out.println("Average Fresh: "+ave);
    System.out.println("Average Stars: "+avestars);
    System.out.println("0   Stars: "+stars_0);
    System.out.println("1/2 Stars: "+stars_h);
    System.out.println("1   Stars: "+stars_1);
    System.out.println("1.5 Stars: "+stars_1h);
    System.out.println("2   Stars: "+stars_2);
    System.out.println("2.5 Stars: "+stars_2h);
    System.out.println("3   Stars: "+stars_3);
    System.out.println("3.5 Stars: "+stars_3h);
    System.out.println("4   Stars: "+stars_4);
    System.out.println("4.5 Stars: "+stars_4h);
    System.out.println("5   Stars: "+stars_5);
  }

  /**
   * Parse a row from the table of Rotten.
   */
  public float countStars(Element element){

   // either its 1/2 a star found below
   //   <span style="color:#F1870A" class="fl"> ½</span> 
   // or its a series of stars like below
   // <span style="color:#F1870A" class="fl">
   //   <span class="glyphicon glyphicon-star"></span><span class="glyphicon glyphicon-star"></span><span class="glyphicon glyphicon-star"></span><span class="glyphicon glyphicon-star"></span><span class="glyphicon glyphicon-star"></span></span> 
    float count = 0f;
    for(Element div : element.getElementsByClass("col-xs-16")){
        for(Element fl : element.getElementsByClass("fl")){
            //System.out.println(fl);
            String html = fl.html();
                             // ½
                             // &frac12;
            if(html.contains("&half;")){
            //if(html.contains("&#xfffd;")){
            //if(html.contains("½")){
                //System.out.println("1/2 star");
                count += 0.5f;
            }
            Elements elements = element.getElementsByClass("glyphicon glyphicon-star");
            //System.out.println("stars = "+elements.size());
            count += elements.size();
        }
    }
    return count;
  }

  public static void main (String args[]) throws Exception{
    RottenScraper scraper = new RottenScraper();
    String url_head = "https://www.rottentomatoes.com/m/star_wars_the_last_jedi/reviews/?page=";
    String url_tail = "&type=user&sort=";

    int total_pages = 51;
    // download all pages first
///*
    SimpleDateFormat dateFormatter = new SimpleDateFormat("E, y-M-d 'at' h:m:s a z");
    System.out.println("Start of download: " + dateFormatter.format(new Date()));

    for(int i = 1; i <= total_pages; i++){
      scraper.downloadPage(url_head+i+url_tail,"pages/"+i+".html");
      System.out.println("Downloading page: "+i+" @ " + dateFormatter.format(new Date()));
      try{
          Thread.sleep(5000);
      }catch(Exception e){}
    }
    System.out.println("End of download: " + dateFormatter.format(new Date()));
//*/

    // parse pages
    for(int i = 1; i <= total_pages; i++){
      File file = new File("pages/"+i+".html");
      Document doc = Jsoup.parse(file,"UTF-8","");
      //Document doc = Jsoup.connect(url).get();
      scraper.formatDocument(doc);

      Element body = doc.body();
      scraper.parseBody(body);
    }

    scraper.printStats();
  }
}
