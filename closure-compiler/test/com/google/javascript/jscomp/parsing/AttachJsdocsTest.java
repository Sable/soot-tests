/*
 * Copyright 2014 The Closure Compiler Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.javascript.jscomp.parsing;

import com.google.javascript.jscomp.parsing.Config.LanguageMode;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.SimpleSourceFile;
import com.google.javascript.rhino.testing.BaseJSTypeTestCase;
import com.google.javascript.rhino.testing.TestErrorReporter;

/**
 * Ported from rhino/testsrc/org/mozilla/javascript/tests/AttachJsDocsTest.java
 */
public class AttachJsdocsTest extends BaseJSTypeTestCase {
  private Config.LanguageMode mode;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    mode = LanguageMode.ECMASCRIPT3;
  }

  public void testOldJsdocAdd() {
    Node root = parse("1 + /** attach */ value;");
    Node plus = root.getFirstChild().getFirstChild();
    assertNotNull(plus.getLastChild().getJSDocInfo());
  }

  public void testOldJsdocArrayLit() {
    Node root = parse("[1, /** attach */ 2]");
    Node lit = root.getFirstChild().getFirstChild();
    assertNotNull(lit.getChildAtIndex(1).getJSDocInfo());
  }

  public void testOldJsdocAssign1() {
    Node root = parse("x = 1; /** attach */ y = 2;");
    Node assign = root.getLastChild().getFirstChild();
    assertNotNull(assign.getJSDocInfo());
  }

  public void testOldJsdocAssign2() {
    Node root = parse("x = 1; /** attach */y.p = 2;");
    Node assign = root.getLastChild().getFirstChild();
    assertNotNull(assign.getJSDocInfo());
  }

  public void testOldJsdocAssign3() {
    Node root =
        parse("/** @const */ var g = {}; /** @type {number} */ (g.foo) = 3;");
    Node assign = root.getLastChild().getFirstChild();
    assertNotNull(assign.getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocBlock1() {
    Node root = parse("if (x) { /** attach */ x; }");
    Node thenBlock = root.getFirstChild().getChildAtIndex(1);
    assertNotNull(thenBlock.getFirstChild().getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocBlock2() {
    Node root = parse("if (x) { x; /** attach */ y; }");
    Node thenBlock = root.getFirstChild().getChildAtIndex(1);
    assertNotNull(thenBlock.getLastChild().getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocBreak() {
    Node root = parse("FOO: for (;;) { break /** don't attach */ FOO; }");
    Node forStm = root.getFirstChild().getLastChild();
    Node breakStm = forStm.getChildAtIndex(3).getFirstChild();
    assertSame(Token.BREAK, breakStm.getType());
    assertNull(breakStm.getJSDocInfo());
    assertNull(breakStm.getFirstChild().getJSDocInfo());
  }

  // public void testOldJsdocCall1() {
  //   Node root = parse("foo/** don't attach */(1, 2);");
  //   Node call = root.getFirstChild().getFirstChild();
  //   assertNull(call.getChildAtIndex(1).getJSDocInfo());
  // }

  public void testOldJsdocCall2() {
    Node root = parse("foo(/** attach */ 1, 2);");
    Node call = root.getFirstChild().getFirstChild();
    assertNotNull(call.getChildAtIndex(1).getJSDocInfo());
  }

  public void testOldJsdocCall3() {
    // Incorrect attachment b/c the parser doesn't preserve comma positions.
    // TODO(dimvar): if this case comes up often, modify the parser to
    // remember comma positions for function decls and calls and fix the bug.
    Node root = parse("foo(1 /** attach to 2nd parameter */, 2);");
    Node call = root.getFirstChild().getFirstChild();
    assertNotNull(call.getChildAtIndex(2).getJSDocInfo());
  }

  public void testOldJsdocCall4() {
    Node root = parse("foo(1, 2 /** don't attach */);");
    Node call = root.getFirstChild().getFirstChild();
    assertNull(call.getChildAtIndex(2).getJSDocInfo());
  }

  public void testOldJsdocCall5() {
    Node root = parse("/** attach */ x(); function f() {}");
    assertNotNull(root.getFirstChild().getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocCall6() {
    Node root = parse("(function f() { /** attach */ var x = 1; })();");
    Node func = root.getFirstChild().getFirstChild().getFirstChild();
    assertTrue(func.isFunction());
    assertNotNull(func.getChildAtIndex(2).getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocCall7() {
    Node root = parse("/** attach */ obj.prop();");
    assertNotNull(root.getFirstChild().getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocCall8() {
    Node root = parse("/** attach */ (obj).prop();");
    Node getProp = root.getFirstChild().getFirstChild().getFirstChild();
    assertTrue(getProp.isGetProp());
    assertNotNull(getProp.getFirstChild().getJSDocInfo());
  }

  // public void testOldJsdocComma1() {
  //   Node root = parse("(/** attach */ x, y, z);");
  //   Node leftComma = root.getFirstChild().getFirstChild().getFirstChild();
  //   assertTrue(leftComma.getType() == Token.COMMA);
  //   assertNotNull(leftComma.getFirstChild().getJSDocInfo());
  // }

  // public void testOldJsdocComma2() {
  //   Node root = parse("(x /** don't attach */, y, z);");
  //   Node leftComma = root.getFirstChild().getFirstChild().getFirstChild();
  //   assertNull(leftComma.getFirstChild().getJSDocInfo());
  //   assertNull(leftComma.getLastChild().getJSDocInfo());
  // }

  public void testOldJsdocComma3() {
    Node root = parse("(x, y, /** attach */ z);");
    Node rightComma = root.getFirstChild().getFirstChild();
    assertNotNull(rightComma.getLastChild().getJSDocInfo());
  }

  public void testOldJsdocContinue() {
    Node root = parse("FOO: for (;;) { continue /** don't attach */ FOO; }");
    Node forStm = root.getFirstChild().getLastChild();
    Node cont = forStm.getChildAtIndex(3).getFirstChild();
    assertSame(Token.CONTINUE, cont.getType());
    assertNull(cont.getJSDocInfo());
    assertNull(cont.getFirstChild().getJSDocInfo());
  }

  // public void testOldJsdocDoLoop1() {
  //   Node root = parse("do /** don't attach */ {} while (x);");
  //   Node doBlock = root.getFirstChild().getFirstChild();
  //   assertNull(doBlock.getJSDocInfo());
  // }

  // public void testOldJsdocDoLoop2() {
  //   Node root = parse("do {} /** don't attach */ while (x);");
  //   Node whileExp = root.getFirstChild().getLastChild();
  //   assertNull(whileExp.getJSDocInfo());
  // }

  public void testOldJsdocDot() {
    Node root = parse("/** attach */a.b;");
    assertNotNull(root.getFirstChild().getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocDot2() {
    Node root = parse(
        "/** attach */\n" +
        "// test\n" +
        "a.b = {};");
    assertNotNull(root.getFirstChild().getFirstChild().getJSDocInfo());
  }

  // public void testOldJsdocForInLoop1() {
  //   Node root = parse("for /** don't attach */ (var p in {}) {}");
  //   Node fil = root.getFirstChild();
  //   assertNull(fil.getJSDocInfo());
  //   assertNull(fil.getChildAtIndex(0).getJSDocInfo());
  //   assertNull(fil.getChildAtIndex(1).getJSDocInfo());
  //   assertNull(fil.getChildAtIndex(2).getJSDocInfo());
  // }

  public void testOldJsdocForInLoop2() {
    Node root = parse("for (/** attach */ var p in {}) {}");
    Node fil = root.getFirstChild();
    assertNull(fil.getJSDocInfo());
    assertNotNull(fil.getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocForInLoop3() {
    Node root = parse("for (var p in /** attach */ {}) {}");
    Node fil = root.getFirstChild();
    assertNull(fil.getJSDocInfo());
    assertNotNull(fil.getChildAtIndex(1).getJSDocInfo());
  }

  // public void testOldJsdocForInLoop4() {
  //   Node root = parse("for (var p in {}) /** don't attach */ {}");
  //   Node fil = root.getFirstChild();
  //   assertNull(fil.getJSDocInfo());
  //   assertNull(fil.getChildAtIndex(2).getJSDocInfo());
  // }

  // public void testOldJsdocForInLoop5() {
  //   Node root = parse("for (var p /** don't attach */ in {}) {}");
  //   Node fil = root.getFirstChild();
  //   assertNull(fil.getJSDocInfo());
  //   assertNull(fil.getChildAtIndex(0).getJSDocInfo());
  //   assertNull(fil.getChildAtIndex(1).getJSDocInfo());
  //   assertNull(fil.getChildAtIndex(2).getJSDocInfo());
  // }

  // public void testOldJsdocForInLoop6() {
  //   Node root = parse("for (var p in {} /** don't attach */) {}");
  //   Node fil = root.getFirstChild();
  //   assertNull(fil.getJSDocInfo());
  //   assertNull(fil.getChildAtIndex(0).getJSDocInfo());
  //   assertNull(fil.getChildAtIndex(1).getJSDocInfo());
  //   assertNull(fil.getChildAtIndex(2).getJSDocInfo());
  // }

  // public void testOldJsdocForLoop1() {
  //   Node root = parse("for /** don't attach */ (i = 0; i < 5; i++) {}");
  //   Node fl = root.getFirstChild();
  //   assertNull(fl.getJSDocInfo());
  //   assertNull(fl.getChildAtIndex(0).getJSDocInfo());
  //   assertNull(fl.getChildAtIndex(1).getJSDocInfo());
  //   assertNull(fl.getChildAtIndex(2).getJSDocInfo());
  //   assertNull(fl.getChildAtIndex(3).getJSDocInfo());
  // }

  public void testOldJsdocForLoop2() {
    Node root = parse("for (/** attach */ i = 0; i < 5; i++) {}");
    Node fl = root.getFirstChild();
    assertNull(fl.getJSDocInfo());
    assertNotNull(fl.getChildAtIndex(0).getJSDocInfo());
  }

  // public void testOldJsdocForLoop3() {
  //   Node root = parse("for (i /** don't attach */ = 0; i < 5; i++) {}");
  //   Node fl = root.getFirstChild();
  //   assertNull(fl.getJSDocInfo());
  //   Node init = fl.getChildAtIndex(0);
  //   assertNull(init.getFirstChild().getJSDocInfo());
  //   assertNull(init.getLastChild().getJSDocInfo());
  //   assertNull(fl.getChildAtIndex(1).getJSDocInfo());
  // }

  public void testOldJsdocForLoop4() {
    Node root = parse("for (i = /** attach */ 0; i < 5; i++) {}");
    Node fl = root.getFirstChild();
    Node init = fl.getChildAtIndex(0);
    assertNull(init.getFirstChild().getJSDocInfo());
    assertNotNull(init.getLastChild().getJSDocInfo());
  }

  // public void testOldJsdocForLoop5() {
  //   Node root = parse("for (i = 0 /** don't attach */; i < 5; i++) {}");
  //   Node fl = root.getFirstChild();
  //   assertNull(fl.getJSDocInfo());
  //   Node init = fl.getChildAtIndex(0);
  //   assertNull(init.getLastChild().getJSDocInfo());
  //   assertNull(fl.getChildAtIndex(1).getJSDocInfo());
  // }

  // public void testOldJsdocForLoop6() {
  //   Node root = parse("for (i = 0; /** attach */ i < 5; i++) {}");
  //   Node fl = root.getFirstChild();
  //   assertNull(fl.getJSDocInfo());
  //   Node cond = fl.getChildAtIndex(1);
  //   assertNotNull(cond.getFirstChild().getJSDocInfo());
  // }

  public void testOldJsdocForLoop7() {
    Node root = parse("for (i = 0; i < /** attach */ 5; i++) {}");
    Node fl = root.getFirstChild();
    assertNull(fl.getJSDocInfo());
    Node cond = fl.getChildAtIndex(1);
    assertNotNull(cond.getLastChild().getJSDocInfo());
  }

  public void testOldJsdocForLoop8() {
    Node root = parse("for (i = 0; i < 5; /** attach */ i++) {}");
    Node fl = root.getFirstChild();
    assertNull(fl.getJSDocInfo());
    assertNotNull(fl.getChildAtIndex(2).getJSDocInfo());
  }

  // public void testOldJsdocForLoop9() {
  //   Node root = parse("for (i = 0; i < 5; i++ /** don't attach */) {}");
  //   Node fl = root.getFirstChild();
  //   assertNull(fl.getJSDocInfo());
  //   assertNull(fl.getChildAtIndex(2).getJSDocInfo());
  //   assertNull(fl.getChildAtIndex(3).getJSDocInfo());
  // }

  // public void testOldJsdocForLoop10() {
  //   Node root = parse("for (i = 0; i < 5; i++) /** don't attach */ {}");
  //   Node fl = root.getFirstChild();
  //   assertNull(fl.getJSDocInfo());
  //   assertNull(fl.getChildAtIndex(3).getJSDocInfo());
  // }

  public void testOldJsdocForLoop11() {
    Node root = parse("for (/** attach */ var i = 0; i < 5; i++) {}");
    Node fl = root.getFirstChild();
    assertNull(fl.getJSDocInfo());
    assertNotNull(fl.getChildAtIndex(0).getJSDocInfo());
  }

  // public void testOldJsdocForLoop12() {
  //   Node root = parse("for (var i = 0 /** dont attach */; i < 5; i++) {}");
  //   Node fl = root.getFirstChild();
  //   assertNull(fl.getJSDocInfo());
  //   assertNull(fl.getChildAtIndex(0).getJSDocInfo());
  //   assertNull(fl.getChildAtIndex(1).getJSDocInfo());
  // }

  public void testOldJsdocFun1() {
    Node root = parse("function f(/** string */ e) {}");
    Node fun = root.getFirstChild();
    Node params = fun.getChildAtIndex(1);
    assertNotNull(params.getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocFun2() {
    Node root = parse("(function() {/** don't attach */})()");
    Node call = root.getFirstChild().getFirstChild();
    assertNull(call.getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocFun3() {
    Node root = parse("function /** string */ f (e) {}");
    Node fun = root.getFirstChild();
    assertNotNull(fun.getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocFun4() {
    Node root = parse("f = /** attach */ function(e) {};");
    Node assign = root.getFirstChild().getFirstChild();
    assertNotNull(assign.getLastChild().getJSDocInfo());
  }

  public void testOldJsdocFun5() {
    Node root = parse("x = 1; /** attach */ function f(e) {}");
    assertNotNull(root.getLastChild().getJSDocInfo());
  }

  public void testOldJsdocFun6() {
    Node root = parse("function f() { /** attach */ function Foo(){} }");
    Node innerFun = root.getFirstChild().getLastChild().getFirstChild();
    assertNotNull(innerFun.getJSDocInfo());
  }

  public void testOldJsdocFun7() {
    Node root = parse("(function f() { /** attach */function Foo(){} })();");
    Node outerFun = root.getFirstChild().getFirstChild().getFirstChild();
    assertNotNull(outerFun.getLastChild().getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocGetElem1() {
    Node root = parse("(/** attach */ {})['prop'];");
    Node getElem = root.getFirstChild().getFirstChild();
    assertNotNull(getElem.getFirstChild().getJSDocInfo());
  }

  // public void testOldJsdocGetElem2() {
  //   Node root = parse("({} /** don't attach */)['prop'];");
  //   Node getElem = root.getFirstChild().getFirstChild();
  //   assertNull(getElem.getFirstChild().getJSDocInfo());
  //   assertNull(getElem.getLastChild().getJSDocInfo());
  // }

  public void testOldJsdocGetElem3() {
    Node root = parse("({})[/** attach */ 'prop'];");
    Node getElem = root.getFirstChild().getFirstChild();
    assertNotNull(getElem.getLastChild().getJSDocInfo());
  }

  public void testOldJsdocGetProp1() {
    Node root = parse("(/** attach */ {}).prop;");
    Node getProp = root.getFirstChild().getFirstChild();
    assertNotNull(getProp.getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocGetProp2() {
    Node root = parse("/** attach */ ({}).prop;");
    Node getProp = root.getFirstChild().getFirstChild();
    assertNotNull(getProp.getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocGetProp3() {
    Node root = parse("/** attach */ obj.prop;");
    Node getProp = root.getFirstChild().getFirstChild();
    assertNotNull(getProp.getJSDocInfo());
  }

  public void testOldJsdocGetter1() {
    mode = LanguageMode.ECMASCRIPT5;
    Node root = parse("({/** attach */ get foo() {}});");
    Node objlit = root.getFirstChild().getFirstChild();
    assertNotNull(objlit.getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocGetter2() {
    mode = LanguageMode.ECMASCRIPT5;
    Node root = parse("({/** attach */ get 1() {}});");
    Node objlit = root.getFirstChild().getFirstChild();
    assertNotNull(objlit.getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocGetter3() {
    mode = LanguageMode.ECMASCRIPT5;
    Node root = parse("({/** attach */ get 'foo'() {}});");
    Node objlit = root.getFirstChild().getFirstChild();
    assertNotNull(objlit.getFirstChild().getJSDocInfo());
  }

  public void testJsdocHook1() {
     Node root = parse("/** attach */ (true) ? 1 : 2;");
     Node hook = root.getFirstChild().getFirstChild();
     assertNotNull(hook.getFirstChild().getJSDocInfo());
  }

  // public void testOldJsdocHook1() {
  //   Node root = parse("/** attach */ true ? 1 : 2;");
  //   Node hook = root.getFirstChild().getFirstChild();
  //   assertNotNull(hook.getFirstChild().getJSDocInfo());
  // }

  // public void testOldJsdocHook2() {
  //   Node root = parse("true /** don't attach */ ? 1 : 2;");
  //   Node hook = root.getFirstChild().getFirstChild();
  //   assertNull(hook.getChildAtIndex(0).getJSDocInfo());
  //   assertNull(hook.getChildAtIndex(1).getJSDocInfo());
  // }

  public void testOldJsdocHook3() {
    Node root = parse("true ? /** attach */ 1 : 2;");
    Node hook = root.getFirstChild().getFirstChild();
    assertNotNull(hook.getChildAtIndex(1).getJSDocInfo());
  }

  // public void testOldJsdocHook4() {
  //   Node root = parse("true ? 1 /** don't attach */ : 2;");
  //   Node hook = root.getFirstChild().getFirstChild();
  //   assertNull(hook.getChildAtIndex(1).getJSDocInfo());
  //   assertNull(hook.getChildAtIndex(2).getJSDocInfo());
  // }

  public void testOldJsdocHook5() {
    Node root = parse("true ? 1 : /** attach */ 2;");
    Node hook = root.getFirstChild().getFirstChild();
    assertNotNull(hook.getChildAtIndex(2).getJSDocInfo());
  }

  public void testOldJsdocIf1() {
    Node root = parse("if (/** attach */ x) {}");
    assertNotNull(root.getFirstChild().getFirstChild().getJSDocInfo());
  }

  // public void testOldJsdocIf2() {
  //   Node root = parse("if (x) /** don't attach */ {}");
  //   assertNull(root.getFirstChild().getChildAtIndex(1).getJSDocInfo());
  // }

  // public void testOldJsdocIf3() {
  //   Node root = parse("if (x) {} else /** don't attach */ {}");
  //   assertNull(root.getFirstChild().getChildAtIndex(2).getJSDocInfo());
  // }

  // public void testOldJsdocIf4() {
  //   Node root = parse("if (x) {} /** don't attach */ else {}");
  //   assertNull(root.getFirstChild().getChildAtIndex(2).getJSDocInfo());
  // }

  // public void testOldJsdocLabeledStm1() {
  //   Node root = parse("/** attach */ FOO: if (x) {};");
  //   assertNotNull(root.getFirstChild().getJSDocInfo());
  // }

  // public void testOldJsdocLabeledStm2() {
  //   Node root = parse("FOO: /** don't attach */ if (x) {};");
  //   assertNull(root.getFirstChild().getJSDocInfo());
  //   assertNull(root.getFirstChild().getLastChild().getJSDocInfo());
  // }

  public void testOldJsdocNew1() {
    Node root = parse("/** attach */ new Foo();");
    Node newexp = root.getFirstChild().getFirstChild();
    assertNotNull(newexp.getJSDocInfo());
  }

  public void testOldJsdocNew2() {
    Node root = parse("new /** don't attach */ Foo();");
    Node newexp = root.getFirstChild().getFirstChild();
    assertNull(newexp.getJSDocInfo());
  }

  // public void testOldJsdocObjLit1() {
  //   Node root = parse("({/** attach */ 1: 2});");
  //   Node objlit = root.getFirstChild().getFirstChild();
  //   assertNotNull(objlit.getFirstChild().getFirstChild().getJSDocInfo());
  // }

  public void testOldJsdocObjLit2() {
    Node root = parse("({1: /** attach */ 2, 3: 4});");
    Node objlit = root.getFirstChild().getFirstChild();
    assertNotNull(objlit.getFirstChild().getLastChild().getJSDocInfo());
  }

  public void testOldJsdocObjLit3() {
    Node root = parse("({'1': /** attach */ (foo())});");
    Node objlit = root.getFirstChild().getFirstChild();
    assertNotNull(objlit.getFirstChild().getLastChild().getJSDocInfo());
  }

  public void testOldJsdocPostfix1() {
    Node root = parse("/** attach */ (x)++;");
    Node unary = root.getFirstChild().getFirstChild();
    assertNotNull(unary.getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocPostfix2() {
    Node root = parse("/** attach */ x++;");
    Node unary = root.getFirstChild().getFirstChild();
    assertNotNull(unary.getJSDocInfo());
  }

  public void testOldJsdocReturn1() {
    Node root = parse("function f(x) { return /** string */ x; }");
    Node ret = root.getFirstChild().getLastChild().getFirstChild();
    assertNotNull(ret.getFirstChild().getJSDocInfo());
  }

  // public void testOldJsdocReturn2() {
  //   Node root = parse("function f(x) { /** string */ return x; }");
  //   Node ret = root.getFirstChild().getLastChild().getFirstChild();
  //   assertNotNull(ret.getFirstChild().getJSDocInfo());
  // }

  public void testOldJsdocReturn3() {
    // The first comment should be attached to the parenthesis, and the
    // second comment shouldn't be attached to any local node.
    // There used to be a bug where the second comment would get attached.
    Node root = parse("function f(e) { return /** 1 */(g(1 /** 2 */)); }\n");
    Node ret = root.getFirstChild().getLastChild().getFirstChild();
    assertNotNull(ret.getFirstChild().getJSDocInfo());
    assertNull(ret.getFirstChild().getLastChild().getJSDocInfo());
  }

  public void testOldJsdocSetter() {
    mode = LanguageMode.ECMASCRIPT5;
    Node root = parse("({/** attach */ set foo(x) {}});");
    Node objlit = root.getFirstChild().getFirstChild();
    assertNotNull(objlit.getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocScript1() {
    Node root = parse("{ 1; /** attach */ 2; }");
    Node block = root.getFirstChild();
    assertNotNull(block.getLastChild().getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocScript2() {
    Node root = parse("1; /** attach */ 2;");
    assertNotNull(root.getLastChild().getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocScript3() {
    Node root = parse("1;/** attach */ function f(){}");
    assertNotNull(root.getLastChild().getJSDocInfo());
  }

  public void testOldJsdocSwitch1() {
    Node root = parse("switch /** attach */ (x) {}");
    Node sw = root.getFirstChild();
    assertNotNull(sw.getFirstChild().getJSDocInfo());
  }

  // public void testOldJsdocSwitch2() {
  //   Node root = parse("switch (x) { /** don't attach */ case 1: ; }");
  //   Node sw = root.getFirstChild();
  //   assertNull(sw.getChildAtIndex(1).getJSDocInfo());
  // }

  public void testOldJsdocSwitch3() {
    Node root = parse("switch (x) { case /** attach */ 1: ; }");
    Node sw = root.getFirstChild();
    assertNotNull(sw.getChildAtIndex(1).getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocSwitch4() {
    Node root = parse("switch (x) { case 1: /** don't attach */ {}; }");
    Node sw = root.getFirstChild();
    assertNull(sw.getChildAtIndex(1).getLastChild().getJSDocInfo());
  }

  public void testOldJsdocSwitch5() {
    Node root = parse("switch (x) { default: /** don't attach */ {}; }");
    Node sw = root.getFirstChild();
    assertNull(sw.getChildAtIndex(1).getLastChild().getJSDocInfo());
  }

  public void testOldJsdocSwitch6() {
    Node root = parse("switch (x) { case 1: /** don't attach */ }");
    Node sw = root.getFirstChild();
    assertNull(sw.getChildAtIndex(1).getLastChild().getJSDocInfo());
  }

  public void testOldJsdocSwitch7() {
    Node root = parse(
        "switch (x) {" +
        "  case 1: " +
        "    /** attach */ y;" +
        "    /** attach */ z;" +
        "}");
    Node sw = root.getFirstChild();
    Node caseBody = sw.getChildAtIndex(1).getLastChild();
    assertNotNull(caseBody.getChildAtIndex(0).getFirstChild().getJSDocInfo());
    assertNotNull(caseBody.getChildAtIndex(1).getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocThrow() {
    Node root = parse("throw /** attach */ new Foo();");
    assertNotNull(root.getFirstChild().getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocTryCatch1() {
    Node root = parse("try {} catch (/** attach */ e) {}");
    Node catchNode = root.getFirstChild().getLastChild().getFirstChild();
    assertNotNull(catchNode.getFirstChild().getJSDocInfo());
  }

  // public void testOldJsdocTryCatch2() {
  //   Node root = parse("try {} /** don't attach */ catch (e) {}");
  //   Node catchNode = root.getFirstChild().getLastChild().getFirstChild();
  //   assertNull(catchNode.getJSDocInfo());
  //   assertNull(catchNode.getFirstChild().getJSDocInfo());
  // }

  public void testOldJsdocTryCatch3() {
    Node root = parse("/** @preserveTry */ try {} catch (e) {}");
    assertNotNull(root.getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocTryFinally() {
    Node root = parse("try {} finally { /** attach */ e; }");
    Node finallyBlock = root.getFirstChild().getLastChild();
    assertNotNull(finallyBlock.getFirstChild().getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocUnary() {
    Node root = parse("!(/** attach */ x);");
    Node exp = root.getFirstChild().getFirstChild();
    assertNotNull(exp.getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocVar1() {
    Node root = parse("/** attach */ var a;");
    assertNotNull(root.getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocVar2() {
    Node root = parse("var a = /** attach */ (x);");
    Node var = root.getFirstChild();
    assertNotNull(var.getFirstChild().getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocVar3() {
    Node root = parse("var a = (/** attach */ {});");
    Node var = root.getFirstChild();
    assertNotNull(var.getFirstChild().getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocVar4() {
    Node root = parse("var /** number */ a = x;");
    Node var = root.getFirstChild();
    assertNotNull(var.getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocVar5() {
    Node root = parse("x = 1; /** attach */ var y = 5;");
    Node var = root.getLastChild();
    assertNotNull(var.getJSDocInfo());
  }

  // public void testOldJsdocWhile1() {
  //   Node root = parse("while (x) /** don't attach */ {}");
  //   Node wh = root.getFirstChild();
  //   assertNull(wh.getLastChild().getJSDocInfo());
  // }

  public void testOldJsdocWhile2() {
    Node root = parse("while /** attach */ (x) {}");
    Node wh = root.getFirstChild();
    assertNotNull(wh.getFirstChild().getJSDocInfo());
  }

  // public void testOldJsdocWhile3() {
  //   Node root = parse("while (x /** don't attach */) {}");
  //   Node wh = root.getFirstChild();
  //   assertNull(wh.getFirstChild().getJSDocInfo());
  //   assertNull(wh.getLastChild().getJSDocInfo());
  // }

  public void testOldJsdocWith1() {
    Node root = parse("with (/** attach */ obj) {};");
    Node with = root.getFirstChild();
    assertNotNull(with.getFirstChild().getJSDocInfo());
  }

  // public void testOldJsdocWith2() {
  //   Node root = parse("with (obj) /** don't attach */ {};");
  //   Node with = root.getFirstChild();
  //   assertNull(with.getLastChild().getJSDocInfo());
  // }

  // public void testOldJsdocWith3() {
  //   Node root = parse("with (obj /** don't attach */) {};");
  //   Node with = root.getFirstChild();
  //   assertNull(with.getFirstChild().getJSDocInfo());
  //   assertNull(with.getLastChild().getJSDocInfo());
  // }

  public void testOldJsdocWith4() {
    Node root = parse(
        "/** @suppress {with} */ with (context) {\n" +
        "  eval('[' + expr + ']');\n" +
        "}\n");
    assertNotNull(root.getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocManyComments1() {
    Node root = parse(
        "function /** number */ f(/** number */ x, /** number */ y) {\n" +
        "  return x + y;\n" +
        "}");
    Node fun = root.getFirstChild();
    assertNotNull(fun.getFirstChild().getJSDocInfo());
    assertNotNull(fun.getChildAtIndex(1).getFirstChild().getJSDocInfo());
    assertNotNull(fun.getChildAtIndex(1).getLastChild().getJSDocInfo());
  }

  public void testOldJsdocManyComments2() {
    Node root = parse("var /** number */ x = 1; var /** string */ y = 2;");
    assertNotNull(root.getFirstChild().getFirstChild().getJSDocInfo());
    assertNotNull(root.getLastChild().getFirstChild().getJSDocInfo());
  }

  public void testOldJsdocManyCommentsOnOneNode() {
    // When many jsdocs could attach to a node, we pick the last one.
    Node root = parse("var x; /** foo */ /** bar */ function f() {}");
    JSDocInfo info = root.getLastChild().getJSDocInfo();
    assertNotNull(info);
    assertEquals("/** bar */", info.getOriginalCommentString());
  }

  private Node parse(String source, String... warnings) {
    TestErrorReporter testErrorReporter = new TestErrorReporter(null, warnings);
    Node script = ParserRunner.parse(
        new SimpleSourceFile("input", false),
        source,
        ParserRunner.createConfig(true, mode, false, null),
        testErrorReporter).ast;

    // verifying that all warnings were seen
    assertTrue(testErrorReporter.hasEncounteredAllErrors());
    assertTrue(testErrorReporter.hasEncounteredAllWarnings());

    return script;
  }
}
