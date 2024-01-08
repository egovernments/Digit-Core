# **GIS SERVICE**

The gis-service has been enhanced to fetch data from the boundary service based on the provided codes and tenantId. This modification involves making an API call to the boundary service, retrieving its JSON response, and converting that response into GeoJson format.

### **Service Dependencies**

boundary-service

### **Service Details**

The application can be run as any other spring boot application but needs lombok extension added in your ide to load it. Once the application is up and running API requests can be posted to the url and GeoJson response can be generated. In case of intellij the plugin can be installed directly, for eclipse the lombok jar location has to be added in eclipse.ini file in this format -javaagent:lombok.jar.

### **API Details**

Method = POST

Path   = boundary/geom/_search

### **Postman collection**

[gis-service.postman_collection.json](..%2F..%2F..%2F..%2Fpostman%20collection%2Fgis-service.postman_collection.json)
