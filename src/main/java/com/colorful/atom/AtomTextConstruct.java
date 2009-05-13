/**
 * Copyright (C) 2009 William R. Brown <info@colorfulsoftware.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/* Change History:
 * 2008-03-16 wbrown - made class immutable.
 */
package com.colorful.atom;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents an Atom 1.0 text construct.
 * 
 * @see <a
 *      href="http://www.atomenabled.org/developers/syndication/atom-format-spec.php">Atom
 *      Syndication Format</a>
 * @author Bill Brown
 * 
 *         <pre>
 *      atomPlainTextConstruct =
 *          atomCommonAttributes,
 *          attribute type { &quot;text&quot; | &quot;html&quot; }?,
 *          text
 * 
 *      atomXHTMLTextConstruct =
 *          atomCommonAttributes,
 *          attribute type { &quot;xhtml&quot; },
 *          xhtmlDiv
 * 
 *      atomTextConstruct = atomPlainTextConstruct | atomXHTMLTextConstruct
 * </pre>
 * 
 */
class AtomTextConstruct implements Serializable {

	private static final long serialVersionUID = 5347393958118559606L;

	private final List<Attribute> attributes;
	private final String text;
	private final String divWrapperStart;
	private final String divWrapperEnd;

	// content elements do a different validation.
	AtomTextConstruct(String text, List<Attribute> attributes,
			boolean isContentElement) throws AtomSpecException {
		System.out.println("text initially: "+text);
		if (attributes == null) {
			this.attributes = null;
		} else {
			// content elements have a slightly different validation.
			if (isContentElement) {
				this.attributes = new LinkedList<Attribute>();
				for (Attribute attr : attributes) {
					// check for unsupported attribute.
					if (!FeedDoc.isAtomCommonAttribute(attr)
							&& !FeedDoc.isUndefinedAttribute(attr)
							&& !attr.getName().equals("type")
							&& !attr.getName().equals("src")) {
						throw new AtomSpecException("Unsuppported attribute "
								+ attr.getName() + " in this content element ");
					}
					this.attributes.add(new Attribute(attr.getName(), attr
							.getValue()));
				}
			} else {
				// add the attributes
				this.attributes = new LinkedList<Attribute>();

				for (Attribute attr : attributes) {
					// check for unsupported attribute.
					if (!FeedDoc.isAtomCommonAttribute(attr)
							&& !FeedDoc.isUndefinedAttribute(attr)
							&& !attr.getName().equals("type")) {
						throw new AtomSpecException("Unsuppported attribute "
								+ attr.getName()
								+ " for this Atom Text Construct.");
					}
					this.attributes.add(new Attribute(attr.getName(), attr
							.getValue()));
				}
			}
		}
		System.out.println("Content type = "+FeedDoc.getContentType(this.attributes));
		if (FeedDoc.getContentType(this.attributes) == FeedDoc.ContentType.XHTML) {
			this.divWrapperStart = getDivWrapperStart(text);
			this.divWrapperEnd = getDivWrapperEnd(text);
			this.text = getXhtmlText(text);
		} else {
			this.divWrapperStart = null;
			this.divWrapperEnd = null;
			this.text = text;
		}
		

		System.out.println("this.divWrapperStart = " + this.divWrapperStart);
		System.out.println("this.divWrapperEnd = " + this.divWrapperEnd);
		System.out.println("this.text = " + this.text);
	}

	// copy constructor
	AtomTextConstruct(String text, List<Attribute> attributes,
			String divWrapperStart, String divWrapperEnd, boolean isContentElement)
			throws AtomSpecException {
		 System.out.println("in copy constructor");
		 System.out.println("divWrapperStart = " + divWrapperStart);
		 System.out.println("divWrapperEnd = " + divWrapperEnd);
		this.text = text;
		this.divWrapperStart = divWrapperStart;
		this.divWrapperEnd = divWrapperEnd;
		
		if (attributes == null) {
			this.attributes = null;
		} else {
			// content elements have a slightly different validation.
			if (isContentElement) {
				this.attributes = new LinkedList<Attribute>();
				for (Attribute attr : attributes) {
					// check for unsupported attribute.
					if (!FeedDoc.isAtomCommonAttribute(attr)
							&& !FeedDoc.isUndefinedAttribute(attr)
							&& !attr.getName().equals("type")
							&& !attr.getName().equals("src")) {
						throw new AtomSpecException("Unsuppported attribute "
								+ attr.getName() + " in this content element ");
					}
					this.attributes.add(new Attribute(attr.getName(), attr
							.getValue()));
				}
			} else {
				// add the attributes
				this.attributes = new LinkedList<Attribute>();

				for (Attribute attr : attributes) {
					// check for unsupported attribute.
					if (!FeedDoc.isAtomCommonAttribute(attr)
							&& !FeedDoc.isUndefinedAttribute(attr)
							&& !attr.getName().equals("type")) {
						throw new AtomSpecException("Unsuppported attribute "
								+ attr.getName()
								+ " for this Atom Text Construct.");
					}
					this.attributes.add(new Attribute(attr.getName(), attr
							.getValue()));
				}
			}
		}
		System.out.println("this.divWrapperStart = " + this.divWrapperStart);
		System.out.println("this.divWrapperEnd = " + this.divWrapperEnd);
		System.out.println("this.text = " + this.text);
	}

	private String getDivWrapperStart(String text) {
		return text.substring(0, text.indexOf(">") + 1);
	}

	private String getDivWrapperEnd(String text) {
		text = reverseIt(text);
		text = text.substring(0, text.indexOf("<") + 1);
		return reverseIt(text);
	}

	private String getXhtmlText(String text) {
		// strip the wrapper start and end tags.
		// System.out.println("textLocal orig = " + textLocal);
		text = text.substring(text.indexOf(">") + 1);
		// System.out.println("textLocal stripped front = " + textLocal);
		text = reverseIt(text);
		// System.out.println("textLocal reversed = " + textLocal);
		text = text.substring(text.indexOf("<") + 1);
		// System.out
		// .println("textLocal stripped back reverse = " + textLocal);
		return reverseIt(text);
		// System.out.println("textLocal final = " + textLocal);

	}

	// Convenience for reversing the string.
	private String reverseIt(String source) {
		int i, len = source.length();
		StringBuffer dest = new StringBuffer(len);
		for (i = (len - 1); i >= 0; i--)
			dest.append(source.charAt(i));
		return dest.toString();
	}

	/**
	 * 
	 * @return the attributes for this element.
	 */
	public List<Attribute> getAttributes() {
		if (attributes == null) {
			return null;
		}
		List<Attribute> attrsCopy = new LinkedList<Attribute>();
		for (Attribute attr : this.attributes) {
			attrsCopy.add(new Attribute(attr.getName(), attr.getValue()));
		}
		return attrsCopy;
	}

	/**
	 * 
	 * @return the text content for this element.
	 */
	public String getText() {
		return text;
	}

	String getDivWrapperStart() {
		return divWrapperStart;
	}

	String getDivWrapperEnd() {
		return divWrapperEnd;
	}
}