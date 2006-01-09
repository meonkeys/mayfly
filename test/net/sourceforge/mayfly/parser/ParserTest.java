package net.sourceforge.mayfly.parser;

import junit.framework.*;

import net.sourceforge.mayfly.*;
import net.sourceforge.mayfly.evaluation.expression.*;
import net.sourceforge.mayfly.ldbc.*;
import net.sourceforge.mayfly.ldbc.what.*;
import net.sourceforge.mayfly.ldbc.where.*;
import net.sourceforge.mayfly.ldbc.where.literal.*;
import net.sourceforge.mayfly.parser.Parser;

import antlr.*;

public class ParserTest extends TestCase {
    
    public void testEmptyString() throws Exception {
        expectFailure("", "expected IDENTIFIER but got end of file");
    }

    public void testIdentifier() throws Exception {
        Parser parser = new Parser("foo");
        parser.parseTableReference();
        assertEquals("", parser.remainingTokens());
    }
    
    public void testRemainingTokens() throws Exception {
        Parser parser = new Parser("foo inner");
        parser.parseTableReference();
        assertEquals("inner", parser.remainingTokens());
    }
    
    public void testIdentifierDot() throws Exception {
        expectFailure("foo.", "expected IDENTIFIER but got end of file");
    }
    
    public void testSchemaDotTable() throws Exception {
        new Parser("mars.foo").parseTableReference();
    }

    public void testDotIdentifier() throws Exception {
        expectFailure(".foo", "expected IDENTIFIER but got DOT");
    }
    
    public void testSchemaDotTableAlias() throws Exception {
        Parser parser = new Parser("mars.foo f");
        parser.parseTableReference();
        assertEquals("", parser.remainingTokens());
    }

    public void testTableAlias() throws Exception {
        Parser parser = new Parser("foo f");
        parser.parseTableReference();
        assertEquals("", parser.remainingTokens());
    }

    public void testSelect() throws Exception {
        Parser parser = new Parser("select * from foo");
        parser.parseSelect();
        assertEquals("", parser.remainingTokens());
    }
    
    public void testExtraneousTokensAtEnd() throws Exception {
        Parser parser = new Parser("select * from foo 5");
        try {
            parser.parseSelect();
            fail();
        } catch (MayflyException e) {
            assertEquals("expected end of file but got 5", e.getMessage());
        }
    }
    
    public void testListOfFromItems() throws Exception {
        Parser parser = new Parser("select * from foo, bar b, baz");
        parser.parseSelect();
        assertEquals("", parser.remainingTokens());
    }
    
    public void testCrossJoin() throws Exception {
        Parser parser = new Parser("foo, bar cross join baz, quux");
        parser.parseFromItems();
        assertEquals("", parser.remainingTokens());
    }
    
    public void testCrossJoinWithOn() throws Exception {
        Parser parser = new Parser("select a, b from foo cross join bar on 1 = 1");
        try {
            parser.parseSelect();
            fail();
        } catch (ParserException e) {
            // In this example, we might say:
            // "Specify INNER JOIN, not CROSS JOIN, if you want an ON condition"
            // but there is a dangling ON issue.  So until we understand how the
            // parser knows which JOIN the ON goes with, let's not get too fancy.
            assertEquals("expected end of file but got ON", e.getMessage());
        }
    }
    
    public void testInnerJoin() throws Exception {
        Parser parser = new Parser("foo, bar inner join baz on a = b, quux");
        parser.parseFromItems();
        assertEquals("", parser.remainingTokens());
    }
    
    public void testInnerJoinOnMissing() throws Exception {
        Parser parser = new Parser("foo inner join bar");
        try {
            parser.parseFromItems();
            fail();
        } catch (ParserException e) {
            assertEquals("expected ON but got end of file", e.getMessage());
        }
    }
    
    public void testLeftOuterJoin() throws Exception {
        Parser parser = new Parser("foo, bar left outer join baz on a = b");
        parser.parseFromItems();
        assertEquals("", parser.remainingTokens());
    }
    
    public void testLeftJoin() throws Exception {
        Parser parser = new Parser("foo, bar left join baz on a = b");
        parser.parseFromItems();
        assertEquals("", parser.remainingTokens());
    }
    
    public void testWhere() throws Exception {
        Parser parser = new Parser("select * from foo where a = b");
        parser.parseSelect();
        assertEquals("", parser.remainingTokens());
    }
    
    public void testTableDotColumn() throws Exception {
        Parser parser = new Parser("f.a = b");
        parser.parseWhere();
        assertEquals("", parser.remainingTokens());
    }

    public void testTableDotColumnRightHandSide() throws Exception {
        Parser parser = new Parser("f = g.b");
        parser.parseWhere();
        assertEquals("", parser.remainingTokens());
    }

    public void testTableDot() throws Exception {
        try {
            new Parser("f. = b").parseWhere();
            fail();
        } catch (ParserException e) {
            assertEquals("expected IDENTIFIER but got EQUAL", e.getMessage());
        }
    }

    public void testBadTokenAfterIs() throws Exception {
        try {
            new Parser("a IS BORING").parseWhere();
            fail();
        } catch (ParserException e) {
            assertEquals("expected NULL but got BORING", e.getMessage());
        }
    }

    public void testBadTokenAfterNot() throws Exception {
        try {
            new Parser("a NOT INTERESTING").parseWhere();
            fail();
        } catch (ParserException e) {
            assertEquals("expected IN but got INTERESTING", e.getMessage());
        }
    }

    public void testBadTokenAfterIsNot() throws Exception {
        try {
            new Parser("a IS NOT SENSIBLE").parseWhere();
            fail();
        } catch (ParserException e) {
            assertEquals("expected NULL but got SENSIBLE", e.getMessage());
        }
    }

    public void testMissingOperator() throws Exception {
        try {
            new Parser("f 5").parseWhere();
            fail();
        } catch (ParserException e) {
            assertEquals("expected boolean operator but got 5", e.getMessage());
        }
    }
    
    public void testAnd() throws Exception {
        Parser parser = new Parser("a = 5 and b = c");
        parser.parseWhere();
        assertEquals("", parser.remainingTokens());
    }
    
    public void testLiteralNumber() throws Exception {
        Parser parser = new Parser("f = 5");
        parser.parseWhere();
        assertEquals("", parser.remainingTokens());
    }
    
    public void testLiteralString() throws Exception {
        Parser parser = new Parser("'hi'");
        assertEquals(new QuotedString("'hi'"), parser.parsePrimary());
        assertEquals("", parser.remainingTokens());
    }
    
    public void testSingleColumnAsWhat() throws Exception {
        Parser parser = new Parser("select a from foo");
        parser.parseSelect();
        assertEquals("", parser.remainingTokens());
    }

    public void testSingleColumnWithTableAsWhat() throws Exception {
        Parser parser = new Parser("foo.a");
        parser.parseWhat();
        assertEquals("", parser.remainingTokens());
    }

    public void testTwoWhatElements() throws Exception {
        Parser parser = new Parser("b, foo.a");
        parser.parseWhat();
        assertEquals("", parser.remainingTokens());
    }

    public void testAllNotLegalWithOthers() throws Exception {
        Parser parser = new Parser("a, *");
        try {
            parser.parseWhat();
            fail();
        } catch (ParserException e) {
            // Really, primary->expression
            assertEquals("expected primary but got ASTERISK", e.getMessage());
        }
    }

    public void testConcatenate() throws Exception {
        Parser parser = new Parser("a || b");
        parser.parseWhatElement();
        assertEquals("", parser.remainingTokens());
    }
    
    public void testExpressionPrecedence() throws Exception {
        // What about concatenate?  It would seem like it can't be in the
        // same expression, due to differing types...

        Parser parser = new Parser("a * b + c / e - a");
        WhatElement expression = parser.parseExpression();
        assertEquals("", parser.remainingTokens());
        assertEquals(
            new Minus(
                new Plus(
                    new Multiply(new SingleColumn("a"), new SingleColumn("b")),
                    new Divide(new SingleColumn("c"), new SingleColumn("e"))
                ),
                new SingleColumn("a")
            ),
            expression
        );
    }
    
    public void testDivideAssociativity() throws Exception {
        Parser parser = new Parser("a / b * c / d");
        WhatElement expression = parser.parseExpression();
        assertEquals("", parser.remainingTokens());
        assertEquals(
            new Divide(
                new Multiply(
                    new Divide(new SingleColumn("a"), new SingleColumn("b")),
                    new SingleColumn("c")
                ),
                new SingleColumn("d")
            ),
            expression
        );
    }

    public void testMinusAssociativity() throws Exception {
        Parser parser = new Parser("a-b-c+d");
        WhatElement expression = parser.parseExpression();
        assertEquals("", parser.remainingTokens());
        assertEquals(
            new Plus(
                new Minus(
                    new Minus(new SingleColumn("a"), new SingleColumn("b")),
                    new SingleColumn("c")
                ),
                new SingleColumn("d")
            ),
            expression
        );
    }

    public void testConcatenateAssociativity() throws Exception {
        // Not sure it matters if concatenate associates right-to-left or left-to-right.
        // But we'll pick the same as the other operators...

        Parser parser = new Parser("a || b || c || d");
        WhatElement expression = parser.parseExpression();
        assertEquals("", parser.remainingTokens());
        assertEquals(
            new Concatenate(
                new Concatenate(
                    new Concatenate(new SingleColumn("a"), new SingleColumn("b")),
                    new SingleColumn("c")
                ),
                new SingleColumn("d")
            ),
            expression
        );
    }

    public void testAliasOmitted() throws Exception {
        Parser parser = new Parser("select name from foo");
        assertEquals(
            new Select(
                new What()
                    .add(new SingleColumn("name")),
                new From()
                    .add(new FromTable("foo")),
                Where.EMPTY
            ),
            parser.parseSelect()
        );
    }

    private void expectFailure(String sql, String expectedMessage) throws ANTLRException {
        try {
            new Parser(sql).parseTableReference();
            fail();
        } catch (ParserException e) {
            assertEquals(expectedMessage, e.getMessage());
        }
    }
    
}
