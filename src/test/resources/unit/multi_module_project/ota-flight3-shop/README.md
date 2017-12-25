# ota-flight3-shop

The beginnings of the 3rd incarnation of the ota-flight-shop web service. 
  
## Previous Versions

### Version 1 
The initial version of the ota-flight-shop which looked to have drawn heavily from the underlying G4 services 
 for its domain objects. This version has a dependency on the enterprise-schema as well as types within the supporting
 G4 libraries. 
 
### Version 2
A follow on that added support for coupon codes as part of the fare club effort. As part of this effort there was a move
to Java 8 as well as the adoption of some of the Java EE 6 validation API's in order to remove a fair amount of validation
code. Finally, the API's were pruned of any unused QueryParams and other orphaned code.
 
### Version 3
Version 3 aims to address the following concerns:
- no dependency on enterprise-schema
- support for multiple providers

# Why ota-flight3-shop?
The 3 was added to the Maven coordinate simply to have it stand out from the previous 2 versions

# Why not use enterprise schema?
- the data model is defined in XSD which isn't being used for the REST services
- it's on Java 6 
- There are no custom JAXB adapters to support Java Time (or even Joda Time) for dates. 
As a result all of the dates/times are modeled as XmlGregorianCalendars and as such it's impossible to tell when a value 
is expected to be a LocalDate vs a LocalDateTime vs a ZonedDateTime.
- the types are used by multiple services and it's often confusing to see the union of properties. Fixing this would 
require updating a large amount of existing code so it's better to start anew. For example, consider that the Leg on a 
shop call returns scheduled, estimated, and actual times for a flight. Only the scheduled value is populated during the
shop process but the others are used by operations. Making the type support the union of all requirements creates bloat
and is confusing.