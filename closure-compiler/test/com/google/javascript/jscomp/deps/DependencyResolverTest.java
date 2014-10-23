/*
 * Copyright 2009 The Closure Compiler Authors.
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

package com.google.javascript.jscomp.deps;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import junit.framework.TestCase;

import java.util.Collection;
import java.util.Set;

/**
 * Tests for DependencyResolver.
 */
public class DependencyResolverTest extends TestCase {

  DependencyFile fakeDeps1 = new DependencyFile(new VirtualFile("deps1",
      "goog.addDependency('a.js', ['a'], []);\n"
      + "goog.addDependency('b.js', ['b'], []);\n"
      + "goog.addDependency('c.js', ['c', 'c2'], ['a']);\n"
      + "goog.addDependency('d.js', ['d'], ['b', 'c']);\n"));

  DependencyFile fakeDeps2 = new DependencyFile(new VirtualFile("deps2",
      "goog.addDependency('e.js', ['e'], ['c2']);\n"
      + "goog.addDependency('f.js', ['f'], ['b', 'c']);\n"
      + "goog.addDependency('g.js', ['g'], ['a', 'b', 'c']);\n"
      + "goog.addDependency('h.js', ['h', 'i'], ['g', 'd', 'c']);\n"));

  DefaultDependencyResolver resolver;

  @Override protected void setUp() throws Exception {
    super.setUp();
    resolver = new DefaultDependencyResolver(ImmutableList.of(fakeDeps1, fakeDeps2), false);
  }

  public void testBasicCase() throws Exception {
     Collection<String> deps = resolver.getDependencies("goog.require('a');");
     assertEquals("base.js,a.js", Joiner.on(",").useForNull("null").join(deps));
  }

  public void testSimpleDependencies() throws Exception {
     Collection<String> deps = resolver.getDependencies("goog.require('c');");
     assertEquals("base.js,a.js,c.js", Joiner.on(",").useForNull("null").join(deps));
  }

  public void testTransitiveDependencies() throws Exception {
     Collection<String> deps = resolver.getDependencies("goog.require('e');");
     assertEquals("base.js,a.js,c.js,e.js", Joiner.on(",").useForNull("null").join(deps));
  }

  public void testMultipleRequires() throws Exception {
     Collection<String> deps = resolver.getDependencies(
         "goog.require('e');goog.require('a');goog.require('b');");
     assertEquals("base.js,a.js,c.js,e.js,b.js", Joiner.on(",").useForNull("null").join(deps));
  }

  public void testOneMoreForGoodMeasure() throws Exception {
    Collection<String> deps = resolver.getDependencies(
        "goog.require('g');goog.require('f');goog.require('c');");
    assertEquals("base.js,a.js,b.js,c.js,g.js,f.js", Joiner.on(",").useForNull("null").join(deps));
  }

  public void testSharedSeenSetNoBaseFile() throws Exception {
    Set<String> seen = Sets.newHashSet();

    Collection<String> deps = resolver.getDependencies(
    "goog.require('g');goog.require('f');goog.require('c');", seen, false);

    Collection<String> depsLater = resolver.getDependencies(
    "goog.require('f');goog.require('c');", seen, false);

    assertEquals("a.js,b.js,c.js,g.js,f.js", Joiner.on(",").useForNull("null").join(deps));
    assertEquals("", Joiner.on(",").useForNull("null").join(depsLater));
  }

  public void testSharedSeenSetNoBaseFileNewRequires() throws Exception {
    Set<String> seen = Sets.newHashSet();

    Collection<String> deps = resolver.getDependencies(
        "goog.require('f');goog.require('c');", seen, false);

    Collection<String> depsLater = resolver.getDependencies(
        "goog.require('g');goog.require('c');", seen, false);

    assertEquals("b.js,a.js,c.js,f.js", Joiner.on(",").useForNull("null").join(deps));
    assertEquals("g.js", Joiner.on(",").useForNull("null").join(depsLater));
  }

  public void testSharedSeenSetNoBaseFileMultipleProvides() throws Exception {
    Set<String> seen = Sets.newHashSet();

    Collection<String> deps = resolver.getDependencies(
        "goog.require('h');goog.require('i');", seen, false);

    assertEquals("a.js,b.js,c.js,g.js,d.js,h.js", Joiner.on(",").useForNull("null").join(deps));
  }

  public void testNonExistantProvideLoose() throws Exception {
    Set<String> seen = Sets.newHashSet();
    resolver = new DefaultDependencyResolver(ImmutableList.of(fakeDeps1), false);
    Collection<String> deps = resolver.getDependencies(
        "goog.require('foo');goog.require('d');", seen, false);

    assertEquals("b.js,a.js,c.js,d.js", Joiner.on(",").useForNull("null").join(deps));
  }

  public void testNonExistantProvideStrict() throws Exception {
    Set<String> seen = Sets.newHashSet();
    resolver = new DefaultDependencyResolver(ImmutableList.of(fakeDeps1), true);
    try {
      Collection<String> deps = resolver.getDependencies(
          "goog.require('foo');goog.require('a');", seen, false);
      fail("Service exception should be thrown");
    } catch (ServiceException expected) {}
  }

}
