package org.spoofax.jsglr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.spoofax.jsglr.StructureSkipper.indentShift;

public class NewStructureSkipper implements IStructureSkipper {

    private final static int MAX_NR_OF_LINES=30;
    private final static int MAX_NR_OF_STRUCTURES=20;
    
    enum indentShift{
        INDENT,
        DEDENT,
        SAME_INDENT
    }
    
    private SGLR myParser;
    private int failureIndex;
    private StructuralTokenRecognizer structTokens;
    
    public ParserHistory getHistory() {
        return myParser.getHistory();
    }
    
    public NewStructureSkipper(SGLR sglr){   
        myParser=sglr;
        structTokens=new StructuralTokenRecognizer();
    }
    
    public void clear() {
        // TODO Auto-generated method stub

    }

    public ArrayList<StructureSkipSuggestion> getCurrentSkipSuggestions()
            throws IOException {
        int indexLastLine=failureIndex;
        if (isScopeOpeningLine(indexLastLine) && indexLastLine>0 && getHistory().getLine(indexLastLine-1).getIndentValue()==getHistory().getLine(indexLastLine).getIndentValue())
            return selectRegion(indexLastLine-1);
        return selectRegion(indexLastLine);
    }
    
    private ArrayList<StructureSkipSuggestion> selectRegion(int indexLine)
    throws IOException {
        if (isScopeClosingLine(indexLine))
            return new ArrayList<StructureSkipSuggestion>();
        IndentInfo startLine = getHistory().getLine(indexLine);
        return selectRegion(indexLine, startLine);
    }

    private ArrayList<StructureSkipSuggestion> selectRegion(int indexLine,
            IndentInfo lineXX) throws IOException {           
        if (isScopeClosingLine(indexLine))
            return new ArrayList<StructureSkipSuggestion>();        
        ArrayList<Integer> endLocations=findCurrentEnd(indexLine);
        ArrayList<StructureSkipSuggestion> skipSuggestions=new ArrayList<StructureSkipSuggestion>();
        IndentInfo startLine=IndentInfo.cloneIndentInfo(getHistory().getLine(indexLine));
        for (Integer endSkipIndex : endLocations) {
            if (endSkipIndex>indexLine) {
                IndentInfo endSkip = IndentInfo.cloneIndentInfo(getHistory()
                        .getLine(endSkipIndex));
                StructureSkipSuggestion skipConstruct = new StructureSkipSuggestion();
                skipConstruct.setSkipLocations(startLine, endSkip, indexLine,
                        endSkipIndex);
                skipSuggestions.add(skipConstruct);
                addSeparatorIncludingRegion_Forwards(skipSuggestions,
                        skipConstruct);
                addSeperatorIncludingRegion_Backwards(skipSuggestions,
                        skipConstruct);
            }
        }        
        return skipSuggestions;
    }    
    
    /*
    private ArrayList<StructureSkipSuggestion> selectRegion(int indexLine,
            IndentInfo lineXX) throws IOException {
        IndentInfo line = getHistory().getLine(indexLine);
        IndentInfo startLine = IndentInfo.cloneIndentInfo(line);
        //IndentInfo startLine = IndentInfo.cloneIndentInfo(line);
        if (isScopeClosingLine(startLine))
            return new ArrayList<StructureSkipSuggestion>();
        //ArrayList<IndentInfo> endLocations=findCurrentEnd(startLine);
        Arr
        ArrayList<StructureSkipSuggestion> skipSuggestions=new ArrayList<StructureSkipSuggestion>();
        for (IndentInfo endSkip : endLocations) {
            StructureSkipSuggestion skipConstruct=new StructureSkipSuggestion();
            skipConstruct.setSkipLocations(startLine, endSkip, indexLine, -1);            
            skipSuggestions.add(skipConstruct);
            addSeparatorIncludingRegion_Forwards(skipSuggestions, skipConstruct);
            addSeperatorIncludingRegion_Backwards(skipSuggestions, skipConstruct);
        }        
        return skipSuggestions;
    } */   

    public StructureSkipSuggestion getErroneousPrefix() throws IOException {
        // TODO Auto-generated method stub
        return new StructureSkipSuggestion();
    }

    public ArrayList<StructureSkipSuggestion> getPickErroneousChild(
            StructureSkipSuggestion prevRegion) throws IOException {
        // TODO Auto-generated method stub
        return new ArrayList<StructureSkipSuggestion>();
    }
    
    public ArrayList<StructureSkipSuggestion> getParentSkipSuggestions() throws IOException{
        ArrayList<StructureSkipSuggestion> parentSkips = new ArrayList<StructureSkipSuggestion>();
        int errorLineIndex=failureIndex;
        for (int i = 0; i < 3; i++) {  //TODO: better control structure          
            int startSkipIndex = findParentBegin(errorLineIndex);
            ArrayList<StructureSkipSuggestion> skips=selectRegion(startSkipIndex);
            if(skips.isEmpty()){
                StructureSkipSuggestion closingSkip=new StructureSkipSuggestion();
                closingSkip.setSkipLocations(IndentInfo.cloneIndentInfo(getHistory().getLine(startSkipIndex)), IndentInfo.cloneIndentInfo(getHistory().getLine(failureIndex)), startSkipIndex, failureIndex);
                parentSkips.add(closingSkip);
            }
            parentSkips.addAll(skips);
            if(skips.size()>0)
                errorLineIndex=skips.get(0).getIndexHistoryStart();
            else
                i=50;
        }
        return parentSkips;
        /*
        IndentInfo endSkip=findParentEnd(startLine);
        IndentInfo startSkip = IndentInfo.cloneIndentInfo(getHistory().getLine(startSkipIndex));
        ArrayList<StructureSkipSuggestion> skipSuggestions=new ArrayList<StructureSkipSuggestion>();
        StructureSkipSuggestion skipConstruct=new StructureSkipSuggestion();            
        skipConstruct.setSkipLocations(startSkip, endSkip, startSkipIndex, -1);
        skipSuggestions.add(skipConstruct);                
        return skipSuggestions;
        */
    }   
    /*
    private IndentInfo findParentEnd(IndentInfo startLine) throws IOException{
        getHistory().setTokenIndex(startLine.getTokensSeen());
        int indentStartLine=startLine.getIndentValue();
        IndentInfo nextLine=skipLine(startLine);
        while(myParser.currentToken!=SGLR.EOF){            
            int indentSkipPosition=nextLine.getIndentValue();
            indentShift shift=calculateShift(indentStartLine, indentSkipPosition);
            if (shift==indentShift.DEDENT) {  
                if(isScopeClosingLine(nextLine)){
                    nextLine=skipLine(nextLine);
                    if(nextLine==null)
                        break;
                }                              
                return nextLine;                
            
            }
            nextLine=skipLine(nextLine);
        }         
        return nextLine; //EOF
    }*/

    private int findParentBegin(int startLineIndex) throws IOException{
        IndentInfo startLine = IndentInfo.cloneIndentInfo(getHistory().getLine(startLineIndex));
        int indentStartLine=separatorIndent(startLine); //startLine.getIndentValue();
        int indexHistoryLines=startLineIndex;
        while(indexHistoryLines > 0){
            indexHistoryLines-=1;
            IndentInfo currentLine=getHistory().getLine(indexHistoryLines);
            int indentSkipPosition=separatorIndent(currentLine); //currentLine.getIndentValue();
            indentShift shift=calculateShift(indentStartLine, indentSkipPosition);
            if (shift==indentShift.DEDENT){
                if(isScopeOpeningLine(indexHistoryLines))
                {
                        IndentInfo prevLine = getHistory().getLine(indexHistoryLines-1);
                        if((!isScopeClosingLine(prevLine)) && calculateShift(currentLine.getIndentValue(), prevLine.getIndentValue())==indentShift.SAME_INDENT){                            
                            return indexHistoryLines-1;
                        }                        
                }                
                return indexHistoryLines;
            }            
        }        
        return 0; //SOF
    } 
    
    public ArrayList<StructureSkipSuggestion> getPreviousSkipSuggestions()
            throws IOException {
        int indexEnd=failureIndex;
        return selectPrevRegion(indexEnd);
    }

    private ArrayList<StructureSkipSuggestion> selectPrevRegion(int indexEnd)
            throws IOException {
        ArrayList<StructureSkipSuggestion> prevRegions=new ArrayList<StructureSkipSuggestion>();       
        boolean onClosing=isScopeClosingLine(indexEnd);
        int indexStart = backwardsSkip(indexEnd, onClosing);
        if(onClosing){
            if(indexEnd>0){
               if(isScopeClosingLine(indexEnd-1))
                   prevRegions.addAll(selectPrevRegion(indexEnd-1));
               else
                   prevRegions.addAll(selectRegion(indexEnd-1));
            }
            indexEnd++;
        }         
        IndentInfo endSkip=IndentInfo.cloneIndentInfo(getHistory().getLine(indexEnd));
        if(indexStart<0)
            return prevRegions;
        IndentInfo startSkip=IndentInfo.cloneIndentInfo(getHistory().getLine(indexStart));
        StructureSkipSuggestion previousRegion=new StructureSkipSuggestion();
        previousRegion.setSkipLocations(startSkip, endSkip, indexStart, indexEnd);
        prevRegions.add(previousRegion);
        
        addSeperatorIncludingRegion_Backwards(prevRegions, previousRegion);
        addSeparatorIncludingRegion_Forwards(prevRegions, previousRegion);            
        return prevRegions;
    }

    private void addSeperatorIncludingRegion_Backwards(
            ArrayList<StructureSkipSuggestion> prevRegions, 
            StructureSkipSuggestion previousRegion) throws IOException {
        int indexStart=previousRegion.getIndexHistoryStart();
        if(indexStart>0 && isSeparatorEndingLine(indexStart-1)){
            char[] toParse = structTokens.removeSeparatorAtTheEnd(readLine(indexStart-1));
            IndentInfo startSkip2=IndentInfo.cloneIndentInfo(getHistory().getLine(indexStart-1));
            IndentInfo endSkip2=IndentInfo.cloneIndentInfo(previousRegion.getEndSkip());
            StructureSkipSuggestion previousRegion2=new StructureSkipSuggestion();
            previousRegion2.setSkipLocations(startSkip2, endSkip2, indexStart-1, previousRegion.getIndexHistoryEnd());
            previousRegion2.setAdditionalTokens(toParse);
            prevRegions.add(previousRegion2);
        }
    }

    private void addSeparatorIncludingRegion_Forwards(
            ArrayList<StructureSkipSuggestion> regions,
            StructureSkipSuggestion aRegion) throws IOException {
        if(isSeparatorStartingLine(aRegion.getEndSkip())){
            IndentInfo startSkip3=IndentInfo.cloneIndentInfo(aRegion.getStartSkip());
            IndentInfo endSkip3=IndentInfo.cloneIndentInfo(aRegion.getEndSkip());
            int indentShift=separatorIndent(endSkip3)- endSkip3.getIndentValue();
            endSkip3.setTokensSeen(endSkip3.getTokensSeen()+indentShift);
            StructureSkipSuggestion previousRegion3=new StructureSkipSuggestion();
            previousRegion3.setSkipLocations(startSkip3, endSkip3, aRegion.getIndexHistoryStart(), aRegion.getIndexHistoryEnd());
            regions.add(previousRegion3);
        }
    }
    
    private int backwardsSkip(int indexLine, boolean onClosing) throws IOException { 
        int indentValue = getHistory().getLine(indexLine).getIndentValue();
        boolean sawChilds=false;
        boolean closingSeen=onClosing;
        boolean openingSeen=false;
        boolean ignoreSeps=!isSeparatorStartingLine(indexLine);
        int indexHistoryLines=indexLine;
        while(indexHistoryLines>0){
            indexHistoryLines-=1;
            int indentSkipPosition=getHistory().getLine(indexHistoryLines).getIndentValue();
            indentShift shift=calculateShift(indentValue, indentSkipPosition);
            switch (shift) {
            case DEDENT:
                if(!sawChilds)
                    return -1;
                return indexHistoryLines;              
            case INDENT:      
                if(!ignoreSeps && !isSeparatorStartingLine(indexHistoryLines) && separatorIndent(indexLine)==indentSkipPosition)
                    return indexHistoryLines;
                sawChilds=true; //TODO: lastChild instead of previous struct?
                break;
            case SAME_INDENT: 
                if(!sawChilds && isScopeClosingLine(indexHistoryLines)){
                    if(closingSeen)
                        return indexHistoryLines+1;
                    closingSeen=true;
                }
                else
                    if(sawChilds && isScopeOpeningLine(indexHistoryLines))
                        openingSeen=true;
                    else
                        if(!(ignoreSeps && isSeparatorStartingLine(indexHistoryLines)))
                            return indexHistoryLines;        
                break;
            default:
                break;
            }
        }  
        if(indexLine != 0)
            return 0;//SOF
        return -1;
    }  
    
    private int separatorIndent(int indexLine) throws IOException {
        int indentValue = getHistory().getLine(indexLine).getIndentValue();
        String lineContent = readLine(indexLine);
        return indentValue+structTokens.separatorIndent(lineContent);
    }
    
    private int separatorIndent(IndentInfo line) throws IOException {
        int indentValue = line.getIndentValue();
        String lineContent = readLine(line);
        return indentValue+structTokens.separatorIndent(lineContent);
    }

    private boolean isScopeOpeningLine(IndentInfo line) throws IOException {
        String lineContent = readLine(line);
        return structTokens.isScopeOpeningLine(lineContent);
    }
    
    private boolean isScopeOpeningLine(int index) throws IOException {
        String lineContent = readLine(index);
        return structTokens.isScopeOpeningLine(lineContent);
    }
    
    private boolean isSeparatorStartingLine(int index) throws IOException {
        String lineContent = readLine(index);
        return structTokens.isSeparatorStartedLine(lineContent);
    }
    
    private boolean isSeparatorStartingLine(IndentInfo line) throws IOException {
        String lineContent = readLine(line);
        return structTokens.isSeparatorStartedLine(lineContent);
    }
    
    private boolean isSeparatorEndingLine(int index) throws IOException {
        String lineContent = readLine(index);
        return structTokens.isSeparatorEndingLine(lineContent);
    }
    
    private boolean isSeparatorEndingLine(IndentInfo line) throws IOException {
        String lineContent = readLine(line);
        return structTokens.isSeparatorEndingLine(lineContent);
    }
    
    private boolean isSeparatorLine(IndentInfo line) throws IOException {
        String lineContent = readLine(line);
        return structTokens.isSeparatorStartedLine(lineContent);
    }

    private indentShift calculateShift(int indentStartLine, int indentSkipPosition) {
        int difference=indentStartLine-indentSkipPosition;
        if(difference>0)
            return indentShift.DEDENT;
        if(difference<0)
            return indentShift.INDENT;
        return indentShift.SAME_INDENT;
    } 

    private boolean isScopeClosingLine(int index) throws IOException {
        String lineContent = readLine(index);
        return structTokens.isScopeClosingLine(lineContent);
    }
    private boolean isScopeClosingLine(IndentInfo line) throws IOException {
        String lineContent = readLine(line);
        return structTokens.isScopeClosingLine(lineContent);
    }

    private String readLine(int index) throws IOException {
        while(getHistory().getIndexLastLine()<=index && myParser.currentToken!=SGLR.EOF)
            getHistory().readRecoverToken(myParser);
        if(index<=getHistory().getIndexLastLine()){
            IndentInfo line=getHistory().getLine(index);
            return readLine(line);
        }
        return "";
    }

    private String readLine(IndentInfo line) {
        int startTok = line.getTokensSeen();
        String lineContent=getHistory().readLine(startTok);
        return lineContent;
    }

    public ArrayList<StructureSkipSuggestion> getPriorSkipSuggestions()
            throws IOException {
        int pos=failureIndex;
        return getPriorRegions(pos);
    }

    private ArrayList<StructureSkipSuggestion> getPriorRegions(int pos)
            throws IOException {
        ArrayList<StructureSkipSuggestion> priorRegions= new ArrayList<StructureSkipSuggestion>();
        ArrayList<StructureSkipSuggestion> prevRegions=selectPrevRegion(pos);
        do{
            if(!prevRegions.isEmpty())
                pos=prevRegions.get(0).getIndexHistoryStart();
            prevRegions=selectPrevRegion(pos);
            priorRegions.addAll(prevRegions);
        }while (pos>0 && !prevRegions.isEmpty());            
        return priorRegions;
    }
    
    public ArrayList<StructureSkipSuggestion> getCurrentAndNextSkipSuggestions()
    throws IOException {
        ArrayList<StructureSkipSuggestion> nextRegions= new ArrayList<StructureSkipSuggestion>();
        ArrayList<StructureSkipSuggestion> currRegions=selectRegion(failureIndex);
        int i=0;
        do{
            i++;
            for (StructureSkipSuggestion r : currRegions) {
                if(r.getAdditionalTokens().length==0)
                nextRegions.add(r);
            }            
            if(!currRegions.isEmpty()){
                System.out.println(currRegions.get(0).getIndexHistoryStart()+" => "+ currRegions.get(0).getIndexHistoryEnd());
                currRegions=selectRegion(currRegions.get(0).getIndexHistoryEnd(), currRegions.get(0).getEndSkip());
            }
        }while (i<10 && !currRegions.isEmpty());
        return nextRegions;
    }

    public ArrayList<StructureSkipSuggestion> getSibblingBackwardSuggestions()
            throws IOException {
        ArrayList<StructureSkipSuggestion> bwSkips=new ArrayList<StructureSkipSuggestion>();
        ArrayList<StructureSkipSuggestion> priorSiblings=getPriorRegions(failureIndex);
        ArrayList<StructureSkipSuggestion> currentRegionSuggestions=selectRegion(failureIndex);
        for (StructureSkipSuggestion currSugestion : currentRegionSuggestions) {
            for (int i = 0; i < priorSiblings.size(); i++) {
                StructureSkipSuggestion priorSuggestion=priorSiblings.get(i);
                if(currSugestion.getAdditionalTokens().length==0){//ignore suggestions based on adding the separator
                    StructureSkipSuggestion mergedSkip=mergeRegions(currSugestion, priorSuggestion);
                    bwSkips.add(mergedSkip);
                }
            }
        }
        return bwSkips;
    }

    private StructureSkipSuggestion mergeRegions(StructureSkipSuggestion fwSuggestion,
            StructureSkipSuggestion bwSuggestion) {
        StructureSkipSuggestion mergedSkip=new StructureSkipSuggestion();
        mergedSkip.setSkipLocations(IndentInfo.cloneIndentInfo(bwSuggestion.getStartSkip()), IndentInfo.cloneIndentInfo(fwSuggestion.getEndSkip()), bwSuggestion.getIndexHistoryStart(), fwSuggestion.getIndexHistoryEnd());
        mergedSkip.setAdditionalTokens(bwSuggestion.getAdditionalTokens());
        return mergedSkip;
    }

    public ArrayList<StructureSkipSuggestion> getSibblingForwardSuggestions()
            throws IOException {
        ArrayList<StructureSkipSuggestion> fwSkips=new ArrayList<StructureSkipSuggestion>();
        ArrayList<StructureSkipSuggestion> nextSiblings=getCurrentAndNextSkipSuggestions();
        ArrayList<StructureSkipSuggestion> prevRegionSuggestions=selectPrevRegion(failureIndex);
        for (StructureSkipSuggestion priorSuggestion : prevRegionSuggestions) {
            for (int i = 0; i < nextSiblings.size(); i++) {
                StructureSkipSuggestion nextSuggestion=nextSiblings.get(i);
                StructureSkipSuggestion mergedSkip=mergeRegions(nextSuggestion, priorSuggestion);
                fwSkips.add(mergedSkip);            
            }
        }
        return fwSkips;
    }

    public ArrayList<StructureSkipSuggestion> getSibblingSurroundingSuggestions()
            throws IOException {
        ArrayList<StructureSkipSuggestion> surroundingSkips=new ArrayList<StructureSkipSuggestion>();
        ArrayList<StructureSkipSuggestion> priorSiblings=getPriorRegions(failureIndex);
        ArrayList<StructureSkipSuggestion> nextSiblings=getCurrentAndNextSkipSuggestions();
        if(nextSiblings.size()>1 && priorSiblings.size()>0){
            nextSiblings.remove(0);
            StructureSkipSuggestion nextSuggestion=null;
            StructureSkipSuggestion priorSuggestion=null;
            int j=0;
            int i = 0;            
            while(i < nextSiblings.size() || j < priorSiblings.size()) {                
                if (i<nextSiblings.size()) {
                    nextSuggestion = nextSiblings.get(i); 
                    i++;
                }                         
                if (j<priorSiblings.size()) {
                    priorSuggestion = priorSiblings.get(j);  
                    j++;
                }
                StructureSkipSuggestion mergedSkip=mergeRegions(nextSuggestion, priorSuggestion);
                surroundingSkips.add(mergedSkip);
                if (j<priorSiblings.size()) {
                    priorSuggestion = priorSiblings.get(j); 
                    if(priorSuggestion.getAdditionalTokens().length!=0){
                        StructureSkipSuggestion mergedSkipPlus=mergeRegions(nextSuggestion, priorSuggestion);
                        surroundingSkips.add(mergedSkipPlus);
                        j++;
                    }
                }
            }
        }
        return surroundingSkips;
    }

    /* (non-Javadoc)
     * @see org.spoofax.jsglr.IStructureSkipper#getZoomOnPreviousSuggestions(org.spoofax.jsglr.StructureSkipSuggestion)
     */
    public ArrayList<StructureSkipSuggestion> getZoomOnPreviousSuggestions(StructureSkipSuggestion prevRegion) throws IOException{
        ArrayList<StructureSkipSuggestion> result = new ArrayList<StructureSkipSuggestion>();
        if(!prevRegion.canBeDecomposed()){ 
            //result.add(prevRegion);
            return result;
        }
        ArrayList<Integer> indentLevels=new ArrayList<Integer>();       
        for (int i = prevRegion.getIndexHistoryStart(); i < prevRegion.getIndexHistoryEnd(); i++) {
            int indentOfLine=getHistory().getLine(i).getIndentValue();
            if(!indentLevels.contains(indentOfLine))
                indentLevels.add(indentOfLine);
        }
        Collections.sort(indentLevels);
        indentLevels.remove(0);       
        System.out.println(indentLevels);        
        int indentOfLevel;
        int lineIndex;
        for (int level = 0; level < indentLevels.size(); level++) {
            indentOfLevel=indentLevels.get(level);
            lineIndex = prevRegion.getIndexHistoryStart();            
            while (lineIndex < prevRegion.getIndexHistoryEnd()) {
                int indentOfLine=getHistory().getLine(lineIndex).getIndentValue();
                if(indentOfLine==indentOfLevel){                    
                    ArrayList<StructureSkipSuggestion> regions = selectRegion(lineIndex);                     
                    if(regions.size()>0){
                        lineIndex=regions.get(0).getIndexHistoryEnd();
                        Collections.reverse(regions);
                        result.addAll(regions);
                    }
                    else
                        lineIndex++;
                }
                else
                    lineIndex++;
            }
       }    
        Collections.reverse(result);
        return result;
    }

    

    public void setFailureIndex(int failureIndex) {
       this.failureIndex=failureIndex;
    }
    
    private ArrayList<IndentInfo> findCurrentEnd(IndentInfo startLine) throws IOException{
        getHistory().setTokenIndex(startLine.getTokensSeen());
        int indentStartLine=separatorIndent(startLine);        
        boolean hasIndentChilds=false;
        boolean isSecondLine=true;
        ArrayList<IndentInfo> endLocations=new ArrayList<IndentInfo>();
        IndentInfo nextLine = skipLine(startLine);
        while(myParser.currentToken!=SGLR.EOF){            
            int indentSkipPosition=nextLine.getIndentValue();
            indentShift shift=calculateShift(indentStartLine, indentSkipPosition);
            switch (shift) {
            case DEDENT:               
                endLocations.add(nextLine);                
                return endLocations;                
            case INDENT:
                hasIndentChilds=true;
                break;
            case SAME_INDENT:
                if(hasIndentChilds && isScopeClosingLine(nextLine)){
                    nextLine = skipLine(nextLine);
                    endLocations.add(nextLine);
                    return endLocations;
                }
                if((!isSecondLine || !isScopeOpeningLine(nextLine)) && !isSeparatorLine(nextLine)){
                     endLocations.add(nextLine);
                     return endLocations;
                }
                break;
            default:
                break;
            }
            isSecondLine=false;
            nextLine=skipLine(nextLine);
        }
        endLocations.add(nextLine); //EOF
        return endLocations;
    }
    
    private ArrayList<Integer> findCurrentEnd(int indexStartLine) throws IOException{
        IndentInfo startLine=getHistory().getLine(indexStartLine);
        int indentStartLine=separatorIndent(startLine);        
        boolean hasIndentChilds=false;
        boolean isSecondLine=true;
        ArrayList<Integer> endLocations=new ArrayList<Integer>();
        int indexNextLine=skipLine(indexStartLine);        
        while(myParser.currentToken!=SGLR.EOF){            
            IndentInfo nextLine = getHistory().getLine(indexNextLine);
            int indentSkipPosition=nextLine.getIndentValue();
            indentShift shift=calculateShift(indentStartLine, indentSkipPosition);
            switch (shift) {
            case DEDENT:               
                endLocations.add(indexNextLine);                
                return endLocations;                
            case INDENT:
                hasIndentChilds=true;
                break;
            case SAME_INDENT:
                if(hasIndentChilds && isScopeClosingLine(nextLine)){
                    indexNextLine = skipLine(indexNextLine);
                    endLocations.add(indexNextLine);
                    return endLocations;
                }
                if((!isSecondLine || !isScopeOpeningLine(nextLine)) && !isSeparatorLine(nextLine)){
                     endLocations.add(indexNextLine);
                     return endLocations;
                }
                break;
            default:
                break;
            }
            isSecondLine=false;
            indexNextLine=skipLine(indexNextLine);            
        }
        endLocations.add(getHistory().getIndexLastLine()); //EOF
        return endLocations;
    }
    
    private IndentInfo skipLine(IndentInfo line) throws IOException {
        IndentationHandler skipIndentHandler=new IndentationHandler();
        getHistory().setTokenIndex(Math.max(0, line.getTokensSeen()-1));
        int newLineNumber=line.getLineNumber();
        skipIndentHandler.setInLeftMargin(false);
        getHistory().readRecoverToken(myParser);
        while(myParser.currentToken!=SGLR.EOF){
            getHistory().readRecoverToken(myParser);
            if(myParser.currentToken=='\n')
                newLineNumber++;
            skipIndentHandler.updateIndentation(myParser.currentToken);
            if(skipIndentHandler.lineMarginEnded()){
                IndentInfo result = new IndentInfo(newLineNumber, getHistory().getTokenIndex()-1, skipIndentHandler.getIndentValue());
                return result;
            }            
        }
        return new IndentInfo(newLineNumber+1, getHistory().getTokenIndex()-1, 0);// EOF
    }
    
    private int skipLine(int indexLine) throws IOException {
        IndentInfo line =getHistory().getLine(indexLine);
        IndentationHandler skipIndentHandler=new IndentationHandler();
        getHistory().setTokenIndex(Math.max(0, line.getTokensSeen()-1));        
        skipIndentHandler.setInLeftMargin(false);
        getHistory().readRecoverToken(myParser);
        while(myParser.currentToken!=SGLR.EOF){
            getHistory().readRecoverToken(myParser);            
            skipIndentHandler.updateIndentation(myParser.currentToken);
            if(skipIndentHandler.lineMarginEnded()){
                return indexLine+=1;
            }            
        }
        return getHistory().getIndexLastLine();// EOF
    }

}
