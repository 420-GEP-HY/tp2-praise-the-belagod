package com.example.libfluxrss;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.util.Xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ParseFluxRss {
    /**
     * @Author Julien Pineault
     * @param url sous forme de string de FluxRSS à désérialiser
     * @return la liste des items
     * Méthode permettant d'obtenir une liste d'items affichable depuis un flux
     */
    public List<RssItem> getItems(String url)throws ParserConfigurationException,SAXException,IOException{
        // À compléter
        List<RssItem> items = new ArrayList<>();
        Document dom;
        DocumentBuilder builder;

        builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        dom = builder.parse(url);
        items = parseFlux(dom);
        return items;
    }

    /**
     * @Author Julien Pineault  et Gabriel Bourque
     * @param doc document contenant les informations d'un flux
     * @return retourne la liste des items (flux rss)
     */
    public List<RssItem> parseFlux(Document doc){
        String titre = "";
        String lien = "";
        String desc = "";
        String mediaType = "";
        Bitmap image;
        RssItem item;
        InputStream in = null;
        @Nullable NodeList children;
        boolean isItem = false;
        List<RssItem> items = new ArrayList<>();

        /**
         * Ajout de l'item servant d'entête du flux au début de la liste.
         */

        String url = "";
        //Validation de la présence d'une image
        try
        {
            children = doc.getElementsByTagName("image").item(0).getChildNodes();
        }
        catch (NullPointerException ex)
        {
            children = null;
        }
        //Obtention de l'url de l'image si celle-ci n'est pas nulle.
        if (children != null)
            url = children.item(3).getTextContent();
        try {
            in = new URL(url).openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bmp = BitmapFactory.decodeStream(in);
        image = bmp;

        //Ajout des informations de présentation (détails) du flux
        items.add(new RssItem(doc.getElementsByTagName("title").item(0).getTextContent(),
                doc.getElementsByTagName("description").item(0).getTextContent(),
                doc.getElementsByTagName("link").item(0).getTextContent(),
                image, null));

        int nbElements = doc.getElementsByTagName("item").getLength();

        //NodeList pour les items
        NodeList nl = doc.getElementsByTagName("item");
        //NodeList pour les ChildNodes (détails) des items
        NodeList nl2;
        for (int i = 0; i < (nbElements - 1); i++)
        {
            nl2 = nl.item(i+1).getChildNodes();
            titre = doc.getElementsByTagName("title").item(i+1).getTextContent();
            //desc = doc.getElementsByTagName("description").item(i+1).getTextContent();
            desc = "";

            //Obtention de toutes les informations de chaque node contenu dans item, concaténées
            //dans une seule longue description.
            for (int j=0;j<nl2.getLength();j++)
            {
                desc += (nl2.item(j).getNodeName() + ": " + nl2.item(j).getTextContent());
            }
            //lien = doc.getElementsByTagName("link").item(i+1).getTextContent();

            //Validation de la présence de contenu média
            try
            {
                //Obtention des informations du contenu média si non-null.
                lien = doc.getElementsByTagName("media:content").item(i+1).getAttributes().getNamedItem("url").getTextContent();
                mediaType = doc.getElementsByTagName("media:content").item(i+1).getAttributes().getNamedItem("type").getTextContent();
            }
            catch (NullPointerException ex)
            {
                lien = "";
                mediaType = "";
            }

            try
            {
                children = doc.getElementsByTagName("media:content").item(i+1).getChildNodes();
            }
            catch (NullPointerException ex)
            {
                children = null;
            }
            if (children != null)
                url = children.item(7).getAttributes().getNamedItem("url").getTextContent();
            try {
                in = new URL(url).openStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            bmp = BitmapFactory.decodeStream(in);
            image = bmp;
            item = new RssItem(titre, desc, lien, image, mediaType);
            items.add(item);
        }

        return items;
    }
}
