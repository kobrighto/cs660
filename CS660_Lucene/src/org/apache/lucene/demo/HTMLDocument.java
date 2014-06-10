package org.apache.lucene.demo;


/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.*;
import java.nio.charset.StandardCharsets;

import org.apache.lucene.document.*;
import org.apache.lucene.demo.html.HTMLParser;

/** 
 * A utility for making Lucene Documents for HTML documents.
 * Modified by T.Y.Tung (2009/06/22) for NTU 2009 term project
 */
public class HTMLDocument {
  static char dirSep = System.getProperty("file.separator").charAt(0);

  public static String uid(File f) {
    // Append path and date into a string in such a way that lexicographic
    // sorting gives the same results as a walk of the file hierarchy.  Thus
    // null (\u0000) is used both to separate directory components and to
    // separate the path from the date.
    return f.getPath().replace(dirSep, '\u0000') +
      "\u0000" +
      DateTools.timeToString(f.lastModified(), DateTools.Resolution.SECOND);
  }

  public static String uid2url(String uid) {
    String url = uid.replace('\u0000', '/');	  // replace nulls with slashes
    return url.substring(0, url.lastIndexOf('/')); // remove date from end
  }

  public static Document Document(InputStream is, String docNo)
       throws IOException, InterruptedException  {
    // make a new, empty document
    Document doc = new Document();

    //HTMLParser parser = new HTMLParser(is);
      
    // Add the tag-stripped contents as a Reader-valued Text field so it will
    // get tokenized and indexed.
    doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))));
    //doc.add(new Field("contents", parser.getReader()));

    // Add the summary as a field that is stored and returned with
    // hit documents for display.
   // doc.add(new StringField("summary", parser.getSummary(), Field.Store.YES));

    // Add the title as a field that it can be searched and that is stored.
    //doc.add(new StringField("title", parser.getTitle(), Field.Store.YES));

    // added by T.Y.Tung due to the project setting
    doc.add(new StringField("DOCNO", docNo, Field.Store.YES));

    // return the document
    return doc;
  }

  private HTMLDocument() {}
}
    
