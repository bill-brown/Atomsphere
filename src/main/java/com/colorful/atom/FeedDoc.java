/*
Atomsphere - an atom feed library.
Copyright (C) 2006 William R. Brown.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Change History:
    2006-11-08 wbrown - changed API to include url's and make the method calls more intuitive.
    2006-11-12 wbrown - added javadoc documentation.
    2007-02-22 wbrown - removed deprecated methods.
    2007-06-20 wbrown - added 2 methods readFeedToString(Feed feed) and readEntryToString(Entry entry)
    2008-03-18 wbrown - added factory methods for all the sub elements. Added new file write methods 
    					to decouple dependency on stax-utils.  Added new input methods to read 
    					input streams into feeds an strings.
    2008-04-08 wbrown - added atomshpereVersion (generator) constant for writing out feeds.  Added more
    					thorws AtomsphereSpecExceptions for attributes that are not atomCommonAttribute
    2008-04-15 wbrown - fixed entry element to String entry document
    2008-04-17 wbrown - added better support for reading and writing Entry documents.
    2008-06-03 wbrown - fixed bug with getContentType()
 */
package com.colorful.atom;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * This class reads and writes atom documents to and from xml files, objects or
 * Strings. It contains all of the factory methods for building immutable copies
 * of the object elements.
 * 
 * @author Bill Brown
 * 
 */
public class FeedDoc {

	/**
	 * 
	 * An enumeration of the different types of supported content.
	 * 
	 */
	public static enum ContentType {
		TEXT, HTML, XHTML, OTHER, EXTERNAL
	}

	/**
	 * the default atom xml namespace attribute of "http://www.w3.org/2005/Atom"
	 */
	public static final Attribute atomBase = buildAttribute("xmlns",
			"http://www.w3.org/2005/Atom");

	/**
	 * the default library language attribute of "en-US"
	 */
	public static final Attribute lang_en = buildAttribute("xml:lang", "en-US");

	/**
	 * the default document encoding of "UTF-8"
	 */
	public static String encoding = "UTF-8";

	/**
	 * the default XML version of "1.0"
	 */
	public static String xml_version = "1.0";

	private static String libUri;
	private static String libVersion;
	static {
		try {
			Properties props = new Properties();
			props.load(FeedDoc.class
					.getResourceAsStream("/atomsphere.properties"));
			libUri = props.getProperty("uri");
			libVersion = props.getProperty("version");
		} catch (Exception e) {
			// should not happen.
			e.printStackTrace();
		}
	}

	/**
	 * @return the Atomsphere library version in the form of a generator
	 *         element. This element is output for all feeds that are generated
	 *         by Atomsphere.
	 */
	public static Generator getAtomsphereVersion() {
		try {
			List<Attribute> attributes = new LinkedList<Attribute>();
			attributes.add(FeedDoc.buildAttribute("uri", libUri));
			attributes.add(FeedDoc.buildAttribute("version", libVersion));
			return FeedDoc.buildGenerator(attributes, "Atomsphere");
		} catch (Exception e) {
			// this shouldn't happen;
			return null;
		}
	}

	/**
	 * the Atomsphere sort extension attribute. See the <a
	 * href="http://www.colorfulsoftware.com/projects/atomsphere"> Project
	 * Page</a> for more details
	 */
	public static final Attribute sort = buildAttribute("xmlns:sort",
			"http://www.colorfulsoftware.com/projects/atomsphere/extension/sort/1.0");

	/**
	 * Comparator for sorting feed entries in ascending order.
	 */
	public static final Comparator<String> SORT_ASC = new Comparator<String>() {
		public int compare(String key1, String key2) {
			return key1.compareTo(key2);

		}
	};

	/**
	 * Comparator for sorting feed entries in descending order
	 */
	public static final Comparator<String> SORT_DESC = new Comparator<String>() {
		public int compare(String key1, String key2) {
			return key2.compareTo(key1);
		}
	};

	/**
	 * 
	 * @param output
	 *            the target output for the feed document.
	 * @param feed
	 *            the atom feed object containing the content of the feed
	 * @param encoding
	 *            the file encoding (default is UTF-8)
	 * @param version
	 *            the xml version (default is 1.0)
	 * @throws Exception
	 *             thrown if the feed cannot be written to the output
	 */
	public static void writeFeedDoc(OutputStream output, Feed feed,
			String encoding, String version) throws Exception {
		writeFeedDoc(XMLOutputFactory.newInstance().createXMLStreamWriter(
				output, encoding), feed, encoding, version);
	}

	/**
	 * 
	 * @param output
	 *            the target output for the entry document.
	 * @param entry
	 *            the atom entry object containing the content.
	 * @param encoding
	 *            the file encoding (default is UTF-8)
	 * @param version
	 *            the xml version (default is 1.0)
	 * @throws Exception
	 *             thrown if the atom document cannot be written to the output
	 */
	public static void writeEntryDoc(OutputStream output, Entry entry,
			String encoding, String version) throws Exception {
		writeEntryDoc(XMLOutputFactory.newInstance().createXMLStreamWriter(
				output, encoding), entry, encoding, version);
	}

	/**
	 * 
	 * @param output
	 *            the target output for the document.
	 * @param feed
	 *            the atom feed object containing the content of the feed
	 * @param encoding
	 *            the file encoding (default is UTF-8)
	 * @param version
	 *            the xml version (default is 1.0)
	 * @throws Exception
	 *             thrown if the feed cannot be written to the output
	 */
	public static void writeFeedDoc(Writer output, Feed feed, String encoding,
			String version) throws Exception {
		writeFeedDoc(XMLOutputFactory.newInstance().createXMLStreamWriter(
				output), feed, encoding, version);
	}

	/**
	 * 
	 * @param output
	 *            the target output for the entry document.
	 * @param entry
	 *            the atom entry object containing the content.
	 * @param encoding
	 *            the file encoding (default is UTF-8)
	 * @param version
	 *            the xml version (default is 1.0)
	 * @throws Exception
	 *             thrown if the atom document cannot be written to the output
	 */
	public static void writeEntryDoc(Writer output, Entry entry,
			String encoding, String version) throws Exception {
		writeEntryDoc(XMLOutputFactory.newInstance().createXMLStreamWriter(
				output), entry, encoding, version);
	}

	/**
	 * For example: to pass the TXW
	 * com.sun.xml.txw2.output.IndentingXMLStreamWriter or the stax-utils
	 * javanet.staxutils.IndentingXMLStreamWriter for indented printing do this:
	 * 
	 * <pre>
	 * XmlStreamWriter writer = new IndentingXMLStreamWriter(XMLOutputFactory
	 * 		.newInstance().createXMLStreamWriter(
	 * 				new FileOutputStream(outputFilePath), encoding));
	 * FeedDoc.writeFeedDoc(writer, myFeed, null, null);
	 * </pre>
	 * 
	 * @param output
	 *            the target output for the feed.
	 * @param feed
	 *            the atom feed object containing the content of the feed
	 * @param encoding
	 *            the file encoding (default is UTF-8)
	 * @param version
	 *            the xml version (default is 1.0)
	 * @throws Exception
	 *             thrown if the feed cannot be written to the output
	 */
	public static void writeFeedDoc(XMLStreamWriter output, Feed feed,
			String encoding, String version) throws Exception {

		try {
			writeFeedOutput(feed, output, encoding, version);
		} catch (Exception e) {
			throw new Exception("error creating the feed document.", e);
		}
	}

	/**
	 * Writes and entry element to a document.
	 * 
	 * @param output
	 *            the target output for the entry document.
	 * @param entry
	 *            the atom entry object containing the content of the entry
	 * @param encoding
	 *            the file encoding (default is UTF-8)
	 * @param version
	 *            the xml version (default is 1.0)
	 * @throws Exception
	 *             thrown if the feed cannot be written to the output see
	 * 
	 *             <code>writeFeedDoc(XMLStreamWriter output,Feed feed,String encoding,String version)</code>
	 */
	public static void writeEntryDoc(XMLStreamWriter output, Entry entry,
			String encoding, String version) throws Exception {
		try {
			writeEntryOutput(entry, output, encoding, version);
		} catch (Exception e) {
			throw new Exception("error creating entry document.", e);
		}
	}

	/**
	 * This method reads in a Feed element and returns the contents as an atom
	 * feed string with formatting specified by the fully qualified
	 * XMLStreamWriter class name (uses reflection internally). For example you
	 * can pass the TXW com.sun.xml.txw2.output.IndentingXMLStreamWriter or the
	 * stax-utils javanet.staxutils.IndentingXMLStreamWriter for indented
	 * printing. It will fall back to
	 * 
	 * <pre>
	 * readFeedToString(Feed)
	 * </pre>
	 * 
	 * if the XMLStreamWriter class cannot be found in the classpath.
	 * 
	 * @param feed
	 *            the feed to be converted to an atom document string.
	 * @param xmlStreamWriter
	 *            the fully qualified XMLStreamWriter class name.
	 * @return an atom feed document string.
	 * @throws Exception
	 *             thrown if the feed cannot be returned as a String
	 */
	public static String readFeedToString(Feed feed, String xmlStreamWriter)
			throws Exception {

		StringWriter theString = new StringWriter();
		try {
			Class<?> cls = Class.forName(xmlStreamWriter);
			Constructor<?> ct = cls
					.getConstructor(new Class[] { XMLStreamWriter.class });
			Object arglist[] = new Object[] { XMLOutputFactory.newInstance()
					.createXMLStreamWriter(theString) };
			XMLStreamWriter writer = (XMLStreamWriter) ct.newInstance(arglist);

			writeFeedOutput(feed, writer, encoding, xml_version);

		} catch (Exception e) {
			return readFeedToString(feed);
		}
		return theString.toString();
	}

	/**
	 * This method reads in an Entry element and returns the contents as an atom
	 * feed document string
	 * 
	 * @param entry
	 *            the entry to be converted to an atom document string.
	 * @param xmlStreamWriter
	 *            the XMLStreamWriter to use
	 * @return the atom entry document string.
	 * @throws Exception
	 *             if the entry cannot be returned as a String see
	 *             <code>readFeedToString(Feed feed, String xmlStreamWriter)</code>
	 */
	public static String readEntryToString(Entry entry, String xmlStreamWriter)
			throws Exception {

		StringWriter theString = new StringWriter();
		try {
			Class<?> cls = Class.forName(xmlStreamWriter);
			Constructor<?> ct = cls
					.getConstructor(new Class[] { XMLStreamWriter.class });
			Object arglist[] = new Object[] { XMLOutputFactory.newInstance()
					.createXMLStreamWriter(theString) };
			XMLStreamWriter writer = (XMLStreamWriter) ct.newInstance(arglist);

			writeEntryOutput(entry, writer, encoding, xml_version);

		} catch (Exception e) {
			return readEntryToString(entry);
		}
		return theString.toString();
	}

	/**
	 * This method reads in a Feed bean and returns the contents as an atom feed
	 * string.
	 * 
	 * @param feed
	 *            the feed to be converted to an atom string.
	 * @return an atom feed document string.
	 * @throws Exception
	 *             thrown if the feed cannot be returned as a String
	 */
	public static String readFeedToString(Feed feed) throws Exception {

		StringWriter theString = new StringWriter();
		try {
			XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
			XMLStreamWriter writer = outputFactory
					.createXMLStreamWriter(theString);

			writeFeedOutput(feed, writer, encoding, xml_version);

		} catch (Exception e) {
			throw new Exception("error creating xml file.", e);
		}
		return theString.toString();
	}

	/**
	 * This method reads in an atom Entry element and returns the contents as an
	 * atom Entry document String containing the entry.
	 * 
	 * @param entry
	 *            the entry to be converted to an atom entry document string.
	 * @return an atom entry document string containing the entry argument
	 *         passed in.
	 * @throws Exception
	 *             thrown if the feed cannot be returned as a String
	 */
	public static String readEntryToString(Entry entry) throws Exception {

		StringWriter theString = new StringWriter();
		try {
			XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
			XMLStreamWriter writer = outputFactory
					.createXMLStreamWriter(theString);

			writeEntryOutput(entry, writer, encoding, xml_version);

		} catch (Exception e) {
			throw new Exception("error creating xml file.", e);
		}
		return theString.toString();
	}

	// used for writing entry documents to their output.
	private static void writeEntryOutput(Entry entry, XMLStreamWriter writer,
			String encoding, String version) throws AtomSpecException,
			Exception, XMLStreamException {
		SortedMap<String, Entry> entries = new TreeMap<String, Entry>();

		// add atom base and language to the entry if they are not there.
		List<Attribute> attributes = entry.getAttributes();
		if (attributes == null) {
			attributes = new LinkedList<Attribute>();
		}
		if (getAttributeFromGroup(attributes, atomBase.getName()) == null) {
			attributes.add(atomBase);
		}
		if (getAttributeFromGroup(attributes, lang_en.getName()) == null) {
			attributes.add(lang_en);
		}

		// rebuild the entry with the added attributes.
		entries
				.put(entry.getUpdated().getText(), buildEntry(entry.getId(),
						entry.getTitle(), entry.getUpdated(),
						entry.getRights(), entry.getContent(), entry
								.getAuthors(), entry.getCategories(), entry
								.getContributors(), entry.getLinks(),
						attributes, entry.getExtensions(),
						entry.getPublished(), entry.getSummary(), entry
								.getSource()));

		// write the xml header.
		writer.writeStartDocument(encoding, version);
		new FeedWriter().writeEntries(writer, entries);
		writer.flush();
		writer.close();
	}

	/**
	 * This method reads an xml string into a Feed element.
	 * 
	 * @param xmlString
	 *            the xml string to be transformed into a Feed element.
	 * @return the atom Feed element
	 * @throws Exception
	 *             if the string cannot be parsed into a Feed element.
	 */
	public static Feed readFeedToBean(String xmlString) throws Exception {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLStreamReader reader = inputFactory
				.createXMLStreamReader(new java.io.StringReader(xmlString));
		return new FeedReader().readFeed(reader);
	}

	/**
	 * This method reads an xml string into a Entry element.
	 * 
	 * @param xmlString
	 *            the xml string to be transformed into a Entry element.
	 * @return the atom Entry element
	 * @throws Exception
	 *             if the string cannot be parsed into a Entry element.
	 */
	public static Entry readEntryToBean(String xmlString) throws Exception {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLStreamReader reader = inputFactory
				.createXMLStreamReader(new java.io.StringReader(xmlString));
		SortedMap<String, Entry> entries = new FeedReader().readEntry(reader,
				null);
		if (entries == null || entries.size() > 1) {
			throw new AtomSpecException(
					"invalid number of entries for this entry document.");
		}
		return entries.values().iterator().next();
	}

	/**
	 * This method reads an xml File object into a Feed element.
	 * 
	 * @param file
	 *            the file object representing an atom file.
	 * @return the atom Feed element.
	 * @throws Exception
	 *             if the file cannot be parsed into a Feed element.
	 */
	public static Feed readFeedToBean(File file) throws Exception {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLStreamReader reader = inputFactory
				.createXMLStreamReader(new FileInputStream(file));
		return new FeedReader().readFeed(reader);
	}

	/**
	 * This method reads an xml File object into an Entry element.
	 * 
	 * @param file
	 *            the file object representing an atom file.
	 * @return the atom Entry element.
	 * @throws Exception
	 *             if the file cannot be parsed into an Entry element.
	 */
	public static Entry readEntryToBean(File file) throws Exception {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLStreamReader reader = inputFactory
				.createXMLStreamReader(new FileInputStream(file));
		SortedMap<String, Entry> entries = new FeedReader().readEntry(reader,
				null);
		if (entries == null || entries.size() > 1) {
			throw new AtomSpecException(
					"invalid number of entries for this entry document.");
		}
		return entries.values().iterator().next();
	}

	/**
	 * This method reads an atom file from a URL into a Feed element.
	 * 
	 * @param url
	 *            the Internet network location of an atom file.
	 * @return the atom Feed element.
	 * @throws Exception
	 *             if the URL cannot be parsed into a Feed element.
	 */
	public static Feed readFeedToBean(URL url) throws Exception {
		return readFeedToBean(url.openStream());
	}

	/**
	 * This method reads an atom file from a URL into a Entry element.
	 * 
	 * @param url
	 *            the Internet network location of an atom file.
	 * @return the atom Entry element.
	 * @throws Exception
	 *             if the URL cannot be parsed into a Entry element.
	 */
	public static Entry readEntryToBean(URL url) throws Exception {
		return readEntryToBean(url.openStream());
	}

	/**
	 * This method reads an atom file from an input stream into a Feed element.
	 * 
	 * @param inputStream
	 *            the input stream containing an atom file.
	 * @return the atom Feed element.
	 * @throws Exception
	 *             if the URL cannot be parsed into a Feed element.
	 */
	public static Feed readFeedToBean(InputStream inputStream) throws Exception {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLStreamReader reader = inputFactory
				.createXMLStreamReader(inputStream);
		return new FeedReader().readFeed(reader);
	}

	/**
	 * This method reads an atom file from an input stream into a Entry element.
	 * 
	 * @param inputStream
	 *            the input stream containing an atom file.
	 * @return the atom Entry element.
	 * @throws Exception
	 *             if the URL cannot be parsed into a Feed element.
	 */
	public static Entry readEntryToBean(InputStream inputStream)
			throws Exception {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLStreamReader reader = inputFactory
				.createXMLStreamReader(inputStream);
		SortedMap<String, Entry> entries = new FeedReader().readEntry(reader,
				null);
		if (entries == null || entries.size() > 1) {
			throw new AtomSpecException(
					"invalid number of entries for this entry document.");
		}
		return entries.values().iterator().next();
	}

	/**
	 * 
	 * @param id
	 *            the unique id element (optional)
	 * @param title
	 *            the title element (optional)
	 * @param updated
	 *            the updated element (optional)
	 * @param rights
	 *            the rights element (optional)
	 * @param authors
	 *            a list of author elements (optional)
	 * @param categories
	 *            a list of category elements (optional)
	 * @param contributors
	 *            a list of contributor elements (optional)
	 * @param links
	 *            a list of link elements (optional)
	 * @param attributes
	 *            additional attributes (optional)
	 * @param extensions
	 *            a list of extension elements (optional)
	 * @param generator
	 *            the generator element (optional)
	 * @param subtitle
	 *            the subtitle element (optional)
	 * @param icon
	 *            the icon element (optional)
	 * @param logo
	 *            the logo element (optional)
	 * @param entries
	 *            a list of entry elements (optional)
	 * @return an immutable Feed object.
	 * @throws AtomSpecException
	 *             if the data violates the <a href=
	 *             "http://atomenabled.org/developers/syndication/atom-format-spec.php"
	 *             >specification</a>.
	 */
	public static Feed buildFeed(Id id, Title title, Updated updated,
			Rights rights, List<Author> authors, List<Category> categories,
			List<Contributor> contributors, List<Link> links,
			List<Attribute> attributes, List<Extension> extensions,
			Generator generator, Subtitle subtitle, Icon icon, Logo logo,
			SortedMap<String, Entry> entries) throws AtomSpecException {
		return new Feed(id, title, updated, rights, authors, categories,
				contributors, links, attributes, extensions, generator,
				subtitle, icon, logo, entries);
	}

	/**
	 * 
	 * @param name
	 *            the attribute name.
	 * @param value
	 *            the attribute value.
	 * @return an immutable Attribute object.
	 */
	public static Attribute buildAttribute(String name, String value) {
		return new Attribute(name, value);
	}

	/**
	 * 
	 * @param name
	 *            the name element. (required)
	 * @param uri
	 *            the uri element.
	 * @param email
	 *            the email element.
	 * @param attributes
	 *            additional attributes.
	 * @param extensions
	 *            a list of extension elements.
	 * @return an immutable Author object.
	 * @throws AtomSpecException
	 *             if the data violates the <a href=
	 *             "http://atomenabled.org/developers/syndication/atom-format-spec.php"
	 *             >specification</a>.
	 */
	public static Author buildAuthor(Name name, URI uri, Email email,
			List<Attribute> attributes, List<Extension> extensions)
			throws AtomSpecException {
		return new Author(name, uri, email, attributes, extensions);
	}

	/**
	 * 
	 * @param attributes
	 *            the attributes list which must contain "term" and may contain
	 *            "scheme", "label" or others
	 * @param content
	 *            the undefined element content.
	 * @return an immutable Category object.
	 * @throws AtomSpecException
	 *             if the data violates the <a href=
	 *             "http://atomenabled.org/developers/syndication/atom-format-spec.php"
	 *             >specification</a>.
	 */
	public static Category buildCategory(List<Attribute> attributes,
			String content) throws AtomSpecException {
		return new Category(attributes, content);
	}

	/**
	 * 
	 * @param content
	 *            the content of this element
	 * @param attributes
	 *            additional attributes.
	 * @return an immutable Content object.
	 */
	public static Content buildContent(String content,
			List<Attribute> attributes) throws AtomSpecException {
		return new Content(content, attributes);
	}

	/**
	 * 
	 * @param name
	 *            the name element. (required)
	 * @param uri
	 *            the uri element.
	 * @param email
	 *            the email element.
	 * @param attributes
	 *            additional attributes.
	 * @param extensions
	 *            a list of extension elements.
	 * @return an immutable Contributor object.
	 * @throws AtomSpecException
	 *             if the data violates the <a href=
	 *             "http://atomenabled.org/developers/syndication/atom-format-spec.php"
	 *             >specification</a>.
	 */
	public static Contributor buildContributor(Name name, URI uri, Email email,
			List<Attribute> attributes, List<Extension> extensions)
			throws AtomSpecException {
		return new Contributor(name, uri, email, attributes, extensions);
	}

	/**
	 * 
	 * @param email
	 *            a human-readable email for the person
	 * @return an immutable Email object.
	 */
	public static Email buildEmail(String email) {
		return new Email(email);
	}

	/**
	 * 
	 * @param id
	 *            the id element (required)
	 * @param title
	 *            the title element (required)
	 * @param updated
	 *            the updated element (required)
	 * @param rights
	 *            the rights element (optional)
	 * @param content
	 *            the content element (optional)
	 * @param authors
	 *            a list of author elements (optional)
	 * @param categories
	 *            a list of category elements (optional)
	 * @param contributors
	 *            a list of contributor elements (optional)
	 * @param links
	 *            a list of link elements (optional)
	 * @param attributes
	 *            additional attributes.(optional)
	 * @param extensions
	 *            a list of extension elements (optional)
	 * @param published
	 *            the published element (optional)
	 * @param summary
	 *            the summary element (optional)
	 * @param source
	 *            the source element (optional)
	 * @return an immutable Entry object.
	 * @throws AtomSpecException
	 *             if the data violates the <a href=
	 *             "http://atomenabled.org/developers/syndication/atom-format-spec.php"
	 *             >specification</a>.
	 */
	public static Entry buildEntry(Id id, Title title, Updated updated,
			Rights rights, Content content, List<Author> authors,
			List<Category> categories, List<Contributor> contributors,
			List<Link> links, List<Attribute> attributes,
			List<Extension> extensions, Published published, Summary summary,
			Source source) throws AtomSpecException {
		return new Entry(id, title, updated, rights, content, authors,
				categories, contributors, links, attributes, extensions,
				published, summary, source);
	}

	/**
	 * 
	 * @param elementName
	 *            the name of the extension element.
	 * @param attributes
	 *            additional attributes.
	 * @param content
	 *            the content of the extension element.
	 * @return an immutable Extension object.
	 */
	public static Extension buildExtension(String elementName,
			List<Attribute> attributes, String content) {
		return new Extension(elementName, attributes, content);
	}

	/**
	 * 
	 * @param attributes
	 *            the attributes list which can contain "uri" and or "version"
	 *            or others
	 * @param text
	 *            the text content.
	 * @return an immutable Generator object.
	 */
	public static Generator buildGenerator(List<Attribute> attributes,
			String text) throws AtomSpecException {
		return new Generator(attributes, text);
	}

	/**
	 * 
	 * @param atomUri
	 *            the URI reference.
	 * @param attributes
	 *            additional attributes.
	 * @return an immutable Icon object.
	 */
	public static Icon buildIcon(List<Attribute> attributes, String atomUri)
			throws AtomSpecException {
		return new Icon(attributes, atomUri);
	}

	/**
	 * 
	 * @param atomUri
	 *            the URI reference.
	 * @param attributes
	 *            additional attributes.
	 * @return an immutable Id object.
	 */
	public static Id buildId(List<Attribute> attributes, String atomUri)
			throws AtomSpecException {
		return new Id(attributes, atomUri);
	}

	/**
	 * 
	 * @param attributes
	 *            the attributes list which must contain "href" and may contain
	 *            "rel", "type", "hreflang", "title", "length" or others
	 * @param content
	 *            the undefined link content.
	 * @return an immutable Link object.
	 * @throws AtomSpecException
	 *             if the data violates the <a href=
	 *             "http://atomenabled.org/developers/syndication/atom-format-spec.php"
	 *             >specification</a>.
	 */
	public static Link buildLink(List<Attribute> attributes, String content)
			throws AtomSpecException {
		return new Link(attributes, content);
	}

	/**
	 * 
	 * @param atomUri
	 *            the logo uri reference.
	 * @param attributes
	 *            additional attributes.
	 * @return an immutable Logo object.
	 */
	public static Logo buildLogo(List<Attribute> attributes, String atomUri)
			throws AtomSpecException {
		return new Logo(attributes, atomUri);
	}

	/**
	 * 
	 * @param name
	 *            a human-readable name for the person
	 * @return an immutable Name object.
	 */
	public static Name buildName(String name) {
		return new Name(name);
	}

	/**
	 * 
	 * @param published
	 *            the date formatted to [RFC3339]
	 * @return an immutable Published object.
	 */
	public static Published buildPublished(Date published,
			List<Attribute> attributes) throws AtomSpecException {
		return new Published(published, attributes);
	}

	/**
	 * 
	 * @param rights
	 *            the rights text.
	 * @param attributes
	 *            additional attributes.
	 * @return an immutable Rights object.
	 */
	public static Rights buildRights(String rights, List<Attribute> attributes)
			throws AtomSpecException {
		return new Rights(rights, attributes);
	}

	/**
	 * 
	 * @param id
	 *            the unique id element (optional)
	 * @param title
	 *            the title element (optional)
	 * @param updated
	 *            the updated element (optional)
	 * @param rights
	 *            the rights element (optional)
	 * @param authors
	 *            a list of author elements (optional)
	 * @param categories
	 *            a list of category elements (optional)
	 * @param contributors
	 *            a list of contributor elements (optional)
	 * @param links
	 *            a list of link elements (optional)
	 * @param attributes
	 *            additional attributes (optional)
	 * @param extensions
	 *            a list of extension elements (optional)
	 * @param generator
	 *            the generator element (optional)
	 * @param subtitle
	 *            the subtitle element (optional)
	 * @param icon
	 *            the icon element (optional)
	 * @param logo
	 *            the logo element (optional)
	 * @return an immutable Source object.
	 * @throws AtomSpecException
	 *             if the data violates the <a href=
	 *             "http://atomenabled.org/developers/syndication/atom-format-spec.php"
	 *             >specification</a>.
	 */
	public static Source buildSource(Id id, Title title, Updated updated,
			Rights rights, List<Author> authors, List<Category> categories,
			List<Contributor> contributors, List<Link> links,
			List<Attribute> attributes, List<Extension> extensions,
			Generator generator, Subtitle subtitle, Icon icon, Logo logo)
			throws AtomSpecException {
		return new Source(id, title, updated, rights, authors, categories,
				contributors, links, attributes, extensions, generator,
				subtitle, icon, logo);
	}

	/**
	 * 
	 * @param subtitle
	 *            the subtitle text.
	 * @param attributes
	 *            additional attributes.
	 * @return an immutable Subtitle object.
	 */
	public static Subtitle buildSubtitle(String subtitle,
			List<Attribute> attributes) throws AtomSpecException {
		return new Subtitle(subtitle, attributes);
	}

	/**
	 * 
	 * @param summary
	 *            the summary text.
	 * @param attributes
	 *            additional attributes.
	 * @return an immutable Summary object.
	 */
	public static Summary buildSummary(String summary,
			List<Attribute> attributes) throws AtomSpecException {
		return new Summary(summary, attributes);
	}

	/**
	 * 
	 * @param title
	 *            the title text
	 * @param attributes
	 *            additional attributes.
	 * @return an immutable Title object.
	 */
	public static Title buildTitle(String title, List<Attribute> attributes)
			throws AtomSpecException {
		return new Title(title, attributes);
	}

	/**
	 * 
	 * @param updated
	 *            the date formatted to [RFC3339]
	 * @return a immutable Updated object.
	 */
	public static Updated buildUpdated(Date updated, List<Attribute> attributes)
			throws AtomSpecException {
		return new Updated(updated, attributes);
	}

	/**
	 * 
	 * @param uri
	 *            the content of the uri according to Section 7 of [RFC3986]
	 * @return and immutable URI object.
	 */
	public static URI buildURI(String uri) {
		return new URI(uri);
	}

	// used to write feed output for several feed writing methods.
	private static void writeFeedOutput(Feed feed, XMLStreamWriter writer,
			String encoding, String version) throws XMLStreamException,
			Exception {

		// make sure the feed is sorted before it is written out to the file.
		// this prevents the client code from having to
		// maintain the sorting during usage
		feed = FeedDoc.checkForAndApplyExtension(feed, FeedDoc.sort);

		// add atom base and xml_language to the entry if they are not there.
		List<Attribute> attributes = feed.getAttributes();
		if (attributes == null) {
			attributes = new LinkedList<Attribute>();
		}
		if (getAttributeFromGroup(attributes, atomBase.getName()) == null) {
			attributes.add(atomBase);
		}
		if (getAttributeFromGroup(attributes, lang_en.getName()) == null) {
			attributes.add(lang_en);
		}

		// rebuild the feed with the updated attributes
		// and atomsphere generator element
		feed = FeedDoc.buildFeed(feed.getId(), feed.getTitle(), feed
				.getUpdated(), feed.getRights(), feed.getAuthors(), feed
				.getCategories(), feed.getContributors(), feed.getLinks(),
				attributes, feed.getExtensions(), FeedDoc
						.getAtomsphereVersion(), feed.getSubtitle(), feed
						.getIcon(), feed.getLogo(), feed.getEntries());

		// write the xml header.
		writer.writeStartDocument(encoding, version);
		new FeedWriter().writeFeed(writer, feed);
		writer.flush();
		writer.close();
	}

	// used internally by feed reader
	static AtomPersonConstruct buildAtomPersonConstruct(Name name, URI uri,
			Email email, List<Attribute> attributes, List<Extension> extensions)
			throws AtomSpecException {
		return new AtomPersonConstruct(name, uri, email, attributes, extensions);
	}

	// checks for and returns the Attribute from the String attribute (argument)
	// in the list of attributes (argument)
	// used by Category, Generator and Link.
	static Attribute getAttributeFromGroup(List<Attribute> attributes,
			String attributeName) {
		if (attributes != null) {
			for(Attribute attr: attributes){
				if (attr.getName().equalsIgnoreCase(attributeName)) {
					return buildAttribute(attr.getName(), attr.getValue());
				}
			}
		}
		return null;
	}

	/**
	 * Convenience method for getting the content type for this element.
	 * Examines the "type" and "src" attributes if they exist in the list.
	 * 
	 * @return the content type for this element. One of TEXT,HTML,XHTML,OTHER
	 *         or EXTERNAL
	 */
	public static ContentType getContentType(List<Attribute> attributes) {
		ContentType contentType = ContentType.TEXT; // default
		if (attributes != null) {
			for(Attribute attr: attributes){
				if (attr.getName().equals("src")) {
					return ContentType.EXTERNAL;
				}
				
				if (attr.getName().equals("type")
						&& attr.getValue().equals("text")) {
					contentType = ContentType.TEXT;
					break;
				} else if (attr.getName().equals("type")
						&& attr.getValue().equals("html")) {
					contentType = ContentType.HTML;
					break;
				} else if (attr.getName().equals("type")
						&& attr.getValue().equals("xhtml")) {
					contentType = ContentType.XHTML;
				} else if (attr.getName().equals("type")
						&& (!attr.getValue().equals("text")
								&& !attr.getValue().equals("html") && !attr
								.getValue().equals("xhtml"))) {
					contentType = ContentType.OTHER;
					break;
				}
			}
		}
		return contentType;
	}

	/**
	 * This method sorts the entries of the feed. The Updated, Title and Summary
	 * are currently the only elementInstance types supported.
	 * 
	 * @param feed
	 *            the feed whose entries are to be sorted
	 * @param comparator
	 *            used to determine sort order
	 * @param elementClass
	 *            serves as the key element for the entries collection
	 * @return the sorted feed.
	 * @throws AtomSpecException
	 *             if the data violates the <a href=
	 *             "http://atomenabled.org/developers/syndication/atom-format-spec.php"
	 *             >specification</a>.
	 */
	public static Feed sortEntries(Feed feed, Comparator<String> comparator,
			Class<?> elementClass) throws AtomSpecException {

		if (feed.getEntries() != null) {
			// sort the entries with the passed in instance as the key
			SortedMap<String, Entry> resortedEntries = new TreeMap<String, Entry>(
					comparator);
			SortedMap<String, Entry> currentEntries = feed.getEntries();
			for(Entry entry: currentEntries.values()){
				if (elementClass.getSimpleName().equals("Updated")) {
					resortedEntries.put(entry.getUpdated().getText(), entry);
				}
				if (elementClass.getSimpleName().equals("Title")) {
					resortedEntries.put(entry.getTitle().getText(), entry);
				}
				if (elementClass.getSimpleName().equals("Summary")) {
					resortedEntries.put(entry.getSummary().getText(), entry);
				}
			}

			// rebuild the top level feed attributes to include the sort
			// if it isn't already there.
			List<Attribute> localFeedAttrs = new LinkedList<Attribute>();
			Attribute attrLocal = FeedDoc
					.buildAttribute("xmlns:sort",
							"http://www.colorfulsoftware.com/projects/atomsphere/extension/sort/1.0");
			if (feed.getAttributes() == null) {
				localFeedAttrs.add(attrLocal);
			} else {
				for(Attribute attr: feed.getAttributes()){
					if (!attr.equals(attrLocal)) {
						localFeedAttrs.add(attr);
					}
				}

				// finally add the sort extension attribute declaration
				localFeedAttrs.add(attrLocal);
			}

			// add or replace this extension element.

			String elementName = null;
			if (comparator == FeedDoc.SORT_ASC) {
				elementName = "sort:asc";
			} else {
				elementName = "sort:desc";
			}
			Attribute sortElement = null;
			if (elementClass.getSimpleName().equals("Updated")) {
				sortElement = FeedDoc.buildAttribute("type", "updated");
			} else if (elementClass.getSimpleName().equals("Title")) {
				sortElement = FeedDoc.buildAttribute("type", "title");
			} else if (elementClass.getSimpleName().equals("Summary")) {
				sortElement = FeedDoc.buildAttribute("type", "summary");
			}
			List<Attribute> extAttrs = new LinkedList<Attribute>();
			extAttrs.add(sortElement);
			Extension localFeedExtension = FeedDoc.buildExtension(elementName,
					extAttrs, null);

			// rebuild the extensions
			// we have to look for the sort extension and
			// replace any occurrences of it with the one we just created.
			List<Extension> localFeedExtensions = new LinkedList<Extension>();
			if (feed.getExtensions() == null) {
				localFeedExtensions.add(localFeedExtension);
			} else {
				for(Extension extn: feed.getExtensions()){
					// if we find an existing sort extension, ignore it.
					// add all others to the return list.
					if (!extn.getElementName().equalsIgnoreCase("sort:asc")
							&& !extn.getElementName().equalsIgnoreCase(
									"sort:desc")) {
						localFeedExtensions.add(extn);
					}
				}
				// finally add the new one.
				localFeedExtensions.add(localFeedExtension);
			}

			// this is an immutable sorted copy of the feed.
			return FeedDoc.buildFeed(feed.getId(), feed.getTitle(), feed
					.getUpdated(), feed.getRights(), feed.getAuthors(), feed
					.getCategories(), feed.getContributors(), feed.getLinks(),
					localFeedAttrs, localFeedExtensions, feed.getGenerator(),
					feed.getSubtitle(), feed.getIcon(), feed.getLogo(),
					resortedEntries);
		}
		// return the feed in the original order.
		return feed;
	}

	// Checks the xmlns (namespace) argument and applies the extension
	// to the feed argument if it is recognized by the atomsphere library.
	// used by FeedReader and FeedWriter
	static Feed checkForAndApplyExtension(Feed feed, Attribute xmlns)
			throws Exception {

		// if there aren't any attributes for the feed and thus no xmlns:sort
		// attr
		// return the defaults.
		if (feed.getAttributes() == null) {
			return feed;
		}

		// check for the first supported extension
		// currently only sort is implemented.
		for(Attribute attr: feed.getAttributes()){
			if (attr.equals(xmlns)) {
				return applySort(feed);
			}
		}
		return feed;
	}

	// check for and apply the first sort extension.
	private static Feed applySort(Feed feed) throws AtomSpecException {
		// only do the work if there are extensions.
		if (feed.getExtensions() != null) {
			// look for the first extension element if the namespace exists.
			for(Extension ext: feed.getExtensions()){
				if (ext.getElementName().equals("sort:asc")) {
					for(Attribute attr: ext.getAttributes()){
						if (attr.getName().equalsIgnoreCase("type")) {
							String value = attr.getValue();
							if (value.equals("updated")) {
								return sortEntries(feed, FeedDoc.SORT_ASC,
										Updated.class);
							}
							if (value.equals("title")) {
								return sortEntries(feed, FeedDoc.SORT_ASC,
										Title.class);
							}
							if (value.equals("summary")) {
								return sortEntries(feed, FeedDoc.SORT_ASC,
										Summary.class);
							}
						}
					}
				} else if (ext.getElementName().equals("sort:desc")) {
					for(Attribute attr: ext.getAttributes()){
						if (attr.getName().equalsIgnoreCase("type")) {
							String value = attr.getValue();
							if (value.equals("updated")) {
								return sortEntries(feed, FeedDoc.SORT_DESC,
										Updated.class);
							}
							if (value.equals("title")) {
								return sortEntries(feed, FeedDoc.SORT_DESC,
										Title.class);
							}
							if (value.equals("summary")) {
								return sortEntries(feed, FeedDoc.SORT_DESC,
										Summary.class);
							}
						}
					}
				}
			}
		}
		return feed;
	}

	// internal method to check for an undefined attribute
	static boolean isUndefinedAttribute(Attribute attr) {
		String name = attr.getName();
		return
		// atomCommonAttribute
		!name.equals("xml:base")
				&& !name.equals("xml:lang")
				// text constructs
				&& !name.equals("type")
				// out of line (external) content
				&& !name.equals("src")
				// category
				&& !name.equals("term")
				&& !name.equals("scheme")
				&& !name.equals("label")
				// generator
				&& !name.equals("uri")
				&& !name.equals("version")
				// link
				&& !name.equals("href") && !name.equals("rel")
				&& !name.equals("type") && !name.equals("hreflang")
				&& !name.equals("title") && !name.equals("length")
				// another namespace
				&& !name.startsWith("xmlns:");
	}

	// internal method to check for an atomCommonAttribute
	static boolean isAtomCommonAttribute(Attribute attr) {
		String name = attr.getName();
		return name.equals("xml:base") || name.equals("xml:lang")
				|| name.startsWith("xmlns:");
	}

}
