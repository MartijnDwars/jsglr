package org.spoofax.jsglr2.benchmark.jsglr2;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.spoofax.jsglr.client.InvalidParseTableException;
import org.spoofax.jsglr2.JSGLR2Variants;
import org.spoofax.jsglr2.JSGLR2Variants.ParseForestConstruction;
import org.spoofax.jsglr2.JSGLR2Variants.ParseForestRepresentation;
import org.spoofax.jsglr2.JSGLR2Variants.Reducing;
import org.spoofax.jsglr2.JSGLR2Variants.StackRepresentation;
import org.spoofax.jsglr2.actions.IReduce;
import org.spoofax.jsglr2.benchmark.BaseBenchmark;
import org.spoofax.jsglr2.benchmark.BenchmarkParserObserver;
import org.spoofax.jsglr2.parseforest.AbstractParseForest;
import org.spoofax.jsglr2.parser.IParser;
import org.spoofax.jsglr2.parser.ParseException;
import org.spoofax.jsglr2.parsetable.IParseTable;
import org.spoofax.jsglr2.parsetable.IState;
import org.spoofax.jsglr2.parsetable.IStateFactory;
import org.spoofax.jsglr2.parsetable.ParseTableReadException;
import org.spoofax.jsglr2.parsetable.ParseTableReader;
import org.spoofax.jsglr2.parsetable.ProductionToGotoRepresentation;
import org.spoofax.jsglr2.parsetable.StateFactory;
import org.spoofax.jsglr2.stack.AbstractStackNode;
import org.spoofax.jsglr2.testset.Input;
import org.spoofax.jsglr2.testset.TestSet;
import org.spoofax.terms.ParseError;

public abstract class JSGLR2GotoBenchmark extends BaseBenchmark {

    IParser<?, ?> parser;
    GotoObserver gotoObserver;

    protected JSGLR2GotoBenchmark(TestSet testSet) {
        super(testSet);
    }

    @Param({ "ForLoop", "JavaHashMap",
        "CapsuleImmutableBinaryRelation" }) public ProductionToGotoRepresentation representation;

    @Setup public void parserSetup() throws ParseError, ParseTableReadException, IOException,
        InvalidParseTableException, InterruptedException, URISyntaxException {
        IStateFactory stateFactory;

        switch(representation) {
            case CapsuleImmutableBinaryRelation:
                stateFactory = new StateFactory(ProductionToGotoRepresentation.CapsuleImmutableBinaryRelation);
                break;
            case ForLoop:
                stateFactory = new StateFactory(ProductionToGotoRepresentation.ForLoop);
                break;
            case JavaHashMap:
                stateFactory = new StateFactory(ProductionToGotoRepresentation.JavaHashMap);
                break;
            default:
                stateFactory = null;
                break;
        }

        IParseTable parseTable = new ParseTableReader(stateFactory).read(testSetReader.getParseTableTerm());

        parser = JSGLR2Variants.getParser(parseTable, ParseForestRepresentation.Basic, ParseForestConstruction.Full,
            StackRepresentation.Basic, Reducing.Basic);

        gotoObserver = new GotoObserver();

        parser.attachObserver(gotoObserver);

        try {
            for(Input input : inputs)
                parser.parseUnsafe(input.content, input.filename, null);
        } catch(ParseException e) {
            throw new IllegalStateException("setup of benchmark should not fail");
        }
    }

    class GotoLookup {

        final IState state;
        final int productionId;

        protected GotoLookup(IState state, int productionId) {
            this.state = state;
            this.productionId = productionId;
        }

        public int execute() {
            return state.getGotoId(productionId);
        }

    }

    class GotoObserver<StackNode extends AbstractStackNode<ParseForest>, ParseForest extends AbstractParseForest>
        extends BenchmarkParserObserver<StackNode, ParseForest> {

        public List<GotoLookup> gotoLookups = new ArrayList<GotoLookup>();

        @Override public void reducer(StackNode stack, IReduce reduce, ParseForest[] parseNodes,
            StackNode activeStackWithGotoState) {
            gotoLookups.add(new GotoLookup(stack.state, reduce.production().productionNumber()));
        }

    }

    @Benchmark public void benchmark(Blackhole bh) throws ParseException {
        for(GotoLookup gotoLookup : ((GotoObserver<?, ?>) gotoObserver).gotoLookups) {
            bh.consume(gotoLookup.execute());
        }
    }

}