package org.gillius.webcrawler.model

/**
 * Describes the status of a resource
 */
enum ResourceState {
	/**
	 * The resource is within the site and verified to exist.
	 */
	Exists,
	/**
	 * The resource is within the site and verified to not exist due to error 404.
	 */
	Broken,
	/**
	 * The resource is within the site but cannot be confirmed to exist due to any error &gt;= 400 other than 404.
	 */
	Error,
	/**
	 * The resource is external to the site and was not checked.
	 */
	External,
}