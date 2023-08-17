import { useQuery } from "react-query";

const useLocation = (tenantId, locationType, config = {}) => {
  switch (locationType) {
    case "Locality":
      return useQuery(["LOCALITY_DETAILS", tenantId], () => window?.Digit?.LocationService.getLocalities(tenantId), config);
    case "Ward":
      return useQuery(["WARD_DETAILS", tenantId], () => window?.Digit?.LocationService.getWards(tenantId), config);
    default:
      break;
  }
};

export default useLocation;
