package click.rightmouse.notreallynitter.corescrape;

import androidx.annotation.NonNull;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Profile {
    public String username = "";
    public String handle = "";
    public String biography = "";

    public String profilePhotoURL = "";
    public String bannerPhotoURL = "";

    public String location = "";
    public String website = "";
    public String joinDate = "";

    public int tweets = 0;
    public int follows = 0;
    public int followers = 0;
    public int likes = 0;

    public boolean verified = false;
    public boolean privateAcc = false;

    public static int parseNitterNumbers(String number){
        return Integer.parseInt(number.replace(",", ""));
    }

    @NonNull
    public String toString(){
        return String.format("""
                %s (%s)
                %s

                ...located in %s, website is %s, joined %s
                %s tweets, following %s and followed by %s, %s likes
                verified? %s; private? %s""",
            this.username, this.handle, 
            this.biography,
            this.location, this.website, this.joinDate,
            this.tweets, this.follows, this.followers, this.likes,
            this.verified, this.privateAcc);
    }

    public Profile fromDocument(Document page){        
        Elements usernameElement = page.select("a.profile-card-fullname");
        username = usernameElement.text();
        Elements handleElement = page.select("a.profile-card-username");
        handle = handleElement.text();

        page.outputSettings().prettyPrint(false);

        Element bioElement = page.select("div.profile-bio > p").first();
        if(bioElement != null) biography = bioElement.wholeText();

        Elements locationElement = page.select(".profile-location");
        location = locationElement.text();
        Elements personalSiteElement = page.select(".profile-website >span >:last-child");
        website = personalSiteElement.attr("href");
        Elements joinDateElement = page.select("div.profile-joindate >span");
        joinDate = joinDateElement.attr("title");

        Elements profilePhotoElement = page.select("a.profile-card-avatar");
        profilePhotoURL = profilePhotoElement.attr("href");
        Elements bannerElement = page.select("div.profile-banner > a");
        bannerPhotoURL = bannerElement.attr("href");

        Elements statElements = page.select("ul.profile-statlist");

        tweets = parseNitterNumbers(statElements.select(".posts .profile-stat-num").text());
        follows = parseNitterNumbers(statElements.select(".following .profile-stat-num").text());
        followers = parseNitterNumbers(statElements.select(".followers .profile-stat-num").text());
        likes = parseNitterNumbers(statElements.select(".likes .profile-stat-num").text());

        Elements verifiedElement = page.select(".profile-card-fullname .icon-container .verified-icon");
        Elements privatedElement = page.select(".profile-card-fullname .icon-container .icon-lock");
        
        if(verifiedElement.first() != null) verified = true;
        if(privatedElement.first() != null) privateAcc = true;

        return this;
    }

    public Profile fromAttribution(Element attribution){
        handle = attribution.attr("href").replace("/", "");
        username = attribution.text();
        profilePhotoURL = attribution.select("img.avatar").attr("src");
        if(attribution.select("icon-container").first() != null) verified = true;
            
        return this;
    }
}
