package com.csulb.edu.set.ui.model;

import javafx.beans.property.StringProperty;

public class Document {

	private StringProperty body;
	private StringProperty url;
	private StringProperty title;

	/**
	 * No args constructor for use in serialization
	 * 
	 */
	public Document() {
	}

	/**
	 * 
	 * @param body
	 * @param title
	 * @param url
	 */
	public Document(StringProperty body, StringProperty url, StringProperty title) {
		this.body = body;
		this.url = url;
		this.title = title;
	}

	/**
	 * 
	 * @return The body
	 */
	public StringProperty getBody() {
		return body;
	}

	/**
	 * 
	 * @param body
	 *            The body
	 */
	public void setBody(StringProperty body) {
		this.body = body;
	}

	/**
	 * 
	 * @return The url
	 */
	public StringProperty getUrl() {
		return url;
	}

	/**
	 * 
	 * @param url
	 *            The url
	 */
	public void setUrl(StringProperty url) {
		this.url = url;
	}

	/**
	 * 
	 * @return The title
	 */
	public StringProperty getTitle() {
		return title;
	}

	/**
	 * 
	 * @param title
	 *            The title
	 */
	public void setTitle(StringProperty title) {
		this.title = title;
	}

	/*@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(body).append(url).append(title).toHashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if ((other instanceof Document) == false) {
			return false;
		}
		Document rhs = ((Document) other);
		return new EqualsBuilder().append(body, rhs.body).append(url, rhs.url).append(title, rhs.title).isEquals();
	}*/

}