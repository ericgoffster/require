/**
 * A utility for making requirements (assertions).
 * 
 * This package does not do anything particularly earth shattering, or new,
 * but it is compact, well documented, well tested, open, and does things very well in a versatile manner.
 * It is intended for usage by enterprise class software.
 * 
 * Not only do we provide
 * a modest library of existing assertions/requirements, we allow for you to insert your own.
 * It is also our goal, that this library be 100% backwards compatible, and CVE-free.
 * Future versions of this library will only add new functionality, reserving old behavior, old signatures, etc.
 * Nothing will ever be deprecated.
 *     
 * This particular package has only one class of any note, {@link org.granite.requirement.Requirements}
 * It contains a library of requirements, and a mechanism for enforcing them.
 * 
 * Note that each requirement *is* something.   It does not *do* something.  This allows it to potentially
 * be used for all sorts of things (i.e.  json validation, unit test assertions, code level assertions, etc)
 */
package org.granite.requirement;