package org.spoofax.jsglr2.stack.elkhound;

import java.util.ArrayList;

import org.spoofax.jsglr2.parseforest.AbstractParseForest;
import org.spoofax.jsglr2.parser.Parse;
import org.spoofax.jsglr2.parser.Position;
import org.spoofax.jsglr2.stack.StackLink;
import org.spoofax.jsglr2.states.IState;

public class BasicElkhoundStackNode<ParseForest extends AbstractParseForest>
    extends AbstractElkhoundStackNode<ParseForest> {

    // Directed to the initial stack node
    private ArrayList<StackLink<ParseForest, AbstractElkhoundStackNode<ParseForest>>> linksOut =
        new ArrayList<StackLink<ParseForest, AbstractElkhoundStackNode<ParseForest>>>();

    public BasicElkhoundStackNode(int stackNumber, IState state, Position position, boolean isRoot) {
        super(stackNumber, state, position, isRoot);
    }

    @Override
    public Iterable<StackLink<ParseForest, AbstractElkhoundStackNode<ParseForest>>> getLinksOut() {
        return linksOut;
    }

    @Override
    public StackLink<ParseForest, AbstractElkhoundStackNode<ParseForest>> getOnlyLinkOut() {
        return linksOut.get(0);
    }

    @Override
    public StackLink<ParseForest, AbstractElkhoundStackNode<ParseForest>> addOutLink(
        StackLink<ParseForest, AbstractElkhoundStackNode<ParseForest>> link,
        Parse<ParseForest, AbstractElkhoundStackNode<ParseForest>> parse) {
        linksOut.add(link);

        link.to.referenceCount++;

        if(linksOut.size() == 1) { // This means the first link is just added.
            deterministicDepth = link.to.deterministicDepth + 1;
        } else if(linksOut.size() == 2) { // The second link is added; this means non-determinism
            deterministicDepth = 0;

            if(referenceCount > 0) {
                parse.observing.notify(observer -> observer.resetDeterministicDepth(this));

                for(AbstractElkhoundStackNode<ParseForest> stack : parse.activeStacks)
                    if(stack != this)
                        stack.resetDeterministicDepth();
            }
        } // We do not handle the case > 2, since the case == 2 already adjusted deterministic depths

        return link;
    }

    @Override
    public boolean allOutLinksRejected() {
        if(linksOut.isEmpty())
            return false;

        for(StackLink<ParseForest, AbstractElkhoundStackNode<ParseForest>> link : linksOut) {
            if(!link.isRejected())
                return false;
        }

        return true;
    }

}
