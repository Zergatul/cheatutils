package com.zergatul.cheatutils.scripting.monaco;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.nodes.BoundFunctionReferenceNode;
import com.zergatul.scripting.binding.nodes.BoundNameExpressionNode;
import com.zergatul.scripting.binding.nodes.BoundNode;
import com.zergatul.scripting.symbols.Symbol;

public class DefinitionProvider {

    public TextRange get(BoundNode node) {
        if (node == null) {
            return null;
        }

        if (node instanceof BoundNameExpressionNode name) {
            Symbol symbol = name.getSymbol();
            if (symbol == null) {
                return null;
            }
            return symbol.getDefinition();
        }

        if (node instanceof BoundFunctionReferenceNode functionReference) {
            Symbol symbol = functionReference.getFunction();
            if (symbol == null) {
                return null;
            }
            return symbol.getDefinition();
        }

        return null;
    }
}