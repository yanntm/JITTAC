package jittac.jdt.contentassist;

import org.eclipse.jdt.ui.text.java.CompletionProposalComparator;

public class AlphabeticalConsistencyPropopsalSorter extends  AbstractConsistencyProposalSorter {
    private static final CompletionProposalComparator comparator;
    static {
        comparator = new CompletionProposalComparator();
        comparator.setOrderAlphabetically(true);
    }
 
    public AlphabeticalConsistencyPropopsalSorter() {
        super(comparator);
    }
}
