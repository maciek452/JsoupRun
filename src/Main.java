import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;


public class Main {

    public static void main(String[] args){

        ArrayList<String> categories = new ArrayList<>();
        String filename = "products.csv";
        try {
            Files.delete(Paths.get(filename));
        }catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
        try {
            Files.write(Paths.get(filename), "".getBytes(), StandardOpenOption.CREATE);
        }catch (IOException e) {
            //exception handling left as an exercise for the reader
        }

        try {
            int index = getResults("https://www.ceneo.pl/Telefony_i_akcesoria", 1, filename, categories);
            for (int i = 1; i<37; i++) {
                index = getResults("https://www.ceneo.pl/Telefony_i_akcesoria;0020-30-0-0-" + i + ".htm", index, filename, categories);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String categoriesFileName = "categories.csv";

        try {
            Files.delete(Paths.get(categoriesFileName));
        }catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
        try {
            Files.write(Paths.get(categoriesFileName), "".getBytes(), StandardOpenOption.CREATE);
        }catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
        int i = 10;
        for (String category:   categories) {
            i++;
            appendLine(i + ";1;" + category + ";Produkty;0;Opis kategorii "+category+";Meta"+category+"\n", categoriesFileName);
        }
    }

    public static int getResults(String url, int index, String file, ArrayList<String> categories)throws IOException{
        Document d = Jsoup.connect(url).timeout(6000).get();
        Elements ele = d.select("div.category-list-body");
        int i = index;
        for (Element element : ele.select("div.cat-prod-row")) {

            String photoURL = element.select("div.cat-prod-row-foto a img").attr("src");
            if(photoURL.startsWith("/content"))
                photoURL = element.select("div.cat-prod-row-foto a img").attr("data-original");
            photoURL =  "https:" + photoURL;
            Elements product = element.select("div.cat-prod-row-content");
            String title = product.select("div.cat-prod-row-desc strong.cat-prod-row-name a").text();
            String[] ratings = product.select("div.cat-prod-row-desc span.prod-review span.score-container span.score-marker")
                    .attr("style").split(" ");
            String rating = "";
            if(ratings.length >=2)
                rating = ratings[1];
            Elements descriptions = product.select("div.cat-prod-row-desc ul.prod-params li");
            String description = "";
            for (Element line : descriptions) {
                description += line.text()+", ";
            }
            if(description.length() > 1)
                description = description.substring(0, description.length()-2);
            String[] markAndModel =  title.split(" ", 2);
            String mark = "";
            String model = "";
            if(markAndModel.length > 1) {
                mark = markAndModel[0];
                model =markAndModel[1];
            }
            if(rating.length()>1)
                rating = rating.substring(0, rating.length()-1);
            String category = product.select("div.cat-prod-row-desc p.cat-prod-row-category").text().substring(10);
            String price = product.select("div.cat-prod-row-price").text().split(" ")[0];
            String availability = product.select("div.cat-prod-row-price span.shop-numb").text().split(" ")[1];
            if(price.charAt(0)=='o')
                price = price.substring(2);
            if(!categories.contains(category))
                categories.add(category);
            appendLine(i+";"+mark+";"+model+";"+rating+";"+description+";"+category+";"+price+";"+availability+";"+photoURL+";1\n", file);
            i++;
        }
        return i;
    }

    public static void appendLine(String line, String file){
        try {
            Files.write(Paths.get(file), line.getBytes(), StandardOpenOption.APPEND);
        }catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
    }
}