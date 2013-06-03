package jittac.jdt.contentassist;

import org.eclipse.jdt.ui.text.java.CompletionProposalComparator;

public class ByRelevanceConsistencyPropopsalSorter extends  AbstractConsistencyProposalSorter {
    private static final CompletionProposalComparator comparator;
    static {
        comparator = new CompletionProposalComparator();
        comparator.setOrderAlphabetically(false);
    }
 
    public ByRelevanceConsistencyPropopsalSorter() {
        super(comparator);
    }
}
