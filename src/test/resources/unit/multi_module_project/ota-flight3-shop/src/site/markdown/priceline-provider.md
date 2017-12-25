# Priceline
Priceline has a set of services that implement their workflow of:

- select departing flight
- select return flight
- enter booking details
- book

![priceline-api-overview](images/Priceline-API.png)

This API isn't well suited to displaying a list of outbound and inbound flights at the same time since the availability 
and price of the inbound flight isn't known until you select the departing flight.
 
### Search results

<img src="images/priceline-results.png" alt="priceline-results" width="400"/>

The response from the departure flight service only includes the details on the departing flight from the first 
origin airport. The options for the return flight requires the selection of a departing flight and its bundle 
key which is then passed to a separate service.

### Conclusion

Priceline isn't well suited to a model where the providers include the pairings of the outbound and inbound 
flights or even providing all options for outbound and inbound. One way of adapting it would be to fetch all 
of the inbound flight options as part of the provider call but this could be quite a large number of items 
which would each be a separate service call.