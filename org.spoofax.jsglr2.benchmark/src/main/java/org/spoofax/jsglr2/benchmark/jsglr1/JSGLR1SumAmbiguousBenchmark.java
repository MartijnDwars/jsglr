package org.spoofax.jsglr2.benchmark.jsglr1;

import org.openjdk.jmh.annotations.Param;
import org.spoofax.jsglr2.testset.TestSet;

public class JSGLR1SumAmbiguousBenchmark extends JSGLR1Benchmark {
    
    public JSGLR1SumAmbiguousBenchmark() {
        super(TestSet.sumAmbiguous);
    }
    
    @Param({"5", "10", "15"})
    public int n;

}
