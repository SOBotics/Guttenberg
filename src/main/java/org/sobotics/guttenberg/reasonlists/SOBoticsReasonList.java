package org.sobotics.guttenberg.reasonlists;

import java.util.ArrayList;
import java.util.List;

import org.sobotics.guttenberg.entities.Post;
import org.sobotics.guttenberg.reasons.*;

public class SOBoticsReasonList implements ReasonList {

	private Post target;
	private List<Post> originals;
	
	public SOBoticsReasonList(Post target, List<Post> originalPosts) {
		this.target = target;
		this.originals = originalPosts;
	}
	
	@Override
	public List<Reason> reasons() {
		return reasons(false);
	}
	
	@Override
	public List<Reason> reasons(boolean ignoringScores) {
		List<Reason> reasons = new ArrayList<Reason>();
		
		//reasons.add(new StringSimilarity(this.target, this.originals, ignoringScores));
		reasons.add(new ExactParagraphMatch(this.target, this.originals));
		reasons.add(new Winnowing(this.target, this.originals));

		
		return reasons;
	}

	@Override
	public String site() {
		return "stackoverflow";
	}

}
