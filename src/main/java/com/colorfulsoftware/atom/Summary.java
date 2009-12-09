/**
 * Copyright (C) 2009 William R. Brown <wbrown@colorfulsoftware.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/* Change History:
 *  2006-11-14 wbrown - added javadoc documentation.
 *  2008-03-16 wbrown - made class immutable.
 */
package com.colorfulsoftware.atom;

import java.io.Serializable;
import java.util.List;

/**
 * This class represents an Atom 1.0 summary element.
 * 
 * @see <a
 *      href="http://www.atomenabled.org/developers/syndication/atom-format-spec.php">Atom
 *      Syndication Format</a>
 * @author Bill Brown
 * 
 *         <pre>
 * 	atomSummary = element atom:summary { atomTextConstruct }
 * </pre>
 */
public class Summary implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8464262582824268938L;
	private final AtomTextConstruct summary;

	// use the factory method in the FeedDoc.
	Summary(String summary, List<Attribute> attributes)
			throws AtomSpecException {
		this.summary = new AtomTextConstruct(summary, attributes, false);
	}

	// copy constructor
	Summary(Summary summary) {
		this.summary = new AtomTextConstruct(summary.summary);
	}

	/**
	 * 
	 * @return the text content for this element.
	 */
	public String getText() {
		return summary.getText();
	}

	/**
	 * 
	 * @return the attributes for this element.
	 */
	public List<Attribute> getAttributes() {
		return summary.getAttributes();
	}

	String getDivStartName() {
		return summary.getDivStartName();
	}

	Attribute getDivStartAttribute() {
		return summary.getDivStartAttribute();
	}

	/**
	 * @param attrName
	 *            the name of the attribute to get.
	 * @return the Attribute object if attrName matches or null if not found.
	 */
	public Attribute getAttribute(String attrName) {
		return summary.getAttribute(attrName);
	}

	AtomTextConstruct.ContentType getContentType() {
		return summary.getContentType();
	}

	/**
	 * Shows the contents of the &lt;summary> element.
	 */
	@Override
	public String toString() {
		return "<summary" + summary + "</summary>";
	}
}
