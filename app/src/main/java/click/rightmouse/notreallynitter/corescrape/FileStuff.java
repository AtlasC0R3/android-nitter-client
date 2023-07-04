package click.rightmouse.notreallynitter.corescrape;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class FileStuff {
    // This class is for all the file thingamajig functions and stuff.
    
    public static void ensureFileExists(String filename){
        try {
            File myObj = new File(filename);
            if (myObj.createNewFile()) {
              System.out.println("File created: " + myObj.getName());
            } else {
              System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("Couldn't ensure file exists. Pro tip: don't unplug the damn hard drive.");
            e.printStackTrace();
        }
    }

    public static void writeFile(String content, String filename){
        try {
            ensureFileExists(filename);
            FileWriter myWriter = new FileWriter(filename);
            myWriter.write(content);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("Couldn't write to the file. It's not nice to delete files while I'm trying to write to them!");
            e.printStackTrace();
        }
    }

    public static String readFile(String filename){
        try {
            File myObj = new File(filename);
            Scanner myReader = new Scanner(myObj);
            StringBuffer response = new StringBuffer();
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                response.append(data);
            }
            myReader.close();
            return response.toString();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            return "";
        }
    }
}
