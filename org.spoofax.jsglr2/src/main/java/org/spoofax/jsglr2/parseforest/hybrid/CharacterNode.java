package org.spoofax.jsglr2.parseforest.hybrid;

import org.spoofax.jsglr2.characterclasses.ICharacterClass;
import org.spoofax.jsglr2.parser.Parse;
import org.spoofax.jsglr2.parser.Position;

public class CharacterNode extends HybridParseForest {

    public final int character;

    public CharacterNode(int nodeNumber, Parse<?, ?> parse, Position position, int character) {
        super(nodeNumber, parse, position,
            ICharacterClass.isNewLine(character) ? position.nextLine() : position.nextColumn());
        this.character = character;
    }

    @Override
    public String descriptor() {
        return "'" + ICharacterClass.intToString(this.character) + "'";
    }

}
