/**
 * Copyright (C) 2009 William R. Brown <wbrown@colorfulsoftware.com>
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
 *  2006-11-14 wbrown - added javadoc documentation.
 *  2008-03-16 wbrown - made class immutable.
 */
package com.colorfulsoftware.atom;

import java.io.Serializable;
import java.util.List;

/**
 * This class represents an Atom 1.0 entry element.
 * 
 * @see <a
 *      href="http://www.atomenabled.org/developers/syndication/atom-format-spec.php">Atom
 *      Syndication Format</a>
 * @author Bill Brown
 * 
 *         <pre>
 *      atomEntry =
 *          element atom:entry {
 *          atomCommonAttributes,
 *          (atomAuthor*
 *          &amp; atomCategory*
 *          &amp; atomContent?
 *          &amp; atomContributor*
 *          &amp; atomId
 *          &amp; atomLink*
 *          &amp; atomPublished?
 *          &amp; atomRights?
 *          &amp; atomSource?
 *          &amp; atomSummary?
 *          &amp; atomTitle
 *          &amp; atomUpdated
 *          &amp; extensionElement*)
 *          }
 * </pre>
 */
public class Entry implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5291388675692200218L;
	private final AtomEntrySourceAdaptor entryAdaptor;
	private final Content content;
	private final Published published;
	private final Source source;
	private final Summary summary;

	// use the factory method in the FeedDoc.
	Entry(Id id, Title title, Updated updated, Rights rights, Content content,
			List<Author> authors, List<Category> categories,
			List<Contributor> contributors, List<Link> links,
			List<Attribute> attributes, List<Extension> extensions,
			Published published, Summary summary, Source source)
			throws AtomSpecException {

		// check for functional requirements here because
		// they are all optional for a Source element.

		// make sure id is present
		if (id == null) {
			throw new AtomSpecException(
					"atom:entry elements MUST contain exactly one atom:id element.");
		}
		// make sure title is present
		if (title == null) {
			throw new AtomSpecException(
					"atom:entry elements MUST contain exactly one atom:title element.");
		}
		// make sure updated is present
		// it is actually checked further up in the reader because of how
		// we store entries.

		this.entryAdaptor = new AtomEntrySourceAdaptor(id, title, updated,
				rights, authors, categories, contributors, links, attributes,
				extensions);

		if (content == null) {
			this.content = null;
		} else {

			if (content.getAttributes() != null) {
				for (Attribute attr : content.getAttributes()) {
					// check for src attribute
					if (attr.getName().equals("src") && summary == null) {
						throw new AtomSpecException(
								"atom:entry elements MUST contain an atom:summary element if the atom:entry contains an atom:content that has a \"src\" attribute (and is thus empty).");
					}
				}
			}

			this.content = new Content(content);
		}

		this.published = (published == null) ? null : new Published(published
				.getDateTime(), published.getAttributes());
		this.source = (source == null) ? null : new Source(source.getId(),
				source.getTitle(), source.getUpdated(), source.getRights(),
				source.getAuthors(), source.getCategories(), source
						.getContributors(), source.getLinks(), source
						.getAttributes(), source.getExtensions(), source
						.getGenerator(), source.getSubtitle(),
				source.getIcon(), source.getLogo());
		this.summary = (summary == null) ? null : new Summary(summary);
	}

	/**
	 * 
	 * @return the content for this entry.
	 * @throws AtomSpecException
	 *             if the format of the data is not valid.
	 */
	public Content getContent() throws AtomSpecException {
		return (content == null) ? null : new Content(content);
	}

	/**
	 * 
	 * @return the published date for this entry.
	 * @throws AtomSpecException
	 *             if the format of the data is not valid.
	 */
	public Published getPublished() throws AtomSpecException {
		return (published == null) ? null : new Published(published
				.getDateTime(), published.getAttributes());
	}

	/**
	 * 
	 * @return the source for this element.
	 * @throws AtomSpecException
	 *             if the format of the data is not valid.
	 */
	public Source getSource() throws AtomSpecException {
		return (source == null) ? null : new Source(source.getId(), source
				.getTitle(), source.getUpdated(), source.getRights(), source
				.getAuthors(), source.getCategories(),
				source.getContributors(), source.getLinks(), source
						.getAttributes(), source.getExtensions(), source
						.getGenerator(), source.getSubtitle(),
				source.getIcon(), source.getLogo());
	}

	/**
	 * 
	 * @return the summary for this element.
	 * @throws AtomSpecException
	 *             if the format of the data is not valid.
	 */
	public Summary getSummary() throws AtomSpecException {
		return (summary == null) ? null : new Summary(summary);
	}

	/**
	 * 
	 * @return the unique identifier for this entry.
	 * @throws AtomSpecException
	 *             if the format of the data is not valid.
	 */
	public Id getId() throws AtomSpecException {
		return entryAdaptor.getId();
	}

	/**
	 * 
	 * @return the title for this element.
	 * @throws AtomSpecException
	 *             if the format of the data is not valid.
	 */
	public Title getTitle() throws AtomSpecException {
		return entryAdaptor.getTitle();
	}

	/**
	 * 
	 * @return the updated date for this element.
	 * @throws AtomSpecException
	 *             if the format of the data is not valid.
	 */
	public Updated getUpdated() throws AtomSpecException {
		return entryAdaptor.getUpdated();
	}

	/**
	 * 
	 * @return the associated rights for this entry.
	 * @throws AtomSpecException
	 *             if the format of the data is not valid.
	 */
	public Rights getRights() throws AtomSpecException {
		return entryAdaptor.getRights();
	}

	/**
	 * 
	 * @return the authors for this entry.
	 * @throws AtomSpecException
	 *             if the format of the data is not valid.
	 */
	public List<Author> getAuthors() throws AtomSpecException {
		return entryAdaptor.getAuthors();
	}

	/**
	 * 
	 * @return the categories for this element.
	 * @throws AtomSpecException
	 *             if the format of the data is not valid.
	 */
	public List<Category> getCategories() throws AtomSpecException {
		return entryAdaptor.getCategories();
	}

	/**
	 * 
	 * @return the contributors for this entry.
	 * @throws AtomSpecException
	 *             if the format of the data is not valid.
	 */
	public List<Contributor> getContributors() throws AtomSpecException {
		return entryAdaptor.getContributors();
	}

	/**
	 * 
	 * @return the links for this entry.
	 * @throws AtomSpecException
	 *             if the format of the data is not valid.
	 */
	public List<Link> getLinks() throws AtomSpecException {
		return entryAdaptor.getLinks();
	}

	/**
	 * 
	 * @return the category attribute list.
	 * @throws AtomSpecException
	 *             if the format of the data is not valid.
	 */
	public List<Attribute> getAttributes() throws AtomSpecException {
		return entryAdaptor.getAttributes();
	}

	/**
	 * 
	 * @return the extensions for this entry.
	 * @throws AtomSpecException
	 *             if the format of the data is not valid.
	 */
	public List<Extension> getExtensions() throws AtomSpecException {
		return entryAdaptor.getExtensions();
	}

	/**
	 * @param attrName
	 *            the name of the attribute to get.
	 * @return the Attribute object if attrName matches or null if not found.
	 * @throws AtomSpecException
	 *             if the format of the data is not valid.
	 */
	public Attribute getAttribute(String attrName) throws AtomSpecException {
		return entryAdaptor.getAttribute(attrName);
	}

	/**
	 * @param name
	 *            the name of the author to get.
	 * @return the Author object if the name matches or null if not found.
	 * @throws AtomSpecException
	 *             if the format of the data is not valid.
	 */
	public Author getAuthor(String name) throws AtomSpecException {
		return entryAdaptor.getAuthor(name);
	}

	/**
	 * @param termValue
	 *            the term value.
	 * @return the Category object if the term matches or null if not found.
	 * @throws AtomSpecException
	 *             if the format of the data is not valid.
	 */
	public Category getCategory(String termValue) throws AtomSpecException {
		return entryAdaptor.getCategory(termValue);
	}

	/**
	 * @param name
	 *            the name of the contributor
	 * @return the Contributor object if name matches or null if not found.
	 * @throws AtomSpecException
	 *             if the format of the data is not valid.
	 */
	public Contributor getContributor(String name) throws AtomSpecException {
		return entryAdaptor.getContributor(name);
	}

	/**
	 * @param hrefVal
	 *            the href attribute value to look for.
	 * @return the Link object if href matches or null if not found.
	 * @throws AtomSpecException
	 *             if the format of the data is not valid.
	 */
	public Link getLink(String hrefVal) throws AtomSpecException {
		return entryAdaptor.getLink(hrefVal);
	}

	/**
	 * @param extName
	 *            the element name of the extension to get.
	 * @return the Extension object if extName matches or null if not found.
	 * @throws AtomSpecException
	 *             if the format of the data is not valid.
	 */
	public Extension getExtension(String extName) throws AtomSpecException {
		return entryAdaptor.getExtension(extName);
	}
}
