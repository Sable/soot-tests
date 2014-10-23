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

/**
 * Runtime functions required for transpilation from ES6 to ES3.
 *
 * @author mattloring@google.com (Matthew Loring)
 */

/** @const */
$jscomp = $jscomp || {};

/**
 * @constructor
 * @template T
 */
$jscomp.IteratorResult = function() {};


/**
 * @type {boolean}
 */
$jscomp.IteratorResult.prototype.done;


/**
 * @type {T}
 */
$jscomp.IteratorResult.prototype.value;



/**
 * @interface
 * @template T
 */
$jscomp.Iterator = function() {};


/**
 * @param {Object=} def
 * @return {!$jscomp.IteratorResult.<T>}
 */
$jscomp.Iterator.prototype.next;



/**
 * @interface
 * @template T
 */
$jscomp.Iterable = function() {};


/**
 * @return {!$jscomp.Iterator.<T>}
 */
$jscomp.Iterable.prototype.$$iterator = function() {};


/**
 * Creates an iterator for the given iterable.
 *
 * @param {!Array.<T>|!$jscomp.Iterable.<T>} iterable
 * @return {!$jscomp.Iterator.<T>}
 * @template T
 */
$jscomp.makeIterator = function(iterable) {
  if (iterable.$$iterator) {
    return iterable.$$iterator();
  }
  if (!(iterable instanceof Array)) {
    throw new Error();
  }
  var index = 0;
  return /** @type {!$jscomp.Iterator} */ ({
    next: function() {
      if (index == iterable.length) {
        return /** @type {!$jscomp.IteratorResult} */ ({ done: true });
      } else {
        return /** @type {!$jscomp.IteratorResult} */ ({
          done: false,
          value: iterable[index++]
        });
      }
    }
  });
};

/**
 * Transfers properties on the from object onto the to object.
 *
 * @param {Object} to
 * @param {Object} from
 */
$jscomp.copyProperties = function(to, from) {
  for (var p in from) {
    to[p] = from[p];
  }
};

/**
 * Inherit the prototype methods from one constructor into another.
 *
 * NOTE: This is a copy of goog.inherits moved here to remove dependency on
 * the closure library for Es6ToEs3 transpilation.
 *
 * Usage:
 * <pre>
 * function ParentClass(a, b) { }
 * ParentClass.prototype.foo = function(a) { };
 *
 * function ChildClass(a, b, c) {
 *   ChildClass.base(this, 'constructor', a, b);
 * }
 * $jscomp$inherits(ChildClass, ParentClass);
 *
 * var child = new ChildClass('a', 'b', 'see');
 * child.foo(); // This works.
 * </pre>
 *
 * @param {Function} childCtor Child class.
 * @param {Function} parentCtor Parent class.
 */
$jscomp.inherits = function(childCtor, parentCtor) {
  /** @constructor */
  function tempCtor() {}
  tempCtor.prototype = parentCtor.prototype;
  childCtor.superClass_ = parentCtor.prototype;
  childCtor.prototype = new tempCtor();
  /** @override */
  childCtor.prototype.constructor = childCtor;

  /**
   * Calls superclass constructor/method.
   *
   * This function is only available if you use $jscomp$inherits to
   * express inheritance relationships between classes.
   *
   * NOTE: This is a replacement for goog.base and for superClass_
   * property defined in childCtor.
   *
   * @param {!Object} me Should always be "this".
   * @param {string} methodName The method name to call. Calling
   *     superclass constructor can be done with the special string
   *     'constructor'.
   * @param {...*} var_args The arguments to pass to superclass
   *     method/constructor.
   * @return {*} The return value of the superclass method/constructor.
   */
  childCtor.base = function(me, methodName, var_args) {
    var args = Array.prototype.slice.call(arguments, 2);
    return parentCtor.prototype[methodName].apply(me, args);
  };
};
