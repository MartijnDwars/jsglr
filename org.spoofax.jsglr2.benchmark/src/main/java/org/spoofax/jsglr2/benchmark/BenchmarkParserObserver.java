package org.spoofax.jsglr2.benchmark;

import java.util.Queue;
import org.spoofax.jsglr2.actions.IAction;
import org.spoofax.jsglr2.actions.IReduce;
import org.spoofax.jsglr2.parseforest.AbstractParseForest;
import org.spoofax.jsglr2.parser.ForShifterElement;
import org.spoofax.jsglr2.parser.IParserObserver;
import org.spoofax.jsglr2.parser.Parse;
import org.spoofax.jsglr2.parser.ParseFailure;
import org.spoofax.jsglr2.parser.ParseSuccess;
import org.spoofax.jsglr2.parsetable.IProduction;
import org.spoofax.jsglr2.stack.AbstractStackNode;
import org.spoofax.jsglr2.stack.StackLink;

public class BenchmarkParserObserver<StackNode extends AbstractStackNode<ParseForest>, ParseForest extends AbstractParseForest> implements IParserObserver<StackNode, ParseForest> {
	
	public void parseStart(Parse<StackNode, ParseForest> parse) {}
	
	public void parseCharacter(int character, Iterable<StackNode> activeStacks) {}
	
	public void createStackNode(StackNode stack) {}
	
	public void createStackLink(StackLink<StackNode, ParseForest> link) {}
	
	public void rejectStackLink(StackLink<StackNode, ParseForest> link) {}
    
	public void forActorStacks(Queue<StackNode> forActor, Queue<StackNode> forActorDelayed) {}
	
	public void actor(StackNode stack, int currentChar, Iterable<IAction> applicableActions) {}
    
	public void skipRejectedStack(StackNode stack) {}
	
	public void addForShifter(ForShifterElement<StackNode, ParseForest> forShifterElement) {}
	
	public void reduce(IReduce reduce, ParseForest[] parseNodes, StackNode activeStackWithGotoState) {}
    
	public void directLinkFound(StackLink<StackNode, ParseForest> directLink) {}
	
	public void accept(StackNode acceptingStack) {}
	
	public void createParseNode(ParseForest parseNode, IProduction production) {}
	
	public void createDerivation(ParseForest[] parseNodes) {}
	
	public void createCharacterNode(ParseForest characterNode, int character) {}
	
	public void addDerivation(ParseForest parseNode) {}
	
	public void shifter(ParseForest termNode, Queue<ForShifterElement<StackNode, ParseForest>> forShifter) {}
	
	public void remark(String remark) {}
	
	public void success(ParseSuccess<ParseForest> success) {}

	public void failure(ParseFailure<ParseForest> failure) {
		throw new IllegalStateException("Failing parses not allowed during benchmarks");
	}
	
}