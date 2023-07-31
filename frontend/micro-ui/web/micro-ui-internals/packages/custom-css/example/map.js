"use strict";
let map;
var script = document.createElement("script");
const key = globalConfigs?.getConfig("GMAPS_API_KEY");
script.src = `https://maps.googleapis.com/maps/api/js?key=${key}&callback=initAutocomplete&libraries=places`;
script.defer = true;
window.initAutocomplete = function () {
  const map = new google.maps.Map(document.getElementById("map"), {
    center: {
      lat: 28.5355,
      lng: 77.391,
    },
    zoom: 15,
    mapTypeId: "roadmap",
  }); // Create the search box and link it to the UI element.

  const input = document.getElementById("pac-input");
  const searchBox = new google.maps.places.SearchBox(input);
  // map.controls[google.maps.ControlPosition.TOP_LEFT].push(input); // Bias the SearchBox results towards current map's viewport.

  map.addListener("bounds_changed", () => {
    searchBox.setBounds(map.getBounds());
  });
  let markers = []; // Listen for the event fired when the user selects a prediction and retrieve
  // more details for that place.

  searchBox.addListener("places_changed", () => {
    const places = searchBox.getPlaces();

    if (places.length == 0) {
      return;
    } // Clear out the old markers.

    markers.forEach((marker) => {
      marker.setMap(null);
    });
    markers = []; // For each place, get the icon, name and location.

    const bounds = new google.maps.LatLngBounds();
    places.forEach((place) => {
      if (!place.geometry) {
        return;
      }

      const icon = {
        url: place.icon,
        size: new google.maps.Size(71, 71),
        origin: new google.maps.Point(0, 0),
        anchor: new google.maps.Point(17, 34),
        scaledSize: new google.maps.Size(25, 25),
      }; // Create a marker for each place.

      markers.push(
        new google.maps.Marker({
          map,
          icon,
          title: place.name,
          position: place.geometry.location,
        })
      );

      if (place.geometry.viewport) {
        // Only geocodes have viewport.
        bounds.union(place.geometry.viewport);
      } else {
        bounds.extend(place.geometry.location);
      }
    });
    map.fitBounds(bounds);
  });
};
document.head.appendChild(script);
