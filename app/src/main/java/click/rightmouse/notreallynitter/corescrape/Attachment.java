package click.rightmouse.notreallynitter.corescrape;

import java.util.ArrayList;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Attachment {
    public String type = new String();  // image, gif, video
    public String url = new String();

    public static ArrayList<Attachment> loadFromAttachments(Element attachmentsDiv){
        ArrayList<Attachment> attachments = new ArrayList<>();
        Elements everyAttachment = attachmentsDiv.select("div.attachment");
        for(Element e : everyAttachment){
            Attachment attach = new Attachment();

            // check using class if it is "image", "gif". do actions with switches
            if(e.select("video").first() != null){
                // it's an animated attachment
                if(e.select("video").attr("class").equals("gif")) attach.type = "gif";
                // it is a gif
                else attach.type = "video";
                // it's not a gif (so it's likely an HLS video)
            } else {
                Element ufo = e.select("a").first();  // ufo: because we don't know what the fuck it is
                if(ufo == null) continue;
                switch (ufo.attr("class")) {
                    case "still-image":
                        attach.type = "image";
                        break;
                
                    default:
                        break;
                }
            }

            switch (attach.type) {
                case "video":
                    attach.url = ("https://nitter.absturztau.be" + e.select("video").attr("data-url"));
                    break;

                case "gif":
                    attach.url = ("https://nitter.absturztau.be" + e.select("video.gif > source").attr("src"));
                    break;
            
                case "image":
                    attach.url = ("https://nitter.absturztau.be" + e.select("a").attr("href"));
                    break;

                default:
                    break;
            }
            attachments.add(attach);
        }

        return attachments;
    }
}
