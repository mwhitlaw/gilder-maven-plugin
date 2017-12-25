# Domain Model
The domain model for `ota-flight3-shop` is not based on the enterprise-schema
objects. The problem with those objects is that they are used across multiple
parts of the business including shopping, ordering, and operations. As such,
the core objects are overloaded with fields which only make sense in certain
cases. A technical limitation of the older enterprise-schema domain model
is that it uses the old style `xsd:date` and `xsd:datetime` without JAXB 
mappings to proper Java Local Date or Local Date Time objects where appropriate.
This results in an API that's harder to use.

The new API focuses on the shopping and ordering use case. Each flight that's
listed in the shopping results includes data relevant to the travel details of
the flight like when and where it's taking off/landing and the pricing details
of the flight like how much it costs.

### Travel
The travel domain model includes the basic info about each Segment, Leg, and 
the connecting info. There are a few key goals with this model:


  - show the customer where, when, and how they're reaching their destination
  - show total travel time versus just the flight time
  - show the type of aircraft


### Pricing
The pricing section is new in `ota-flight3-shop` because with the addition
of multiple providers it's possible that a given flight could be offered by
different providers with different prices. 

The pricing model supports the following:

  - one way fares
  - round trip fares
  - adhoc packages
  - named fares
  - details of how the prices are computed
  - summary of sales totals for quick grouping/sorting

#### One Way Fares
A one way fare is simply a flight that can booked without any requirement to
book a complementary flight. See the [packages](packages.html) page for more 
details.

#### Round Trip Fares
A round trip fare is a fare on a specific flight that requires the selection 
of a complementing flight in order to get the fare. In short, this exists 
because in some cases, a provider only gives us the package price for both
legs of the round trip journey. See the [packages](packages.html) page for more 
details.

#### Adhoc Packages
We'll allow the customer to assemble their own custom travel package similar
to how Kayak offers "hack a fare." This is simply allowing a customer to 
put together their own custom round trip or multi-city journey from the 
inventory available in the search results. In some cases, the resulting flight
selection may match to an previously constructed package deal from a provider
but in other cases the Allegiant OTA will manufacture its own package from
separate one way fares. See the [packages](packages.html) page for more 
details.

#### Named Fares
The domain model supports named fares as detailed in the [G4 provider page](g4-provider.html).
While it's likely that Allegiant OTA won't be able to create special fares that
include features like free bags or priority boarding, it seems very doable for us
to construct a fare from another carrier like Delta that includes Trip Flex. 

When presenting named fares, it's not uncommon to see other carriers have 
some flight options where the given named fare doesn't apply. JetBlue is a good
example where some markets don't have any options for their JetBlue Mosaic fare
but that fare still shows up as an option in the results with x's or blanks for
the flights where it doesn't apply. Thus, it might not be that off-putting
for customers to see some flight options where there were different numbers of
fares.

#### Fare Details
The domain model supports a detailed breakdown of each fare. In most cases,
if a customer is shopping for multiple travelers at once then each traveler
has the fare. However, Sabre and other providers support different fares by
traveler type such that a party of one adult, one senior, and one child might
get 3 different fares in the displayed sales total. This domain model supports
that functionality despite not seeing it appear in search results. It's 
conceivable that we could offer discounted fares in this manner based on the
provided demographics of the travelers.

#### Summary of Sales Totals
While the fare detail breakdown is helpful, consumers of this API will want to 
have a quick summary of a given fare in order to avoid having to compute it
from the available data. This information is provided in the domain model in
a manner where it's derived from the details on demand and made available in
the serialized form so consumers can perform quick grouping / sorting of the
results. 

## UML

The diagram below is the basic structure of the shop domain model which illustrates the two main sub-domains.

![UML class diagram](images/domain.png)

### Travel
The travel sub-domain includes in the elements that model the flights including origin and destination plus times of
travel.

### Pricing
The pricing sub-domain includes the various packages and pricing details from the providers that offer the flight.

### Supporting
The data dictionary is designed as a way of providing additional information about commonly referenced items. For example,
a flight travel segment will reference airports, airlines, and aircrafts by their IATA codes instead of repeating the 
elements like the name and other properties for each of these items wherever they appear in the graph. This helps reduce
the overall size of the message and harks back to the `xml:id` object referencing. The data dictionary allows the consumer
to resolve the given codes to an object that might provide more details for the rendering of the data in a UI. A good example
of this is the name and logo for an airline. 