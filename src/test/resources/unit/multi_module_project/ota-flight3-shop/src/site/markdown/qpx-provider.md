# Overview
[QPX is an API from Google](https://developers.google.com/qpx-express/) that offers airline pricing and 
shopping in a well defined data model for many airline providers.  

### Note how QPX differs from G4
- it's a single request to the provider that includes all of the details for the outbound and inbound flights
- the results are packaged and priced as round trip flights
- some flights appear in multiple packages. 
 
### Status
QPX is currently on hold as a provider. It's data model and API were useful for shaping the `flight3` domain
model but their lack of an ordering API is a hinderance. Their ordering process is basically a deep link into
another airline's website which isn't suitable for our needs.