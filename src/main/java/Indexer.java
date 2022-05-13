import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;
import opennlp.tools.stemmer.PorterStemmer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.json.JSONObject;
import org.json.JSONException;

public class Indexer {
    List<String> all_words = new ArrayList<>();
    List<JSONObject> all_dicts= new ArrayList<>();
    
    List<String> stoppingWords = new ArrayList<String>();
    
    Indexer()
    {
        // fill stoppingWords list from text file
        File file = new File("stoppingWords.txt");
        try {
            Scanner in = new Scanner(file);
            while (in.hasNextLine())
            {
                stoppingWords.add(in.nextLine());
            }
            in.close();
        } catch (FileNotFoundException e) {
           
            e.printStackTrace();
        }
       // System.out.print(stoppingWords); 
    }

    public String stem(String input)
    {
        PorterStemmer porterStemmer = new PorterStemmer();
        return porterStemmer.stem(input.toLowerCase());
    }

    public void preprocessing(Website website) throws IOException, URISyntaxException, JSONException{
        
        String url = website.getURL();
        Document doc = Jsoup.connect(url).userAgent("Mozilla").get();
        
        Hashtable<String,JSONObject> titlesDict  = processTitles(doc,url);
        Hashtable<String,JSONObject> headingDict  = processHeadings(doc,url);
        Hashtable<String,JSONObject> textDict  = processtext(doc,url);

        for (String key : titlesDict.keySet()) {
            JSONObject jo= titlesDict.get(key);
            jo = jo.getJSONObject("url");
            all_words.add(key);
            all_dicts.add(jo);
        }

        for (String key : headingDict.keySet()) {
            JSONObject jo= headingDict.get(key);
            jo = jo.getJSONObject("url");
            all_words.add(key);
            all_dicts.add(jo);
        }

        for (String key : textDict.keySet()) {
            JSONObject jo= textDict.get(key);
            jo = jo.getJSONObject("url");
            all_words.add(key);
            all_dicts.add(jo);
        }
        
    }

    public Hashtable<String,JSONObject> processTitles( Document doc,String link) throws URISyntaxException, MalformedURLException
    {
        URI uri = new URI(link);
        String url = uri.getHost().toString();
        String[] urlcomp = url.split(".");
        System.out.println(urlcomp );
        Hashtable<String,JSONObject> dict = new Hashtable<>();
        Elements elements = doc.select("title");
        String[] words = elements.text().split(" ");
        
        List<String> processed= new ArrayList<String>();
        for (String word:words)
        {
            //BY DETECT SPACES BARDO
            word = word.replaceAll("[^A-Za-z]", "");
            if(word!=""  && !stoppingWords.contains(word.toLowerCase())){
                processed.add(stem(word));
            }
        
        }
        System.out.println(processed);
        for(String word: processed)
        {
            if(dict.get(word) != null){

                int termFreq;
                int titlefreq;
                try {

                    termFreq = dict.get(word).getJSONObject("url").getInt("termFreq")+1;
                    titlefreq = dict.get(word).getJSONObject("url").getInt("titlefreq")+1;
                    dict.get(word).getJSONObject("url").put("termFreq", termFreq);
                    dict.get(word).getJSONObject("url").put("frequency", titlefreq);

                } catch (JSONException e) {
                    
                    e.printStackTrace();
                }
            
            }else{
                JSONObject obj = new JSONObject();
               
                try {
                    obj.put("termFreq", 1);
                    obj.put("headingsFreq", 0);
                    obj.put("titlefreq", 1);            
                    obj.put("textFreq", 0);
                    obj.put("url", url);
                   
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dict.put(word, obj);
                // System.out.println(obj);
                // System.out.println(json); 
                // System.out.println("------------------------------ "); 
            }
        }
        
        return dict;
    }

    public Hashtable<String,JSONObject> processHeadings( Document doc,String url)
    {
       
        Hashtable<String,JSONObject> dict = new Hashtable<>();
        Elements elements = doc.select("h1");
        doc.select("h2").forEach((e)->{
            elements.add(e);
        });
        doc.select("h3").forEach((e)->{
            elements.add(e);
        });
        doc.select("h4").forEach((e)->{
            elements.add(e);
        });
        doc.select("h5").forEach((e)->{
            elements.add(e);
        });
        doc.select("h6").forEach((e)->{
            elements.add(e);
        });
        
        String[] words = elements.text().split(" ");
        //System.out.println(elements);
        List<String> processed= new ArrayList<String>();
        for (String word:words)
        {
            //BY DETECT SPACES BARDO
            word = word.replaceAll("[^A-Za-z\t]", "");
            if(word!=""  && !stoppingWords.contains(word.toLowerCase())){
                processed.add(stem(word));
            }
           
         }
        for(String word: processed)
        {
            if(dict.get(word) != null){

                int termFreq;
                int headingsFreq;
                try {

                    termFreq = dict.get(word).getJSONObject("url").getInt("termFreq")+1;
                    headingsFreq = dict.get(word).getJSONObject("url").getInt("headingsFreq")+1;
                    dict.get(word).getJSONObject("url").put("termFreq", termFreq);
                    dict.get(word).getJSONObject("url").put("headingsFreq", headingsFreq);

                } catch (JSONException e) {
                    
                    e.printStackTrace();
                }
            
            }else{
                JSONObject obj = new JSONObject();
               
                try {
                    obj.put("termFreq", 0);
                    obj.put("headingsFreq", 1);
                    obj.put("titlefreq", 0);            
                    obj.put("textFreq", 0);
                    obj.put("url", url);
                   
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dict.put(word, obj);
                 System.out.println(dict);
                // System.out.println(json); 
                // System.out.println("------------------------------ "); 
            }
       }
        
        return dict;
    }

    public Hashtable<String,JSONObject> processtext( Document doc,String link) throws URISyntaxException, MalformedURLException
    {
        URI uri = new URI(link);
        String url = uri.getHost().toString();
        //String[] url_comp = url.split(".");
        // //String url = url_comp[1];
        // for (String a : url_comp)
        //     System.out.println(a);
        Hashtable<String,JSONObject> dict = new Hashtable<>();
        Elements elements = doc.select("p");
        doc.select("li").forEach((e)->{
            elements.add(e);
        });
        doc.select("pre").forEach((e)->{
            elements.add(e);
        });
        String[] words = elements.text().split(" ");
        
        List<String> processed= new ArrayList<String>();
        for (String word:words)
        {
            //BY DETECT SPACES BARDO
            word = word.replaceAll("[^A-Za-z]", "");
            if(word!=""  && !stoppingWords.contains(word.toLowerCase())){
                processed.add(stem(word));
            }
        
        }
        //System.out.println(processed);
        for(String word: processed)
        {
            if(dict.get(word) != null){

                int termFreq;
                int textFreq;
                try {

                    termFreq = dict.get(word).getJSONObject("url").getInt("termFreq")+1;
                    textFreq = dict.get(word).getJSONObject("url").getInt("textFreq")+1;
                    dict.get(word).getJSONObject("url").put("termFreq", termFreq);
                    dict.get(word).getJSONObject("url").put("textFreq", textFreq);

                } catch (JSONException e) {
                    
                    e.printStackTrace();
                }
            
            }else{
                JSONObject obj = new JSONObject();
               
                try {
                    obj.put("termFreq", 1);
                    obj.put("headingsFreq", 0);
                    obj.put("titlefreq", 0);            
                    obj.put("textFreq", 1);
                    obj.put("url", url);
                   
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dict.put(word, obj);
                // System.out.println(obj);
                // System.out.println(json); 
                // System.out.println("------------------------------ "); 
            }
        }
        
        return dict;
    }
}