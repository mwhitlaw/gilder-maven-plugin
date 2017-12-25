# Packages
Some GDS and flight shop providers offer packaged fares for one-way and multi-city 
journeys such that the pricing for the individual legs of the journey are not broken
out separately in the quote. It may be in most cases that the price for a round trip
journey is the same as two separately purchased one-way fares. However, there are a
few examples of cases where a round trip ticket is actually priced differently than
separate one-ways. Even if the price is the same, when we're shopping with an external
provider and we're presented with packaged flights, we need a mechanism to present
these flights to the customer such that if they select a given outbound flight then
we'll know if the options or pricing for the return flights are somehow tied to the
selection of the outbound flight based on a package.

## How to model packages?

The pic below illustrates a call to a provider that
returns 3 packaged options for a flight between 
BLI-LAS. 

![shop request pic](images/how-to-model.png)

There are three options to choose from. The 
outbound flight is shown in blue and the return 
flight that it's paired with is shown in orange.

While this example comes from the QPX provider, the
results are similar with Sabre. 

If rendered exactly as shown, the customer would
see three options for their ticket purchase. However,
if you look closely, you'll notice that there are
actually only two options for the outbound flight
and two options for the return flight. 

- AS 422 appears as an outbound flight in two of the packages
- AS 635 appears as an inbound flight in two of the packages

## Splitting the pairs

The approach to incorporating this into the provider framework is to 
break up the pairs and treat them as separate, but connected options. By 
splitting them up, we can display options for outbound and inbound flights 
separately as we do today. The trick comes in pricing these since you wouldn't 
be able to display the price for the return segment until the outbound segment 
was selected. Consider the example below:

- the customer sees two outbound flights: **AS 422** and **AS 2491/AS 608**. 
- the round trip price starts at $192.40 assuming that they'll pick **AS 635** as the returning flight
- they pick **AS 422** for outbound and then see two options for the return flight: **AS 635** and **AS 471/AS 431**.
- **AS 635** would show a price of $0 while **AS 471/AS 431** would show a price of $99.60

This allows the customer to see the cheapest option for their journey upfront. The customer can change
 their selection for the return flight on a following screen based on their preferences for the 
 carrier, flight time, travel time, etc. 
Seeing the difference in the cost as opposed to having to do the math 
 themselves is an alternative to displaying the total trip cost. The OTA UI could also highlight certain features of the options to help the customer decide.
 For example, the customer might opt to spend a little more to select a flight that fits better with 
 their time of travel. They might also opt to spend a little more to avoid having a connecting flight.
 In any case, the differences in the flights with respect to their prices are shown as deltas from the
 cheapest fare.
  
### Example

The image below illustrates how the three different packages discussed above are split
into separate but connected flights. We'll split the packages into distinct flight
options where each flight option represents a single flight from point A to point B.
If there are multiple instances of a flight within the packaged results, then these
dupes get added as a named package within the flight so we'll know that the selection of
 this given flight is restricted to the set of intersecting complementing flights. 

![splitting pairs viz](images/splitting.png)

Thus, if you select **AS 422** as the outbound flight, you're limited to selecting from the package prices
tied to flight **AS 635** or flight **AS 471/AS 431** as the return. If you select the outbound flight combo of 
**AS 2491 / AS 608** then you can only select **AS635** as the return flight.


