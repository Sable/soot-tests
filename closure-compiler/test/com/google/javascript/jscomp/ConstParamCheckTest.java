/*
 * Copyright 2013 The Closure Compiler Authors.
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

package com.google.javascript.jscomp;

/**
 * Tests for {@link ConstParamCheck}.
 */
public class ConstParamCheckTest extends CompilerTestCase {

  static final String CLOSURE_DEFS = ""
      + "var goog = {};"
      + "goog.string = {};"
      + "goog.string.Const = {};"
      + "goog.string.Const.from = function(x) {};";

  public ConstParamCheckTest() {
    enableNormalize();
  }

  @Override
  public CompilerPass getProcessor(Compiler compiler) {
    return new ConstParamCheck(compiler);
  }

  @Override
  public int getNumRepetitions() {
    return 1;
  }

  // Tests for string literal arguments.

  public void testStringLiteralArgument() {
    testSame(CLOSURE_DEFS
        + "goog.string.Const.from('foo');");
  }

  public void testConcatenatedStringLiteralArgument() {
    testSame(CLOSURE_DEFS
        + "goog.string.Const.from('foo' + 'bar' + 'baz');");
  }

  public void testNotStringLiteralArgument1() {
    test(CLOSURE_DEFS
        + "goog.string.Const.from(null);",
        null, ConstParamCheck.CONST_NOT_STRING_LITERAL_ERROR);
  }

  public void testNotStringLiteralArgument2() {
    test(CLOSURE_DEFS
        + "var myFunction = function() {};"
        + "goog.string.Const.from(myFunction());",
        null, ConstParamCheck.CONST_NOT_STRING_LITERAL_ERROR);
  }

  public void testNotStringLiteralArgument3() {
    test(CLOSURE_DEFS
        + "var myFunction = function() {};"
        + "goog.string.Const.from('foo' + myFunction() + 'bar');",
        null, ConstParamCheck.CONST_NOT_STRING_LITERAL_ERROR);
  }

  public void testNotStringLiteralArgumentAliased() {
    test(CLOSURE_DEFS
        + "var myFunction = function() {};"
        + "var mkConst = goog.string.Const.from;"
        + "mkConst(myFunction());",
        null, ConstParamCheck.CONST_NOT_STRING_LITERAL_ERROR);
  }

  // Tests for string literal constant arguments.

  public void testStringLiteralConstantArgument() {
    testSame(CLOSURE_DEFS
        + "var FOO = 'foo';"
        + "goog.string.Const.from(FOO);");
  }

  public void testStringLiteralAnnotatedConstantArgument() {
    testSame(CLOSURE_DEFS
        + "/** @const */ var foo = 'foo';"
        + "goog.string.Const.from(foo);");
  }

  public void testNotConstantArgument() {
    test(CLOSURE_DEFS
        + "var foo = 'foo';"
        + "goog.string.Const.from(foo);",
        null, ConstParamCheck.CONST_NOT_STRING_LITERAL_ERROR);
  }

  public void testStringLiteralConstantArgumentOrder() {
    testSame(CLOSURE_DEFS
        + "var myFun = function() { goog.string.Const.from(FOO); };"
        + "var FOO = 'asdf';"
        + "myFun();");
  }

  public void testConcatenatedStringLiteralConstantArgument() {
    testSame(CLOSURE_DEFS
        + "var FOO = 'foo' + 'bar' + 'baz';"
        + "goog.string.Const.from(FOO);");
  }

  public void testNotStringLiteralConstantArgument1() {
    test(CLOSURE_DEFS
        + "var FOO = null;"
        + "goog.string.Const.from(FOO);",
        null, ConstParamCheck.CONST_NOT_ASSIGNED_STRING_LITERAL_ERROR);
  }

  public void testNotStringLiteralConstantArgument2() {
    test(CLOSURE_DEFS
        + "var myFunction = function() {};"
        + "var FOO = myFunction();"
        + "goog.string.Const.from(FOO);",
        null, ConstParamCheck.CONST_NOT_ASSIGNED_STRING_LITERAL_ERROR);
  }
}
