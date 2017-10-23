package org.sobotics.guttenberg.search;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.sobotics.guttenberg.clients.Guttenberg;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.CustomsearchRequestInitializer;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;

/**
 * Internet search, search on internet based on SearchTerms
 * @author Petter Friberg
 *
 */
public class InternetSearch {
	
	
	/**
	 * Google search
	 * @param st
	 * @return
	 * @throws IOException
	 */
	public SearchResult google(SearchTerms st) throws IOException{
		Customsearch cs;
		try {
			cs = new Customsearch.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), null) 
					   .setApplicationName("Guttenberg") 
					   .setGoogleClientRequestInitializer(new CustomsearchRequestInitializer(Guttenberg.getLoginProperties().getProperty("google-api",""))) 
					   .build();
		} catch (GeneralSecurityException e) {
			throw new IOException("Can't instance GoogleNetHttpTransport",e);
		} 
		
		Customsearch.Cse.List list = cs.cse().list(st.getQuery()).setCx("002845322276752338984:vxqzfa86nqc"); 
		if (st.getExactTerm()!=null){
			list.setExactTerms(st.getExactTerm());
		}
		
		SearchResult sr = new SearchResult();
		Search result = list.execute();
		if (result.getItems()!=null){
			for (Result ri : result.getItems()) {
				boolean onsite = ri.getLink().toLowerCase().contains("stackoverflow.com");
				SearchItem si = new SearchItem(ri.getTitle(), ri.getLink(), onsite);
				sr.getItems().add(si);
			}
		}
		return sr;
		
	}

}
