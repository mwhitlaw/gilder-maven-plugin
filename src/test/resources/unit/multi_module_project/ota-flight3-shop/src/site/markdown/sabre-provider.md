# Overview
[Saber's InstaFlight Search API](https://developer.sabre.com/docs/read/rest_apis/air/search/instaflights_search) 
offers REST and SOAP API's. The REST API's were used for the initial 
prototyping but we'd probably want to use the SOAP bindings for the production implementation since it 
seems more polished.

### Test Platform Limitations
The Sabre test platform is only available for its REST API's. You need to have a production level account in
order to get access to their SOAP API's and their full data set. The following are the known limitations of the 
Sabre test platform:
- REST API only
- limited numbers of markets as reportred by the [City Pairs](https://developer.sabre.com/docs/read/rest_apis/air/utility/city_pairs_lookup) service
- fees don't appear in the search results but are part of the data model (need to research this) 

