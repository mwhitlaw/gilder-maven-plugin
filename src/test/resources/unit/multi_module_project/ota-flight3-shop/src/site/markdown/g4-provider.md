# G4 Provider

The G4 Provider for `ota-flight3-shop` uses the supporting services from G4 flight services to make the shop
calls. The G4 provider implements the following behavior:

- one way searches 
- round trip searches
- multi-city searches
- variable dates of travel
- markup of one way fares
- creation of named fares

### One-Way Searches
The one way searches are little more than a direct pass-through to the underlying
G4 shop calls.

### Round Trip Searches
The round trip searches are implemented as separate searches for each leg of the
travel. The underlying G4 services don't currently offer a mechanism for searching
for round trips in one call.

### Multi-City Searches
Similar to the round trip searches, the multi-city searches are implemented 
as separate searches for each leg of the journey.

### Variable dates of travel
The G4 shop calls provide a mechanism for searching a range of dates for
the shop calls. This allows us to build a UI that shows alternate dates of
travel which are typically rendered in a calendar control. 

### Markup of one way fares
As a proof of concept, we're showing how the OTA layer can add a small markup
to one way flights as opposed to round trip flights. Since the OTA layer
is aware of the one way vs round trip context, it can decide which flights
to markup.

The implementation currently applies a simple percentage markup to all flights
and correctly adjusts the base fare and resulting taxes to produce nicely
rounded prices with the provided markup. 

This feature could easily change to incorporate the market information in order
to determine what markup to apply. It would be desirable to have this 
functionality implemented in the G4 shop layer but it's easy enough to do
in the OTA layer.

### Creation of Named Fares
The G4 provider currently returns a single fare (the "rack" fare) based on the availble fare 
class (aka buckets) and coupon codes for the given flight. The G4 provider 
for `ota-flight3-shop` takes the given fare and derives two additional named
fares based on the "rack" fare to offer the customer a fare that includes
Trip Flex and another that includes Trip Flex, 1 carry on, and priority boarding.

#### Trip Flex
The fare with Trip Flex is intended to increase the sale of the Trip Flex 
insurance by presenting it to the customer at an earlier stage in the 
shopping process. Since the Trip Flex is being presented as a different type
of fare, we need to adjust the base fare for the flight to include the 
cost of Trip Flex which means the fare presented needs to have its Federal
Excise Tax adjusted as well.

#### Trip Flex Bundle
The Trip Flex Bundle includes a slightly discounted Trip Flex along with 
one carry on and priority boarding. The idea of the bundle is to entice the
customer to buy a few extra servies at a savings.

#### Pricing of Trip Flex and Trip Flex Bundle
For the purposes of the demo, we're hard coding the cost of Trip Flex at
$20 and the cost of the Trip Flex Bundle at $18 + $18 + $8. These numbers
approximate the cost of Trip Flex and the items in the bundle. For the
real implementation we could replace these hard coded values with the market
specific values for the products. While the actual prices are made up, the
mechanism for creating the new fare is real and correctly adjusts the base 
fare and tax.