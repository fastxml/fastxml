# FastXml

**A simple, high-performance, small memory footprint, pull based XML parser.**


 * FastXml suppose the document was valid, and does not do full validation for best performance, just valid when necessary. For example, no validation for end tag, the first char of tag name etc.
 * FastXml focus on significant xml content, DECLARE, comments and DOCTYPE will be ignored.
 * Text should not contain comments.
 * TagName should not contain white space, tab or newline
 * Attribute name should be close to '=', and '=' should be close to '\"'
 * Both tag name and attribute name can only contain ascii chars.
 * Namespace prefix is allowed, but no validation for namespace

*(Beta version now, welcome to merge request or submit issues)*

# Why FastXml

 * Very good performance, nearly twice as fast as VTD-XMl and 30% faster than XPP3/MXP1
 * Hardly consumes memory, save more memory than VTD-XML and XPP3/MXP1
 * No dependencies
 * Minimum size of jar(size<16K), great in J2ME environment
 * The api of FastXml is very simple and easy to use

# Benchmark

[https://github.com/fastxml/fastxml-benchmark](https://github.com/fastxml/fastxml-benchmark)

# How could FastXml be so fast
 * Decoding characters as few as possible. Not all character need decode,
   I found that tag names and attribute names are usually ascii character which can be casted from byte directly,
   and only attribute value and text content need to be decoded,
 * Just skip commit, DECLARE, DOCTYPE, whitespace, tab etc,
   they are no significant for you most of the time.
 * Convert byte array segment to Integer\Long\Short directly,
   without convert byte array segment to String and then convert to Integer\Long\Short.
 * You can skip a whole tag element when traverse the xml document, if you need.
   This is very useful when you just want to read some tags from xml document.

# License
FastXml source code is licensed under the Apache Licence, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0.html).

# TODO
 * Support big file
 * To be more faster