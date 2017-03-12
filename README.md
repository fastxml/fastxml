# FastXml

**A simple, high-performance, small memory footprint, pull based XML parser.**

 * Very good performance, nearly three times faster than [VTD-XML](http://vtd-xml.sourceforge.net), twice times faster than [XPP3/MXP1](http://www.extreme.indiana.edu/xgws/xsoap/xpp/mxp1/)
 * Very small memory footprint, save more memory than VTD-XML and XPP3/MXP1
 * No dependencies
 * Minimum size of jar(size:18K), great in J2ME environment
 * The api of FastXml is very simple and easy to use

 *(welcome to merge request or submit issues)*

# Benchmark

[https://github.com/fastxml/fastxml-benchmark](https://github.com/fastxml/fastxml-benchmark)

# Usage and example

[https://github.com/fastxml/fastxml-example](https://github.com/fastxml/fastxml-example)

# How could FastXml be so fast
 * Decoding characters as few as possible. Not all character need decode,
   I found that tag names and attribute names are usually ascii character which can be casted from byte directly,
   and only attribute value and text content need to be decoded most of the time.
 * Just skip comment, DECLARE, DOCTYPE, whitespace, tab etc,
   they are no significant for you most of the time.
 * Convert byte array segment to Integer\Long\Short directly,
   without convert byte array segment to String and then convert to Integer\Long\Short.
 * Almost without creating temporary string or other object.
 * Extreme JIT Optimization.
 * You can skip a whole tag element when traverse the xml document, if you need.
   This is very useful when you just want to read some tags from xml document.

# Notice

 * FastXml suppose the document was valid, and does not do full validation for best performance, just valid when necessary. For example, no validation for end tag, the first char of tag name etc.
 * FastXml focus on significant xml content. DECLARE, comments and DOCTYPE will be ignored.
 * Text content should not contain comments.
 * TagName should not contain white space, tab or newline
 * Both tag name and attribute name can only contain ascii chars.
 * Namespace prefix is allowed, but no validation for namespace

# License
FastXml source code is licensed under the [Apache Licence, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

# TODO
 * Support big file
 * To be more faster
