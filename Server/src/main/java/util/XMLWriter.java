package util;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Chaun on 4/7/2016.
 */
public class XMLWriter {
    private XMLStreamWriter xmlStreamWriter;
    public XMLWriter(File file) {
        try {
            FileWriter writer = new FileWriter(file.getAbsolutePath());
            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(writer);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    public void writeStart() {
        try {
//            xmlStreamWriter.writeStartDocument();
            xmlStreamWriter.writeStartElement("WISEInkXML");
            xmlStreamWriter.writeStartElement("Paths");
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    public void writePathStart(String pointer, String points) {
        try {
            xmlStreamWriter.writeStartElement("Path");
            xmlStreamWriter.writeStartElement("Pointer");
            xmlStreamWriter.writeCharacters(pointer);
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeStartElement("Points");
            xmlStreamWriter.writeCharacters(points);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    public void writePathMove(String points) {
        try {
            xmlStreamWriter.writeCharacters(points);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    public void writePathEnd(String points) {
        try {
            xmlStreamWriter.writeCharacters(points);
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeEndElement();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    public void changePage() {
        try {
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeStartElement("Paths");
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    public void writeEnd() {
        try {
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeEndDocument();
            xmlStreamWriter.flush();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }
}
