/*
 * Copyright (C) 2019 SOBotics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.sobotics.guttenberg.search;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.CustomsearchRequestInitializer;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;
import org.sobotics.guttenberg.clients.Guttenberg;
import org.sobotics.guttenberg.entities.Post;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Internet search, search on internet based on SearchTerms
 *
 * @author Petter Friberg
 */
public class InternetSearch {


  public static void main(String[] args) throws GeneralSecurityException, IOException {

    String searchQuery = "test"; //The query to search
    String cx = "002845322276752338984:vxqzfa86nqc"; //Your search engine

    //Instance Customsearch
    Customsearch cs = new Customsearch.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), null)
      .setApplicationName("MyApplication")
      .setGoogleClientRequestInitializer(new CustomsearchRequestInitializer("your api key"))
      .build();

    //Set search parameter
    Customsearch.Cse.List list = cs.cse().list(searchQuery).setCx(cx);

    //Execute search
    Search result = list.execute();
    if (result.getItems() != null) {
      for (Result ri : result.getItems()) {
        //Get title, link, body etc. from search
        System.out.println(ri.getTitle() + ", " + ri.getLink());
      }
    }

  }


  /**
   * Google search
   *
   * @param st
   * @return
   * @throws IOException
   */
  public SearchResult google(Post post, SearchTerms st) throws IOException {
    Customsearch cs;
    try {
      cs = new Customsearch.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), null)
        .setApplicationName("Guttenberg")
        .setGoogleClientRequestInitializer(new CustomsearchRequestInitializer(Guttenberg.getLoginProperties().getProperty("google-api", "")))
        .build();
    } catch (GeneralSecurityException e) {
      throw new IOException("Can't instance GoogleNetHttpTransport", e);
    }

    Customsearch.Cse.List list = cs.cse().list(st.getQuery()).setCx("002845322276752338984:vxqzfa86nqc");
    if (st.getExactTerm() != null) {
      list.setExactTerms(st.getExactTerm());
    }

    SearchResult sr = new SearchResult(post);
    Search result = list.execute();
    if (result.getItems() != null) {
      for (Result ri : result.getItems()) {
        boolean onsite = ri.getLink().toLowerCase().contains("stackoverflow.com");
        SearchItem si = new SearchItem(ri.getTitle(), ri.getLink(), onsite);
        sr.getItems().add(si);
      }
    }
    return sr;

  }
}
